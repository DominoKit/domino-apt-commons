package org.dominokit.domino.apt.commons;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Abstract builder for a {@link org.dominokit.domino.apt.commons.BaseProcessor.ProcessingStep}
 *
 * @param <T> anything extends {@link AbstractProcessingStep}
 */
public abstract class StepBuilder<T extends AbstractProcessingStep> {

    protected ProcessingEnvironment processingEnv;

    /**
     * builds the step
     *
     * @return the step
     */
    public abstract T build();

    /**
     * sets the processing environment
     *
     * @param processingEnv the processing environment
     * @return the same builder
     */
    public StepBuilder<T> setProcessingEnv(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        return this;
    }
}
