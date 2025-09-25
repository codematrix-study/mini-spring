package com.cw.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thisdcw
 * @date 2025年09月25日 15:27
 */
public class AppContext {

    public AppContext(String packageName) throws Exception {
        init(packageName);
    }

    private Map<String, Object> ioc = new HashMap<>();

    public Object getBean(String name) {
        return this.ioc.get(name);
    }

    public <T> T getBean(Class<T> beanType) {
        return this.ioc.values()
                .stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean)
                .findAny()
                .orElse(null);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.ioc.values()
                .stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean)
                .toList();
    }

    private List<Class<?>> scanPackage(String packageName) throws Exception {

        List<Class<?>> classes = new ArrayList<>();
        URL url = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = Path.of(url.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")) {
                    String replaceStr = absolutePath.toString().replace(File.separatorChar, '.');
                    int packageIndex = replaceStr.indexOf(packageName);
                    String substring = replaceStr.substring(packageIndex, replaceStr.length() - ".class".length());
                    try {
                        classes.add(Class.forName(substring));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });
        return classes;
    }

    protected void createBean(BeanDefinition beanDefinition) {

        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return;
        }
        doCreateBean(beanDefinition);
    }

    private void doCreateBean(BeanDefinition beanDefinition) {
        Constructor constructor = beanDefinition.getConstructor();
        Object o = null;
        try {
            o = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ioc.put(beanDefinition.getName(), o);
    }

    protected BeanDefinition wrapper(Class<?> type) {
        return new BeanDefinition(type);
    }

    protected boolean scanCreate(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    public void init(String packageName) throws Exception {

        scanPackage(packageName).stream().filter(this::scanCreate).map(this::wrapper).forEach(this::createBean);

    }
}
