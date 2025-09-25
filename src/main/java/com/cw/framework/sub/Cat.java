package com.cw.framework.sub;

import com.cw.framework.annotations.Autowired;
import com.cw.framework.annotations.Component;
import com.cw.framework.annotations.PostConstruct;

/**
 * @author thisdcw
 * @date 2025年09月25日 16:36
 */
@Component
public class Cat {

    @Autowired
    private Dog dog;
    @PostConstruct
    public void init() {
        System.out.println("初始化 属性=>" + dog);
    }
}
