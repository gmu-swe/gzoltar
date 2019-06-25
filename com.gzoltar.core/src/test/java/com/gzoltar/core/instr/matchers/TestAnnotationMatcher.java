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
import org.gzoltar.examples.DeprecatedAnnotation;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.util.ClassTestUtils;

@SuppressWarnings("deprecation")
public class TestAnnotationMatcher {

  @Test
  public void testClassDeprecatedMatcher() throws Exception {
    ClassAnnotationMatcher classAnnotationMatcher =
        new ClassAnnotationMatcher(Deprecated.class.getCanonicalName());
    ClassNode ctClass = ClassTestUtils.getClassNode(DeprecatedAnnotation.class.getCanonicalName());
    assertTrue(classAnnotationMatcher.matches(ctClass));
  }

  @Test
  public void testMethodDeprecatedMatcher() throws Exception {
    MethodAnnotationMatcher methodAnnotationMatcher =
        new MethodAnnotationMatcher(Deprecated.class.getCanonicalName());
    MethodNode ctBehavior = ClassTestUtils
        .getMethodNode(DeprecatedAnnotation.class.getCanonicalName(), "deprecatedMethod");
    assertTrue(methodAnnotationMatcher.matches(ctBehavior));
  }

  @Test
  public void testFieldDeprecatedMatcher() throws Exception {
    FieldAnnotationMatcher fieldAnnotationMatcher =
        new FieldAnnotationMatcher(Deprecated.class.getCanonicalName());
    FieldNode ctField = ClassTestUtils.getFieldNode(DeprecatedAnnotation.class.getCanonicalName(),
        "deprecatedField");
    assertTrue(fieldAnnotationMatcher.matches(ctField));
  }

}
