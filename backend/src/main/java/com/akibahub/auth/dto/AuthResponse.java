package com.akibahub.auth.dto;

import com.akibahub.user.entity.User;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private User.UserDto user;
}