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

public class SuperclassMatcher extends AbstractWildcardMatcher {

  public SuperclassMatcher(String expression) {
    super(expression);
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    String superClass = ctClass.superName;
    while (superClass != null && !superClass.equals("java/lang/Object")) {
      try {
        ClassReader cr = new ClassReader(superClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);
        if (this.matches(cn.name.replace('/', '.'))) {
          return true;
        }
        superClass = cn.superName;
      } catch (IOException e) {
        // NO-OP
      }
    }
    return false;
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    throw new UnsupportedOperationException();
  }

}
