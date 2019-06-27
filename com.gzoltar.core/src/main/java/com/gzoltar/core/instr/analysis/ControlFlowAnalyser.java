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

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.CALOAD;
import static org.objectweb.asm.Opcodes.CASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class ControlFlowAnalyser {

  private static final int LIKELY_NUMBER_OF_LINES_PER_BLOCK = 7;

  private static boolean shouldSkipBlock(AbstractInsnNode insn){
    if(insn.getType() == AbstractInsnNode.LINE)
      return false;
    while(insn.getNext() != null && (insn.getNext().getType() == AbstractInsnNode.FRAME || insn.getNext().getType() == AbstractInsnNode.LABEL))
    {
      insn = insn.getNext();
    }
    return insn.getNext() != null && insn.getNext().getType() == AbstractInsnNode.LINE;
  }
  public static List<Block> analyze(final MethodNode mn) {
    final List<Block> blocks = new ArrayList<>(mn.instructions.size());

    final Set<LabelNode> jumpTargets = findJumpTargets(mn.instructions);

    // not managed to construct bytecode to show need for this
    // as try catch blocks usually have jumps at their boundaries anyway.
    // so possibly useless, but here for now. Because fear.
    addtryCatchBoundaries(mn, jumpTargets);

    Set<Integer> blockLines = smallSet();
    int lastLine = Integer.MIN_VALUE;

    final int lastInstruction = mn.instructions.size() - 1;


    int blockStart = 0;
    for (int i = 0; i != mn.instructions.size(); i++) {

      final AbstractInsnNode ins = mn.instructions.get(i);

      if (ins instanceof LineNumberNode) {
        final LineNumberNode lnn = (LineNumberNode) ins;
//        blockLines.add(lnn.line);
        lastLine = lnn.line;
      }
      if(ins instanceof FrameNode){
        //Correct the last block to not start until after this frame
        blockStart = i;
      }
      if (jumpTargets.contains(ins) && (blockStart != i) && !shouldSkipBlock(ins)){
        if (blockLines.isEmpty() && blocks.size() > 0 && !blocks
            .get(blocks.size() - 1).getLines().isEmpty()) {
          blockLines.addAll(blocks.get(blocks.size() - 1).getLines());
        }
        blocks.add(new Block(blockStart, i - 1, blockLines));
        blockStart = i;
        blockLines = smallSet();
      } else if (endsBlock(ins) && !shouldSkipBlock(ins)) {
        if (blockLines.isEmpty() && blocks.size() > 0 && !blocks
            .get(blocks.size() - 1).getLines().isEmpty()) {
          blockLines.addAll(blocks.get(blocks.size() - 1).getLines());
        }
        if (!blockLines.isEmpty() && (blockStart != i || isReturn(ins)))
          blocks.add(new Block(blockStart, i, blockLines));
        blockStart = i + 1;
        blockLines = smallSet();
        blockLines.add(lastLine);
      } else if ((lastLine != Integer.MIN_VALUE)) { // && isInstruction(ins)) {
        blockLines.add(lastLine);
      }
    }

    // this will not create a block if the last block contains only a single
    // instruction.
    // In the case of the hanging labels that eclipse compiler seems to generate
    // this is desirable.
    // Not clear if this will create problems in other scenarios
    if (blockStart != lastInstruction) {
      blocks.add(new Block(blockStart, lastInstruction, blockLines));
    }

    return blocks;

  }

  private static HashSet<Integer> smallSet() {
    return new HashSet<>(LIKELY_NUMBER_OF_LINES_PER_BLOCK);
  }

  private static boolean isInstruction(final AbstractInsnNode ins) {
    return !((ins instanceof LabelNode) || (ins instanceof FrameNode));
  }

  private static void addtryCatchBoundaries(final MethodNode mn,
      final Set<LabelNode> jumpTargets) {
    for (final Object each : mn.tryCatchBlocks) {
      final TryCatchBlockNode tcb = (TryCatchBlockNode) each;
      jumpTargets.add(tcb.handler);
    }
  }

  private static boolean endsBlock(final AbstractInsnNode ins) {
    return (ins instanceof JumpInsnNode && ins.getOpcode() != Opcodes.GOTO) || isReturn(ins)
     || ins instanceof LineNumberNode;
//        || isMightThrowException(ins);
  }

  private static boolean isMightThrowException(int opcode) {
    switch (opcode) {
    //division by 0
    case IDIV:
    case FDIV:
    case LDIV:
    case DDIV:
      //NPE
    case MONITORENTER:
    case MONITOREXIT: //or illegalmonitor
      //ArrayIndexOutOfBounds or null pointer
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
    case CHECKCAST: //incompatible cast
      //trigger class initialization
//    case NEW: //will break powermock :(
    case NEWARRAY:
    case GETSTATIC:
    case PUTSTATIC:
    case GETFIELD:
    case PUTFIELD:
      return true;
    default:
      return false;
    }
  }

  private static boolean isMightThrowException(AbstractInsnNode ins) {
    switch (ins.getType()) {
    case AbstractInsnNode.MULTIANEWARRAY_INSN:
      return true;
    case AbstractInsnNode.INSN:
    case AbstractInsnNode.TYPE_INSN:
    case AbstractInsnNode.FIELD_INSN:
      return isMightThrowException(ins.getOpcode());
    case AbstractInsnNode.METHOD_INSN:
      return true;
    default:
      return false;
    }

  }

  private static boolean isReturn(final AbstractInsnNode ins) {
    final int opcode = ins.getOpcode();
    switch (opcode) {
    case RETURN:
    case ARETURN:
    case DRETURN:
    case FRETURN:
    case IRETURN:
    case LRETURN:
    case ATHROW:
      return true;
    }

    return false;

  }

  private static Set<LabelNode> findJumpTargets(final InsnList instructions) {
    final Set<LabelNode> jumpTargets = new HashSet<>();
    final ListIterator<AbstractInsnNode> it = instructions.iterator();
    while (it.hasNext()) {
      final AbstractInsnNode o = it.next();
      if (o instanceof JumpInsnNode) {
        jumpTargets.add(((JumpInsnNode) o).label);
      } else if (o instanceof TableSwitchInsnNode) {
        final TableSwitchInsnNode twn = (TableSwitchInsnNode) o;
        jumpTargets.add(twn.dflt);
        jumpTargets.addAll(twn.labels);
      } else if (o instanceof LookupSwitchInsnNode) {
        final LookupSwitchInsnNode lsn = (LookupSwitchInsnNode) o;
        jumpTargets.add(lsn.dflt);
        jumpTargets.addAll(lsn.labels);
      }
    }
    return jumpTargets;
  }

}
