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

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.Assertions.assertThat;

public class XmlParserHelperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void invalid_prolog() {
    thrown.expectMessage("Error while parsing the XML file: ");
    thrown.expectMessage("invalid_prolog.txt");

    new XmlParserHelper(new File("src/test/resources/xml_parser_helper/invalid_prolog.txt")).nextStartTag();
  }

  @Test
  public void nextStartOrEndTag() {
    XmlParserHelper xml = new XmlParserHelper(new File("src/test/resources/xml_parser_helper/valid.xml"));
    assertThat(xml.nextStartOrEndTag()).isEqualTo("<foo>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("<bar>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("</bar>");
    assertThat(xml.nextStartOrEndTag()).isEqualTo("</foo>");
    assertThat(xml.nextStartOrEndTag()).isNull();
  }

  @Test
  public void getRequiredDoubleAttribute() {
    XmlParserHelper xml = new XmlParserHelper(new File("src/test/resources/xml_parser_helper/valid.xml"));
    xml.nextStartTag();
    assertThat(xml.getRequiredDoubleAttribute("myDouble")).isEqualTo(0.123);

    thrown.expectMessage("valid.xml");
    thrown.expectMessage("Expected an double instead of \"hello\" for the attribute \"myString\"");
    xml.getRequiredDoubleAttribute("myString");
  }

}
