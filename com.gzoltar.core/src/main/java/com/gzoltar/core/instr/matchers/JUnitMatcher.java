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

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.HashMap;

public class JUnitMatcher implements IMatcher {

  public final IMatcher matcher;

  public JUnitMatcher() {
    this.matcher = new AndMatcher(
        // a JUnit test class must be public
        new ClassModifierMatcher(Modifier.PUBLIC),
        // a JUnit test class cannot be an abstract class
        new NotMatcher(new ClassModifierMatcher(Modifier.ABSTRACT)),
        // a JUnit test class cannot be an interface class
        new NotMatcher(new ClassModifierMatcher(Modifier.INTERFACE)),
        // a JUnit test class cannot be an anonymous class
        new NotMatcher(new AnonymousMatcher()),
        // a JUnit3/4 test class must has ...
        new OrMatcher(
            // a JUnit3 test class must has a specific super class
            new SuperclassMatcher("junit.framework.TestCase"),
            // a JUnit4 test class must has at least one method annotated with specific JUnit tags
            new OrMatcher(new MethodAnnotationMatcher("org.junit.Test"),
                new MethodAnnotationMatcher("org.junit.experimental.theories.Theory"))));
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    return this.matcher.matches(ctClass);
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    throw new UnsupportedOperationException();
  }
}
