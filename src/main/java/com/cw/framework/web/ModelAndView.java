package com.cw.framework.web;


import java.util.HashMap;
import java.util.Map;

/**
 * @author thisdcw
 * @date 2025年09月26日 21:34
 */
public class ModelAndView {

    private String view;

    private final Map<String, String> context = new HashMap<>();

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView() {
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
