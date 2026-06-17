package com.akibahub.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    private Long userId;
    private BigDecimal amount;

    // This will later become PayHero transaction ID
    private String reference;
}