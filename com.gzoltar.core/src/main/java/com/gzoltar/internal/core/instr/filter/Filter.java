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
package com.gzoltar.internal.core.instr.filter;

import com.gzoltar.internal.core.instr.Outcome;
import com.gzoltar.internal.core.instr.actions.IAction;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Filter implements IFilter {

  private final List<IAction> actions = new ArrayList<IAction>();

  public Filter(final IAction... actions) {
    this.actions.addAll(Arrays.asList(actions));
  }

  public void add(final IAction action) {
    this.actions.add(action);
  }

  @Override
  public Outcome filter(final ClassNode ctClass) {
    return this.filter(ctClass, this.actions);
  }

  @Override
  public Outcome filter(final MethodNode ctBehavior) {
    return this.filter(ctBehavior, this.actions);
  }

  protected Outcome filter(final Object object, final List<IAction> actions) {
    for (IAction action : actions) {
      switch (this.filter(object, action)) {
        case ACCEPT:
        default:
          continue;
        case REJECT:
          return Outcome.REJECT;
      }
    }
    return Outcome.ACCEPT;
  }

  protected Outcome filter(final Object object, final IAction action) {
    if (object instanceof ClassNode) {
      return action.getAction((ClassNode) object);
    } else if (object instanceof MethodNode) {
      return action.getAction((MethodNode) object);
    } else {
      throw new IllegalArgumentException(
          "Object of type " + object.getClass().getName() + " is not allowed");
    }
  }
}