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

import java.io.File;
import org.apache.tools.ant.DirectoryScanner;
import static org.fest.assertions.Assertions.assertThat;

import static org.fest.assertions.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sonar.api.config.Settings;

public class CoverageAggregatorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasCoverageProperty() {
    Settings settings = mock(Settings.class);

    CoverageConfiguration coverageConf = new CoverageConfiguration("", "ncover", "opencover", "dotcover", "visualstudio");

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(false);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isFalse();

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(false);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isTrue();

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(true);
    when(settings.hasKey("visualstudio")).thenReturn(false);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isTrue();

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(true);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isTrue();

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.hasKey("dotcover")).thenReturn(true);
    when(settings.hasKey("visualstudio")).thenReturn(true);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isTrue();

    coverageConf = new CoverageConfiguration("", "ncover2", "opencover2", "dotcover2", "visualstudio2");
    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.hasKey("dotcover")).thenReturn(true);
    when(settings.hasKey("visualstudio")).thenReturn(true);
    assertThat(new CoverageAggregator(coverageConf, settings).hasCoverageProperty()).isFalse();
  }

  @Test
  public void aggregate() {
    CoverageConfiguration coverageConf = new CoverageConfiguration("", "ncover", "opencover", "dotcover", "visualstudio");
    Settings settings = mock(Settings.class);

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.getString("ncover")).thenReturn("foo.nccov");
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(false);

    NCover3ReportParser ncoverParser = mock(NCover3ReportParser.class);
    OpenCoverReportParser openCoverParser = mock(OpenCoverReportParser.class);
    DotCoverReportsAggregator dotCoverParser = mock(DotCoverReportsAggregator.class);
    VisualStudioCoverageXmlReportParser visualStudioCoverageXmlReportParser = mock(VisualStudioCoverageXmlReportParser.class);
    Coverage coverage = mock(Coverage.class);
    
    DirectoryScanner scanner = mock(DirectoryScanner.class);
    String files[] = {"foo.nccov"};
    when(scanner.getIncludedFiles()).thenReturn(files);
    
    new CoverageAggregator(coverageConf, settings, ncoverParser, openCoverParser, dotCoverParser, visualStudioCoverageXmlReportParser).aggregate(coverage, scanner);
    verify(ncoverParser).parse(new File("foo.nccov"), coverage);
    verify(openCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(dotCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(visualStudioCoverageXmlReportParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.getString("opencover")).thenReturn("bar.xml");
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(false);
    ncoverParser = mock(NCover3ReportParser.class);
    openCoverParser = mock(OpenCoverReportParser.class);
    dotCoverParser = mock(DotCoverReportsAggregator.class);
    visualStudioCoverageXmlReportParser = mock(VisualStudioCoverageXmlReportParser.class);
    coverage = mock(Coverage.class);

    DirectoryScanner scannerOp = mock(DirectoryScanner.class);
    String files2[] = {"bar.xml"};
    when(scannerOp.getIncludedFiles()).thenReturn(files2);
    
    new CoverageAggregator(coverageConf, settings, ncoverParser, openCoverParser, dotCoverParser, visualStudioCoverageXmlReportParser).aggregate(coverage, scannerOp);
    verify(ncoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(openCoverParser).parse(new File("bar.xml"), coverage);
    verify(dotCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(visualStudioCoverageXmlReportParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(true);
    when(settings.getString("dotcover")).thenReturn("baz.html");
    when(settings.hasKey("visualstudio")).thenReturn(false);
    ncoverParser = mock(NCover3ReportParser.class);
    openCoverParser = mock(OpenCoverReportParser.class);
    dotCoverParser = mock(DotCoverReportsAggregator.class);
    visualStudioCoverageXmlReportParser = mock(VisualStudioCoverageXmlReportParser.class);
    coverage = mock(Coverage.class);
    
    DirectoryScanner scannerDotCover = mock(DirectoryScanner.class);
    String files3[] = {"baz.html"};
    when(scannerDotCover.getIncludedFiles()).thenReturn(files3);
    
    new CoverageAggregator(coverageConf, settings, ncoverParser, openCoverParser, dotCoverParser, visualStudioCoverageXmlReportParser).aggregate(coverage, scannerDotCover);
    verify(ncoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(openCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(dotCoverParser).parse(new File("baz.html"), coverage);
    verify(visualStudioCoverageXmlReportParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));

    when(settings.hasKey("ncover")).thenReturn(false);
    when(settings.hasKey("opencover")).thenReturn(false);
    when(settings.hasKey("dotcover")).thenReturn(false);
    when(settings.hasKey("visualstudio")).thenReturn(true);
    when(settings.getString("visualstudio")).thenReturn("qux.coveragexml");
    ncoverParser = mock(NCover3ReportParser.class);
    openCoverParser = mock(OpenCoverReportParser.class);
    dotCoverParser = mock(DotCoverReportsAggregator.class);
    visualStudioCoverageXmlReportParser = mock(VisualStudioCoverageXmlReportParser.class);
    coverage = mock(Coverage.class);
    
    DirectoryScanner scannerVS = mock(DirectoryScanner.class);
    String files4[] = {"qux.coveragexml"};
    when(scannerVS.getIncludedFiles()).thenReturn(files4);
    
    new CoverageAggregator(coverageConf, settings, ncoverParser, openCoverParser, dotCoverParser, visualStudioCoverageXmlReportParser).aggregate(coverage, scannerVS);
    verify(ncoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(openCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(dotCoverParser, Mockito.never()).parse(Mockito.any(File.class), Mockito.any(Coverage.class));
    verify(visualStudioCoverageXmlReportParser).parse(new File("qux.coveragexml"), coverage);

    when(settings.hasKey("ncover")).thenReturn(true);
    when(settings.getString("ncover")).thenReturn(",foo.nccov  ,bar.nccov");
    when(settings.hasKey("opencover")).thenReturn(true);
    when(settings.getString("opencover")).thenReturn("bar.xml");
    when(settings.hasKey("dotcover")).thenReturn(true);
    when(settings.getString("dotcover")).thenReturn("baz.html");
    when(settings.hasKey("visualstudio")).thenReturn(true);
    when(settings.getString("visualstudio")).thenReturn("qux.coveragexml");
    ncoverParser = mock(NCover3ReportParser.class);
    openCoverParser = mock(OpenCoverReportParser.class);
    dotCoverParser = mock(DotCoverReportsAggregator.class);
    visualStudioCoverageXmlReportParser = mock(VisualStudioCoverageXmlReportParser.class);
    coverage = mock(Coverage.class);

    DirectoryScanner scannerCombined = mock(DirectoryScanner.class);
    String call1[] = {"foo.nccov"};
    String call2[] = {"bar.nccov"};
    String call3[] = {"bar.xml"};
    String call4[] = {"baz.html"};
    String call5[] = {"qux.coveragexml"};
    when(scannerCombined.getIncludedFiles()).thenReturn(call1).thenReturn(call2).thenReturn(call3).thenReturn(call4).thenReturn(call5);
    
    new CoverageAggregator(coverageConf, settings, ncoverParser, openCoverParser, dotCoverParser, visualStudioCoverageXmlReportParser).aggregate(coverage, scannerCombined);
    verify(ncoverParser).parse(new File("foo.nccov"), coverage);
    verify(ncoverParser).parse(new File("bar.nccov"), coverage);
    verify(openCoverParser).parse(new File("bar.xml"), coverage);
    verify(dotCoverParser).parse(new File("baz.html"), coverage);
    verify(visualStudioCoverageXmlReportParser).parse(new File("qux.coveragexml"), coverage);
  }

}
