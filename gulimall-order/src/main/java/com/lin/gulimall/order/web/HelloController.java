package com.lin.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description TODO
 * @Date 2024/7/11 11:20
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String helloPage(@PathVariable("page") String page) {

        return page;
    }
}
