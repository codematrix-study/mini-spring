package com.cw.framework.sub;

import com.cw.framework.annotations.Autowired;
import com.cw.framework.annotations.Component;
import com.cw.framework.annotations.PostConstruct;

/**
 * @author thisdcw
 * @date 2025年09月25日 16:46
 */
@Component
public class Dog {

    @Autowired
    private Cat cat;

    @PostConstruct
    public void init() {
        System.out.println("初始化 属性=>" + cat);
    }
}
