package com.akibahub.service;

import java.math.BigDecimal;

public interface PaymentService {
    String processDeposit(Long userId, BigDecimal amount);
    String processWithdrawal(Long userId, BigDecimal amount);
}