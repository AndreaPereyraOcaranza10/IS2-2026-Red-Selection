package com.masterlab.springboot_jwt_auth_demo.dto;

public class AuthResponse {
    private String message;
    private String token;

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
    public String getMessage() { return message; }
    public String getToken() { return token; }
}