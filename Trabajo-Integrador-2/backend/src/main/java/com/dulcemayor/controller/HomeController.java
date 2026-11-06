package com.dulcemayor.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class HomeController extends ApiControllerTemplate {
    @GetMapping("/")
    public Map<String, String> root() { return ejecutarEndpoint(() -> Map.of("application", "Dulce Mayor", "api", "/api/health")); }
}
