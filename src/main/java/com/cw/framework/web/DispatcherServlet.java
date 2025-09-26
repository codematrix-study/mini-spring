package com.cw.framework.web;

import com.alibaba.fastjson2.JSONObject;
import com.cw.framework.BeanPostProcessor;
import com.cw.framework.annotations.Component;
import com.cw.framework.annotations.Controller;
import com.cw.framework.annotations.RequestMapping;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thisdcw
 * @date 2025年09月26日 13:50
 */
@Component
public class DispatcherServlet extends HttpServlet implements BeanPostProcessor {


    private Map<String, WebHandler> handlerMap = new HashMap<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebHandler handler = findHandler(req);
        if (handler == null) {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write("<h1>你的请求没有对于的处理器</h1></br>" + req.getRequestURL().toString());
            return;
        }
        Object bean = handler.getControllerBean();
        try {
            Object result = handler.getMethod().invoke(bean);
            switch (handler.getResultType()) {
                case HTML -> {
                    resp.setContentType("text/html;charset=utf-8");
                    resp.getWriter().write(result.toString());
                }
                case JSON -> {
                    resp.setContentType("application/json;charset=utf-8");
                    resp.getWriter().write(JSONObject.toJSONString(result));
                }
                case LOCAL -> {
                    resp.setContentType("text/html;charset=utf-8");
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }


    }

    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        if (!bean.getClass().isAnnotationPresent(Controller.class)) {
            return bean;
        }
        Annotation annotation = bean.getClass().getAnnotation(RequestMapping.class);
        String classUrl = "";
        if (annotation != null) {
//            classUrl = annotation.getClass()
        }
        return bean;
    }
}
