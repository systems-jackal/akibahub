package com.akibahub.dto.request;

import lombok.*;

@Getter
@Setter
public class CreateTransactionRequest {

    private Long userId;
    private Long groupId; // optional
    private double amount;
    private String type; // DEPOSIT or WITHDRAW
}