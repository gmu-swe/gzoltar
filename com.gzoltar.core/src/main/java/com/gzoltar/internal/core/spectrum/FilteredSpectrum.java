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

import com.gzoltar.internal.core.AgentConfigs;
import com.gzoltar.internal.core.instr.Outcome;
import com.gzoltar.internal.core.instr.actions.BlackList;
import com.gzoltar.internal.core.instr.actions.WhiteList;
import com.gzoltar.internal.core.instr.filter.Filter;
import com.gzoltar.internal.core.instr.granularity.GranularityLevel;
import com.gzoltar.internal.core.instr.matchers.ClassNameMatcher;
import com.gzoltar.internal.core.instr.matchers.MethodAnnotationMatcher;
import com.gzoltar.internal.core.instr.matchers.MethodModifierMatcher;
import com.gzoltar.internal.core.instr.matchers.MethodNameMatcher;
import com.gzoltar.internal.core.model.Node;
import com.gzoltar.internal.core.model.NodeType;
import com.gzoltar.internal.core.model.Transaction;
import com.gzoltar.internal.core.runtime.Probe;
import com.gzoltar.internal.core.runtime.ProbeGroup;
import com.gzoltar.internal.core.util.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class FilteredSpectrum {

  private final GranularityLevel granularity;

  private final Filter classFilter;

  private final Filter methodFilter = new Filter();

  /**
   * 
   */
  public FilteredSpectrum(AgentConfigs configs) {

    this.granularity = configs.getGranularity();

    // === Class level filters ===

    // instrument some classes
    WhiteList includeClasses = new WhiteList(new ClassNameMatcher(configs.getIncludes()));

    // do not instrument some classes
    BlackList excludeClasses = new BlackList(new ClassNameMatcher(configs.getExcludes()));

    this.classFilter = new Filter(includeClasses, excludeClasses);

    // === Method level filters ===

    if (!configs.getInclPublicMethods()) {
      this.methodFilter.add(new BlackList(new MethodModifierMatcher(Opcodes.ACC_PUBLIC)));
    }

    if (!configs.getInclStaticConstructors()) {
      this.methodFilter.add(new BlackList(new MethodNameMatcher("<clinit>*")));
    }

    if (!configs.getInclDeprecatedMethods()) {
      this.methodFilter
          .add(new BlackList(new MethodAnnotationMatcher(Deprecated.class.getCanonicalName())));
    }
  }

  /**
   * Returns a filtered {@link ISpectrum} object according to user's
   * preferences.
   * 
   * @param source
   * @return
   */
  public ISpectrum filter(ISpectrum source) {
    if (source == null) {
      return null;
    }

    ISpectrum filteredSpectrum = new Spectrum();

    // === Filter probeGroups and probes ===

    for (ProbeGroup probeGroup : source.getProbeGroups()) {
      // does 'probeGroup' match any filter?

      ClassNode fakeCN = new ClassNode();
      fakeCN.name = probeGroup.getCtClass();
      if (this.classFilter.filter(fakeCN) == Outcome.REJECT) {
        continue;
      }

      ProbeGroup newProbeGroup = new ProbeGroup(probeGroup.getHash(), probeGroup.getCtClass());

      Filter granularityMethodFilter = new Filter();
      for (Probe probe : probeGroup.getProbes()) {
        // does 'probe' match any filter?
        MethodNode fakeMN = new MethodNode();
        fakeMN.name = probe.getMethodName();
        if (this.methodFilter.filter(fakeMN) == Outcome.REJECT) {
          continue;
        }

        // === Skip nodes according to a granularity level ===

        if (granularityMethodFilter.filter(fakeMN) == Outcome.REJECT) {
          continue;
        }

        newProbeGroup.registerProbe(probe.getNode(), probe.getMethodName(), probe.getMethodDesc());

        if (this.granularity == GranularityLevel.CLASS) {
          throw new UnsupportedOperationException();
        } else if (this.granularity == GranularityLevel.METHOD) {
          Node node = probe.getNode();
          String methodName =
              node.getName().substring(node.getName().indexOf(NodeType.METHOD.getSymbol()) + 1,
                  node.getName().indexOf(NodeType.LINE.getSymbol()));

          granularityMethodFilter.add(new BlackList(new MethodNameMatcher(methodName)));
          throw new UnsupportedOperationException();
        }
      }

      if (!newProbeGroup.isEmpty()) {
        filteredSpectrum.addProbeGroup(newProbeGroup);
      }
    }

    // === Filter transactions ===

    for (Transaction transaction : source.getTransactions()) {
      Transaction newTransaction =
          new Transaction(transaction.getName(), transaction.getTransactionOutcome(),
              transaction.getRuntime(), transaction.getStackTrace());

      for (String hash : transaction.getProbeGroupsHash()) {
        if (!filteredSpectrum.containsProbeGroupByHash(hash)) {
          // probeGroup has been ignored, therefore it could also be ignore here
          continue;
        }

        // shrink hitArray

        ProbeGroup probeGroup = source.getProbeGroupByHash(hash);
        int[] hitArray = transaction.getHitArrayByProbeGroupHash(hash);

        ProbeGroup newProbeGroup = filteredSpectrum.getProbeGroupByHash(hash);
        int[] newHitArray = new int[newProbeGroup.getNumberOfProbes()];

        if(newProbeGroup.getNumberOfProbes() != probeGroup.getNumberOfProbes())
        {
          throw new RuntimeException("Wrong # of probes in " + transaction.getName() + " Expected " + probeGroup.getNumberOfProbes() + " but found " + newProbeGroup.getNumberOfProbes());
        }

        for (Probe probe : probeGroup.getProbes()) {
          Probe newProbe = newProbeGroup.findProbeByNode(probe.getNode());
          if (newProbe == null) {
            // probe has been removed, therefore it could also be ignore here
            continue;
          }

          newHitArray[newProbe.getArrayIndex()] = hitArray[probe.getArrayIndex()];
        }

        if (ArrayUtils.containsNonZeroValue(newHitArray)) {
          newTransaction.addActivity(hash,
              new ImmutablePair<String, int[]>(newProbeGroup.getName(), newHitArray));
        }
      }

      // check whether it has any activation is performed in the method itself
      filteredSpectrum.addTransaction(newTransaction);
    }

    // === Reset name and type according to a granularity level ===

    if (this.granularity != GranularityLevel.LINE) {
      for (ProbeGroup probeGroup : filteredSpectrum.getProbeGroups()) {
        for (Probe probe : probeGroup.getProbes()) {
          Node node = probe.getNode();

          String newNodeName = null;
          NodeType newNodeType = null;

          switch (this.granularity) {
            case CLASS:
              newNodeName =
                  node.getName().substring(0, node.getName().indexOf(NodeType.METHOD.getSymbol()));
              newNodeType = NodeType.CLASS;
              break;
            case METHOD:
              newNodeName =
                  node.getName().substring(0, node.getName().indexOf(NodeType.LINE.getSymbol()));
              newNodeType = NodeType.METHOD;
              break;
            case BASICBLOCK:
            case LINE:
            default:
              break;
          }

          if (newNodeName != null && newNodeType != null) {
            node.setName(newNodeName);
            node.setNodeType(newNodeType);
          }
        }
      }
    }

    return filteredSpectrum;
  }

}