package com.akibahub.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/secure")
public class SecureController {

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "secure endpoint working");
    }
}