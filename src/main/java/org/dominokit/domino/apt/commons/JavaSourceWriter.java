package org.dominokit.domino.apt.commons;

import java.io.IOException;

/**
 * Abstract class for all java source writers
 */
public abstract class JavaSourceWriter {

    protected final ProcessorElement processorElement;

    public JavaSourceWriter(ProcessorElement processorElement) {
        this.processorElement = processorElement;
    }

    /**
     * @return the java source as a string
     * @throws IOException if something went wrong
     */
    public abstract String write() throws IOException;
}
