package com.cw.framework;

import com.cw.framework.annotations.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
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

    public AppContext(Class<?> clazz) throws Exception {
        init(clazz.getPackage().getName());
    }

    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private final Map<String, Object> ioc = new HashMap<>();

    private final Map<String, Object> loadingIoc = new HashMap<>();

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    /**
     * 通过bean名字获取bean
     *
     * @param name beanName
     * @return bean
     * @author thisdcw
     * @date 2025/09/25 17:36
     */
    public Object getBean(String name) {
        if (name == null) {
            return null;
        }
        Object o = this.ioc.get(name);
        if (o != null) {
            return o;
        }
        if (this.beanDefinitionMap.containsKey(name)) {
            return createBean(beanDefinitionMap.get(name));
        }
        return null;
    }

    /**
     * 通过beanType获取bean
     *
     * @param beanType beanType
     * @param <T>
     * @return
     * @author thisdcw
     * @date 2025/09/25 17:37
     */
    public <T> T getBean(Class<T> beanType) {
        String beanName = this.beanDefinitionMap.values()
                .stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(beanName);
    }

    /**
     * 通过beanType获取bean list
     *
     * @param beanType beanType
     * @param <T>
     * @return
     * @author thisdcw
     * @date 2025/09/25 17:37
     */
    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values()
                .stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T) bean)
                .toList();
    }

    /**
     * 扫描包
     *
     * @param packageName 包名
     * @return
     * @throws Exception
     * @author thisdcw
     * @date 2025/09/25 17:38
     */
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

    /**
     * 创建bean
     *
     * @param beanDefinition
     * @return
     * @author thisdcw
     * @date 2025/09/25 17:38
     */
    protected Object createBean(BeanDefinition beanDefinition) {

        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)) {
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowireBean(bean, beanDefinition);
            bean = initializeBean(bean, beanDefinition);
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bean;
    }

    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException, IllegalAccessException {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }
        if (beanDefinition.getPostConstructMethod() != null) {
            beanDefinition.getPostConstructMethod().invoke(bean);
        }
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.afterInitializeBean(bean, beanDefinition.getName());
        }
        return bean;
    }

    /**
     * 自动注入bean
     *
     * @param o
     * @param beanDefinition
     * @author thisdcw
     * @date 2025/09/25 17:38
     */
    private void autowireBean(Object o, BeanDefinition beanDefinition) {
        for (Field autoField : beanDefinition.getAutoFields()) {
            autoField.setAccessible(true);
            try {
                //处理List注入
                if (List.class.isAssignableFrom(autoField.getType())) {
                    //获取List的泛型类型
                    Type genericType = autoField.getGenericType();
                    //检查是否参数化类型
                    if (genericType instanceof ParameterizedType) {
                        //取出参数类型
                        Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                        //判断是否是class
                        if (actualType instanceof Class) {
                            List beans = getBeans((Class) actualType);
                            autoField.set(o, beans);
                        }
                    }
                } else {
                    //普通注入
                    autoField.set(o, getBean(autoField.getType()));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(autoField.getName() + "注入失败");
            }
        }
    }

    /**
     * 包装bean
     *
     * @param type
     * @return
     * @author thisdcw
     * @date 2025/09/25 17:39
     */
    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("duplicate bean name: " + beanDefinition.getName());
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    /**
     * 创建扫描器
     *
     * @param clazz
     * @return
     * @author thisdcw
     * @date 2025/09/25 17:39
     */
    protected boolean canCreate(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    /**
     * 初始化
     *
     * @param packageName
     * @throws Exception
     * @author thisdcw
     * @date 2025/09/25 17:40
     */
    public void init(String packageName) throws Exception {
        scanPackage(packageName).stream().filter(this::canCreate).forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitionMap.values().forEach(this::createBean);
    }

    private void initBeanPostProcessor() {
        beanDefinitionMap.values()
                .stream()
                .filter(bd ->
                        BeanPostProcessor.class.isAssignableFrom(bd.getBeanType())
                ).map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(beanPostProcessors::add);
    }
}
