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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class ExceptionUtil {

  /**
   * @param messager the messager to print the exception stack trace
   * @param e exception to be printed
   */
  public static void messageStackTrace(Messager messager, Exception e) {
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    messager.printMessage(
        Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString());
  }

  /**
   * @param messager the messager to print the exception stack trace
   * @param e exception to be printed
   * @param element the element producing the error
   */
  public static void messageStackTrace(Messager messager, Exception e, Element element) {
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    messager.printMessage(
        Diagnostic.Kind.ERROR,
        "error while creating source file " + out.getBuffer().toString(),
        element);
  }

  /**
   * @param messager the messager to print the exception stack trace
   * @param e exception to be printed
   * @param element the element producing the error
   * @param annotationMirror the annotation producing the error
   */
  public static void messageStackTrace(
      Messager messager, Exception e, Element element, AnnotationMirror annotationMirror) {
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    messager.printMessage(
        Diagnostic.Kind.ERROR,
        "error while creating source file " + out.getBuffer().toString(),
        element,
        annotationMirror);
  }
}
