package com.cw.framework.web;

import com.alibaba.fastjson2.JSONObject;
import com.cw.framework.AppContext;
import com.cw.framework.BeanPostProcessor;
import com.cw.framework.annotations.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thisdcw
 * @date 2025年09月26日 13:50
 */
@Component
public class DispatcherServlet extends HttpServlet implements BeanPostProcessor {

    public static final Pattern PATTERN = Pattern.compile("cw\\{(.*?)}");

    private Map<String, WebHandler> handlerMap = new HashMap<>();


    @Autowired
    private List<HandlerInterceptor> interceptors;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebHandler handler = findHandler(req);
        if (handler == null) {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write("<h1>你的请求没有对于的处理器</h1></br>" + req.getRequestURL().toString());
            return;
        }
        try {
            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(req, resp, handler)) {
                    resp.setContentType("text/html;charset=utf-8");
                    resp.getWriter().write("<h1>你的请求被拦截</h1></br>" + req.getRequestURL().toString());
                    return;
                }
            }
            Object bean = handler.getControllerBean();
            Object[] args = resolveArgs(req, handler.getMethod());
            Object result = handler.getMethod().invoke(bean, args);
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.postHandle(req, resp, handler, null);
            }
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
                    ModelAndView view = (ModelAndView) result;
                    String v = view.getView();
                    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(v);
                    try (resourceAsStream) {
                        String html = new String(resourceAsStream.readAllBytes());
                        html = renderTemplate(html, view.getContext());
                        resp.setContentType("text/html;charset=utf-8");
                        resp.getWriter().write(html);
                    }
                }
            }
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.afterCompletion(req, resp, handler, null);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }


    }

    private String renderTemplate(String html, Map<String, String> context) {

        Matcher matcher = PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = context.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private Object[] resolveArgs(HttpServletRequest req, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String value = null;
            Param param = parameter.getAnnotation(Param.class);
            if (param != null) {
                value = req.getParameter(param.value());
            } else {
                value = req.getParameter(parameter.getName());
            }
            Class<?> type = parameter.getType();
            if (String.class.isAssignableFrom(type)) {
                args[i] = value;
            } else if (Integer.class.isAssignableFrom(type)) {
                args[i] = Integer.parseInt(value);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        if (!bean.getClass().isAnnotationPresent(Controller.class)) {
            return bean;
        }
        RequestMapping classRm = bean.getClass().getAnnotation(RequestMapping.class);
        String classUrl = classRm == null ? "" : classRm.value();
        Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(b -> b.isAnnotationPresent(RequestMapping.class))
                .forEach(m -> {
                    RequestMapping methodRm = m.getAnnotation(RequestMapping.class);
                    String key = classUrl.concat(methodRm.value());
                    WebHandler webHandler = new WebHandler(bean, m);
                    if (handlerMap.put(key, webHandler) != null) {
                        throw new RuntimeException("api路径重复 => " + key);
                    }
                });
        return bean;
    }
}
