package com.akibahub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String password;
}