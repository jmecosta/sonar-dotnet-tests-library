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

import org.fest.assertions.MapAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OpenCoverReportParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void invalid_root() {
    thrown.expectMessage("<CoverageSession>");
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/invalid_root.xml"), mock(Coverage.class));
  }

  @Test
  public void missing_start_line() {
    thrown.expectMessage("Missing attribute \"sl\" in element <SequencePoint>");
    thrown.expectMessage("missing_start_line.xml at line 27");
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/missing_start_line.xml"), mock(Coverage.class));
  }

  @Test
  public void wrong_start_line() {
    thrown.expectMessage("Expected an integer instead of \"foo\" for the attribute \"sl\"");
    thrown.expectMessage("wrong_start_line.xml at line 27");
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/wrong_start_line.xml"), mock(Coverage.class));
  }

  @Test
  public void non_existing_file() {
    thrown.expectMessage("non_existing_file.xml");
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/non_existing_file.xml"), mock(Coverage.class));
  }

  @Test
  public void valid() throws Exception {
    Coverage coverage = new Coverage();
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/valid.xml"), coverage);

    assertThat(coverage.files()).containsOnly(
      new File("MyLibraryNUnitTest\\AdderNUnitTest.cs").getCanonicalPath(),
      new File("MyLibrary\\Adder.cs").getCanonicalPath(),
      new File("MyLibrary\\Multiplier.cs").getCanonicalPath());

    assertThat(coverage.hits(new File("MyLibrary\\Adder.cs").getCanonicalPath()))
      .hasSize(15)
      .includes(
        MapAssert.entry(11, 2),
        MapAssert.entry(12, 2),
        MapAssert.entry(13, 0),
        MapAssert.entry(14, 0),
        MapAssert.entry(15, 0),
        MapAssert.entry(18, 2),
        MapAssert.entry(22, 6),
        MapAssert.entry(26, 2),
        MapAssert.entry(27, 2),
        MapAssert.entry(30, 4),
        MapAssert.entry(31, 4),
        MapAssert.entry(32, 4),
        MapAssert.entry(35, 2),
        MapAssert.entry(36, 2),
        MapAssert.entry(37, 2));

    assertThat(coverage.hits(new File("MyLibrary\\Multiplier.cs").getCanonicalPath()))
      .hasSize(3)
      .includes(
        MapAssert.entry(11, 0),
        MapAssert.entry(12, 0),
        MapAssert.entry(13, 0));
  }

  @Test
  public void should_not_fail_with_invalid_path() {
    new OpenCoverReportParser().parse(new File("src/test/resources/opencover/invalid_path.xml"), mock(Coverage.class));
  }

}
