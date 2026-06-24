package com.akibahub.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitiatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.00", message = "Minimum amount is KES 10")
    @DecimalMax(value = "150000.00", message = "Maximum amount is KES 150,000")
    private BigDecimal amount;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(07|01|\\+2547|\\+2541)\\d{8}$",
             message = "Invalid Kenyan phone number")
    private String phoneNumber;

    // For group contributions only
    private String groupId;
}
