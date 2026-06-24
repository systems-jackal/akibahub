package com.akibahub.audit.dto.request;

import com.akibahub.audit.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LedgerEventRequest {

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotBlank(message = "Actor ID is required")
    private String actorId;

    private String actorEmail;
    private String resourceType;
    private String resourceId;
    private String groupId;
    private BigDecimal amount;
    private String currency;
    private String metadata;
    private String ipAddress;

    @NotBlank(message = "Service source is required")
    private String serviceSource;
}
