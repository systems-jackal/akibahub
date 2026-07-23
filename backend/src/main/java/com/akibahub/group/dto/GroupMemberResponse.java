package com.akibahub.group.dto;

import com.akibahub.group.entity.GroupMember;

import java.time.LocalDateTime;

/**
 * Member list payload. GroupMember.user is @JsonIgnore on the entity, so
 * returning the raw entity left the frontend with only {id, joinedAt} —
 * every member rendered as "Unknown".
 */
public record GroupMemberResponse(
        Long id,
        LocalDateTime joinedAt,
        UserSummary user
) {
    public record UserSummary(Long id, String fullName, String phoneNumber) {}

    public static GroupMemberResponse from(GroupMember member) {
        var user = member.getUser();
        return new GroupMemberResponse(
                member.getId(),
                member.getJoinedAt(),
                new UserSummary(user.getId(), user.getFullName(), user.getPhoneNumber())
        );
    }
}
