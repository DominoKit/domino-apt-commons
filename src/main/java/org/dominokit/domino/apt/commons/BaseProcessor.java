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

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/** A base processor that provides a list of helper methods */
public abstract class BaseProcessor extends AbstractProcessor {

  protected Types typeUtils;
  protected Elements elementUtils;
  protected Filer filer;
  protected Messager messager;
  protected ElementFactory elementFactory;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.typeUtils = processingEnv.getTypeUtils();
    this.elementUtils = processingEnv.getElementUtils();
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
    this.elementFactory = new ElementFactory(elementUtils, typeUtils);
  }

  /**
   * Creates a {@link Writer} to a class with a specific package and name
   *
   * @param targetPackage the package to write to
   * @param className the class name
   * @return the writer
   * @throws IOException if the writer cannot be created
   */
  protected Writer obtainSourceWriter(String targetPackage, String className) throws IOException {
    return createSourceFile(targetPackage, className).openWriter();
  }

  /**
   * Creates a source file to a class with a specific package and name
   *
   * @param targetPackage the package to write to
   * @param className the class name
   * @return the source file as a {@link JavaFileObject}
   * @throws IOException if the writer cannot be created
   */
  protected JavaFileObject createSourceFile(String targetPackage, String className)
      throws IOException {
    return filer.createSourceFile(targetPackage + "." + className);
  }

  /**
   * Creates a {@link Writer} to a resource with a specific package and name
   *
   * @param targetPackage the package to write to
   * @param resourceName the resource name
   * @return the writer
   * @throws IOException if the writer cannot be created
   */
  protected Writer obtainResourceWriter(String targetPackage, String resourceName)
      throws IOException {
    return createResourceFile(targetPackage, resourceName).openWriter();
  }

  /**
   * Creates a resource file to a resource with a specific package and name
   *
   * @param targetPackage the package to write to
   * @param resourceName the resource name
   * @return the resource file as a {@link FileObject}
   * @throws IOException if the writer cannot be created
   */
  protected FileObject createResourceFile(String targetPackage, String resourceName)
      throws IOException {
    return filer.createResource(StandardLocation.SOURCE_OUTPUT, targetPackage, resourceName);
  }

  /**
   * Checks if an element is of a specific {@link ElementKind}
   *
   * @param element the element
   * @param kind the kind
   * @return {@code true} if the element is of the kind, throws exception otherwise
   * @throws ProcessingException if the element is not of the kind
   */
  protected boolean validateElementKind(Element element, ElementKind kind) {
    if (element.getKind() != kind)
      throw new ProcessingException(element, "Only " + kind + " can be annotated with @%s");
    return true;
  }

  /**
   * Creates a new {@link ProcessorElement} for a specific element
   *
   * @param element the element
   * @return a new processor element
   */
  protected ProcessorElement newProcessorElement(Element element) {
    return new ProcessorElement(element, elementUtils, typeUtils, messager);
  }

  /** Factory class for {@link ProcessorElement} */
  public class ElementFactory {

    private final Elements elementUtils;
    private final Types typeUtils;

    public ElementFactory(Elements elementUtils, Types typeUtils) {
      this.elementUtils = elementUtils;
      this.typeUtils = typeUtils;
    }

    public ProcessorElement make(Element element) {
      return new ProcessorElement(element, this.elementUtils, this.typeUtils, messager);
    }
  }

  /** A step in the process of generation */
  @FunctionalInterface
  public interface ProcessingStep {

    /**
     * Process the step passing all the elements annotated with the supported annotation
     *
     * @param elementsByAnnotation the elements annotated with the supported annotation
     */
    void process(Set<? extends Element> elementsByAnnotation);
  }
}
