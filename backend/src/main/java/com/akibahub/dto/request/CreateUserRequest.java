package com.akibahub.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;  // CRITICAL - WAS MISSING!
}