package com.akibahub.auth;

import com.akibahub.auth.entity.RefreshToken;
import com.akibahub.auth.entity.RefreshTokenRepository;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh tokens exist so the access token (the JWT that goes on every
 * request) can have a short lifetime - minutes, not hours - while the
 * user still doesn't have to log in again constantly. The refresh token
 * is opaque (not a JWT), long-lived, stored server-side (as a hash, not
 * the raw value), and can actually be revoked - which is the whole
 * problem with a stateless-only JWT design: there's no way to invalidate
 * a token before it naturally expires. This service is where that
 * capability lives.
 *
 * Rotation: every time a refresh token is used, the old one is revoked
 * and a new one is issued. This means a stolen refresh token is only
 * useful until the legitimate user's next natural refresh - at which
 * point the thief's copy stops working and (if you add detection for it
 * later) reuse of an already-rotated token is a strong signal the token
 * was stolen.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final long expirationDays;
    private static final SecureRandom RANDOM = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repo,
                                @Value("${jwt.refresh-token-expiration-days:30}") long expirationDays) {
        this.repo = repo;
        this.expirationDays = expirationDays;
    }

    @Transactional
    public String issue(User user) {
        String rawToken = generateRawToken();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                .revoked(false)
                .build();
        repo.save(token);
        return rawToken;
    }

    /**
     * Validates a raw refresh token, revokes it (rotation), and issues a
     * replacement for the same user. Throws ForbiddenException if the
     * token is unknown, already revoked, or expired.
     */
    @Transactional
    public RotationResult validateAndRotate(String rawToken) {
        RefreshToken existing = repo.findByTokenHashAndRevokedFalse(hash(rawToken))
                .orElseThrow(() -> new ForbiddenException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("Refresh token has expired");
        }

        existing.setRevoked(true);
        repo.save(existing);

        String newRawToken = issue(existing.getUser());
        return new RotationResult(existing.getUser(), newRawToken);
    }

    /**
     * Revokes every active refresh token for a user - used on password
     * change (so a stolen token stops working the moment the legitimate
     * user notices and changes their password) and on explicit logout.
     * This is a "log out everywhere" operation; per-device logout would
     * need the client to also send back which specific refresh token to
     * revoke, which is a reasonable future enhancement but adds UI
     * complexity (device list, etc.) this pass doesn't attempt.
     */
    @Transactional
    public void revokeAllForUser(Long userId) {
        repo.findByUserIdAndRevokedFalse(userId).forEach(t -> t.setRevoked(true));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record RotationResult(User user, String newRawToken) {}
}