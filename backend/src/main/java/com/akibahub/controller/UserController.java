package com.akibahub.controller;

import com.akibahub.dto.request.CreateUserRequest;
import com.akibahub.model.User;
import com.akibahub.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
}