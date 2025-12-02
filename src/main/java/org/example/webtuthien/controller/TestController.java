package org.example.webtuthien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/payment/momo")
public class TestController {
    
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "MoMo routes are working! Current time: " + System.currentTimeMillis();
    }
}
