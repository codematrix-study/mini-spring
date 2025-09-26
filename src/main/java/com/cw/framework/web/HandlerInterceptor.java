package com.cw.framework.web;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author thisdcw
 * @date 2025年09月26日 21:57
 */
public interface HandlerInterceptor {

    /**
     * 在目标方法执行前调用
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器（Controller 方法对象）
     * @return true 继续执行, false 中断流程
     * @throws Exception 异常
     */
    default boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {
        return true;
    }

    /**
     * 在目标方法执行后，视图渲染前调用
     *
     * @param request      当前请求
     * @param response     当前响应
     * @param handler      处理器
     * @param modelAndView 返回的视图模型对象，可为 null
     * @throws Exception 异常
     */
    default void postHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler,
                            ModelAndView modelAndView) throws Exception {

    }

    /**
     * 在请求完成后调用（渲染视图后）
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器
     * @param ex       执行过程中抛出的异常，可为 null
     * @throws Exception 异常
     */
    default void afterCompletion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) throws Exception {

    }

}
