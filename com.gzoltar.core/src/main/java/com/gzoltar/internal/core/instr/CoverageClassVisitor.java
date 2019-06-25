/*
 * Copied from pit - https://github.com/hcoles/pitest/
 *
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
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

package com.gzoltar.internal.core.instr;

import com.gzoltar.internal.core.AgentConfigs;
import com.gzoltar.internal.core.instr.analysis.CoverageAnalyser;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Instruments a class with probes on each line
 */
public class CoverageClassVisitor extends ClassVisitor {

	private final ProbeGroup probeGroup;
	boolean hasClinit = false;
	private InstrumentationLevel instrumentationLevel;
	private boolean offline;
	private boolean isEnum;
	private String className;
	private boolean isInnerClass = false;
	private boolean addFrames;

	public CoverageClassVisitor(final ClassVisitor writer, final AgentConfigs agentConfigs, final ProbeGroup probeGroup) {
		super(Opcodes.ASM5, writer);
		this.instrumentationLevel = agentConfigs.getInstrumentationLevel();
		switch (this.instrumentationLevel) {
			case FULL:
			default:
				this.offline = false;
				break;
			case OFFLINE:
				this.offline = true;
				break;
			case NONE:
				break;
		}

		this.probeGroup = probeGroup;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.isEnum = (access & Opcodes.ACC_ENUM) != 0;
		this.className = name;
		this.addFrames = (version & 0xFFFF) >= Opcodes.V1_7;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		isInnerClass = true;
		super.visitOuterClass(owner, name, desc);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if ((access & Opcodes.ACC_SYNTHETIC) != 0)
			return mv; //exclude synthetic methods

		//Call INIT method for *all* methods to avoid issues with static inner classes
		mv = new MethodVisitor(InstrumentationConstants.ASM_VERSION, mv) {
			@Override
			public void visitCode() {
				super.visitCode();
				super.visitMethodInsn(Opcodes.INVOKESTATIC, className, InstrumentationConstants.INIT_METHOD_NAME, "()V", false);
			}
		};

		if(name.equals("<clinit>"))
			hasClinit = true;

		/**
		 * Filters methods 'values' and 'valueOf' of enum classes.
		 */
		if (isEnum && (name.equals("values") || name.equals("valueOf")))
			return mv;
		//also skip constructors of anonymous classes
		if (isInnerClass && name.equals("<init>"))
			return mv;

		return new CoverageAnalyser(this, this.probeGroup, this.className,
				mv, access, addFrames, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		super.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE, null, null);


		MethodVisitor mv = super.visitMethod(InstrumentationConstants.INIT_METHOD_ACC, InstrumentationConstants.INIT_METHOD_NAME, "()V", null, null);
		GeneratorAdapter ga = new GeneratorAdapter(mv, InstrumentationConstants.INIT_METHOD_ACC, InstrumentationConstants.INIT_METHOD_NAME, "()V");
		ga.visitCode();

		Label done = new Label();
		ga.visitFieldInsn(Opcodes.GETSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);
		ga.visitJumpInsn(Opcodes.IFNONNULL, done);

		ga.push(3);
		ga.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

		ga.visitInsn(Opcodes.DUP);
		ga.push(0);
		ga.visitLdcInsn(this.probeGroup.getHash());
		ga.visitInsn(Opcodes.AASTORE);

		ga.visitInsn(Opcodes.DUP);
		ga.push(1);
		ga.visitLdcInsn(this.className);
		ga.visitInsn(Opcodes.AASTORE);

		ga.visitInsn(Opcodes.DUP);
		ga.push(2);
		ga.push(this.probeGroup.getNumberOfProbes());
		ga.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer","valueOf","(I)Ljava/lang/Integer;",false);
		ga.visitInsn(Opcodes.AASTORE);

		ga.visitInsn(Opcodes.DUP);
		/*
		SIDE-EFFECT WARNING

		For whatever reason, this is how we initialize our probe array. the "object" field
		is really the Collector, and "equals" really modifies entry 0 of the array to be the desired probe array

		When migrating to ASM, it was easier to just leave this as it is.
		 */
		ga.visitFieldInsn(Opcodes.GETSTATIC, InstrumentationConstants.SYSTEM_CLASS_NAME, InstrumentationConstants.SYSTEM_CLASS_FIELD_NAME, "Ljava/lang/Object;");
		ga.visitInsn(Opcodes.SWAP);
		ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		ga.visitInsn(Opcodes.POP);


		ga.push(0);
		ga.visitInsn(Opcodes.AALOAD);
		ga.visitTypeInsn(Opcodes.CHECKCAST,"[I");
		ga.visitFieldInsn(Opcodes.PUTSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);

		ga.visitLabel(done);
		ga.visitFrame(Opcodes.F_NEW, 0, new Object[0], 0, new Object[0]);
		ga.visitInsn(Opcodes.RETURN);
		ga.visitMaxs(0, 0);
		ga.visitEnd();

		if (!hasClinit) {
			mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, InstrumentationConstants.INIT_METHOD_NAME, "()V", false);

			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

	}

}
