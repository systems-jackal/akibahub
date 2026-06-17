package com.akibahub.controller;

import com.akibahub.model.User;
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

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        return Map.of("token", authService.register(user));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {
        return Map.of(
                "token",
                authService.login(req.get("email"), req.get("password"))
        );
    }
}