/*
 * Copied from pit - https://github.com/hcoles/pitest/
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.gzoltar.core.instr.analysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.instr.CoverageClassVisitor;
import com.gzoltar.core.instr.InstrumentationConstants;
import com.gzoltar.core.runtime.ProbeGroup;

/**
 * Need to count the number of blocks in the method. Storing method as a tree
 * enables a second scan by the instrumenting visitor
 *
 */
public class CoverageAnalyser extends MethodNode {

  private static final int           MAX_SUPPORTED_LOCAL_PROBES = 15;

  private final CoverageClassVisitor parent;
  private final MethodVisitor mv;
  private final ProbeGroup probeGroup;
  private final String className;
  private final boolean addFrames;

  public CoverageAnalyser(final CoverageClassVisitor parent, final ProbeGroup probeGroup,
                          final String className, final MethodVisitor mv, final int access, final boolean addFrames,
                          final String name, final String desc, final String signature,
                          final String[] exceptions) {
    super(InstrumentationConstants.ASM_VERSION, access, name, desc, signature, exceptions);
    this.mv = mv;
    this.addFrames = addFrames;
    this.parent = parent;
    this.probeGroup = probeGroup;
    this.className = className;
  }

  protected LabelNode getLabelNode(Label l) {
    if (!(l.info instanceof LabelNode)) {
      l.info = new LabelNode(l);
    }
    return (LabelNode)l.info;
  }

  @Override
  public void visitEnd() {

    //Later on we will run into a mess if we insert a jump between a label and a NEW that follows it.
    //As a countermeasure, we defensively add new labels for each NEW and rewrite all frames.
    fixLabelsForFrames();

    final List<Block> blocks = findRequriedProbeLocations();

    final int blockCount = blocks.size();

    // according to the jvm spec
    // "There must never be an uninitialized class instance in a local variable in code protected by an exception handler"
    // the code to add finally blocks used by the local variable and array based
    // probe approaches is not currently
    // able to meet this guarantee for constructors. Although they appear to
    // work, they are rejected by the
    // java 7 verifier - hence fall back to a simple but slow approach.
    final DefaultInstructionCounter counter = new DefaultInstructionCounter();

    boolean skipExceptionProbes = this.instructions.size() > 30000;
      // for now fall back to the naive implementation - could instead use array
      // passing version
    AnalyzerAdapter analyzerAdapter = new AnalyzerAdapter(className, this.access, this.name, this.desc, this.mv);
      accept(new InstructionTrackingMethodVisitor(
          new ArrayProbeCoverageMethodVisitor(blocks, counter,this.className, analyzerAdapter,
              analyzerAdapter, this.addFrames, this.access, this.name, this.desc, this.probeGroup, skipExceptionProbes),
              counter));

  }

  private void fixLabelsForFrames() {
    AbstractInsnNode insn = this.instructions.getFirst();
    LabelNode lastLabel = null;
    HashMap<LabelNode, LabelNode> labelToLabel = new HashMap<>();
    LinkedList<FrameNode> frames = new LinkedList<>();
    while(insn != null){
      if(insn instanceof LabelNode)
        lastLabel = (LabelNode) insn;
      else if(insn instanceof FrameNode)
        frames.add((FrameNode) insn);
      else if(insn.getOpcode() == Opcodes.NEW){
        //Add a new label before the NEW
        LabelNode newLbl = new LabelNode(new Label());
        this.instructions.insertBefore(insn, new LabelNode(new Label())); //where the probe will go, if there is supposed to be one here
        this.instructions.insertBefore(insn, newLbl);
        if(lastLabel != null)
          labelToLabel.put(lastLabel, newLbl);
      }
      insn = insn.getNext();
    }

    for(FrameNode fn : frames){
      for(int i = 0; i < fn.local.size(); i++){
      	if(fn.local.get(i) != null && labelToLabel.containsKey(fn.local.get(i))){
      	  fn.local.set(i, labelToLabel.get(fn.local.get(i)));
        }
      }
      for(int i = 0; i < fn.stack.size(); i++){
      	Object o = fn.stack.get(i);
      	if(fn.stack.get(i) != null && labelToLabel.containsKey(fn.stack.get(i))){
      	  fn.stack.set(i, labelToLabel.get(fn.stack.get(i)));
        }
      }
    }
  }

  private List<Block> findRequriedProbeLocations() {
    return ControlFlowAnalyser.analyze(this);
  }
}
