package com.akibahub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Pattern(regexp = "^\\+254\\d{9}$", message = "Phone must be +254xxxxxxxxx")
    private String phoneNumber;

    @NotBlank @Size(min = 6)
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank @Pattern(regexp = "^\\d{8}$", message = "ID number must be 8 digits")
    private String idNumber;
}