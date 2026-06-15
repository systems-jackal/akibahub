package com.akibahub.controller;

import com.akibahub.dto.request.LoginRequest;
import com.akibahub.dto.request.UserRegisterRequest;
import com.akibahub.dto.response.AuthResponse;
import com.akibahub.dto.response.UserResponse;
import com.akibahub.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}