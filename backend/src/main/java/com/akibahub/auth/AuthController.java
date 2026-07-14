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

import java.util.Map;

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

    // Public on purpose: by the time a client needs to refresh, its
    // access token may well have already expired, so this can't require
    // the normal Bearer-token auth. The refresh token itself, validated
    // inside authService.refresh(...), is what proves who's asking.
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> body) {
        AuthResponse res = authService.refresh(body.get("refreshToken"));
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true).message("Token refreshed").data(res).build());
    }

    // Requires a valid access token, unlike the endpoints above - this is
    // an authenticated user asking to revoke their own sessions.
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        authService.logout(user);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Logged out").build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User.UserDto>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<User.UserDto>builder()
                .success(true).data(user.toDto()).build());
    }
}