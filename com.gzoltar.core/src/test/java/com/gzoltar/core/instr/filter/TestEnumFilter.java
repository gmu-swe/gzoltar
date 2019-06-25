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
package com.gzoltar.core.instr.filter;

import static org.junit.Assert.assertEquals;
import org.gzoltar.examples.EnumClass;
import org.junit.Test;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.instr.Outcome;
import com.gzoltar.core.util.ClassTestUtils;

public class TestEnumFilter {

  @Test
  public void should_accept_clinit_method() {
    EnumFilter enumFilter = new EnumFilter();
    String className = EnumClass.class.getCanonicalName();
    MethodNode clinit = ClassTestUtils.getMethodNode(className, "<clinit>");
    assertEquals(Outcome.ACCEPT, enumFilter.filter(clinit));
  }

  @Test
  public void should_accept_init_method() {
    EnumFilter enumFilter = new EnumFilter();
    String className = EnumClass.class.getCanonicalName();
    MethodNode init = ClassTestUtils.getMethodNode(className, "<init>");
    assertEquals(Outcome.ACCEPT, enumFilter.filter(init));
  }

  @Test
  public void should_reject_values_method() {
    EnumFilter enumFilter = new EnumFilter();
    String className = EnumClass.class.getCanonicalName();
    MethodNode values = ClassTestUtils.getMethodNode(className, "values");
    assertEquals(Outcome.REJECT, enumFilter.filter(values));
  }

  @Test
  public void should_reject_valuesof_method() {
    EnumFilter enumFilter = new EnumFilter();
    String className = EnumClass.class.getCanonicalName();
    MethodNode valuesOf = ClassTestUtils.getMethodNode(className, "valuesOf");
    assertEquals(Outcome.REJECT, enumFilter.filter(valuesOf));
  }
}
