package org.dominokit.domino.apt.commons;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Modifier;

/**
 * Utility methods for creating javapoet specs
 */
public class DominoTypeBuilder {

    /**
     * Creates a {@link TypeSpec} for a class
     *
     * @param name           the name of the class
     * @param processorClass the processor class
     * @return the type spec as builder
     */
    public static TypeSpec.Builder classBuilder(String name, Class<? extends Processor> processorClass) {
        return TypeSpec.classBuilder(name)
                .addAnnotation(generatedAnnotation(processorClass))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("This is generated class, please don't modify\n");
    }

    /**
     * Creates a {@link TypeSpec} for an interface
     *
     * @param name           the name of the interface
     * @param processorClass the processor class
     * @return the type spec as builder
     */
    public static TypeSpec.Builder interfaceBuilder(String name, Class<? extends Processor> processorClass) {
        return TypeSpec.interfaceBuilder(name)
                .addAnnotation(generatedAnnotation(processorClass))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("This is generated class, please don't modify\n");
    }

    /**
     * Creates a {@link TypeSpec} for an enum
     *
     * @param name           the name of the enum
     * @param processorClass the processor class
     * @return the type spec as builder
     */
    public static TypeSpec.Builder enumBuilder(String name, Class<? extends Processor> processorClass) {
        return TypeSpec.enumBuilder(name)
                .addAnnotation(generatedAnnotation(processorClass))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("This is generated class, please don't modify\n");
    }

    private static AnnotationSpec generatedAnnotation(Class<? extends Processor> processorClass) {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "\"" + processorClass.getCanonicalName() + "\"")
                .build();
    }
}
