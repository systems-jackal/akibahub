package com.akibahub.auth;

import com.akibahub.audit.AuditLogService;
import com.akibahub.auth.dto.AuthResponse;
import com.akibahub.auth.dto.LoginRequest;
import com.akibahub.auth.dto.RegisterRequest;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.user.entity.User;
import com.akibahub.user.entity.UserRepository;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
            throw new RuntimeException("Phone number already registered");
        }
        if (userRepo.existsByIdNumber(request.getIdNumber())) {
            throw new RuntimeException("ID number already registered");
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

        auditLog.logEvent("USER_REGISTERED", user);
        return issueAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // try phone first, then ID
        User user = userRepo.findByPhoneNumber(request.getLogin())
                .orElseGet(() -> userRepo.findByIdNumber(request.getLogin())
                        .orElseThrow(() -> new RuntimeException("Invalid credentials")));

        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        auditLog.logEvent("USER_LOGGED_IN", user);
        return issueAuthResponse(user);
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
        auditLog.logEvent("USER_LOGGED_OUT", user);
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