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
import org.gzoltar.examples.AbstractClass;
import org.gzoltar.examples.EnumClass;
import org.gzoltar.examples.InterfaceClass;
import org.gzoltar.examples.PrivateModifiers;
import org.gzoltar.examples.ProtectedModifiers;
import org.gzoltar.examples.PublicFinalModifiers;
import org.gzoltar.examples.PublicModifiers;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.util.ClassTestUtils;

public class TestModifierMatcher {

  @Test
  public void testPublicClassModifier() throws Exception {
    ClassModifierMatcher classModifierMatcher = new ClassModifierMatcher(Opcodes.ACC_PUBLIC);
    ClassNode ctClass = ClassTestUtils.getClassNode(PublicModifiers.class.getCanonicalName());
    assertTrue(classModifierMatcher.matches(ctClass));
  }

  @Test
  public void testPublicMethodModifier() throws Exception {
    MethodModifierMatcher methodModifierMatcher = new MethodModifierMatcher(Opcodes.ACC_PUBLIC);
    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(PublicModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(methodModifierMatcher.matches(ctBehavior));
  }

  @Test
  public void testPublicFieldModifier() throws Exception {
    FieldModifierMatcher fieldModifierMatcher = new FieldModifierMatcher(Opcodes.ACC_PUBLIC);
    FieldNode ctField =
        ClassTestUtils.getFieldNode(PublicModifiers.class.getCanonicalName(), "string");
    assertTrue(fieldModifierMatcher.matches(ctField));
  }

  @Test
  public void testPrivateMethodModifier() throws Exception {
    MethodModifierMatcher methodModifierMatcher = new MethodModifierMatcher(Opcodes.ACC_PRIVATE);
    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(PrivateModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(methodModifierMatcher.matches(ctBehavior));
  }

  @Test
  public void testPrivateFieldModifier() throws Exception {
    FieldModifierMatcher fieldModifierMatcher = new FieldModifierMatcher(Opcodes.ACC_PRIVATE);
    FieldNode ctField =
        ClassTestUtils.getFieldNode(PrivateModifiers.class.getCanonicalName(), "string");
    assertTrue(fieldModifierMatcher.matches(ctField));
  }

  @Test
  public void testPublicFinalClassModifier() throws Exception {
    ClassModifierMatcher classModifierMatcher = new ClassModifierMatcher(Opcodes.ACC_FINAL);
    ClassNode ctClass = ClassTestUtils.getClassNode(PublicFinalModifiers.class.getCanonicalName());
    assertTrue(classModifierMatcher.matches(ctClass));
  }

  @Test
  public void testPublicFinalMethodModifier() throws Exception {
    MethodModifierMatcher methodModifierMatcher = new MethodModifierMatcher(Opcodes.ACC_FINAL);
    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(PublicFinalModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(methodModifierMatcher.matches(ctBehavior));
  }

  @Test
  public void testPublicFinalFieldModifier() throws Exception {
    FieldModifierMatcher fieldModifierMatcher = new FieldModifierMatcher(Opcodes.ACC_FINAL);
    FieldNode ctField =
        ClassTestUtils.getFieldNode(PublicFinalModifiers.class.getCanonicalName(), "string");
    assertTrue(fieldModifierMatcher.matches(ctField));
  }

  @Test
  public void testProtectedMethodModifier() throws Exception {
    MethodModifierMatcher methodModifierMatcher = new MethodModifierMatcher(Opcodes.ACC_PROTECTED);
    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(ProtectedModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(methodModifierMatcher.matches(ctBehavior));
  }

  @Test
  public void testProtectedFieldModifier() throws Exception {
    FieldModifierMatcher fieldModifierMatcher = new FieldModifierMatcher(Opcodes.ACC_PROTECTED);
    FieldNode ctField =
        ClassTestUtils.getFieldNode(ProtectedModifiers.class.getCanonicalName(), "string");
    assertTrue(fieldModifierMatcher.matches(ctField));
  }

  @Test
  public void testAbstractClassModifier() throws Exception {
    ClassModifierMatcher classModifierMatcher = new ClassModifierMatcher(Opcodes.ACC_ABSTRACT);
    ClassNode ctClass = ClassTestUtils.getClassNode(AbstractClass.class.getCanonicalName());
    assertTrue(classModifierMatcher.matches(ctClass));
  }

  @Test
  public void testAbstractMethodModifier() throws Exception {
    MethodModifierMatcher methodModifierMatcher = new MethodModifierMatcher(Opcodes.ACC_ABSTRACT);
    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(AbstractClass.class.getCanonicalName(), "isNegative");
    assertTrue(methodModifierMatcher.matches(ctBehavior));
  }

  @Test
  public void testEnumClassModifier() throws Exception {
    ClassModifierMatcher classModifierMatcher = new ClassModifierMatcher(Opcodes.ACC_ENUM);
    ClassNode ctClass = ClassTestUtils.getClassNode(EnumClass.class.getCanonicalName());
    assertTrue(classModifierMatcher.matches(ctClass));
  }

  @Test
  public void testInterfaceModifier() throws Exception {
    ClassModifierMatcher classModifierMatcher = new ClassModifierMatcher(Opcodes.ACC_INTERFACE);
    ClassNode ctClass = ClassTestUtils.getClassNode(InterfaceClass.class.getCanonicalName());
    assertTrue(classModifierMatcher.matches(ctClass));
  }

}
