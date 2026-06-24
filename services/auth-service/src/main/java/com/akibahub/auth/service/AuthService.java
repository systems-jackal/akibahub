package com.akibahub.auth.service;

import com.akibahub.auth.model.Role;
import com.akibahub.auth.model.User;
import com.akibahub.auth.repository.UserRepository;
import com.akibahub.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public User processOAuthUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String displayName = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setGoogleId(googleId);
                    existing.setDisplayName(displayName);
                    existing.setAvatarUrl(avatarUrl);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .googleId(googleId)
                            .displayName(displayName)
                            .avatarUrl(avatarUrl)
                            .roles(Set.of(Role.USER))
                            .build();
                    log.info("New user registered: {}", email);
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setRefreshToken(token);
        user.setRefreshTokenExpiry(
                LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        userRepository.save(user);
        return token;
    }

    @Transactional
    public TokenPair refreshTokens(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new IllegalArgumentException("Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);
        return new TokenPair(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            log.info("User logged out: {}", userId);
        });
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
