package com.akibahub.auth;

import com.akibahub.audit.AuditLogService;
import com.akibahub.auth.dto.AuthResponse;
import com.akibahub.auth.dto.LoginRequest;
import com.akibahub.auth.dto.RegisterRequest;
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
    private final AuditLogService auditLog;

    public AuthService(UserRepository userRepo, WalletRepository walletRepo,
                       PasswordEncoder encoder, JwtUtil jwtUtil, AuditLogService auditLog) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
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
        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return AuthResponse.builder().token(token).user(user.toDto()).build();
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
        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return AuthResponse.builder().token(token).user(user.toDto()).build();
    }

    public User getUserByPhone(String phone) {
        return userRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}