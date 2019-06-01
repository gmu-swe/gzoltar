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
package com.gzoltar.internal.core.instr.analysis;

import com.gzoltar.internal.core.instr.InstrumentationConstants;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;

abstract class AbstractCoverageStrategy extends AdviceAdapter {

  protected final MethodVisitor methodVisitor;
  protected final ProbeGroup probeGroup;
  protected final List<Block> blocks;

  protected  final String className;
  protected final String methodName;
  protected final String methodDesc;
  protected int line;
  protected final InstructionCounter counter;

  /**
   * label to mark start of try finally block that is added to each method
   */
  private final Label before     = new Label();

  /**
   * label to mark handler block of try finally
   */
  private final Label handler    = new Label();


  AbstractCoverageStrategy(List<Block> blocks, InstructionCounter counter, String className,
                           final MethodVisitor writer, final int access,
                           final String name, final String desc, final ProbeGroup probeGroup) {
    super(InstrumentationConstants.ASM_VERSION, writer, access, name, desc);

    this.methodVisitor = writer;
    this.className = className;
    this.counter = counter;
    this.blocks = blocks;
    this.probeGroup = probeGroup;
    this.methodName = name;
    this.methodDesc = desc;
  }

  abstract void prepare();

  abstract void generateProbeReportCode();

  abstract void insertNormalProbeForLine();

  @Override
  public void visitCode() {
    super.visitCode();

    prepare();

    this.mv.visitLabel(this.before);
  }

  protected void pushConstant(final int value) {
    switch (value) {
    case 0:
      this.mv.visitInsn(ICONST_0);
      break;
    case 1:
      this.mv.visitInsn(ICONST_1);
      break;
    case 2:
      this.mv.visitInsn(ICONST_2);
      break;
    case 3:
      this.mv.visitInsn(ICONST_3);
      break;
    case 4:
      this.mv.visitInsn(ICONST_4);
      break;
    case 5:
      this.mv.visitInsn(ICONST_5);
      break;
    default:
      if (value <= Byte.MAX_VALUE) {
        this.mv.visitIntInsn(Opcodes.BIPUSH, value);
      } else if (value <= Short.MAX_VALUE) {
        this.mv.visitIntInsn(Opcodes.SIPUSH, value);
      } else {
        this.mv.visitLdcInsn(value);
      }
    }
  }

  @Override
  public void visitFrame(final int type, final int nLocal,
                         final Object[] local, final int nStack, final Object[] stack) {
    insertProbeIfAppropriate();
    super.visitFrame(type, nLocal, local, nStack, stack);
  }

  @Override
  public void visitInsn(final int opcode) {
    insertProbeIfAppropriate();
    super.visitInsn(opcode);
  }

  @Override
  public void visitIntInsn(final int opcode, final int operand) {
    insertProbeIfAppropriate();
    super.visitIntInsn(opcode, operand);
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    insertProbeIfAppropriate();
    super.visitVarInsn(opcode, var);
  }

  @Override
  public void visitTypeInsn(final int opcode, final String type) {
    insertProbeIfAppropriate();
    super.visitTypeInsn(opcode, type);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
                             final String name, final String desc) {
    insertProbeIfAppropriate();
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
                              final String name, final String desc, boolean itf) {
    insertProbeIfAppropriate();
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitInvokeDynamicInsn(final String name, final String desc,
                                     final Handle bsm, final Object... bsmArgs) {
    insertProbeIfAppropriate();
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    insertProbeIfAppropriate();
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitLabel(final Label label) {
    super.visitLabel(label);
    // note - probe goes after the label
    insertProbeIfAppropriate();
  }

  @Override
  public void visitLdcInsn(final Object cst) {
    insertProbeIfAppropriate();
    super.visitLdcInsn(cst);
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    insertProbeIfAppropriate();
    super.visitIincInsn(var, increment);
  }

  @Override
  public void visitTableSwitchInsn(final int min, final int max,
                                   final Label dflt, final Label... labels) {
    insertProbeIfAppropriate();
    super.visitTableSwitchInsn(min, max, dflt, labels);
  }

  @Override
  public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
                                    final Label[] labels) {
    insertProbeIfAppropriate();
    super.visitLookupSwitchInsn(dflt, keys, labels);
  }

  @Override
  public void visitMultiANewArrayInsn(final String desc, final int dims) {
    insertProbeIfAppropriate();
    super.visitMultiANewArrayInsn(desc, dims);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    this.line = line;
    insertProbeIfAppropriate();
    super.visitLineNumber(line, start);
  }

  private void insertProbeIfAppropriate() {
    if (needsProbe(this.counter.currentInstructionCount()) && line > 0) {
      insertNormalProbeForLine();
    }
  }

  private boolean needsProbe(int currentInstructionCount) {
    for (final Block each : this.blocks) {
      if (each.firstInstructionIs(currentInstructionCount - 1)) {
        return true;
      }
    }
    return false;
  }

}