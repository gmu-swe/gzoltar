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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractAttributeMatcher implements IMatcher {

  private final String attribute;

  protected AbstractAttributeMatcher(final String attribute) {
    this.attribute = attribute;
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    for (Attribute attr : ctClass.attrs) {
      if (attr.type.equals(this.attribute)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    for (Attribute attr : ctBehavior.attrs) {
      if (attr.type.equals(this.attribute)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    for (Attribute attr : ctField.attrs) {
      if (attr.type.equals(this.attribute)) {
        return true;
      }
    }
    return false;
  }

}
