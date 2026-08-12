package com.example.nexus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("deployTestController")
public class TestController {

    @GetMapping("/api/deploy-test")
    public String test() {
        return "green";
    }
}
