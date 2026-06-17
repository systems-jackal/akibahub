package com.akibahub.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String fullName;
    private String email;
    private String phoneNumber;
}