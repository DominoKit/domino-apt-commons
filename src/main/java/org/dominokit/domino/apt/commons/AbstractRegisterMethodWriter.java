package org.dominokit.domino.apt.commons;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Collection;

import static java.util.Objects.nonNull;

/**
 * Abstract class for all register methods writers
 *
 * @param <E> any thing extends {@link ItemEntry}
 * @param <I> the items to register
 */
public abstract class AbstractRegisterMethodWriter<E extends AbstractRegisterMethodWriter.ItemEntry, I> {

    private final TypeSpec.Builder clientModuleTypeBuilder;

    public AbstractRegisterMethodWriter(TypeSpec.Builder clientModuleTypeBuilder) {
        this.clientModuleTypeBuilder = clientModuleTypeBuilder;
    }

    /**
     * Write the method to register a list of items
     *
     * @param items the items to register
     */
    public void write(Collection<I> items) {
        if (!items.isEmpty()) {
            MethodSpec.Builder registerViewsMethodBuilder = MethodSpec.methodBuilder(methodName())
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);

            if (nonNull(registryClass())) {
                registerViewsMethodBuilder.addParameter(registryClass(), "registry");
            }
            items.stream().map(this::parseEntry)
                    .forEach(e -> registerItem(e, registerViewsMethodBuilder));
            clientModuleTypeBuilder.addMethod(registerViewsMethodBuilder.build());
        }
    }

    /**
     * @return the method name
     */
    protected abstract String methodName();

    /**
     * @return the registry class
     */
    protected abstract Class<?> registryClass();

    /**
     * Abstract method to write the line for registering the entry
     *
     * @param entry         the entry to register
     * @param methodBuilder the method builder to add the line to it
     */
    protected abstract void registerItem(E entry, MethodSpec.Builder methodBuilder);

    /**
     * Converts the item to {@link ItemEntry}
     *
     * @param item the item to parse
     * @return the item entry
     */
    protected abstract E parseEntry(I item);

    /**
     * A marker interface to indicate that an item is an entry to be registered
     */
    public interface ItemEntry {
    }
}
