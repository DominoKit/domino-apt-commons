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

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Modifier;

/** Utility methods for creating javapoet specs */
public class DominoTypeBuilder {

  /**
   * Creates a {@link TypeSpec} for a class
   *
   * @param name the name of the class
   * @param processorClass the processor class
   * @return the type spec as builder
   */
  public static TypeSpec.Builder classBuilder(
      String name, Class<? extends Processor> processorClass) {
    return TypeSpec.classBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("This is a generated class, please don't modify\n");
  }

  /**
   * Creates a {@link TypeSpec} for an interface
   *
   * @param name the name of the interface
   * @param processorClass the processor class
   * @return the type spec as builder
   */
  public static TypeSpec.Builder interfaceBuilder(
      String name, Class<? extends Processor> processorClass) {
    return TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("This is a generated class, please don't modify\n");
  }

  /**
   * Creates a {@link TypeSpec} for an enum
   *
   * @param name the name of the enum
   * @param processorClass the processor class
   * @return the type spec as builder
   */
  public static TypeSpec.Builder enumBuilder(
      String name, Class<? extends Processor> processorClass) {
    return TypeSpec.enumBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc("This is a generated class, please don't modify\n");
  }
}
