package com.akibahub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthTestController {

    @GetMapping("/auth/test")
    public Map<String, String> test() {
        return Map.of(
                "status", "auth endpoint working",
                "service", "AkibaHub"
        );
    }
}