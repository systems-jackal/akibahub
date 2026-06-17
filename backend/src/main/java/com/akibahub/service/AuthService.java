package com.akibahub.service;

import com.akibahub.model.User;
import com.akibahub.repository.UserRepository;
import com.akibahub.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String register(String email, String rawPassword, String fullName) {

        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProvider("LOCAL");
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user);

        return jwtService.generateToken(email);
    }

    public String login(String email, String rawPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(email);
    }
}