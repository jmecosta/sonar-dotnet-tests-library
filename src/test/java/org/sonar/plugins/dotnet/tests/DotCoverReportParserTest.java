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

public class DotCoverReportParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void no_title() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The report contents does not match the following regular expression: .*?<title>(.*?)</title>.*");
    new DotCoverReportParser().parse(new File("src/test/resources/dotcover/no_title.html"), mock(Coverage.class));
  }

  @Test
  public void no_highlight() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The report contents does not match the following regular expression: "
      + ".*<script type=\"text/javascript\">\\s*+highlightRanges\\(\\[(.*?)\\]\\);\\s*+</script>.*");
    new DotCoverReportParser().parse(new File("src/test/resources/dotcover/no_highlight.html"), mock(Coverage.class));
  }

  @Test
  public void valid() throws Exception {
    Coverage coverage = new Coverage();
    new DotCoverReportParser().parse(new File("src/test/resources/dotcover/valid.html"), coverage);

    assertThat(coverage.files()).containsOnly(
      new File("mylibrary\\calc.cs").getCanonicalPath());

    assertThat(coverage.hits(new File("mylibrary\\calc.cs").getCanonicalPath()))
      .hasSize(16)
      .includes(
        MapAssert.entry(12, 0),
        MapAssert.entry(13, 0),
        MapAssert.entry(14, 0),
        MapAssert.entry(17, 1),
        MapAssert.entry(18, 1),
        MapAssert.entry(19, 1),
        MapAssert.entry(22, 0),
        MapAssert.entry(23, 0),
        MapAssert.entry(24, 0),
        MapAssert.entry(25, 0),
        MapAssert.entry(26, 0),
        MapAssert.entry(28, 0),
        MapAssert.entry(29, 0),
        MapAssert.entry(32, 0),
        MapAssert.entry(33, 0),
        MapAssert.entry(34, 0));
  }

  @Test
  public void should_not_fail_with_invalid_path() {
    new DotCoverReportParser().parse(new File("src/test/resources/dotcover/invalid_path.html"), mock(Coverage.class));
  }

}
