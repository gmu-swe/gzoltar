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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractModifierMatcher implements IMatcher {

  private final int modifierMask;

  protected AbstractModifierMatcher(final int modifierMask) {
    this.modifierMask = modifierMask;
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    return this.matches(ctClass.access);
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    return this.matches(ctBehavior.access);
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    return this.matches(ctField.access);
  }

  private final boolean matches(final int modifier) {
    return (modifier & this.modifierMask) != 0;
  }

}
