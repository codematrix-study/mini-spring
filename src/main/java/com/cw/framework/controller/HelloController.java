package com.cw.framework.controller;

import com.cw.framework.annotations.*;
import com.cw.framework.sub.User;
import com.cw.framework.web.ModelAndView;

/**
 * @author thisdcw
 * @date 2025年09月26日 13:47
 */
@Controller
@Component
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/a")
    public String hello(@Param("name") String name, @Param("age") Integer age) {
        return String.format("<h1>hello</h1></br> name=%s age=%s", name, age);
    }

    @RequestMapping("/c")
    public ModelAndView html(@Param("name") String name, @Param("age") Integer age) {

        ModelAndView mv = new ModelAndView();
        mv.setView("index.html");
        mv.getContext().put("name", name);
        mv.getContext().put("age", age.toString());
        return mv;
    }

    @RequestMapping("/b")
    @ResponseBody
    public User json(@Param("name") String name, @Param("age") Integer age) {

        User user = new User();
        user.setName(name);
        user.setAge(age);

        return user;
    }


}
