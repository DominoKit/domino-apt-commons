/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.domino.apt.commons;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/** Helper class provides information about a class */
public class FullClassName {
  private final String completeClassName;

  public FullClassName(String completeClassName) {
    if (Objects.isNull(completeClassName) || completeClassName.trim().isEmpty())
      throw new InvalidClassName();
    this.completeClassName = completeClassName.trim().replace(" ", "");
  }

  /** @return The simple name of the class */
  public String asSimpleName() {
    return getSimpleName(simpleFullName());
  }

  private String getSimpleName(String name) {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  /** @return The package of the class */
  public String asPackage() {
    return getPackage(simpleFullName());
  }

  private String getPackage(String name) {
    return name.substring(0, name.lastIndexOf('.') > -1 ? name.lastIndexOf('.') : 0);
  }

  private String simpleFullName() {
    return new StringTokenizer(completeClassName, "<,>").nextToken();
  }

  /** @return the class as an import statement */
  public String asImport() {
    return simpleFullName();
  }

  /** @return the simple name for a generic class */
  public String asSimpleGenericName() {
    String result = this.completeClassName;
    StringTokenizer st = new StringTokenizer(completeClassName, "<,>");
    while (st.hasMoreTokens())
      result = result.replace(appendDot(new FullClassName(st.nextToken()).asPackage()), "");
    return result;
  }

  private String appendDot(String part) {
    return part.isEmpty() ? part : dottedPart(part);
  }

  private String dottedPart(String part) {
    return part.endsWith(".") ? part : (part + ".");
  }

  /** @return all the imports needed for this class including generics if exist */
  public List<String> allImports() {
    StringTokenizer st = new StringTokenizer(completeClassName, "<,>");
    List<String> imports = new LinkedList<>();
    while (st.hasMoreTokens()) {
      String s = new FullClassName(st.nextToken()).asImport();
      if (!imports.contains(s)) imports.add(s);
    }
    return imports;
  }

  /** Unchecked exception thrown when the class name is invalid */
  public static class InvalidClassName extends RuntimeException {}
}
