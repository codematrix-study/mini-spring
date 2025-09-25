package com.cw.framework;

import java.lang.reflect.Constructor;

/**
 * @author thisdcw
 * @date 2025年09月25日 15:40
 */
public class BeanDefinition {

    private String name;

    private Constructor<?> constructor;

    public BeanDefinition(Class<?> type) {
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return this.name;
    }

    public Constructor<?> getConstructor() {
        return this.constructor;
    }

}
