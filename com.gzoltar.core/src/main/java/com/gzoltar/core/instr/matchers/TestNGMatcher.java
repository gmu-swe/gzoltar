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

import java.lang.reflect.Modifier;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class TestNGMatcher implements IMatcher {

  public final IMatcher matcher;

  public TestNGMatcher() {
    this.matcher = new AndMatcher(
        // a TestNG test class must be public
        new ClassModifierMatcher(Modifier.PUBLIC),
        // a TestNG test class cannot be an abstract class
        new NotMatcher(new ClassModifierMatcher(Modifier.ABSTRACT)),
        // a TestNG test class cannot be an interface class
        new NotMatcher(new ClassModifierMatcher(Modifier.INTERFACE)),
        // a TestNG test class cannot be an anonymous class
        new NotMatcher(new AnonymousMatcher()),
        // a TestNG test class must has at least a method annotated with specific tags
        new MethodAnnotationMatcher("org.testng.annotations.Test"));
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
