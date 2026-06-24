package com.akibahub.auth.controller;

import com.akibahub.auth.dto.request.RefreshTokenRequest;
import com.akibahub.auth.dto.response.AuthResponse;
import com.akibahub.auth.model.Role;
import com.akibahub.auth.model.User;
import com.akibahub.auth.repository.UserRepository;
import com.akibahub.auth.security.JwtService;
import com.akibahub.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        User user = authService.processOAuthUser(oAuth2User);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        return ResponseEntity.ok(buildAuthResponse(user, accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthService.TokenPair tokens = authService.refreshTokens(request.getRefreshToken());
        String userId = jwtService.extractUserId(tokens.accessToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(buildAuthResponse(user, tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.extractUserId(token);
        authService.logout(userId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtService.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }
        Claims claims = jwtService.validateAndExtract(token);
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", claims.getSubject(),
                "email", claims.get("email"),
                "roles", claims.get("roles")
        ));
    }

    @GetMapping("/introspect/{token}")
    public ResponseEntity<Map<String, Object>> introspect(@PathVariable String token) {
        if (!jwtService.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("active", false));
        }
        Claims claims = jwtService.validateAndExtract(token);
        return ResponseEntity.ok(Map.of(
                "active", true,
                "sub", claims.getSubject(),
                "email", claims.get("email"),
                "roles", claims.get("roles"),
                "exp", claims.getExpiration().getTime()
        ));
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .build();
    }
}
