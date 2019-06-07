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
import com.gzoltar.internal.core.instr.OffsetPreservingLabel;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.tree.FrameNode;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractCoverageStrategy extends AdviceAdapter {

	public static final boolean INSERT_EXTRA_PROBES = System.getenv("EXTRA_PROBES") != null;
	protected final MethodVisitor methodVisitor;
	protected final ProbeGroup probeGroup;
	protected final List<Block> blocks;

	protected final String className;
	protected final String methodName;
	protected final String methodDesc;
	protected final InstructionCounter counter;
	protected final AnalyzerAdapter analyzerAdapter;
	protected boolean isUninitConstructor;
	/**
	 * label to mark start of try finally block that is added to each method
	 */
	private final Label before = new Label();
	/**
	 * label to mark handler block of try finally
	 */
	private final Label handler = new Label();
	protected int line;
	protected final boolean addFrames;


	AbstractCoverageStrategy(List<Block> blocks, InstructionCounter counter, String className, final AnalyzerAdapter analyzer,
	                         final MethodVisitor writer, final int access, final boolean addFrames,
	                         final String name, final String desc, final ProbeGroup probeGroup) {
		super(InstrumentationConstants.ASM_VERSION, writer, access, name, desc);

		this.methodVisitor = writer;
		this.className = className;
		this.counter = counter;
		this.blocks = blocks;
		this.probeGroup = probeGroup;
		this.methodName = name;
		this.methodDesc = desc;
		this.analyzerAdapter = analyzer;
		this.addFrames = addFrames;
		this.isUninitConstructor = name.equals("<init>");
	}

	abstract void prepare();

	abstract void generateProbeReportCode();

	abstract void insertNormalProbeForLine();

	abstract void insertPreInsnProbe();

	abstract void insertPostInsnProbe();

	abstract void insertDecisionProbe(String decision);

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
		if(this.addFrames)
			super.visitFrame(type, nLocal, local, nStack, stack);
		insertProbeIfAppropriate();
	}

	@Override
	public void visitInsn(final int opcode) {
		insertProbeIfAppropriate();
		switch (opcode) {
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
				insertPreInsnProbe();
				super.visitInsn(opcode);
				insertPostInsnProbe();
				break;
			default:
				super.visitInsn(opcode);
				break;
		}
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
		switch (opcode) {
			case CHECKCAST: //incompatible cast
				//trigger class initialization
//    case NEW: //will break powermock :(
			case NEWARRAY:
				insertPreInsnProbe();
				super.visitTypeInsn(opcode, type);
				insertPostInsnProbe();
				break;
			default:
				super.visitTypeInsn(opcode, type);
				break;
		}
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
	                           final String name, final String desc) {
		insertProbeIfAppropriate();
		insertPreInsnProbe();
		super.visitFieldInsn(opcode, owner, name, desc);
		insertPostInsnProbe();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
	                            final String name, final String desc, boolean itf) {
		insertProbeIfAppropriate();
		super.visitMethodInsn(opcode, owner, name, desc, itf);
		if(isUninitConstructor && opcode == INVOKESPECIAL && name.equals("<init>"))
			isUninitConstructor = false;
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc,
	                                   final Handle bsm, final Object... bsmArgs) {
		insertProbeIfAppropriate();
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	private static Object[] removeLongsDoubleTopVal(List<Object> in) {
		if(in == null)
			return new Object[0];
		ArrayList<Object> ret = new ArrayList<Object>();
		boolean lastWas2Word = false;
		for (Object n : in) {
			if ((n == Opcodes.TOP) && lastWas2Word) {
				//nop
			} else
				ret.add(n);
			if (n == Opcodes.DOUBLE || n == Opcodes.LONG )
				lastWas2Word = true;
			else
				lastWas2Word = false;
		}
		return ret.toArray();
	}
	private FrameNode getCurrentFrameNode()
	{
		Object[] locals = removeLongsDoubleTopVal(analyzerAdapter.locals);
		Object[] stack = removeLongsDoubleTopVal(analyzerAdapter.stack);
		FrameNode ret = new FrameNode(Opcodes.F_NEW, locals.length, locals, stack.length, stack);
		return ret;
	}

	private FrameNode getCurrentFrameNodeWithoutStackTop() {
		Object[] locals = removeLongsDoubleTopVal(analyzerAdapter.locals);
		ArrayList<Object> stack_list = new ArrayList<>(analyzerAdapter.stack);
		stack_list.remove(stack_list.size() - 1);
		Object[] stack = removeLongsDoubleTopVal(stack_list);
		FrameNode ret = new FrameNode(Opcodes.F_NEW, locals.length, locals, stack.length, stack);
		return ret;
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		insertProbeIfAppropriate();
		if (INSERT_EXTRA_PROBES && !isUninitConstructor && opcode != Opcodes.GOTO) {
			Label newJumpTarget = new Label();
			Label endOfJumpHandler = new Label();
			super.visitJumpInsn(opcode, newJumpTarget);
			FrameNode currFrame = getCurrentFrameNode();
			//This is the case of branch not taken
			insertDecisionProbe("BranchNotTaken");

			super.visitJumpInsn(GOTO, endOfJumpHandler);
			super.visitLabel(newJumpTarget);
			if (addFrames)
				currFrame.accept(this.mv);

			insertDecisionProbe("BranchTakenTo" + ((OffsetPreservingLabel) label).getOriginalPosition());
			super.visitJumpInsn(GOTO, label);
			super.visitLabel(endOfJumpHandler);
			if (addFrames) {
				currFrame.accept(this.mv);
				super.visitInsn(NOP);
			}
		} else
			{
			super.visitJumpInsn(opcode, label);
		}


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
		if(INSERT_EXTRA_PROBES) {
			Label[] oldLabels = labels;
			Label[] newLabels = new Label[oldLabels.length];
			for (int i = 0; i < oldLabels.length; i++) {
				newLabels[i] = new Label();
			}
			FrameNode curFrame = getCurrentFrameNodeWithoutStackTop();
			//Remove the top element off of the stack, simulating the tableswitch
			super.visitTableSwitchInsn(min, max, dflt, newLabels);
			for (int i = 0; i < newLabels.length; i++) {
				super.visitLabel(newLabels[i]);
				if (addFrames)
					curFrame.accept(this.mv);
				insertDecisionProbe("SwitchToOffset" + ((OffsetPreservingLabel) oldLabels[i]).getOriginalPosition());
				super.visitJumpInsn(Opcodes.GOTO, oldLabels[i]);
			}
		} else {
			super.visitTableSwitchInsn(min, max, dflt, labels);
		}
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
	                                  final Label[] labels) {
		insertProbeIfAppropriate();
		if (INSERT_EXTRA_PROBES) {
			Label[] oldLabels = labels;
			Label[] newLabels = new Label[oldLabels.length];
			for (int i = 0; i < oldLabels.length; i++) {
				newLabels[i] = new Label();
			}
			FrameNode curFrame = getCurrentFrameNodeWithoutStackTop();
			super.visitLookupSwitchInsn(dflt, keys, labels);
			for (int i = 0; i < newLabels.length; i++) {
				super.visitLabel(newLabels[i]);
				if (addFrames)
					curFrame.accept(this.mv);
				insertDecisionProbe("SwitchToOffset" + ((OffsetPreservingLabel) oldLabels[i]).getOriginalPosition());
				super.visitJumpInsn(Opcodes.GOTO, oldLabels[i]);
			}
		} else {
			super.visitLookupSwitchInsn(dflt, keys, labels);
		}
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