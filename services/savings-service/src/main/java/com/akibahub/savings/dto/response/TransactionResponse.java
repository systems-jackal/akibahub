package com.akibahub.savings.dto.response;

import com.akibahub.savings.model.Transaction;
import com.akibahub.savings.model.TransactionStatus;
import com.akibahub.savings.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .description(tx.getDescription())
                .paymentReference(tx.getPaymentReference())
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .build();
    }
}
