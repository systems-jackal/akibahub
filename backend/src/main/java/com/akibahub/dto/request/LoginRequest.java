package com.akibahub.dto.request;

import lombok.*;

@Getter
@Setter
public class LoginRequest {
    private String phoneNumber;
    private String password;
}