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

import static org.junit.Assert.assertTrue;
import org.gzoltar.examples.PublicModifiers;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import com.gzoltar.core.util.ClassTestUtils;

public class TestSuperclassMatcher {

  @Test
  public void testSuperclassMatcher() throws Exception {
    SuperclassMatcher superclassMatcher = new SuperclassMatcher("org.gzoltar.*.Abstract*");
    ClassNode ctClass = ClassTestUtils.getClassNode(PublicModifiers.class.getCanonicalName());
    assertTrue(superclassMatcher.matches(ctClass));
  }

}
