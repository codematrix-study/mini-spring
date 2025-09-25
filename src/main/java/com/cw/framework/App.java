package com.cw.framework;

/**
 * @author thisdcw
 * @date 2025年09月25日 15:59
 */
public class App {
    public static void main(String[] args) throws Exception {
        AppContext ico = new AppContext("com.cw.framework");
        Object cat = ico.getBean("myDog");
        System.out.println(cat);
    }
}
