package com.akibahub.payments.dto;

import com.akibahub.payments.entity.PendingPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentStatusResponse(
        String reference,
        String status,
        BigDecimal amount,
        String phone,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        BigDecimal balance
) {
    public static PaymentStatusResponse from(PendingPayment payment) {
        return from(payment, null);
    }

    public static PaymentStatusResponse from(PendingPayment payment, BigDecimal balance) {
        return new PaymentStatusResponse(
                payment.getReference(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getPhone(),
                payment.getExpiresAt(),
                payment.getCreatedAt(),
                payment.getCompletedAt(),
                balance
        );
    }
}
