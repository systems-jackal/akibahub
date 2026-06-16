package com.akibahub.controller;

import com.akibahub.dto.request.LoginRequest;
import com.akibahub.dto.request.RegisterRequest;
import com.akibahub.dto.response.ApiResponse;
import com.akibahub.model.User;
import com.akibahub.repository.UserRepository;
import com.akibahub.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validate: at least email OR phone number
        if ((request.getEmail() == null || request.getEmail().isEmpty()) &&
            (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Email or phone number is required", null));
        }

        // Validate: name is required
        if (request.getName() == null || request.getName().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Name is required", null));
        }

        // Check if email exists
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Email already registered", null));
            }
        }

        // Check if phone exists
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Phone number already registered", null));
            }
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setName(request.getName());
        user.setProvider("LOCAL");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(
            user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
            user.getId()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful",
            Map.of("token", token, "userId", user.getId())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = Optional.empty();

        // Try email first
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            userOpt = userRepository.findByEmail(request.getEmail());
        }

        // Try phone number
        if (userOpt.isEmpty() && request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            userOpt = userRepository.findByPhoneNumber(request.getPhoneNumber());
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "User not found with email or phone", null));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Invalid password", null));
        }

        String token = jwtUtil.generateToken(
            user.getEmail() != null ? user.getEmail() : user.getPhoneNumber(),
            user.getId()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful",
            Map.of("token", token, "userId", user.getId())));
    }
}