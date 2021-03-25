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

import javax.lang.model.SourceVersion;

/** A holder for the current source version based on the processing environment */
public class CurrentSourceVersion {

  private static SourceVersion sourceVersion;

  public static void set(SourceVersion sourceVersion) {
    CurrentSourceVersion.sourceVersion = sourceVersion;
  }

  public static SourceVersion get() {
    return CurrentSourceVersion.sourceVersion;
  }
}
