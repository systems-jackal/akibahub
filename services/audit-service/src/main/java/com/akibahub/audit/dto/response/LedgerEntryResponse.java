package com.akibahub.audit.dto.response;

import com.akibahub.audit.model.EventType;
import com.akibahub.audit.model.LedgerEntry;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LedgerEntryResponse {

    private String id;
    private EventType eventType;
    private String actorId;
    private String actorEmail;
    private String resourceType;
    private String resourceId;
    private String groupId;
    private BigDecimal amount;
    private String currency;
    private String metadata;
    private String serviceSource;
    private LocalDateTime createdAt;

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .eventType(entry.getEventType())
                .actorId(entry.getActorId())
                .actorEmail(entry.getActorEmail())
                .resourceType(entry.getResourceType())
                .resourceId(entry.getResourceId())
                .groupId(entry.getGroupId())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .metadata(entry.getMetadata())
                .serviceSource(entry.getServiceSource())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
