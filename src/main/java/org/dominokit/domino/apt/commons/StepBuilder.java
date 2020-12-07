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
