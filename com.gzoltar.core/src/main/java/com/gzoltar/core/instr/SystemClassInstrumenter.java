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
package com.gzoltar.core.instr;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.gzoltar.core.runtime.Collector;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

/**
 * This class adds a new static field to a bootstrap class that will be used by instrumented
 * classes. As the system class itself needs to be instrumented this instrumenter requires a Java
 * agent.
 */
public final class SystemClassInstrumenter {

  /**
   * Adds a new static field to a bootstrap class that will be used by instrumented classes.
   * 
   * @param inst
   * @param className
   * @param accessFieldName
   * @throws Exception
   */
  public static void instrumentSystemClass(final Instrumentation inst, final String className,
      final String accessFieldName) throws Exception {

    final ClassFileTransformer transformer = new ClassFileTransformer() {
      public byte[] transform(final ClassLoader loader, final String name,
          final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
          final byte[] source) throws IllegalClassFormatException {
        if (name.equals(className)) {
          try {
          	final ClassReader cr = new ClassReader(source);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ClassVisitor(InstrumentationConstants.ASM_VERSION, cw) {
              @Override
              public void visitEnd() {
              	super.visitField(InstrumentationConstants.SYSTEM_CLASS_FIELD_ACC, accessFieldName, InstrumentationConstants.SYSTEM_CLASS_FIELD_DESC, null, null);
                super.visitEnd();
              }
            };
            cr.accept(cv, 0);
            return cw.toByteArray();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return null;
      }
    };

    // enable instrumentation
    inst.addTransformer(transformer);
    // load system class
    final Class<?> clazz = Class.forName(InstrumentationConstants.SYSTEM_CLASS_NAME_JVM);
    // disable instrumentation
    inst.removeTransformer(transformer);

    // setup field to use GZoltar's collector
    try {
      // has the new field been added?
      final Field field = clazz.getField(accessFieldName);
      // point field to GZoltar' runtime collector
      field.set(null, Collector.instance());
    } catch (final NoSuchFieldException e) {
      throw new RuntimeException("Class '" + className + "' could not be instrumented.", e);
    }
  }
}
