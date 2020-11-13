package org.dominokit.domino.apt.commons;

import com.squareup.javapoet.TypeSpec;

import java.util.List;

/**
 * Interface for all source builders
 */
public interface SourceBuilder {

    /**
     * @return the type spec of the source
     */
    List<TypeSpec.Builder> asTypeBuilder();
}
