/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotCoverReportsAggregator implements CoverageParser {

  private static final Logger LOG = LoggerFactory.getLogger(DotCoverReportsAggregator.class);

  private final DotCoverReportParser parser;

  public DotCoverReportsAggregator(DotCoverReportParser parser) {
    this.parser = parser;
  }

  @Override
  public void parse(File file, Coverage coverage) {
    LOG.info("Aggregating the HTML reports from " + file.getAbsolutePath());
    checkIsHtml(file);

    String folderName = extractFolderName(file);
    File folder = new File(file.getParentFile(), folderName + "/src");
    Preconditions.checkArgument(folder.exists(), "The following report dotCover report HTML sources folder cannot be found: " + folder.getAbsolutePath());

    Collection<File> reportFiles = FileUtils.listFiles(folder, new String[] {"html"}, false);
    for (File reportFile : reportFiles) {
      if (!isExcluded(reportFile)) {
        parser.parse(reportFile, coverage);
      }
    }
    Preconditions.checkArgument(!reportFiles.isEmpty(), "No dotCover report HTML source file found under: " + folder.getAbsolutePath());
  }

  private static void checkIsHtml(File file) {
    String contents;
    try {
      contents = Files.toString(file, Charsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    Preconditions.checkArgument(contents.startsWith("<!DOCTYPE html>"), "Only dotCover HTML reports which start with \"<!DOCTYPE html>\" are supported.");
  }

  private static String extractFolderName(File file) {
    String name = file.getName();
    int lastDot = name.lastIndexOf('.');
    Preconditions.checkArgument(lastDot != -1, "The following dotCover report name should have an extension: " + name);

    return name.substring(0, lastDot);
  }

  private static boolean isExcluded(File file) {
    return "nosource.html".equals(file.getName());
  }

}
