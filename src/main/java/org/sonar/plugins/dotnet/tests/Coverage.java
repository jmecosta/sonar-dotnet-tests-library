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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;
import java.util.Set;

public class Coverage {

  private final Table<String, Integer, Integer> hitsByLineAndFile = HashBasedTable.create();

  public void addHits(String file, int line, int hits) {
    Integer oldHits = hitsByLineAndFile.get(file, line);

    int newHits;
    if (oldHits == null) {
      newHits = hits;
    } else {
      newHits = hits + oldHits;
    }

    hitsByLineAndFile.put(file, line, newHits);
  }

  public Set<String> files() {
    return hitsByLineAndFile.rowKeySet();
  }

  public Map<Integer, Integer> hits(String file) {
    return hitsByLineAndFile.row(file);
  }

}
