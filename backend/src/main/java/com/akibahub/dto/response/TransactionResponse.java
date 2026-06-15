package com.akibahub.dto.response;

import com.akibahub.model.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponse {

    private Long id;
    private Long userId;
    private Long groupId;
    private double amount;
    private TransactionType type;
    private LocalDateTime createdAt;
}