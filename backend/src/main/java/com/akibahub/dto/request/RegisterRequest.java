package com.akibahub.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String phoneNumber;
    private String name;
    private String password;
}