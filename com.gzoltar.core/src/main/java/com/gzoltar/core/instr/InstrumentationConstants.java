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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Constants for byte code instrumentation.
 */
public final class InstrumentationConstants {

  public static final String EOL = ";";

  // === System Class' Field ===

  public static final String SYSTEM_CLASS_NAME = "java/lang/UnknownError";

  public static final String SYSTEM_CLASS_NAME_JVM = "java.lang.UnknownError";

  public static final String SYSTEM_CLASS_FIELD_NAME = "$gzoltarAccess";

  public static final String SYSTEM_CLASS_FIELD_DESC = "Ljava/lang/Object;";

  public static final int SYSTEM_CLASS_FIELD_ACC =
      Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT;

  // === Data Field ===

  public static final String FIELD_NAME = "$gzoltarData";

  public static final String FIELD_INIT_VALUE = "null";

  public static final String FIELD_DESC_BYTECODE = "[I";

  public static final String FIELD_DESC_HUMAN = "int[] ";

  public static final int FIELD_ACC =
          Opcodes.ACC_PRIVATE  | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT;

  public static final int FIELD_INTF_ACC =
          Opcodes.ACC_PRIVATE  | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT;

  // === Init method ===

  public static final String INIT_METHOD_NAME = "$gzoltarInit";

  public static final String INIT_METHOD_NAME_WITH_ARGS = INIT_METHOD_NAME + "()";

  public static final String INIT_METHOD_DESC_HUMAN = "void ";

  public static final int INIT_METHOD_ACC =
      Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC;

  public static final int ASM_VERSION = Opcodes.ASM6;

	// TODO we may need extra constants for the init method of Java-8 interfaces

  private InstrumentationConstants() {
    // NO-OP
  }


  static String toJavassistType(Type t)
  {
    switch (t.getSort()) {
      case Type.ARRAY:
        StringBuilder ret = new StringBuilder();
        ret.append(toJavassistType(t.getElementType()));
        for (int i = 0; i < t.getDimensions(); i++)
          ret.append("[]");
        return ret.toString();
      case Type.OBJECT:
        return t.getInternalName().replace('/', '.');
      case Type.BOOLEAN:
        return "boolean";
      case Type.BYTE:
        return "byte";
      case Type.CHAR:
        return "char";
      case Type.DOUBLE:
        return "double";
      case Type.FLOAT:
        return "float";
      case Type.INT:
        return "int";
      case Type.LONG:
        return "long";
      case Type.SHORT:
        return "short";
      default:
        throw new UnsupportedOperationException();
    }
  }

  public static String toJavassistDescriptor(String methodDesc){
    StringBuilder ret = new StringBuilder();
    ret.append('(');
    Type[] args = Type.getArgumentTypes(methodDesc);
    for(Type t : args){
      ret.append(toJavassistType(t));
      ret.append(',');
    }
    if(args.length > 0)
      ret.deleteCharAt(ret.length()-1);
    ret.append(')');
    return ret.toString();
  }
}
