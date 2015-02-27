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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.sonar.api.utils.SonarException;

import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class XmlParserHelper {

  private final File file;
  private final InputStreamReader reader;
  private final XMLStreamReader stream;

  public XmlParserHelper(File file) {
    try {
      this.file = file;
      this.reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
      this.stream = xmlFactory.createXMLStreamReader(reader);

    } catch (FileNotFoundException e) {
      throw Throwables.propagate(e);
    } catch (XMLStreamException e) {
      throw Throwables.propagate(e);
    }
  }

  public void checkRootTag(String name) {
    String rootTag = nextTag();

    if (!name.equals(rootTag)) {
      throw parseError("Missing root element <" + name + ">");
    }
  }

  @Nullable
  public String nextTag() {
    try {
      while (stream.hasNext()) {
        if (stream.next() == XMLStreamConstants.START_ELEMENT) {
          return stream.getLocalName();
        }
      }

      return null;
    } catch (XMLStreamException e) {
      throw new SonarException("Error while parsing the XML file: " + file.getAbsolutePath(), e);
    }
  }

  public void checkRequiredAttribute(String name, int expectedValue) {
    int actualValue = getRequiredIntAttribute(name);
    if (expectedValue != actualValue) {
      throw parseError("Expected \"" + expectedValue + "\" instead of \"" + actualValue + "\" for the \"" + name + "\" attribute");
    }
  }

  public int getRequiredIntAttribute(String name) {
    String value = getRequiredAttribute(name);

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw parseError("Expected an integer instead of \"" + value + "\" for the attribute \"" + name + "\"");
    }
  }

  public String getRequiredAttribute(String name) {
    String value = getAttribute(name);
    if (value == null) {
      throw parseError("Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
    }

    return value;
  }

  @Nullable
  public String getAttribute(String name) {
    for (int i = 0; i < stream.getAttributeCount(); i++) {
      if (name.equals(stream.getAttributeLocalName(i))) {
        return stream.getAttributeValue(i);
      }
    }

    return null;
  }

  public ParseErrorException parseError(String message) {
    return new ParseErrorException(message + " in " + file.getAbsolutePath() + " at line " + stream.getLocation().getLineNumber());
  }

  public void close() {
    Closeables.closeQuietly(reader);

    if (stream != null) {
      try {
        stream.close();
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  public XMLStreamReader stream() {
    return stream;
  }

}
