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
package com.gzoltar.internal.report.metrics;

public enum Metric {

  /** {@see AmbiguityMetric} */
  AMBIGUITY(new AmbiguityMetric()),

  /** {@see CoverageMetric} */
  COVERAGE(new CoverageMetric()),

  /** {@see DDUMetric} */
  DDU(new DDUMetric()),

  /** {@see DDUMetric.GlobalDDUMetric} */
  GLOBALDDU(new DDUMetric.GlobalDDUMetric()),

  /** {@see EntropyMetric} */
  ENTROPY(new EntropyMetric()),

  /** {@see RhoMetric} */
  RHO(new RhoMetric()),

  /** {@see RhoMetric.NormalizedRho} */
  NORMALIZED_RHO(new RhoMetric.NormalizedRho()),

  /** {@see SimpsonMetric} */
  SIMPSON(new SimpsonMetric()),

  /** {@see SimpsonMetric.InvertedSimpsonMetric} */
  INVERTED_SIMPSON(new SimpsonMetric.InvertedSimpsonMetric()),

  /** {@see SimpsonMetric.GlobalSimpsonMetric} */
  GLOBAL_SIMPSON(new SimpsonMetric.GlobalSimpsonMetric()),

  /** {@see SimpsonMetric.GlobalInvertedSimpsonMetric} */
  GLOBAL_INVERTED_SIMPSON(new SimpsonMetric.GlobalInvertedSimpsonMetric());

  private final IMetric metric;

  private Metric(final IMetric metric) {
    this.metric = metric;
  }

  public IMetric getMetric() {
    return this.metric;
  }
}
