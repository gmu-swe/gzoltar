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
import org.gzoltar.examples.PrivateModifiers;
import org.gzoltar.examples.ProtectedModifiers;
import org.gzoltar.examples.PublicModifiers;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import com.gzoltar.core.util.ClassTestUtils;

public class TestOrMatcher {

  @Test
  public void testPublicOrProtectedMethods() throws Exception {
    MethodModifierMatcher publicMethodsMatcher = new MethodModifierMatcher(Opcodes.ACC_PUBLIC);
    MethodModifierMatcher protectedMethodsMatcher =
        new MethodModifierMatcher(Opcodes.ACC_PROTECTED);
    OrMatcher orMatcher = new OrMatcher(publicMethodsMatcher, protectedMethodsMatcher);

    MethodNode ctBehavior =
        ClassTestUtils.getMethodNode(PublicModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(orMatcher.matches(ctBehavior));

    ctBehavior =
        ClassTestUtils.getMethodNode(ProtectedModifiers.class.getCanonicalName(), "isNegative");
    assertTrue(orMatcher.matches(ctBehavior));

    ctBehavior =
        ClassTestUtils.getMethodNode(PrivateModifiers.class.getCanonicalName(), "isNegative");
    assertFalse(orMatcher.matches(ctBehavior));
  }

}
