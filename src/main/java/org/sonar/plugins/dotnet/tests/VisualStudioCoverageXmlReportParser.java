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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class VisualStudioCoverageXmlReportParser implements CoverageParser {

  private static final Logger LOG = LoggerFactory.getLogger(VisualStudioCoverageXmlReportParser.class);

  @Override
  public void parse(File file, Coverage coverage) {
    LOG.info("Parsing the Visual Studio coverage XML report " + file.getAbsolutePath());
    new Parser(file, coverage).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final Multimap<Integer, Integer> coveredLines = HashMultimap.create();
    private final Multimap<Integer, Integer> uncoveredLines = HashMultimap.create();
    private final Coverage coverage;

    public Parser(File file, Coverage coverage) {
      this.file = file;
      this.coverage = coverage;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);
        checkRootTag();
        dispatchTags();
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void dispatchTags() {
      String tagName;
      while ((tagName = xmlParserHelper.nextTag()) != null) {
        if ("module".equals(tagName)) {
          handleModuleTag();
        } else if ("range".equals(tagName)) {
          handleRangeTag();
        } else if ("source_file".equals(tagName)) {
          handleSourceFileTag();
        }
      }
    }

    private void handleModuleTag() {
      coveredLines.clear();
      uncoveredLines.clear();
    }

    private void handleRangeTag() {
      int source = xmlParserHelper.getRequiredIntAttribute("source_id");
      String covered = xmlParserHelper.getRequiredAttribute("covered");

      int line = xmlParserHelper.getRequiredIntAttribute("start_line");

      if ("yes".equals(covered) || "partial".equals(covered)) {
        coveredLines.put(source, line);
      } else if ("no".equals(covered)) {
        uncoveredLines.put(source, line);
      } else {
        throw xmlParserHelper.parseError("Unsupported \"covered\" value \"" + covered + "\", expected one of \"yes\", \"partial\" or \"no\"");
      }
    }

    private void handleSourceFileTag() {
      int id = xmlParserHelper.getRequiredIntAttribute("id");
      String path = xmlParserHelper.getRequiredAttribute("path");

      String canonicalPath;
      try {
        canonicalPath = new File(path).getCanonicalPath();
      } catch (IOException e) {
        LOG.debug("Skipping the import of Visual Studio XML code coverage for the invalid file path: " + path
          + " at line " + xmlParserHelper.stream().getLocation().getLineNumber(), e);
        return;
      }

      for (Integer line : coveredLines.get(id)) {
        coverage.addHits(canonicalPath, line, 1);
      }

      for (Integer line : uncoveredLines.get(id)) {
        coverage.addHits(canonicalPath, line, 0);
      }
    }

    private void checkRootTag() {
      xmlParserHelper.checkRootTag("results");
    }

  }

}
