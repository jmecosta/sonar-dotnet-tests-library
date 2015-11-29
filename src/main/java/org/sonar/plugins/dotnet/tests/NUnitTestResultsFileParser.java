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
        
        if (checkRootTagNunit2()) {
          handleTestResultsTag();
        } else {
          handleTestResultsTagNunit3();
        }
        
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private boolean checkRootTagNunit2() {
      try
      {
        xmlParserHelper.checkRootTag("test-results");  
        return true;
      } catch(Exception ex) {
        LOG.info("Parsing the NUnit Test Results file V2 Failed: {1}", ex.getMessage());
        return false;
      }      
    }
    
    private void handleTestResultsTag() {
      LOG.info("Parsing the NUnit Test Results file V2: " + file.getAbsolutePath());
      int total = xmlParserHelper.getRequiredIntAttribute("total");
      int errors = xmlParserHelper.getRequiredIntAttribute("errors");
      int failures = xmlParserHelper.getRequiredIntAttribute("failures");
      int inconclusive = xmlParserHelper.getRequiredIntAttribute("inconclusive");
      int ignored = xmlParserHelper.getRequiredIntAttribute("ignored");

      int tests = total - inconclusive;
      int passed = total - errors - failures - inconclusive;
      int skipped = inconclusive + ignored;

      unitTestResults.add(tests, passed, skipped, failures, errors);        
    }
    
    private void handleTestResultsTagNunit3() {
      LOG.info("Parsing the NUnit Test Results file V3: " + file.getAbsolutePath());
      int total = xmlParserHelper.getRequiredIntAttribute("total");
      int passed = xmlParserHelper.getRequiredIntAttribute("passed");
      int failed = xmlParserHelper.getRequiredIntAttribute("failed");
      int skipped = xmlParserHelper.getRequiredIntAttribute("skipped");

      unitTestResults.add(total, passed, skipped, failed, 0);   
    }    
  }

}
