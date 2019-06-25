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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.gzoltar.core.instr.matchers.JUnitMatcher;
import com.gzoltar.core.util.ClassTestUtils;

import org.gzoltar.examples.tests.AbstractTestClass;
import org.gzoltar.examples.tests.ChildTestClassWithTestCases;
import org.gzoltar.examples.tests.ChildTestClassWithoutTestCases;
import org.gzoltar.examples.tests.JUnitClassWithInnerClass;
import org.gzoltar.examples.tests.JUnitClassWithInnerClass.SomeStaticInnerClass;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class TestJUnitMatcher {


  @Test
  public void testAbstractClass() throws Exception {
    JUnitMatcher matcher = new JUnitMatcher();
    ClassNode ctClass = ClassTestUtils.getClassNode(Type.getInternalName(AbstractTestClass.class));
    assertFalse(matcher.matches(ctClass));
  }

  @Test
  public void testChildTestClassWithoutTestCases() throws Exception {
    JUnitMatcher matcher = new JUnitMatcher();
    ClassNode ctClass = ClassTestUtils.getClassNode(Type.getInternalName(ChildTestClassWithoutTestCases.class));
    assertTrue(matcher.matches(ctClass));
  }

  @Test
  public void testChildTestClassWithTestCases() throws Exception {
    JUnitMatcher matcher = new JUnitMatcher();
    ClassNode ctClass = ClassTestUtils.getClassNode(Type.getInternalName(ChildTestClassWithTestCases.class));
    assertTrue(matcher.matches(ctClass));
  }

  @Test
  public void testJUnitClassWithInnerClass() throws Exception {
    JUnitMatcher matcher = new JUnitMatcher();
    ClassNode ctClass = ClassTestUtils.getClassNode(Type.getInternalName(JUnitClassWithInnerClass.class));
    assertTrue(matcher.matches(ctClass));
    ctClass = ClassTestUtils.getClassNode(Type.getInternalName(SomeStaticInnerClass.class));
    assertTrue(matcher.matches(ctClass));
  }
}
