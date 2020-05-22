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

    public Messager getMessager() {
        return messager;
    }

    public Types getTypes() {
        return types;
    }

    public Elements getElements() {
        return elements;
    }

    public List<Element> getAnnotatedMethods(TypeMirror beanType, Class<? extends Annotation> annotation) {
        return getAnnotatedElements(beanType, annotation, element -> ElementKind.METHOD.equals(element.getKind()));
    }

    public List<Element> getAnnotatedFields(TypeMirror beanType, Class<? extends Annotation> annotation) {
        return getAnnotatedElements(beanType, annotation, element -> ElementKind.FIELD.equals(element.getKind()));
    }

    public List<Element> getAnnotatedElements(TypeMirror beanType, Class<? extends Annotation> annotation, Function<Element, Boolean> filter) {
        TypeElement typeElement = (TypeElement) types.asElement(beanType);

        final List<Element> methods = new ArrayList<>();

        List<Element> annotatedMethods = getAnnotatedElements(typeElement, annotation, filter);
        methods.addAll(annotatedMethods);

        return methods;
    }

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

    public String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public String smallFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public String lowerFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public boolean isAssignableFrom(Element element, Class<?> targetClass) {
        return types.isAssignable(element.asType(), types.getDeclaredType(elements.getTypeElement(targetClass.getCanonicalName())));
    }

    public boolean isAssignableFrom(TypeMirror element, Class<?> targetClass) {
        return types.isAssignable(element, types.getDeclaredType(elements.getTypeElement(targetClass.getCanonicalName())));
    }

    public <A extends Annotation> A findClassAnnotation(Element classElement, Class<A> annotation) {
        A result = classElement.getAnnotation(annotation);
        if (nonNull(result)) {
            return result;
        }
        TypeMirror superclass = ((TypeElement) classElement).getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return null;
        } else {
            return findClassAnnotation(types.asElement(superclass), annotation);
        }
    }


    public Optional<TypeMirror> findClassValueFromClassAnnotation(Element classElement, Class<? extends Annotation> annotation, String paramName) {
        Optional<TypeMirror> result = getClassValueFromAnnotation(classElement, annotation, paramName);
        if (result.isPresent()) {
            return result;
        }
        TypeMirror superclass = ((TypeElement) classElement).getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return Optional.empty();
        } else {
            return findClassValueFromClassAnnotation(types.asElement(superclass), annotation, paramName);
        }
    }

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


    public List<ExecutableElement> getElementMethods(Element element) {
        return element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());
    }

    public boolean isStringType(TypeMirror typeMirror) {
        TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
        return types.isAssignable(stringType, typeMirror);
    }

    /**
     * <p>wrapperType.</p>
     *
     * @param type a {@link TypeMirror} object.
     * @return a {@link TypeName} object.
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

    public boolean isPrimitive(TypeMirror typeMirror) {
        return typeMirror.getKind().isPrimitive();
    }

    /**
     * <p>isPrimitiveArray.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isPrimitiveArray(TypeMirror typeMirror) {
        return (isArray(typeMirror) && isPrimitive(arrayComponentType(typeMirror))) || isPrimitive2dArray(typeMirror);
    }

    private boolean isPrimitive2dArray(TypeMirror typeMirror) {
        return is2dArray(typeMirror) && isPrimitiveArray(arrayComponentType(typeMirror));
    }

    /**
     * <p>isArray.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isArray(TypeMirror typeMirror) {
        return TypeKind.ARRAY.compareTo(typeMirror.getKind()) == 0;
    }

    /**
     * <p>is2dArray.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean is2dArray(TypeMirror typeMirror) {
        return isArray(typeMirror) && isArray(arrayComponentType(typeMirror));
    }

    /**
     * <p>arrayComponentType.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link TypeMirror} object.
     */
    public TypeMirror arrayComponentType(TypeMirror typeMirror) {
        return ((ArrayType) typeMirror).getComponentType();
    }

    /**
     * <p>deepArrayComponentType.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link TypeMirror} object.
     */
    public TypeMirror deepArrayComponentType(TypeMirror typeMirror) {
        TypeMirror type = ((ArrayType) typeMirror).getComponentType();
        return isArray(type) ? arrayComponentType(type) : type;
    }

    /**
     * <p>isEnum.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isEnum(TypeMirror typeMirror) {
        return !isNull(types.asElement(typeMirror))
                && !isPrimitive(typeMirror)
                && !isPrimitiveArray(typeMirror)
                && ElementKind.ENUM.compareTo(types.asElement(typeMirror).getKind()) == 0;
    }

    /**
     * <p>isCollection.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isCollection(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Collection.class);
    }

    /**
     * <p>isIterable.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isIterable(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Iterable.class);
    }

    /**
     * <p>isMap.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a boolean.
     */
    public boolean isMap(TypeMirror typeMirror) {
        return !isPrimitive(typeMirror) && isAssignableFrom(typeMirror, Map.class);
    }

    /**
     * <p>firstTypeArgument.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link TypeMirror} object.
     */
    public TypeMirror firstTypeArgument(TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).getTypeArguments().get(FIRST_ARGUMENT);
    }

    /**
     * <p>secondTypeArgument.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link TypeMirror} object.
     */
    public TypeMirror secondTypeArgument(TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).getTypeArguments().get(SECOND_ARGUMENT);
    }

    /**
     * <p>getPackage.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link String} object.
     */
    public String getPackage(TypeMirror typeMirror) {
        return elements.getPackageOf(types.asElement(typeMirror)).getSimpleName().toString();
    }

    /**
     * <p>simpleName.</p>
     *
     * @param typeMirror a {@link TypeMirror} object.
     * @return a {@link Name} object.
     */
    public Name simpleName(TypeMirror typeMirror) {
        return types.asElement(typeMirror).getSimpleName();
    }

}
