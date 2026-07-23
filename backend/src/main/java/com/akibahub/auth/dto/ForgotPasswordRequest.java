package com.akibahub.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Identity-based password reset for the current product stage.
 * Users prove ownership with the two unique identifiers collected at
 * registration (phone + national ID). When SMS OTP is wired later,
 * this can become "request reset → OTP → set password" without
 * changing the final set-password step much.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ForgotPasswordRequest {
    @NotBlank @Pattern(regexp = "^\\+254\\d{9}$", message = "Phone must be +254xxxxxxxxx")
    private String phoneNumber;

    @NotBlank @Pattern(regexp = "^\\d{8}$", message = "ID number must be 8 digits")
    private String idNumber;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
