package org.dominokit.domino.apt.commons;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility methods for operating on elements.
 */
public class ProcessorElement {

    private final Element element;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Messager messager;

    public ProcessorElement(Element element, Elements elementUtils, Types typeUtils,
                            Messager messager) {
        this.element = element;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.messager = messager;
    }

    /**
     * Creates new processor element for different element
     *
     * @param element the new element
     * @return new processor element
     */
    public ProcessorElement make(Element element) {
        return new ProcessorElement(element, elementUtils, typeUtils, messager);
    }

    /**
     * @return the element package
     */
    public String elementPackage() {
        return elementUtils.getPackageOf(element).getQualifiedName().toString();
    }

    /**
     * @return the element as a {@link TypeElement}
     */
    public TypeElement asTypeElement() {
        return (TypeElement) element;
    }

    /**
     * @return the simple name of the element
     */
    public String simpleName() {
        return element.getSimpleName().toString();
    }

    /**
     * @return the full qualified name for the element
     */
    public String fullQualifiedNoneGenericName() {
        return elementPackage() + "." + simpleName();
    }

    /**
     * Returns the annotation if exist in the element
     *
     * @param annotation the annotation class
     * @param <A>        anything extends annotation
     * @return the annotation if exists, null otherwise
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return element.getAnnotation(annotation);
    }

    /**
     * Returns all the fields as a {@link Stream}
     *
     * @return the fields stream
     */
    public Stream<Element> fieldsStream() {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> (Element) e);
    }

    /**
     * Returns all the methods as a {@link Stream}
     *
     * @return the methods stream
     */
    public Stream<ExecutableElement> methodsStream() {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e);
    }

    /**
     * Returns all the fields annotation with an annotation as a {@link Stream}
     *
     * @return the fields stream
     */
    public <A extends Annotation> Stream<Element> fieldsAnnotatedWithStream(Class<A> annotationClass) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD)
                .filter(e -> fieldAnnotatedWith(e, annotationClass)).map(ele -> (Element) ele);
    }

    /**
     * Checks if the element is annotated with an annotation
     *
     * @return true if the element is annotated, false otherwise
     */
    private <A extends Annotation> boolean fieldAnnotatedWith(Element element, Class<A> annotationClass) {
        return Objects.nonNull(element.getAnnotation(annotationClass));
    }

    /**
     * Checks if the element is assignable of the target class (i.e. if it's a derivative type of it)
     *
     * @param targetClass the target class
     * @return true if the element is assignable from the target class, false otherwise
     */
    public boolean isAssignableFrom(Class<?> targetClass) {
        return typeUtils.isAssignable(element.asType(), typeUtils.getDeclaredType(elementUtils.getTypeElement(targetClass.getName())));
    }

    /**
     * Checks if the element implements a generic interface
     *
     * @param targetInterface the target interface
     * @return true if the element implements the interface, false otherwise
     */
    public boolean isImplementsGenericInterface(Class<?> targetInterface) {
        return asTypeElement().getInterfaces().stream().anyMatch(i -> isSameInterface(i, targetInterface));
    }

    private boolean isSameInterface(TypeMirror i, Class<?> targetInterface) {
        return targetInterface.getCanonicalName().equals(make(typeUtils.asElement(i)).fullQualifiedNoneGenericName());
    }

    /**
     * Returns the type mirror of an interface if the element implements it
     *
     * @param targetInterface the target interface
     * @return the type mirror of the interface
     * @throws IllegalArgumentException if the element does not implements the interface
     */
    public TypeMirror getInterfaceType(Class<?> targetInterface) {
        return asTypeElement().getInterfaces().stream().filter(i -> isSameInterface(i, targetInterface))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Returns the full qualified interface name
     *
     * @param targetInterface the target class
     * @return the full qualified interface name
     */
    public String getInterfaceFullQualifiedGenericName(Class<?> targetInterface) {
        return typeUtils.capture(getInterfaceType(targetInterface)).toString();
    }

    /**
     * Validate if the element is of kind
     *
     * @param kind the kind to check
     * @return true if the element is of the same kind, throws exception otherwise
     * @throws ProcessingException if the element is not of the same kind
     */
    public boolean validateElementKind(ElementKind kind) {
        if (element.getKind() != kind)
            throw new ProcessingException(element, "Only " + kind + " can be annotated with @%s");
        return true;
    }

    /**
     * @return the element
     */
    public Element getElement() {
        return element;
    }

    /**
     * @return the messager
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * @return the element utils
     */
    public Elements getElementUtils() {
        return elementUtils;
    }

    /**
     * @return the type utils
     */
    public Types getTypeUtils() {
        return typeUtils;
    }
}
