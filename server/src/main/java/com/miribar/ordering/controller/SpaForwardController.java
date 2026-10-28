package com.miribar.ordering.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA 路由转发 - 将非API请求转发到前端 index.html
 * 仅在 production 模式（Vue build 后的静态文件由 Spring Boot 托管时）生效
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {
        "/customer", "/customer/**",
        "/kitchen", "/kitchen/**",
        "/admin", "/admin/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
