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
package com.gzoltar.core.util;

import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassTestUtils {

  /**
   * 
   * @param className
   * @return
   */
  public static ClassNode getClassNode(final String className) {
    try {
      ClassReader cr = new ClassReader(className);
      ClassNode cn = new ClassNode();
      cr.accept(cn, 0);
      return cn;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 
   * @param className
   * @param methodName
   * @return
   */
  public static MethodNode getMethodNode(final String className, final String methodName) {
    try {
      ClassReader cr = new ClassReader(className);
      ClassNode cn = new ClassNode();
      cr.accept(cn, 0);
      for (MethodNode mn : cn.methods) {
        if (mn.name.equals(methodName)) {
          return mn;
        }
      }
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
