package com.akibahub.group.dto.response;

import com.akibahub.group.model.Group;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private String createdBy;
    private BigDecimal groupWalletBalance;
    private BigDecimal totalContributed;
    private String currency;
    private int maxMembers;
    private int contributionThreshold;
    private LocalDateTime createdAt;

    public static GroupResponse from(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy())
                .groupWalletBalance(group.getGroupWalletBalance())
                .totalContributed(group.getTotalContributed())
                .currency(group.getCurrency())
                .maxMembers(group.getMaxMembers())
                .contributionThreshold(group.getContributionThreshold())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
