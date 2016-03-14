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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class NUnitTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(NUnitTestResultsFileParser.class);

  @Override
  public void parse(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the NUnit Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);

        xmlParserHelper.nextTag();
        testresulttags();
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void testresulttags() {
      LOG.info("Parsing the NUnit Test Results file: " + file.getAbsolutePath());
      int total = 0;
      int errors = 0;
      int failures = 0;
      int inconclusive = 0;
      int ignored = 0;
      int passed = 0;
      int skipped = 0;

      total = xmlParserHelper.getRequiredIntAttribute("total");

      if(xmlParserHelper.isAttributePresent("failures")) {
        failures = xmlParserHelper.getRequiredIntAttribute("failures");
      } else {
        if(xmlParserHelper.isAttributePresent("failed")) {
          failures = xmlParserHelper.getRequiredIntAttribute("failed");
        }
      }

      if(xmlParserHelper.isAttributePresent("inconclusive")) {
        inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      }

      if(xmlParserHelper.isAttributePresent("errors")) {
        errors = xmlParserHelper.getRequiredIntAttribute("errors");
      }

      if(xmlParserHelper.isAttributePresent("ignored")) {
        ignored = xmlParserHelper.getRequiredIntAttribute("ignored");
      } else {
        if(xmlParserHelper.isAttributePresent("not-run")) {
          ignored = xmlParserHelper.getRequiredIntAttribute("not-run");
        }
      }

      if(xmlParserHelper.isAttributePresent("skipped")) {
        skipped = xmlParserHelper.getRequiredIntAttribute("skipped");
      }

      if(xmlParserHelper.isAttributePresent("errors")) {
        errors = xmlParserHelper.getRequiredIntAttribute("errors");
      }

      int tests = total - inconclusive;
      if (passed == 0) {
        passed = total - errors - failures - inconclusive;
      }
      
      if (skipped == 0) {
        skipped = inconclusive + ignored;
      }

      unitTestResults.add(tests, passed, skipped, failures, errors);
    }
  }
}
