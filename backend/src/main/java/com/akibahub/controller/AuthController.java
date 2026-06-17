package com.akibahub.controller;

import com.akibahub.dto.request.LoginRequest;
import com.akibahub.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        return Map.of("token", token);
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "auth endpoint working");
    }
}