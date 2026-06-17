package com.akibahub.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayHeroCallbackRequest {

    private String transactionId;   // PayHero unique ID
    private String reference;       // your internal reference
    private BigDecimal amount;
    private String phoneNumber;
    private String status;          // SUCCESS / FAILED
}