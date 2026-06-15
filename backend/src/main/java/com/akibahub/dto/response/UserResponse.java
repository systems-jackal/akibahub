package com.akibahub.dto.response;

import com.akibahub.model.Role;
import lombok.*;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Role role;
}