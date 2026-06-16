package com.akibahub.service.impl;

import com.akibahub.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${payhero.api.key}")
    private String apiKey;

    @Override
    public String processDeposit(Long userId, BigDecimal amount) {
        System.out.println("💰 [PAYHERO] Deposit KES " + amount + " for User " + userId);
        return "PAYHERO-DEP-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public String processWithdrawal(Long userId, BigDecimal amount) {
        System.out.println("💰 [PAYHERO] Withdraw KES " + amount + " for User " + userId);
        return "PAYHERO-WIT-" + UUID.randomUUID().toString().substring(0, 8);
    }
}