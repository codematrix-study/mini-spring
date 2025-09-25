package com.cw.framework;

import com.cw.framework.annotations.Autowired;
import com.cw.framework.annotations.Component;
import com.cw.framework.annotations.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author thisdcw
 * @date 2025年09月25日 15:40
 */
public class BeanDefinition {

    private final String name;

    private final Constructor<?> constructor;

    private final Method method;

    private final List<Field> fields;

    private final Class<?> beanType;

    public BeanDefinition(Class<?> type) {
        //赋值beanType
        this.beanType = type;
        Component component = type.getDeclaredAnnotation(Component.class);
        //赋值bean名字
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            //构造器
            this.constructor = type.getConstructor();
            //带PostConstruct注解的方法
            this.method = Arrays.stream(type.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PostConstruct.class)).findFirst().orElse(null);
            //带Autowired的字段
            this.fields = Arrays.stream(type.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Autowired.class)).toList();
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

    public Method getPostConstructMethod() {
        return this.method;
    }

    public List<Field> getAutoFields() {
        return this.fields;
    }

    public Class<?> getBeanType() {
        return this.beanType;
    }
}
