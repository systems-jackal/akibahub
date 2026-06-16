package com.akibahub.service;

import com.akibahub.model.PersonalWallet;

import java.math.BigDecimal;

public interface PersonalSavingsService {
    PersonalWallet deposit(Long userId, BigDecimal amount);
    BigDecimal getBalance(Long userId);
}