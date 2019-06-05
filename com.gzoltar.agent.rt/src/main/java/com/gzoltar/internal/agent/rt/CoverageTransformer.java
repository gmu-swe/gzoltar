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
package com.gzoltar.internal.agent.rt;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import com.gzoltar.internal.core.AgentConfigs;
import com.gzoltar.internal.core.instr.Instrumenter;
import com.gzoltar.internal.core.instr.Outcome;
import com.gzoltar.internal.core.instr.actions.BlackList;
import com.gzoltar.internal.core.instr.actions.WhiteList;
import com.gzoltar.internal.core.instr.filter.Filter;
import com.gzoltar.internal.core.instr.matchers.ClassNameMatcher;
import com.gzoltar.internal.core.instr.matchers.PrefixMatcher;
import com.gzoltar.internal.core.instr.matchers.SourceLocationMatcher;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

public class CoverageTransformer implements ClassFileTransformer {

  private final Instrumenter instrumenter;

  private final String buildLocation;

  private final boolean inclNoLocationClasses;

  public CoverageTransformer(final AgentConfigs agentConfigs) throws Exception {
    this.instrumenter = new Instrumenter(agentConfigs);

    this.buildLocation = new File(agentConfigs.getBuildLocation()).getCanonicalPath();
    this.inclNoLocationClasses = agentConfigs.getInclNoLocationClasses();

  }

  public byte[] transform(final ClassLoader loader, final String className,
      final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
      final byte[] classfileBuffer) {

    if (loader == null) {
      // do not instrument bootstrap classes, e.g., "javax.", "java.", "sun.", "com.sun."
      return null;
    }

    // only instrument classes under a build location, e.g., target/classes/ or build/classes/
    SourceLocationMatcher excludeClassesNotInBuildLocation = new SourceLocationMatcher(
            this.inclNoLocationClasses, this.buildLocation, protectionDomain);
    ClassNode fakeCN = new ClassNode();
    fakeCN.name = className;
    if (!excludeClassesNotInBuildLocation.matches(fakeCN)) {
      return null;
    }

    if (classBeingRedefined != null) {
      // avoid re-instrumention
      return null;
    }

    try {
      return this.instrumenter.instrument(classfileBuffer);
    } catch (Throwable e) {
      System.err.println("Error while instrumenting "+ className);
      e.printStackTrace();
      return null;
    }
  }

}
