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
package com.gzoltar.agent.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Properties;

import org.junit.Test;

import com.gzoltar.agent.rt.ConfigLoader;

/**
 * Unit tests for {@link ConfigLoader}
 */
public class TestConfigLoader {

  @Test
  public void testInvalidPrefix() {
    Properties system = new Properties();
    system.setProperty("gzoltar-invalid-previx.property", "p");
    Properties config = ConfigLoader.load(system);
    assertNull(config.get("property"));
  }

  @Test
  public void testSystemProperties() {
    Properties system = new Properties();
    system.setProperty("gzoltar-agent.property", "p");
    Properties config = ConfigLoader.load(system);
    assertEquals("p", config.get("property"));
  }
}
