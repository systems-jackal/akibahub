package com.akibahub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthSuccessController {

    @GetMapping("/auth/oauth2/success")
    public String success() {
        return "OAuth login successful";
    }
}