package com.akibahub.group.dto.response;

import com.akibahub.group.model.GroupMember;
import com.akibahub.group.model.GroupRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupMemberResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String displayName;
    private GroupRole role;
    private LocalDateTime joinedAt;

    public static GroupMemberResponse from(GroupMember member) {
        return GroupMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .userEmail(member.getUserEmail())
                .displayName(member.getDisplayName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
