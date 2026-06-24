package com.akibahub.group.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 100, message = "Group name must be 3-100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
