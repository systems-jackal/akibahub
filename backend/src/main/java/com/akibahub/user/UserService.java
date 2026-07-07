package com.akibahub.user;

import com.akibahub.user.entity.User;
import com.akibahub.user.entity.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    public User updateProfile(User user, Map<String, String> body) {
        if (body.containsKey("fullName")) user.setFullName(body.get("fullName"));
        if (body.containsKey("phoneNumber")) user.setPhoneNumber(body.get("phoneNumber"));
        // idNumber change can be added later with proper validation
        return userRepo.save(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);
    }
}