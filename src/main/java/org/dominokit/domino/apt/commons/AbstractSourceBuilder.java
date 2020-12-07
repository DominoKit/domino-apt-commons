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

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Abstract class for source builders that has all necessary helper classes */
public abstract class AbstractSourceBuilder implements SourceBuilder {

  protected final Messager messager;
  protected final Filer filer;
  protected final Types types;
  protected final Elements elements;
  protected final ProcessingEnvironment processingEnv;
  protected final ProcessorUtil processorUtil;

  public AbstractSourceBuilder(ProcessingEnvironment processingEnv) {
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.types = processingEnv.getTypeUtils();
    this.elements = processingEnv.getElementUtils();
    this.processingEnv = processingEnv;
    this.processorUtil = new ProcessorUtil(processingEnv);
  }
}
