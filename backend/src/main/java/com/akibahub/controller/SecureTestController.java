package com.akibahub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecureTestController {

    @GetMapping("/secure/test")
    public Map<String, Object> test() {
        return Map.of(
                "service", "AkibaHub",
                "status", "secure endpoint working"
        );
    }
}