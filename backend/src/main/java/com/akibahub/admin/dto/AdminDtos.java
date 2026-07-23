package com.akibahub.admin.dto;

import com.akibahub.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminDtos {

    public record PlatformStats(
            long totalUsers,
            long totalGroups,
            long totalProposals,
            long pendingProposals,
            BigDecimal totalPersonalSavings,
            BigDecimal totalGroupSavings
    ) {}

    public record AdminUserSummary(
            Long id,
            String fullName,
            String phoneNumber,
            String idNumber,
            User.Role role,
            User.AccountStatus status,
            LocalDateTime createdAt
    ) {}

    public record AdminGroupSummary(
            Long id,
            String name,
            long memberCount,
            BigDecimal balance,
            LocalDateTime createdAt
    ) {}

    public record AuditLogEntry(
            Long id,
            String eventType,
            String payload,
            LocalDateTime createdAt
    ) {}
}