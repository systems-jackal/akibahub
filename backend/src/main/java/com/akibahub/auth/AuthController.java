package com.akibahub.auth;

import com.akibahub.auth.dto.AuthResponse;
import com.akibahub.auth.dto.LoginRequest;
import com.akibahub.auth.dto.RegisterRequest;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse res = authService.register(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true).message("Registration successful").data(res).build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse res = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true).message("Login successful").data(res).build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User.UserDto>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<User.UserDto>builder()
                .success(true).data(user.toDto()).build());
    }
}