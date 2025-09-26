package com.cw.framework.web;

import com.cw.framework.annotations.ResponseBody;

import java.lang.reflect.Method;

/**
 * @author thisdcw
 * @date 2025年09月26日 13:55
 */
public class WebHandler {

    private final Object controllerBean;

    private final Method method;

    private final ResultType resultType;

    public WebHandler(Object controllerBean, Method method) {
        this.controllerBean = controllerBean;
        this.method = method;
        this.resultType = resolveResultType(controllerBean, method);
    }

    private ResultType resolveResultType(Object bean, Method method) {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            return ResultType.JSON;
        }
        return ResultType.HTML;
    }

    public Object getControllerBean() {
        return controllerBean;
    }

    public Method getMethod() {
        return method;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public enum ResultType {
        JSON, HTML, LOCAL
    }
}
