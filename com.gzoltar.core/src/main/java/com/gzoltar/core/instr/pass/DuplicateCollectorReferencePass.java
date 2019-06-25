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
package com.gzoltar.core.instr.pass;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.instr.Outcome;
import com.gzoltar.core.instr.filter.DuplicateCollectorReferenceFilter;

public class DuplicateCollectorReferencePass implements IPass {

  private final DuplicateCollectorReferenceFilter duplicateCollectorReferenceFilter =
      new DuplicateCollectorReferenceFilter();

  @Override
  public Outcome transform(ClassNode ctClass) throws Exception {
    return this.duplicateCollectorReferenceFilter.filter(ctClass);
  }

  @Override
  public Outcome transform(ClassNode ctClass, MethodNode ctBehavior) throws Exception {
    return this.duplicateCollectorReferenceFilter.filter(ctBehavior);
  }

}
