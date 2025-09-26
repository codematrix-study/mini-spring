package com.cw.framework.web;

import com.cw.framework.annotations.Autowired;
import com.cw.framework.annotations.Component;
import com.cw.framework.annotations.PostConstruct;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.logging.LogManager;

/**
 * @author thisdcw
 * @date 2025年09月26日 9:56
 */
@Component
public class TomcatServer {

    @Autowired
    private DispatcherServlet dispatcherServlet;

    @PostConstruct
    public void start() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        int port = 8080;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        tomcat.addServlet(contextPath, "dispatcherServlet", dispatcherServlet);

        context.addServletMappingDecoded("/*", "dispatcherServlet");
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException("Tomcat start failed", e);
        }
        System.out.println("Tomcat started at " + port);
    }
}
