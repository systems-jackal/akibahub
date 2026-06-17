package com.akibahub.controller;

import com.akibahub.security.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OAuthSuccessController {

    private final JwtService jwtService;

    public OAuthSuccessController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/auth/oauth2/success")
    public Map<String, String> success(org.springframework.security.core.Authentication auth) {

        String email = auth.getName();
        String token = jwtService.generateToken(email);

        return Map.of("token", token);
    }
}