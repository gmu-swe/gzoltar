/**
 * Copyright (C) 2019 GZoltar contributors.
 * 
 * This file is part of GZoltar.
 * 
 * GZoltar is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * GZoltar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with GZoltar. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.gzoltar.core.instr.pass;

import static javassist.bytecode.Opcode.AALOAD;
import static javassist.bytecode.Opcode.AASTORE;
import static javassist.bytecode.Opcode.BALOAD;
import static javassist.bytecode.Opcode.BASTORE;
import static javassist.bytecode.Opcode.CALOAD;
import static javassist.bytecode.Opcode.CASTORE;
import static javassist.bytecode.Opcode.CHECKCAST;
import static javassist.bytecode.Opcode.DALOAD;
import static javassist.bytecode.Opcode.DASTORE;
import static javassist.bytecode.Opcode.DDIV;
import static javassist.bytecode.Opcode.FALOAD;
import static javassist.bytecode.Opcode.FASTORE;
import static javassist.bytecode.Opcode.FDIV;
import static javassist.bytecode.Opcode.GETFIELD;
import static javassist.bytecode.Opcode.GETSTATIC;
import static javassist.bytecode.Opcode.IALOAD;
import static javassist.bytecode.Opcode.IASTORE;
import static javassist.bytecode.Opcode.IDIV;
import static javassist.bytecode.Opcode.IFEQ;
import static javassist.bytecode.Opcode.IFNONNULL;
import static javassist.bytecode.Opcode.IFNULL;
import static javassist.bytecode.Opcode.IF_ACMPNE;
import static javassist.bytecode.Opcode.INVOKEDYNAMIC;
import static javassist.bytecode.Opcode.INVOKEINTERFACE;
import static javassist.bytecode.Opcode.INVOKESPECIAL;
import static javassist.bytecode.Opcode.INVOKESTATIC;
import static javassist.bytecode.Opcode.INVOKEVIRTUAL;
import static javassist.bytecode.Opcode.LALOAD;
import static javassist.bytecode.Opcode.LASTORE;
import static javassist.bytecode.Opcode.LDIV;
import static javassist.bytecode.Opcode.MONITORENTER;
import static javassist.bytecode.Opcode.MONITOREXIT;
import static javassist.bytecode.Opcode.NEWARRAY;
import static javassist.bytecode.Opcode.PUTFIELD;
import static javassist.bytecode.Opcode.PUTSTATIC;
import static javassist.bytecode.Opcode.SALOAD;
import static javassist.bytecode.Opcode.SASTORE;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.gzoltar.core.AgentConfigs;
import com.gzoltar.core.instr.InstrumentationConstants;
import com.gzoltar.core.instr.InstrumentationLevel;
import com.gzoltar.core.instr.Outcome;
import com.gzoltar.core.instr.actions.AnonymousClassConstructorFilter;
import com.gzoltar.core.instr.filter.DuplicateCollectorReferenceFilter;
import com.gzoltar.core.instr.filter.EmptyMethodFilter;
import com.gzoltar.core.instr.filter.EnumFilter;
import com.gzoltar.core.instr.filter.IFilter;
import com.gzoltar.core.instr.filter.SyntheticFilter;
import com.gzoltar.core.model.Node;
import com.gzoltar.core.model.NodeFactory;
import com.gzoltar.core.runtime.Collector;
import com.gzoltar.core.runtime.Probe;
import com.gzoltar.core.runtime.ProbeGroup;
import com.gzoltar.core.util.MD5;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.analysis.ControlFlow;
import javassist.bytecode.analysis.ControlFlow.Block;

public class CoveragePass implements IPass {

  private final InstrumentationLevel instrumentationLevel;

  private final FieldPass fieldPass = new FieldPass();

  private AbstractInitMethodPass initMethodPass = null;

  private final StackSizePass stackSizePass = new StackSizePass();

  private final DuplicateCollectorReferenceFilter duplicateCollectorFilter =
      new DuplicateCollectorReferenceFilter();

  private final List<IFilter> filters = new ArrayList<IFilter>();

  private ProbeGroup probeGroup;

  public CoveragePass(final AgentConfigs agentConfigs) {

    this.instrumentationLevel = agentConfigs.getInstrumentationLevel();
    switch (this.instrumentationLevel) {
      case FULL:
      default:
        this.initMethodPass = new InitMethodPass();
        break;
      case OFFLINE:
        this.initMethodPass = new OfflineInitMethodPass();
        break;
      case NONE:
        break;
    }

    // exclude synthetic methods
    this.filters.add(new SyntheticFilter());

    // exclude methods 'values' and 'valuesOf' of enum classes
    this.filters.add(new EnumFilter());

    // exclude methods without any source code
    this.filters.add(new EmptyMethodFilter());

    // exclude constructor of an Anonymous class as the same line number is handled by the
    // superclass
    this.filters.add(new AnonymousClassConstructorFilter());
  }

  @Override
  public synchronized Outcome transform(final CtClass ctClass) throws Exception {
    boolean instrumented = false;

    byte[] originalBytes = ctClass.toBytecode(); // toBytecode() method frozens the class
    // in order to be able to modify it, it has to be defrosted
    ctClass.defrost();

    String hash = MD5.calculateHash(originalBytes);
    this.probeGroup = new ProbeGroup(hash, ctClass);

    for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
      boolean behaviorInstrumented =
          this.transform(ctClass, ctBehavior).equals(Outcome.REJECT) ? false : true;
      instrumented = instrumented || behaviorInstrumented;

      if (behaviorInstrumented) {
        // update stack size
        this.stackSizePass.transform(ctClass, ctBehavior);
      }
    }

    // register class' probes
    Collector.instance().regiterProbeGroup(this.probeGroup);

    if (instrumented && this.initMethodPass != null) {
      // make GZoltar's field
      this.fieldPass.transform(ctClass);

      // make method to init GZoltar's field
      this.initMethodPass.setHash(hash);
      this.initMethodPass.transform(ctClass);

      // make sure GZoltar's field is initialised. note: the following code requires the init method
      // to be in the instrumented class, otherwise a compilation error is thrown

      boolean hasAnyStaticInitializerBeenInstrumented = false;
      for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
        if (ctBehavior.getName().equals(InstrumentationConstants.INIT_METHOD_NAME)) {
          // for obvious reasons, init method cannot call itself
          continue;
        }

        // before executing the code of every single method, check whether FIELD_NAME has been
        // initialised. if not, init method should initialise the field
        this.initMethodPass.transform(ctClass, ctBehavior);

        if (hasAnyStaticInitializerBeenInstrumented == false
            && ctBehavior.getMethodInfo2().isStaticInitializer()) {
          hasAnyStaticInitializerBeenInstrumented = true;
        }
      }

      if (!hasAnyStaticInitializerBeenInstrumented) {
        CtConstructor clinit = ctClass.makeClassInitializer();
        this.initMethodPass.transform(ctClass, clinit);
      }
    }

    return Outcome.ACCEPT;
  }

  @Override
  public Outcome transform(final CtClass ctClass, final CtBehavior ctBehavior) throws Exception {
    Outcome instrumented = Outcome.REJECT;

    // check whether this method should be instrumented
    for (IFilter filter : this.filters) {
      switch (filter.filter(ctBehavior)) {
        case REJECT:
          return instrumented;
        case ACCEPT:
        default:
          continue;
      }
    }

    boolean injectBytecode = this.duplicateCollectorFilter.filter(ctClass) == Outcome.ACCEPT
        && (this.instrumentationLevel == InstrumentationLevel.FULL
            || this.instrumentationLevel == InstrumentationLevel.OFFLINE);

    MethodInfo methodInfo = ctBehavior.getMethodInfo();
    CodeAttribute ca = methodInfo.getCodeAttribute();

    assert ca != null;
    CodeIterator ci = ca.iterator();

    Queue<Integer> blocks = new LinkedList<Integer>();
    try {
      ControlFlow cf = new ControlFlow(ctClass, methodInfo);
      for (Block block : cf.basicBlocks()) {
        blocks.add(block.position());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    int index = 0, prevLine = -1, curLine = -1, instrSize = 0;
    boolean prevInsnWasConditionalJump = false;
    boolean prevInsnMightHaveThrownException = false;
    Probe fauxJumpProbe = null;
    boolean addedExtraProbeThisLine = false;
    while (ci.hasNext()) {
      index = ci.next();
      curLine = methodInfo.getLineNumber(index);

      if (curLine == -1) {
        continue;
      }

      boolean isNewBlock = !blocks.isEmpty() && index >= instrSize + blocks.peek();
      if (isNewBlock) {
        blocks.poll();
      }

      // If the previous instruction was a conditional jump, then insert a probe immediately after
      // it to ensure that we can differentiate between cases where the branch was or was not taken.
      if (prevInsnWasConditionalJump) {
        Probe probe;
        if (curLine != prevLine || fauxJumpProbe == null) {
          Node node = NodeFactory.createNode(ctClass, ctBehavior, prevLine);
          assert node != null;
          node.setFakeProbeForJump(true);
          probe = this.probeGroup.registerProbe(node, ctBehavior);
          assert probe != null;
          fauxJumpProbe = probe;
        } else {
          probe = fauxJumpProbe;
        }
        if (injectBytecode) {
          Bytecode bc = this.getInstrumentationCode(ctClass, probe, methodInfo.getConstPool());
          ci.insert(index, bc.get());
          instrSize += bc.length();
          instrumented = Outcome.ACCEPT;
        } else {
          instrumented = Outcome.REJECT;
        }
        addedExtraProbeThisLine = true;
      }

      /*if (prevInsnMightHaveThrownException && !prevInsnWasConditionalJump && !isNewBlock
          && prevLine == curLine) { // do NOT insert two probes immediately adjacent, ever!
        Node node = NodeFactory.createNode(ctClass, ctBehavior, prevLine);
        assert node != null;
        node.setFakeProbeForJump(true);
        Probe probe = this.probeGroup.registerProbe(node, ctBehavior);
        assert probe != null;
        if (injectBytecode) {
          Bytecode bc = this.getInstrumentationCode(ctClass, probe, methodInfo.getConstPool());
          ci.insert(index, bc.get());
          instrSize += bc.length();
          instrumented = Outcome.ACCEPT;
        } else {
          instrumented = Outcome.REJECT;
        }
      }*/

      int opcode = ci.byteAt(index) & 0xff;
      if (prevLine != curLine || isNewBlock) {
        // a line is always considered for instrumentation if and only if: 1) it's line number has
        // not been instrumented; 2) or, if it's in a different block

        addedExtraProbeThisLine = false;
        Node node = NodeFactory.createNode(ctClass, ctBehavior, curLine);
        assert node != null;
        Probe probe = this.probeGroup.registerProbe(node, ctBehavior);
        assert probe != null;

        if (injectBytecode) {
          Bytecode bc = this.getInstrumentationCode(ctClass, probe, methodInfo.getConstPool());
          ci.insert(index, bc.get());
          instrSize += bc.length();
          instrumented = Outcome.ACCEPT;
        } else {
          instrumented = Outcome.REJECT;
        }

        prevLine = curLine;
      }

      // Is this a conditional jump?
      prevInsnWasConditionalJump =
          ((IFEQ <= opcode && opcode <= IF_ACMPNE) || opcode == IFNULL || opcode == IFNONNULL);
      prevInsnMightHaveThrownException = isMightThrowException(opcode, false);
    }

    return instrumented;
  }

  private static boolean isMightThrowException(int opcode, final boolean ignoreArrayStores) {
    switch (opcode) {
      // division by 0
      case IDIV:
      case FDIV:
      case LDIV:
      case DDIV:
        // NPE
      case MONITORENTER:
      case MONITOREXIT: // or illegalmonitor
        return true;
      // ArrayIndexOutOfBounds or null pointer
      case IALOAD:
      case LALOAD:
      case SALOAD:
      case DALOAD:
      case BALOAD:
      case FALOAD:
      case CALOAD:
      case AALOAD:
      case IASTORE:
      case LASTORE:
      case SASTORE:
      case DASTORE:
      case BASTORE:
      case FASTORE:
      case CASTORE:
      case AASTORE:
        return !ignoreArrayStores;
      case CHECKCAST: // incompatible cast
        // trigger class initialization
        // case NEW: //will break powermock :(
      case NEWARRAY:
      case GETSTATIC:
      case PUTSTATIC:
      case GETFIELD:
      case PUTFIELD:
      case INVOKEVIRTUAL:
      case INVOKESTATIC:
      case INVOKEINTERFACE:
      case INVOKEDYNAMIC:
      case INVOKESPECIAL:
        return true;
      default:
        return false;
    }
  }

  private Bytecode getInstrumentationCode(CtClass ctClass, Probe probe, ConstPool constPool) {
    Bytecode b = new Bytecode(constPool);
    b.addGetstatic(ctClass, InstrumentationConstants.FIELD_NAME,
        InstrumentationConstants.FIELD_DESC_BYTECODE);
    b.addIconst(probe.getArrayIndex());
    b.addOpcode(Opcode.ICONST_1);
    b.addOpcode(Opcode.BASTORE);

    return b;
  }

}
