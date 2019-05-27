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

import com.gzoltar.core.instr.CoverageClassVisitor;
import com.gzoltar.core.instr.InstrumentationConstants;
import com.gzoltar.core.runtime.ProbeGroup;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

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

  public CoverageAnalyser(final CoverageClassVisitor parent, final ProbeGroup probeGroup,
                          final String className, final MethodVisitor mv, final int access,
                          final String name, final String desc, final String signature,
                          final String[] exceptions) {
    super(InstrumentationConstants.ASM_VERSION, access, name, desc, signature, exceptions);
    this.mv = mv;
    this.parent = parent;
    this.probeGroup = probeGroup;
    this.className = className;
  }

  @Override
  public void visitEnd() {
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

      // for now fall back to the naive implementation - could instead use array
      // passing version
      accept(new InstructionTrackingMethodVisitor(
          new ArrayProbeCoverageMethodVisitor(blocks, counter,this.className,
              this.mv, this.access, this.name, this.desc, this.probeGroup),
              counter));

  }

  private List<Block> findRequriedProbeLocations() {
    return ControlFlowAnalyser.analyze(this);
  }
}
