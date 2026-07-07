package com.akibahub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String login;   // can be phoneNumber or idNumber

    @NotBlank
    private String password;
}