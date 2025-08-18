package com.company.dotaadminbackend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/css-counter")
    public String cssCounterTest() {
        return "css-counter-test";
    }
}