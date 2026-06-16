package com.akibahub.controller;

import com.akibahub.dto.request.LoginRequest;
import com.akibahub.dto.response.ApiResponse;
import com.akibahub.model.User;
import com.akibahub.repository.UserRepository;
import com.akibahub.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("GOOGLE".equals(user.getProvider())) {
            throw new RuntimeException("Please use Google OAuth to login");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", Map.of("token", token, "userId", user.getId())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getEmail().split("@")[0]);
        user.setProvider("LOCAL");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful", Map.of("token", token, "userId", user.getId())));
    }
}