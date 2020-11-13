package org.dominokit.domino.apt.commons;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {

    /**
     * @param messager the messager to print the exception stack trace
     * @param e        exception to be printed
     */
    public static void messageStackTrace(Messager messager, Exception e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        messager.printMessage(Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString());
    }

    /**
     * @param messager the messager to print the exception stack trace
     * @param e        exception to be printed
     * @param element  the element producing the error
     */
    public static void messageStackTrace(Messager messager, Exception e, Element element) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        messager.printMessage(Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString(), element);
    }

    /**
     * @param messager         the messager to print the exception stack trace
     * @param e                exception to be printed
     * @param element          the element producing the error
     * @param annotationMirror the annotation producing the error
     */
    public static void messageStackTrace(Messager messager, Exception e, Element element, AnnotationMirror annotationMirror) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        messager.printMessage(Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString(), element, annotationMirror);
    }

}
