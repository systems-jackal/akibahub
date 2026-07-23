package com.akibahub.auth;

import com.akibahub.audit.AuditLogService;
import com.akibahub.auth.dto.AuthResponse;
import com.akibahub.auth.dto.ForgotPasswordRequest;
import com.akibahub.auth.dto.LoginRequest;
import com.akibahub.auth.dto.RegisterRequest;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.user.entity.User;
import com.akibahub.user.entity.UserRepository;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final WalletRepository walletRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLog;

    public AuthService(UserRepository userRepo, WalletRepository walletRepo,
                       PasswordEncoder encoder, JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService, AuditLogService auditLog) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.auditLog = auditLog;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone number already registered");
        }
        if (userRepo.existsByIdNumber(request.getIdNumber())) {
            throw new ConflictException("ID number already registered");
        }
        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .idNumber(request.getIdNumber())
                .passwordHash(encoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();
        user = userRepo.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .type(Wallet.WalletType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepo.save(wallet);

        // Never audit the raw User entity — Jackson would serialize
        // passwordHash into audit_log.payload. toDto() is the safe shape.
        auditLog.logEvent("USER_REGISTERED", user.toDto());
        return issueAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // try phone first, then ID
        User user = userRepo.findByPhoneNumber(request.getLogin())
                .orElseGet(() -> userRepo.findByIdNumber(request.getLogin())
                        .orElseThrow(() -> new BadRequestException("Invalid credentials")));

        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }
        auditLog.logEvent("USER_LOGGED_IN", user.toDto());
        return issueAuthResponse(user);
    }

    /**
     * Resets a password when the caller proves they know both unique
     * identifiers collected at registration (phone + national ID).
     * Both must match the same account — a generic error is returned
     * otherwise so callers cannot probe which field was wrong.
     * All refresh tokens are revoked so any stolen sessions die with
     * the old password.
     */
    @Transactional
    public void resetPassword(ForgotPasswordRequest request) {
        User user = userRepo.findByPhoneNumber(request.getPhoneNumber())
                .filter(u -> u.getIdNumber().equals(request.getIdNumber()))
                .orElseThrow(() -> new BadRequestException(
                        "Phone number and ID number do not match any account"));

        if (encoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from your current password");
        }

        user.setPasswordHash(encoder.encode(request.getNewPassword()));
        userRepo.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
        auditLog.logEvent("PASSWORD_RESET", Map.of("userId", user.getId()));
    }

    /**
     * Exchanges a still-valid, not-yet-expired refresh token for a new
     * access token and a new refresh token (rotation - see
     * RefreshTokenService for why the old one is revoked in the process).
     */
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ForbiddenException("Refresh token is required");
        }
        RefreshTokenService.RotationResult result = refreshTokenService.validateAndRotate(refreshToken);
        String accessToken = jwtUtil.generateToken(result.user().getId());
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(result.newRawToken())
                .user(result.user().toDto())
                .build();
    }

    /**
     * Revokes every refresh token for this user - the access token they
     * already hold keeps working until it naturally expires (it's
     * stateless, minutes away by design - see jwt.access-token-expiration-ms),
     * but they can no longer mint a new one after that without logging
     * in again.
     */
    public void logout(User user) {
        refreshTokenService.revokeAllForUser(user.getId());
        auditLog.logEvent("USER_LOGGED_OUT", user.toDto());
    }

    public User getUserByPhone(String phone) {
        return userRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AuthResponse issueAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(user.toDto())
                .build();
    }
}