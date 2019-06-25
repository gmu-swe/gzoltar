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
package com.gzoltar.core.runtime;

import com.gzoltar.core.events.IEventListener;
import com.gzoltar.core.events.MultiEventListener;
import com.gzoltar.core.model.Transaction;
import com.gzoltar.core.model.TransactionOutcome;
import com.gzoltar.core.spectrum.Spectrum;
import com.gzoltar.core.util.ArrayUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Collector {

  private static Collector collector;

  private MultiEventListener listener;

  private final Spectrum spectrum;

  /** <ProbeGroup hash, hitArray> */
  private final Map<String, Pair<String, int[]>> hitArrays;

  /**
   * 
   * @return
   */
  public static Collector instance() {
    if (collector == null) {
      collector = new Collector();
    }
    return collector;
  }

  /**
   * 
   */
  public static void restart() {
    if (collector != null) {
      Collector newCollector = new Collector();
      newCollector.listener = collector.listener;
      collector = newCollector;
    }
  }

  /**
   * 
   * @param listener
   */
  private Collector() {
    this.listener = new MultiEventListener();
    this.spectrum = new Spectrum();
    this.hitArrays = new LinkedHashMap<String, Pair<String, int[]>>();
  }

  /**
   * 
   * @param listener
   */
  public void addListener(final IEventListener listener) {
    if (listener != null) {
      this.listener.add(listener);
    }
  }

  /**
   * 
   * @return
   */
  public Spectrum getSpectrum() {
    return this.spectrum;
  }

  /**
   * 
   * @param probeGroup
   */
  public synchronized void regiterProbeGroup(final ProbeGroup probeGroup) {
    if (probeGroup.isEmpty()) {
      return;
    }

    this.spectrum.addProbeGroup(probeGroup);
    this.listener.regiterProbeGroup(probeGroup);
  }

  /**
   * 
   * @param probeGroup
   * @return
   */
  public synchronized ProbeGroup getProbeGroup(final ProbeGroup probeGroup) {
    return this.spectrum.getProbeGroup(probeGroup);
  }

  /**
   * 
   * @param probeGroupHash
   * @return
   */
  public synchronized ProbeGroup getProbeGroupByHash(final String probeGroupHash) {
    return this.spectrum.getProbeGroupByHash(probeGroupHash);
  }

  /**
   * 
   * @param transactionName
   * @param outcome
   * @param runtime
   * @param stackTrace
   */
  public synchronized void endTransaction(final String transactionName,
      final TransactionOutcome outcome, final long runtime, final String stackTrace) {

    if (this.hitArrays.isEmpty()) {
      return;
    }

    // collect coverage
    Map<String, Pair<String, int[]>> activity =
        new LinkedHashMap<String, Pair<String, int[]>>();
    for (Entry<String, Pair<String, int[]>> entry : this.hitArrays.entrySet()) {
      String hash = entry.getKey();
      int[] hitArray = entry.getValue().getRight();

      if (!ArrayUtils.containsNonZeroValue(hitArray)) {
        // although the class has been loaded and instrumented, no line has been covered
        activity.put(hash,
            new ImmutablePair<String, int[]>(entry.getValue().getLeft(), hitArray));
        continue;
      }

      int[] cloneHitArray = new int[hitArray.length];
      System.arraycopy(hitArray, 0, cloneHitArray, 0, hitArray.length);
      activity.put(hash,
          new ImmutablePair<String, int[]>(entry.getValue().getLeft(), cloneHitArray));

      // reset probes
      for (int i = 0; i < hitArray.length; i++) {
        hitArray[i] = 0;
      }
    }

    if (activity.isEmpty()) {
      return;
    }

    // create a new transaction
    Transaction transaction = new Transaction(transactionName, activity, outcome, runtime, stackTrace);
    this.spectrum.addTransaction(transaction);
    // and inform all listeners
    this.listener.endTransaction(transaction);
  }

  /**
   * 
   */
  public synchronized void endSession() {
    this.listener.endSession();
  }

  /**
   * 
   * @param args
   */
  public synchronized void getHitArray(final Object[] args) {
    assert args.length == 3;

    final String hash = (String) args[0];
    final String probeGroupName = (String) args[1];
    final Integer numberOfProbes = (Integer) args[2];

    if (!this.hitArrays.containsKey(hash)) {
      this.hitArrays.put(hash,
          new ImmutablePair<String, int[]>(probeGroupName, new int[numberOfProbes]));
    }

    args[0] = this.hitArrays.get(hash).getRight();
  }

  /**
   * In violation of the regular semantic of {@link Object#equals(Object)} this implementation is
   * used as the interface to the runtime data.
   * 
   * @param args the arguments as an {@link Object} array
   * @return has no meaning
   */
  @Override
  public boolean equals(final Object args) {
    if (args instanceof Object[]) {
      this.getHitArray((Object[]) args);
    }
    return super.equals(args);
  }
}
