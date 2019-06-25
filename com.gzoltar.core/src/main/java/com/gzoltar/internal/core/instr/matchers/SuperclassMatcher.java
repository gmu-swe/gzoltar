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
package com.gzoltar.internal.core.instr.matchers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.HashMap;

public class SuperclassMatcher extends AbstractWildcardMatcher {

  private final static HashMap<String, ClassNode> classNodeHashMap = new HashMap<>();
  public SuperclassMatcher(String expression) {
    super(expression);
  }

  static ClassNode getOrFindClassNode(String name){
    if(classNodeHashMap.containsKey(name))
      return classNodeHashMap.get(name);
    try {
      ClassReader cr = new ClassReader(name);
      ClassNode cn = new ClassNode();
      cr.accept(cn, ClassReader.SKIP_CODE);
      classNodeHashMap.put(name, cn);
      return cn;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }
  @Override
  public boolean matches(final ClassNode ctClass) {
    String superClass = ctClass.superName;
    while (superClass != null && !super.matches(superClass.replace('/','.')) && !superClass.equals("java/lang/Object")) {
      ClassNode cn = getOrFindClassNode(superClass);
      if (cn != null) {
        superClass = cn.superName;
      } else {
        superClass = null;
      }
    }
    if (superClass != null && super.matches(superClass.replace('/','.')))
      return true;
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
