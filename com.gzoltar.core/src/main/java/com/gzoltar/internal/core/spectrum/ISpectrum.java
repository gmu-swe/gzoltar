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
package com.gzoltar.internal.core.spectrum;

import com.gzoltar.internal.core.model.Node;
import com.gzoltar.internal.core.model.Transaction;
import com.gzoltar.internal.core.runtime.ProbeGroup;

import java.util.Collection;
import java.util.List;

public interface ISpectrum {

  // === ProbeGroups ===

  /**
   * Register a new probe group.
   */
  public void addProbeGroup(final ProbeGroup probeGroup);

  /**
   * Checks whether a probe group has been registered.
   */
  public boolean containsProbeGroup(final ProbeGroup probeGroup);

  /**
   * Checks whether a probe group has been registered.
   */
  public boolean containsProbeGroupByHash(final String hash);

  /**
   * Returns the {@link ProbeGroup} with a given name, or null if there is
   * not any.
   */
  public ProbeGroup getProbeGroup(final ProbeGroup probeGroup);

  /**
   * Returns the {@link ProbeGroup} with a given name, or null if there is
   * not any.
   */
  public ProbeGroup getProbeGroupByHash(final String hash);

  /**
   * Returns all {@link ProbeGroup} that have been registered.
   */
  public Collection<ProbeGroup> getProbeGroups();

  // === Nodes ===

  /**
   * Returns all {@link Node} objects registered in each
   * {@link ProbeGroup}.
   */
  public List<Node> getNodes();

  /**
   * Returns the number of all {@link Node} objects registered in each
   * {@link ProbeGroup}.
   */
  public int getNumberOfNodes();

  /**
   * Returns all executed {@link Node} objects of a particular
   * {@link Transaction} object.
   */
  public List<Node> getHitNodes(Transaction transaction);

  // === Transaction ===

  /**
   * Registers a {@link Transaction}.
   */
  public void addTransaction(final Transaction transaction);

  /**
   * Returns all {@link Transaction} that have been registered.
   */
  public List<Transaction> getTransactions();

  /**
   * Returns the number of all {@link Transaction} that have been registered.
   */
  public int getNumberOfTransactions();
}