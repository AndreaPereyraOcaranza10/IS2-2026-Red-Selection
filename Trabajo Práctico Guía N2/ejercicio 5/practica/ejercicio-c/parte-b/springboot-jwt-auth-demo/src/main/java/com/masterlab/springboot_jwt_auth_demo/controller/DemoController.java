package com.masterlab.springboot_jwt_auth_demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/api/demo")
    public Map<String, String> demo() {
        return Collections.singletonMap("message", "You accessed a protected endpoint!");
    }
}

