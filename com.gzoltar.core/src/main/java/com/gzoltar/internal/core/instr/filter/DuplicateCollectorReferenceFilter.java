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

import com.gzoltar.internal.core.instr.actions.BlackList;
import com.gzoltar.internal.core.instr.matchers.DuplicateCollectorReferenceMatcher;

/**
 * Filter classes that have been instrumented.
 */
public class DuplicateCollectorReferenceFilter extends Filter {

  public DuplicateCollectorReferenceFilter() {
    this.add(new BlackList(new DuplicateCollectorReferenceMatcher()));
  }

}