package com.akibahub.savings.dto.response;

import com.akibahub.savings.model.Wallet;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletResponse {
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private String currency;
    private LocalDateTime createdAt;

    public static WalletResponse from(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .totalDeposited(wallet.getTotalDeposited())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
