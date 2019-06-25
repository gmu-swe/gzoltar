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
package com.gzoltar.core.instr.matchers;

import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.instr.InstrumentationConstants;

public class MethodNameMatcher extends AbstractWildcardMatcher {

  public MethodNameMatcher(final String expression) {
    super(expression);
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    if (ctClass.methods != null) {
      for (MethodNode ctBehavior : ctClass.methods) {
        if (this.matches(ctBehavior)) {
          return true;
        }
      }
    }

    // Include non-private methods inherited from the superclasses
    String parent = ctClass.superName;
    if (parent != null) {
      try {
        ClassReader cr = new ClassReader(parent);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);
        return matches(cn);
      } catch (IOException e) {
        // NO-OP
      }
    }

    return false;
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    return super.matches(
        ctBehavior.name + InstrumentationConstants.toJavassistDescriptor(ctBehavior.desc));
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    throw new UnsupportedOperationException();
  }

}
