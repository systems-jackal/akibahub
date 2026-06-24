package com.akibahub.proposal.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProposalRequest {

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be 5-200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least KES 1")
    private BigDecimal amount;

    @NotBlank(message = "Recipient phone is required")
    @Pattern(regexp = "^(07|01|\\+2547|\\+2541)\\d{8}$",
             message = "Invalid Kenyan phone number")
    private String recipientPhone;

    @Size(max = 500)
    private String recipientDescription;
}
