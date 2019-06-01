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
package com.gzoltar.core.util;

import static org.junit.Assert.assertEquals;

import com.gzoltar.internal.core.util.VMUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestVMUtils {

  @Test
  public void testEmptyClassName() {
    Assert.assertEquals("", VMUtils.toVMName(""));
  }

  @Test
  public void testValidClassName() {
    assertEquals("foo/bar/MyClass", VMUtils.toVMName("foo.bar.MyClass"));
  }

}
