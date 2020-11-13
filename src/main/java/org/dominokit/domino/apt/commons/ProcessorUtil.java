package org.dominokit.domino.apt.commons;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Utility methods for operating on elements.
 */
public class ProcessorUtil {
    private static final int FIRST_ARGUMENT = 0;
    private static final int SECOND_ARGUMENT = 1;

    protected final Messager messager;
    protected final Filer filer;
    protected final Types types;
    protected final Elements elements;
    protected final ProcessingEnvironment processingEnv;

    public ProcessorUtil(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.processingEnv = processingEnv;
    }

    /**
     * @return {@link Messager} to print messages
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * @return {@link Types} for operating on types
     */
    public Types getTypes() {
        return types;
    }

    /**
     * @return {@link Elements} for operating on program elements
     */
    public Elements getElements() {
        return elements;
    }

    /**
     * Returns all methods annotated with an annotation
     *
     * @param beanType   the type to search in
     * @param annotation the target annotation
     * @return list of methods that are annotated with the annotation
     */
    public List<Element> getAnnotatedMethods(TypeMirror beanType, Class<? extends Annotation> annotation) {
        return getAnnotatedElements(beanType, annotation, element -> ElementKind.METHOD.equals(element.getKind()));
    }

    /**
     * Returns all fields annotated with an annotation
     *
     * @param beanType   the type to search in
     * @param annotation the target annotation
     * @return list of fields that are annotated with the annotation
     */
    public List<Element> getAnnotatedFields(TypeMirror beanType, Class<? extends Annotation> annotation) {
        return getAnnotatedElements(beanType, annotation, element -> ElementKind.FIELD.equals(element.getKind()));
    }

    /**
     * Returns all elements annotated with an annotation based on a filter
     *
     * @param beanType   the type to search in
     * @param annotation the target annotation
     * @return list of the elements that are annotated with the annotation
     */
    public List<Element> getAnnotatedElements(TypeMirror beanType, Class<? extends Annotation> annotation, Function<Element, Boolean> filter) {
        TypeElement typeElement = (TypeElement) types.asElement(beanType);

        List<Element> annotatedMethods = getAnnotatedElements(typeElement, annotation, filter);
        return new ArrayList<>(annotatedMethods);
    }

    /**
     * Returns all elements annotated with an annotation based on a filter
     *
     * @param typeElement the element to search in
     * @param annotation  the target annotation
     * @return list of the elements that are annotated with the annotation
     */
    public List<Element> getAnnotatedElements(TypeElement typeElement, Class<? extends Annotation> annotation, Function<Element, Boolean> filter) {
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return new ArrayList<>();
        }

        List<Element> methods = typeElement.getEnclosedElements()
                .stream()
                .filter(filter::apply)
                .filter(element -> nonNull(element.getAnnotation(annotation)))
                .collect(Collectors.toList());

