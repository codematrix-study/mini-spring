package com.cw.framework;

import com.cw.framework.annotations.Component;

/**
 * @author thisdcw
 * @date 2025年09月25日 18:02
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object beforeInitializeBean(Object bean, String beanName) {
        System.out.println("beforeInitializeBean ==> " + beanName);
        return bean;
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println("afterInitializeBean ==> " + beanName);
        return bean;
    }
}
