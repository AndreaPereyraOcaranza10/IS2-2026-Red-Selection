package com.masterlab.springboot_jwt_auth_demo.controller;

import com.masterlab.springboot_jwt_auth_demo.dto.LoginRequest;
import com.masterlab.springboot_jwt_auth_demo.dto.AuthResponse;
import com.masterlab.springboot_jwt_auth_demo.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Hardcoded user
        if ("admin".equals(request.getUsername()) &&
                "password".equals(request.getPassword())) {

            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse("Login successful", token));
        }

        return ResponseEntity.status(401).body("Invalid credentials!");
    }
}