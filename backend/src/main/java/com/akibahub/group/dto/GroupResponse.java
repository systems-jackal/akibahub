package com.akibahub.group.dto;

import com.akibahub.group.entity.Group;

import java.time.LocalDateTime;

/**
 * Safe group payload for the API. Group.createdBy is @JsonIgnore on the
 * entity (correctly — serializing the full User would leak passwordHash
 * history via Jackson if that ever changed), but the frontend still needs
 * createdById to decide whether to show the invite-code UI.
 */
public record GroupResponse(
        Long id,
        String name,
        String description,
        String rules,
        String inviteCode,
        Long createdById,
        LocalDateTime createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getRules(),
                group.getInviteCode(),
                group.getCreatedBy() != null ? group.getCreatedBy().getId() : null,
                group.getCreatedAt()
        );
    }
}
