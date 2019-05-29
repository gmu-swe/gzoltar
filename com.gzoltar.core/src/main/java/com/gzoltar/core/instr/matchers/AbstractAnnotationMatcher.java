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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractAnnotationMatcher implements IMatcher {

  private final String annotation;

  protected AbstractAnnotationMatcher(final String annotation) {
  	if(annotation.startsWith("L"))
      this.annotation = annotation;
  	else
  	  this.annotation = Type.getObjectType(annotation.replace('.','/')).getDescriptor();
  }

  @Override
  public boolean matches(final ClassNode ctClass) {
    for(AnnotationNode each : ctClass.visibleAnnotations){
      if(each.desc.equals(annotation))
        return true;
    }

    for(AnnotationNode each : ctClass.invisibleAnnotations){
      if(each.desc.equals(annotation))
        return true;
    }
    return false;
  }

  @Override
  public boolean matches(final MethodNode ctBehavior) {
    if (ctBehavior.visibleAnnotations != null)
      for (AnnotationNode each : ctBehavior.visibleAnnotations) {
        if (each.desc.equals(annotation))
          return true;
      }

    if (ctBehavior.invisibleAnnotations != null)
      for (AnnotationNode each : ctBehavior.invisibleAnnotations) {
        if (each.desc.equals(annotation))
          return true;
      }
    return false;
  }

  @Override
  public boolean matches(final FieldNode ctField) {
    throw new UnsupportedOperationException();
  }

}
