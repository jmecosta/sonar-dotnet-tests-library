/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.io.File;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;

public class CoverageAggregator implements BatchExtension {

  private final CoverageConfiguration coverageConf;
  private final Settings settings;
  private final NCover3ReportParser ncover3ReportParser;
  private final OpenCoverReportParser openCoverReportParser;
  private final DotCoverReportsAggregator dotCoverReportsAggregator;
  private final VisualStudioCoverageXmlReportParser visualStudioCoverageXmlReportParser;
  
  private static final Logger LOG = LoggerFactory.getLogger(CoverageAggregator.class);

  public CoverageAggregator(CoverageConfiguration coverageConf, Settings settings) {
    this(coverageConf, settings,
      new NCover3ReportParser(), new OpenCoverReportParser(), new DotCoverReportsAggregator(new DotCoverReportParser()), new VisualStudioCoverageXmlReportParser());
  }

  @VisibleForTesting
  public CoverageAggregator(CoverageConfiguration coverageConf, Settings settings,
    NCover3ReportParser ncover3ReportParser,
    OpenCoverReportParser openCoverReportParser,
    DotCoverReportsAggregator dotCoverReportsAggregator,
    VisualStudioCoverageXmlReportParser visualStudioCoverageXmlReportParser) {

    this.coverageConf = coverageConf;
    this.settings = settings;
    this.ncover3ReportParser = ncover3ReportParser;
    this.openCoverReportParser = openCoverReportParser;
    this.dotCoverReportsAggregator = dotCoverReportsAggregator;
    this.visualStudioCoverageXmlReportParser = visualStudioCoverageXmlReportParser;
  }

  public boolean hasCoverageProperty() {
    return hasNCover3ReportPaths() || hasOpenCoverReportPaths() || hasDotCoverReportPaths() || hasVisualStudioCoverageXmlReportPaths();
  }

  private boolean hasNCover3ReportPaths() {
    return settings.hasKey(coverageConf.ncover3PropertyKey());
  }

  private boolean hasOpenCoverReportPaths() {
    return settings.hasKey(coverageConf.openCoverPropertyKey());
  }

  private boolean hasDotCoverReportPaths() {
    return settings.hasKey(coverageConf.dotCoverPropertyKey());
  }

  private boolean hasVisualStudioCoverageXmlReportPaths() {
    return settings.hasKey(coverageConf.visualStudioCoverageXmlPropertyKey());
  }

  public Coverage aggregate(Coverage coverage, DirectoryScanner scanner) {
    
    if (hasNCover3ReportPaths()) {
      aggregate(settings.getString(coverageConf.ncover3PropertyKey()), ncover3ReportParser, coverage, scanner);
    }

    if (hasOpenCoverReportPaths()) {
      aggregate(settings.getString(coverageConf.openCoverPropertyKey()), openCoverReportParser, coverage, scanner);
    }

    if (hasDotCoverReportPaths()) {
      aggregate(settings.getString(coverageConf.dotCoverPropertyKey()), dotCoverReportsAggregator, coverage, scanner);
    }

    if (hasVisualStudioCoverageXmlReportPaths()) {
      aggregate(settings.getString(coverageConf.visualStudioCoverageXmlPropertyKey()), visualStudioCoverageXmlReportParser, coverage, scanner);
    }

    return coverage;
  }

  private static void aggregate(String reportPaths, CoverageParser parser, Coverage coverage, DirectoryScanner scanner) {
    for (String reportPath : Splitter.on(',').trimResults().omitEmptyStrings().split(reportPaths)) {        
        scanner.setIncludes(new String[]{reportPath});
        scanner.setBasedir(".");
        scanner.setCaseSensitive(false);
        scanner.scan();        
        String[] files = scanner.getIncludedFiles();
        if(files != null) {
            for (String file : files) {
                parser.parse(new File(file), coverage);  
            }                          
        }
    }
  }
}
