package com.cw.framework.controller;

import com.cw.framework.annotations.Controller;
import com.cw.framework.annotations.RequestMapping;

/**
 * @author thisdcw
 * @date 2025年09月26日 13:47
 */
@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/a")
    public String hello() {
        return "hello";
    }
}
