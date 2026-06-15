package com.akibahub.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String phoneNumber;
}