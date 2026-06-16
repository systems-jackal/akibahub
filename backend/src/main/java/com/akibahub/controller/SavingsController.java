package com.akibahub.controller;

import com.akibahub.dto.response.ApiResponse;
import com.akibahub.security.JwtUtil;
import com.akibahub.service.PersonalSavingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/savings")
public class SavingsController {
    private final PersonalSavingsService savingsService;
    private final JwtUtil jwtUtil;

    public SavingsController(PersonalSavingsService savingsService, JwtUtil jwtUtil) {
        this.savingsService = savingsService;
        this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        Long userId = getUserId(request);
        BigDecimal amount = new BigDecimal(payload.get("amount"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Deposit successful", savingsService.deposit(userId, amount)));
    }

    @GetMapping("/balance")
    public ResponseEntity<?> balance(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Balance fetched", savingsService.getBalance(userId)));
    }
}