package com.cw.framework;

/**
 * @author thisdcw
 * @date 2025年09月25日 17:51
 */
public interface BeanPostProcessor {

    default Object beforeInitializeBean(Object bean, String beanName) {
        return bean;
    }

    default Object afterInitializeBean(Object bean, String beanName) {
        return bean;
    }
}
