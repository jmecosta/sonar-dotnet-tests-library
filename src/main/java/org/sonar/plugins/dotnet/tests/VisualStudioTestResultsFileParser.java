/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * sonarqube@googlegroups.com
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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VisualStudioTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(VisualStudioTestResultsFileParser.class);

  @Override
  public void parse(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the Visual Studio Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;

    private boolean foundCounters;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);
        checkRootTag();
        dispatchTags();
        Preconditions.checkArgument(foundCounters, "The mandatory <Counters> tag is missing in " + file.getAbsolutePath());
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void dispatchTags() {
      String tagName;
      while ((tagName = xmlParserHelper.nextTag()) != null) {
        if ("Counters".equals(tagName)) {
          handleCountersTag();
        }
      }
    }

    private void handleCountersTag() {
      foundCounters = true;

      int passed = xmlParserHelper.getRequiredIntAttribute("passed");
      int failed = xmlParserHelper.getRequiredIntAttribute("failed");
      int errors = xmlParserHelper.getRequiredIntAttribute("error");
      int timeout = xmlParserHelper.getRequiredIntAttribute("timeout");
      int aborted = xmlParserHelper.getRequiredIntAttribute("aborted");

      int inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");

      int tests = passed + failed + errors + timeout + aborted;
      int skipped = inconclusive;
      int failures = timeout + failed + aborted;

      unitTestResults.add(tests, passed, skipped, failures, errors);
    }

    private void checkRootTag() {
      xmlParserHelper.checkRootTag("TestRun");
    }

  }

}
