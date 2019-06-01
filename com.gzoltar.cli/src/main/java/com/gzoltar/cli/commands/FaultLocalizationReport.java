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
package com.gzoltar.cli.commands;

import java.util.Arrays;
import java.util.Locale;
import org.kohsuke.args4j.Option;
import com.gzoltar.internal.core.instr.granularity.GranularityLevel;
import com.gzoltar.internal.fl.FaultLocalizationFamily;
import com.gzoltar.internal.report.ReportFormatter;
import com.gzoltar.internal.report.fl.FaultLocalizationReportBuilder;
import com.gzoltar.internal.report.fl.FaultLocalizationReportFormatterFactory;
import com.gzoltar.internal.report.fl.config.ConfigFaultLocalizationFamily;
import com.gzoltar.internal.report.metrics.Metric;
import com.gzoltar.sfl.SFLFormulas;

/**
 * The <code>faultLocalizationReport</code> command.
 */
public class FaultLocalizationReport extends AbstractReport {

  @Option(name = "--granularity", usage = "source code granularity level of report",
      metaVar = "<line|method|class>", required = false)
  private String granularity = GranularityLevel.LINE.name();

  @Option(name = "--inclPublicMethods",
      usage = "specifies whether public static methods of each class should be reported",
      metaVar = "<boolean>", required = false)
  private Boolean inclPublicMethods = true;

  @Option(name = "--inclStaticConstructors",
      usage = "specifies whether public static constructors of each class should be reported",
      metaVar = "<boolean>", required = false)
  private Boolean inclStaticConstructors = true;

  @Option(name = "--inclDeprecatedMethods",
      usage = "specifies whether methods annotated as deprecated should be reported",
      metaVar = "<boolean>", required = false)
  private Boolean inclDeprecatedMethods = true;

  @Option(name = "--family", usage = "fault localization family", metaVar = "<family>",
      required = false)
  private String family = FaultLocalizationFamily.SFL.name();

  @Option(name = "--formula", usage = "fault localization formulas", metaVar = "<formula>",
      required = false)
  private String formula = SFLFormulas.OCHIAI.name();

  @Option(name = "--metric", usage = "fault localization ranking metrics", metaVar = "<metric>",
      required = false)
  private String metric = Metric.AMBIGUITY.name();

  @Option(name = "--formatter", usage = "fault localization report formatter",
      metaVar = "<formatter>", required = false)
  private String formatter = ReportFormatter.TXT.name();

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return "faultLocalizationReport";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String description() {
    return "Create a fault localization report based on previously collected data.";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void generateReport(final Locale locale) throws Exception {
    this.setGranularity(this.granularity);
    this.setInclPublicMethods(this.inclPublicMethods);
    this.setInclStaticConstructors(this.inclStaticConstructors);
    this.setInclDeprecatedMethods(this.inclDeprecatedMethods);

    final ConfigFaultLocalizationFamily configFlFamily = new ConfigFaultLocalizationFamily();

    // set fault localization family
    configFlFamily
        .setFaultLocalizationFamily(
            FaultLocalizationFamily.valueOf(this.family.toUpperCase(locale)));
    // set formula
    configFlFamily.setFormulas(Arrays.asList(this.formula));
    // set metrics
    configFlFamily.setMetrics(Arrays.asList(this.metric));
    // set formatters
    configFlFamily.setFormatters(Arrays.asList(FaultLocalizationReportFormatterFactory
        .createReportFormatter(ReportFormatter.valueOf(this.formatter.toUpperCase(locale)))));

    // build a fault localization report
    FaultLocalizationReportBuilder.build(this.buildLocation.getAbsolutePath(), this.agentConfigs,
        this.outputDirectory, this.dataFile, Arrays.asList(configFlFamily));
  }
}
