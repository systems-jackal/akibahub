package com.akibahub.group.dto.request;

import com.akibahub.group.model.GroupRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotNull(message = "Target user ID is required")
    private String targetUserId;

    @NotNull(message = "New role is required")
    private GroupRole newRole;
}
