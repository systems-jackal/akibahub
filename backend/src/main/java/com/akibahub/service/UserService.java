package com.akibahub.service;

import com.akibahub.dto.request.UserRegisterRequest;
import com.akibahub.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse register(UserRegisterRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();
}