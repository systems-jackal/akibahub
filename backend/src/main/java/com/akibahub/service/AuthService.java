package com.akibahub.service;

import com.akibahub.dto.request.CreateUserRequest;
import com.akibahub.model.User;
import com.akibahub.repository.UserRepository;
import com.akibahub.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String register(CreateUserRequest request) {
        // Check if user exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider("LOCAL");
        
        // Generate a unique member code
        String memberCode = generateMemberCode();
        user.setMemberCode(memberCode);
        
        userRepository.save(user);
        
        return jwtService.generateToken(user.getEmail());
    }

    public String login(String email, String password) {
        // Use AuthenticationManager for proper authentication
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        // If authentication successful, generate token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtService.generateToken(user.getEmail());
    }

    /**
     * Generates a unique member code in format: MBR-XXXXX-XXXXX
     * Example: MBR-A1B2C-D3E4F
     */
    private String generateMemberCode() {
        String prefix = "MBR";
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String code = prefix + "-" + uniqueId.substring(0, 4) + "-" + uniqueId.substring(4);
        
        // Check if code already exists (very unlikely but just in case)
        while (userRepository.findByMemberCode(code).isPresent()) {
            uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            code = prefix + "-" + uniqueId.substring(0, 4) + "-" + uniqueId.substring(4);
        }
        
        return code;
    }
}