package com.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 销售系统主页
     */
    @GetMapping("/sales")
    public String sales() {
        return "index";
    }

    /**
     * 销售系统首页
     */
    @GetMapping("/sales/home")
    public String home() {
        return "index";
    }
}
