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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PrefixMatcher implements IMatcher {

  private List<String> prefix = new LinkedList<String>();

  public PrefixMatcher(final String... strings) {
    this.prefix.addAll(Arrays.asList(strings));
  }

  public PrefixMatcher(final List<String> strings) {
    this.prefix.addAll(strings);
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    return this.matchesPrefix(ctClass.name);
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    return this.matchesPrefix(ctBehavior.name);
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    return this.matchesPrefix(ctField.name);
  }

  private boolean matchesPrefix(String name) {
    for (String p : this.prefix) {
      if (name.startsWith(p)) {
        return true;
      }
    }
    return false;
  }

}
