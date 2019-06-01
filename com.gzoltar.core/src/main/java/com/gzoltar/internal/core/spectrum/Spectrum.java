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
import com.gzoltar.internal.core.runtime.Probe;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Spectrum implements ISpectrum {

  /** <ProbeGroup hash, ProbeGroup> */
  private final Map<String, ProbeGroup> probeGroups;

  private final List<Transaction> transactions;

  /**
   * Constructs a new {@link Spectrum}.
   */
  public Spectrum() {
    this.probeGroups = new LinkedHashMap<String, ProbeGroup>();
    this.transactions = new ArrayList<Transaction>();
  }

  // === ProbeGroups ===

  /**
   * Register a new probe group.
   */
  public void addProbeGroup(final ProbeGroup probeGroup) {
    // Mocking frameworks, application servers, or persistence frameworks may cause GZoltar to see
    // the same class several times
    if (!this.probeGroups.containsKey(probeGroup.getHash())) {
      this.probeGroups.put(probeGroup.getHash(), probeGroup);
    }
  }

  /**
   * Checks whether a probe group has been registered.
   */
  public boolean containsProbeGroup(final ProbeGroup probeGroup) {
    return this.probeGroups.containsKey(probeGroup.getHash());
  }

  /**
   * Checks whether a probe group has been registered.
   */
  public boolean containsProbeGroupByHash(final String hash) {
    return this.probeGroups.containsKey(hash);
  }

  /**
   * Returns the {@link ProbeGroup} with a given name, or null if there is
   * not any.
   */
  public ProbeGroup getProbeGroup(final ProbeGroup probeGroup) {
    return this.containsProbeGroup(probeGroup) ? this.probeGroups.get(probeGroup.getHash()) : null;
  }

  /**
   * Returns the {@link ProbeGroup} with a given name, or null if there is
   * not any.
   */
  public ProbeGroup getProbeGroupByHash(final String hash) {
    return this.containsProbeGroupByHash(hash) ? this.probeGroups.get(hash) : null;
  } 

  /**
   * Returns all {@link ProbeGroup} that have been registered.
   */
  public Collection<ProbeGroup> getProbeGroups() {
    return this.probeGroups.values();
  }

  // === Nodes ===

  /**
   * Returns all {@link Node} objects registered in each
   * {@link ProbeGroup}.
   */
  public List<Node> getNodes() {
    List<Node> nodes = new ArrayList<Node>();
    for (ProbeGroup probeGroup : this.probeGroups.values()) {
      nodes.addAll(probeGroup.getNodes());
    }
    return nodes;
  }

  /**
   * Returns the number of all {@link Node} objects registered in each
   * {@link ProbeGroup}.
   */
  public int getNumberOfNodes() {
    return this.getNodes().size();
  }

  /**
   * Returns all executed {@link Node} objects of a particular
   * {@link Transaction} object.
   */
  public List<Node> getHitNodes(Transaction transaction) {
    List<Node> nodes = new ArrayList<Node>();

    for (Entry<String, Pair<String, int[]>> activity : transaction.getActivity().entrySet()) {
      String probeGroupHash = activity.getKey();
      ProbeGroup probeGroup = this.probeGroups.get(probeGroupHash);
      int[] hitArray = activity.getValue().getRight();
      assert hitArray.length == probeGroup.getNumberOfProbes();

      for (Probe probe : probeGroup.getProbes()) {
        if (hitArray[probe.getArrayIndex()] > 0) {
          nodes.add(probe.getNode());
        }
      }
    }

    return nodes;
  }

  // === Transactions ===

  /**
   * Registers a {@link Transaction}.
   */
  public void addTransaction(final Transaction transaction) {
    if (transaction.hasActivations()) {
      this.transactions.add(transaction);
    }
  }

  /**
   * Returns all {@link Transaction} that have been registered.
   */
  public List<Transaction> getTransactions() {
    return this.transactions;
  }

  /**
   * Returns the number of all {@link Transaction} that have been registered.
   */
  public int getNumberOfTransactions() {
    return this.transactions.size();
  }

  // === Overrides ===

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    List<Node> nodes = this.getNodes();
    for (Node node : nodes) {
      sb.append(node.toString());
      sb.append("\n");
    }

    for (Transaction transaction : this.transactions) {
      sb.append(transaction.toString());
      sb.append("\n");
    }

    sb.append("\nNumber of target nodes: " + this.getNumberOfNodes() + "\n");
    sb.append("Number of transactions: " + this.transactions.size() + "\n");

    return sb.toString();
  }
}
