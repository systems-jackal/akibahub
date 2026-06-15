package com.akibahub.service;

import com.akibahub.dto.request.LoginRequest;
import com.akibahub.dto.request.UserRegisterRequest;
import com.akibahub.dto.response.AuthResponse;
import com.akibahub.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(UserRegisterRequest request);

    AuthResponse login(LoginRequest request);
}