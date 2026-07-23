package com.akibahub.user;

import com.akibahub.auth.RefreshTokenService;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.user.entity.User;
import com.akibahub.user.entity.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepo, PasswordEncoder encoder,
                       RefreshTokenService refreshTokenService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public User updateProfile(User user, Map<String, String> body) {
        if (body.containsKey("fullName")) user.setFullName(body.get("fullName"));
        if (body.containsKey("phoneNumber")) user.setPhoneNumber(body.get("phoneNumber"));
        return userRepo.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("Current and new password are required");
        }
        if (newPassword.length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters");
        }
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (encoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from your current password");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);

        // The access token the user is holding right now still works
        // until it naturally expires - it's a stateless JWT, now a short
        // one (see jwt.access-token-expiration-ms) - but revoking every
        // refresh token means nobody (including a possible attacker with
        // a stolen token) can mint a new access token afterward without
        // knowing the NEW password.
        refreshTokenService.revokeAllForUser(user.getId());
    }
}