        methods.addAll(getAnnotatedElements((TypeElement) types.asElement(superclass), annotation, filter));
        return methods;
    }

    /**
     * Find the most first type argument that is assignable of the target class, this method goes through the interfaces and the super classes of the element.
     * <p>
     * Returns an {@link Optional#empty} if there is no type argument is assignable of the target class
     *
     * @param element     the element to check for its type arguments
     * @param targetClass the target class to check the type argument if it is assignable from
     * @return The type mirror of the found type argument, {@link Optional#empty} if there is no type argument found
     */
    public Optional<TypeMirror> findTypeArgument(TypeMirror element, Class<?> targetClass) {
        if (element.getKind().equals(TypeKind.NONE)) {
            return Optional.empty();
        }

        DeclaredType elementType = (DeclaredType) element;
        List<? extends TypeMirror> typeArguments = elementType.getTypeArguments();
        for (TypeMirror type : typeArguments) {
            if (isAssignableFrom(type, targetClass)) {
                return Optional.of(type);
            }
        }


        TypeElement typeElement = (TypeElement) types.asElement(element);

        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        for (TypeMirror interfaceType : interfaces) {
            List<? extends TypeMirror> interfaceTypeArguments = ((DeclaredType) interfaceType).getTypeArguments();
            for (TypeMirror type : interfaceTypeArguments) {
                if (isAssignableFrom(type, targetClass)) {
                    return Optional.of(type);
                }
            }
        }
        return findTypeArgument(typeElement.getSuperclass(), targetClass);
    }

    /**
     * Capitalize the first letter of a {@link String}
     *
     * @param input the string
     * @return The new string with capital first letter
     */
    public String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Un-capitalize the first letter of a {@link String}
     *
     * @param input the string
     * @return The new string with small first letter
     */
    public String smallFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    /**
     * @deprecated use {@link ProcessorUtil#smallFirstLetter(String)} instead
     */
    @Deprecated
    public String lowerFirstLetter(String input) {
        return smallFirstLetter(input);
    }

    /**
     * Checks if the element type is assignable of the target class (i.e. if it's a derivative type of it)
     *
     * @param element     the element
     * @param targetClass the target class
     * @return true if the element is assignable from the target class, false otherwise
     */
    public boolean isAssignableFrom(Element element, Class<?> targetClass) {
        return isAssignableFrom(element.asType(), targetClass);
    }

    /**
     * Checks if the type mirror is assignable of the target class (i.e. if it's a derivative type of it)
     *
     * @param typeMirror  the type mirror
     * @param targetClass the target class
     * @return true if the type mirror is assignable from the target class, false otherwise
     */
    public boolean isAssignableFrom(TypeMirror typeMirror, Class<?> targetClass) {
        return types.isAssignable(typeMirror, types.getDeclaredType(elements.getTypeElement(targetClass.getCanonicalName())));
    }

    /**
     * Searches for an annotation of a specific class in an element.
     *
     * @param element    the element
     * @param annotation the annotation class
     * @param <A>        any type extends {@link Annotation}
     * @return The annotation if the element or any of its super classes has it, {@code null} otherwise
     */
    public <A extends Annotation> A findClassAnnotation(Element element, Class<A> annotation) {
        A result = element.getAnnotation(annotation);
        if (nonNull(result)) {
            return result;
        }
        TypeMirror superclass = ((TypeElement) element).getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return null;
        } else {
            return findClassAnnotation(types.asElement(superclass), annotation);
        }
    }

    /**
     * Finds the type mirror of a class defined as an annotation parameter <em>recursively</em>.
     * This method goes through all the super classes of the element.
     *
     * @param element    the element
     * @param annotation the annotation
     * @param paramName  the class parameter name
     * @return The type mirror of the class, {@link Optional#empty()} otherwise
     * @see ProcessorUtil#getClassValueFromAnnotation(Element, Class, String)
     */
    public Optional<TypeMirror> findClassValueFromClassAnnotation(Element element, Class<? extends Annotation> annotation, String paramName) {
        Optional<TypeMirror> result = getClassValueFromAnnotation(element, annotation, paramName);
        if (result.isPresent()) {
            return result;
        }
        TypeMirror superclass = ((TypeElement) element).getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return Optional.empty();
        } else {
            return findClassValueFromClassAnnotation(types.asElement(superclass), annotation, paramName);
        }
    }

    /**
     * Finds the type mirror of a class defined as an annotation parameter.
     * <p>
     * For example:
     * <pre>
     * interface &#64;MyAnnotation {
     *  Class&#60;?&#62; myClass();
     * }
     * </pre>
     * <p>
     *
     * @param element    the element
     * @param annotation the annotation
     * @param paramName  the class parameter name
     * @return The type mirror of the class, {@link Optional#empty()} otherwise
     */
    public Optional<TypeMirror> getClassValueFromAnnotation(Element element, Class<? extends Annotation> annotation, String paramName) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (types.isSameType(am.getAnnotationType(), elements.getTypeElement(annotation.getCanonicalName()).asType())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if (paramName.equals(entry.getKey().getSimpleName().toString())) {
                        AnnotationValue annotationValue = entry.getValue();
                        return Optional.of((DeclaredType) annotationValue.getValue());
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a list of type mirrors for classes defined as an annotation parameter.
     * <p>
     * For example:
     * <pre>
     * interface &#64;MyAnnotation {
     *  Class&#60;?&#62;[] myClasses();
     * }
     * </pre>
     * <p>
     *
     * @param element    the element
     * @param annotation the annotation
     * @param paramName  the class parameter name
     * @return The list of type mirrors for the classes, empty list otherwise
     */
    public List<TypeMirror> getClassArrayValueFromAnnotation(Element element, Class<? extends Annotation> annotation, String paramName) {

        List<TypeMirror> values = new ArrayList<>();

        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (types.isSameType(am.getAnnotationType(), elements.getTypeElement(annotation.getCanonicalName()).asType())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if (paramName.equals(entry.getKey().getSimpleName().toString())) {
                        List<AnnotationValue> classesTypes = (List<AnnotationValue>) entry.getValue().getValue();
                        Iterator<? extends AnnotationValue> iterator = classesTypes.iterator();

                        while (iterator.hasNext()) {
                            AnnotationValue next = iterator.next();
                            values.add((TypeMirror) next.getValue());
                        }
                    }
                }
            }
        }
        return values;
    }

    /**
     * Returns a list of methods enclosed within an element
     *
     * @param element the element
     * @return a list of all methods enclosed within an element
     */
    public List<ExecutableElement> getElementMethods(Element element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the type mirror as assignable from {@link String}
     *
     * @param typeMirror the type mirror
     * @return {@code true} if the type mirror is a {@link String}, {@code false} otherwise
     */
    public boolean isStringType(TypeMirror typeMirror) {
        TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
        return types.isAssignable(stringType, typeMirror);
    }

    /**
     * Returns the type name of the type mirror.
     * <p>
     * If the type is a primitive type, then the type name of its wrapper type will be returned.
     *
     * @param type the type mirror.
     * @return The type name.
     */
    public TypeName wrapperType(TypeMirror type) {
        if (isPrimitive(type)) {
            if ("boolean".equals(type.toString())) {
                return TypeName.get(Boolean.class);
            } else if ("byte".equals(type.toString())) {
                return TypeName.get(Byte.class);
            } else if ("short".equals(type.toString())) {
                return TypeName.get(Short.class);
            } else if ("int".equals(type.toString())) {
                return TypeName.get(Integer.class);
            } else if ("long".equals(type.toString())) {
                return TypeName.get(Long.class);
            } else if ("char".equals(type.toString())) {
                return TypeName.get(Character.class);
            } else if ("float".equals(type.toString())) {
                return TypeName.get(Float.class);
            } else if ("double".equals(type.toString())) {
                return TypeName.get(Double.class);
            } else {
                return TypeName.get(Void.class);
            }
        } else {
            return TypeName.get(type);
        }
    }

    /**
     * Checks if the type mirror is a primitive.
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a primitive, {@code false} otherwise
     */
    public boolean isPrimitive(TypeMirror typeMirror) {
        return typeMirror.getKind().isPrimitive();
    }

    /**
     * Checks if the type mirror is a primitive array.
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a primitive array, {@code false} otherwise
     */
    public boolean isPrimitiveArray(TypeMirror typeMirror) {
        return (isArray(typeMirror) && isPrimitive(arrayComponentType(typeMirror))) || isPrimitive2dArray(typeMirror);
    }

    /**
     * Checks if the type mirror is a primitive 2D array.
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a primitive 2D array, {@code false} otherwise
     */
    private boolean isPrimitive2dArray(TypeMirror typeMirror) {
        return is2dArray(typeMirror) && isPrimitiveArray(arrayComponentType(typeMirror));
    }

    /**
     * Checks if the type mirror is an array.
     *
     * @param typeMirror the type
     * @return {@code true} if the type is an array, {@code false} otherwise
     */
    public boolean isArray(TypeMirror typeMirror) {
        return TypeKind.ARRAY.compareTo(typeMirror.getKind()) == 0;
    }

    /**
     * Checks if the type mirror is a 2D array.
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a 2D array, {@code false} otherwise
     */
    public boolean is2dArray(TypeMirror typeMirror) {
        return isArray(typeMirror) && isArray(arrayComponentType(typeMirror));
    }

    /**
     * a wrapper for {@link Types#isSameType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)}
     *
     * @param typeMirror  the type mirror
     * @param targetClass the target class
     * @return {@code true} if the type is same type as the target class, {@code false} otherwise
     */
    public boolean isSameType(TypeMirror typeMirror, Class<?> targetClass) {
        return types.isSameType(typeMirror, elements.getTypeElement(targetClass.getCanonicalName()).asType());
    }

    /**
     * a wrapper for {@link Types#isSameType(javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror)}
     *
     * @param typeMirror      the type
     * @param otherTypeMirror the other type
     * @return {@code true} if the type is same type as the target class, {@code false} otherwise
     */
    public boolean isSameType(TypeMirror typeMirror, TypeMirror otherTypeMirror) {
        return types.isSameType(typeMirror, otherTypeMirror);
    }

    /**
     * Returns the component type of an array.
     *
     * @param typeMirror the array type mirror
     * @return the component type of an array
     */
    public TypeMirror arrayComponentType(TypeMirror typeMirror) {
        return ((ArrayType) typeMirror).getComponentType();
    }

    /**
     * Returns the component type of an array <em>recursively</em>.
     *
     * @param typeMirror the array type mirror
     * @return the most inner component type of an array
     */
    public TypeMirror deepArrayComponentType(TypeMirror typeMirror) {
        TypeMirror type = ((ArrayType) typeMirror).getComponentType();
        return isArray(type) ? arrayComponentType(type) : type;
    }

    /**
     * Checks if the type is an {@link Enum}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is an {@link Enum}, {@code false} otherwise.
     */
    public boolean isEnum(TypeMirror typeMirror) {
        return !isNull(types.asElement(typeMirror))
                && !isPrimitive(typeMirror)
                && !isPrimitiveArray(typeMirror)
                && ElementKind.ENUM.compareTo(types.asElement(typeMirror).getKind()) == 0;
    }

    /**
     * Checks if the type is assignable of a {@link Collection}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Collection} or assignable from, {@code false} otherwise.
     */
    public boolean isCollection(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Collection.class);
    }

    /**
     * Checks if the type is assignable of a {@link Iterable}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Iterable} or assignable from, {@code false} otherwise.
     */
    public boolean isIterable(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Iterable.class);
    }

    /**
     * Checks if the type is assignable of a {@link Map}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Map} or assignable from, {@code false} otherwise.
     */
    public boolean isMap(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Map.class);
    }

    /**
     * Checks if the type is a {@link Float}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Float}, {@code false} otherwise.
     */
    public boolean isFloat(TypeMirror typeMirror) {
        return (isPrimitive(typeMirror) && "float".equals(typeMirror.toString()))
                || isSameType(typeMirror, Float.class);
    }

    /**
     * Checks if the type is a {@link Integer}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Integer}, {@code false} otherwise.
     */
    public boolean isInteger(TypeMirror typeMirror) {
        return (isPrimitive(typeMirror) && "int".equals(typeMirror.toString()))
                || isSameType(typeMirror, Integer.class);
    }

    /**
     * Checks if the type is a {@link Double}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Double}, {@code false} otherwise.
     */
    public boolean isDouble(TypeMirror typeMirror) {
        return (isPrimitive(typeMirror) && "double".equals(typeMirror.toString()))
                || isSameType(typeMirror, Double.class);
    }

    /**
     * Checks if the type is a {@link Boolean}
     *
     * @param typeMirror the type
     * @return {@code true} if the type is a {@link Boolean}, {@code false} otherwise.
     */
    public boolean isBoolean(TypeMirror typeMirror) {
        return (isPrimitive(typeMirror) && "boolean".equals(typeMirror.toString()))
                || isSameType(typeMirror, Boolean.class);
    }

    /**
     * Returns the first type argument of a class.
     *
     * @param typeMirror the type
     * @return the first type argument of the element
     */
    public TypeMirror firstTypeArgument(TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).getTypeArguments().get(FIRST_ARGUMENT);
    }

    /**
     * Returns the second type argument of a class.
     *
     * @param typeMirror the type
     * @return the second type argument of the element
     */
    public TypeMirror secondTypeArgument(TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).getTypeArguments().get(SECOND_ARGUMENT);
    }

    /**
     * Returns the package of the type.
     *
     * @param typeMirror the type
     * @return the package of the type
     */
    public String getPackage(TypeMirror typeMirror) {
        return elements.getPackageOf(types.asElement(typeMirror)).getSimpleName().toString();
    }

    /**
     * Returns the simple name of the type.
     *
     * @param typeMirror the type
     * @return the simple name of the type
     */
    public Name simpleName(TypeMirror typeMirror) {
        return types.asElement(typeMirror).getSimpleName();
    }

}
