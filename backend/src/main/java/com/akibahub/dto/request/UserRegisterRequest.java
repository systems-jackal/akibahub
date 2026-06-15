package com.akibahub.dto.request;

import lombok.*;

@Getter
@Setter
public class UserRegisterRequest {

    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
}