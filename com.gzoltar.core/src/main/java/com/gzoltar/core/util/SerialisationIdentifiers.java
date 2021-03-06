/**
 * Copyright (C) 2018 GZoltar contributors.
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
package com.gzoltar.core.util;

public final class SerialisationIdentifiers {

  /** File format version, will be incremented for each incompatible change. */
  public static final char FORMAT_VERSION;

  static {
    // Runtime initialise to ensure the compiler does not inline the value.
    FORMAT_VERSION = 0x0001;
  }

  /** Magic number in header for file format identification. */
  public static final char MAGIC_NUMBER = 0xC0C0;

  /** Block identifier for file headers. */
  public static final byte BLOCK_HEADER = 0x01;

  /** Block identifier for transaction information. */
  public static final byte BLOCK_TRANSACTION = 0x10;

}
