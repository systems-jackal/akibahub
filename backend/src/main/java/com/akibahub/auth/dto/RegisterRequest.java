package com.akibahub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    private String phoneNumber;
    @NotBlank @Size(min = 6)
    private String password;
    @NotBlank
    private String fullName;
}