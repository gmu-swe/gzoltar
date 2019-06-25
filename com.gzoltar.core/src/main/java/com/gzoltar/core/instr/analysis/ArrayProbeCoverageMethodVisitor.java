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

package com.gzoltar.core.instr.analysis;

import java.util.List;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import com.gzoltar.core.instr.InstrumentationConstants;
import com.gzoltar.core.model.Node;
import com.gzoltar.core.model.NodeFactory;
import com.gzoltar.core.runtime.Probe;
import com.gzoltar.core.runtime.ProbeGroup;

/**
 * Instruments a method adding probes at each line. The strategy requires the
 * compiler to be configured to add line number debug information.
 *
 * Probes are implemented by adding an array to each method. Lines hits are
 * registered by a write to this local array. Each method exit point is then
 * augmented with a call that passes this array to the coverage store class that
 * handles communication of this data back to the parent process on the
 * completion of each test.
 *
 * All methods are wrapped in a try finally block to ensure that coverage data
 * is sent in the event of a runtime exception.
 *
 * Creating a new array on each method entry is not cheap - other coverage
 * systems add a static field used across all methods. We must clear down all
 * coverage history for each test however. Resetting static fields in all loaded
 * classes would be messy to implement - it may or may not be faster than the
 * current approach.
 */
public class ArrayProbeCoverageMethodVisitor extends AbstractCoverageStrategy {

  private int probeHitArrayLocal;

  private String formattedNameForProbe;
  private String formattedDescForProbe;

  private boolean skipExceptionProbes;
  public ArrayProbeCoverageMethodVisitor(List<Block> blocks,
                                         InstructionCounter counter, String className, final AnalyzerAdapter analyzer,
                                         final MethodVisitor writer, final boolean addFrames, final int access, final String name,
                                         final String desc, final ProbeGroup probeGroup, boolean skipExceptionProbes) {
    super(blocks, counter, className, analyzer, writer, access, addFrames, name, desc, probeGroup);
    this.skipExceptionProbes = skipExceptionProbes;
    formattedNameForProbe = name;
    formattedDescForProbe = InstrumentationConstants.toJavassistDescriptor(methodDesc);
    if(name.equals("<init>"))
    {
      //Make it the name of the class
      String classNameWithoutPackage = className;
      if (classNameWithoutPackage.contains("/"))
        classNameWithoutPackage = classNameWithoutPackage.substring(classNameWithoutPackage.lastIndexOf('/') + 1);
      formattedNameForProbe = classNameWithoutPackage;
    }
  }

  @Override
  void prepare() {
  }


  @Override
  void generateProbeReportCode() {

  }

  @Override
  void insertDecisionProbe(String decision) {
  	if(skipExceptionProbes)
  	  return;
    Node parent = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line);
    parent = this.probeGroup.findNodeByNode(parent);

    Node node = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line, decision, parent);
    Probe probe = this.probeGroup.registerProbe(node, formattedNameForProbe, formattedDescForProbe);

    mv.visitFieldInsn(Opcodes.GETSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);
    mv.visitInsn(Opcodes.DUP);
    pushConstant(probe.getArrayIndex());
    mv.visitInsn(Opcodes.DUP_X1);
    mv.visitInsn(Opcodes.IALOAD);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IADD);
    mv.visitInsn(Opcodes.IASTORE);
  }

  @Override
  void insertPostInsnProbe() {
    if(skipExceptionProbes || !INSERT_EXTRA_PROBES)
      return;
    Node parent = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line);
    parent = this.probeGroup.findNodeByNode(parent);
    Node node = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line, "ExceptionalInsn"+this.counter.currentInstructionCount(), parent);
    Probe probe = this.probeGroup.registerProbe(node, formattedNameForProbe, formattedDescForProbe);

    mv.visitFieldInsn(Opcodes.GETSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);
    mv.visitInsn(Opcodes.DUP);
    pushConstant(probe.getArrayIndex());
    mv.visitInsn(Opcodes.DUP_X1);
    mv.visitInsn(Opcodes.IALOAD);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.ISUB);
    mv.visitInsn(Opcodes.IASTORE);


  }

  @Override
  void insertPreInsnProbe() {
    if(skipExceptionProbes || !INSERT_EXTRA_PROBES)
      return;
    Node parent = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line);
    parent = this.probeGroup.findNodeByNode(parent);

    Node node = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line, "ExceptionalInsn"+this.counter.currentInstructionCount(), parent);
    Probe probe = this.probeGroup.registerProbe(node, formattedNameForProbe, formattedDescForProbe);

    mv.visitFieldInsn(Opcodes.GETSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);
    mv.visitInsn(Opcodes.DUP);
    pushConstant(probe.getArrayIndex());
    mv.visitInsn(Opcodes.DUP_X1);
    mv.visitInsn(Opcodes.IALOAD);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IADD);
    mv.visitInsn(Opcodes.IASTORE);

  }

  @Override
  void insertNormalProbeForLine() {

    Node node = NodeFactory.createNode(className, formattedNameForProbe, formattedDescForProbe, line);
    Probe probe = this.probeGroup.registerProbe(node, formattedNameForProbe, formattedDescForProbe);

    mv.visitFieldInsn(Opcodes.GETSTATIC, className, InstrumentationConstants.FIELD_NAME, InstrumentationConstants.FIELD_DESC_BYTECODE);
    mv.visitInsn(Opcodes.DUP);
    pushConstant(probe.getArrayIndex());
    mv.visitInsn(Opcodes.DUP_X1);
    mv.visitInsn(Opcodes.IALOAD);
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IADD);
    mv.visitInsn(Opcodes.IASTORE);

  }

}
