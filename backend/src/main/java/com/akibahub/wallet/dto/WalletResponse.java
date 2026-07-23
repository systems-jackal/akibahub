package com.akibahub.wallet.dto;

import com.akibahub.wallet.entity.Wallet;

import java.math.BigDecimal;

/**
 * Wallet payload with enough identity for the client to tell personal
 * and group wallets apart. Wallet.user / Wallet.group are @JsonIgnore,
 * so the raw entity only exposed id/type/balance — every GROUP wallet
 * looked identical and group.html could never resolve a balance.
 */
public record WalletResponse(
        Long id,
        Wallet.WalletType type,
        BigDecimal balance,
        Long version,
        Long userId,
        Long groupId,
        String groupName
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getType(),
                wallet.getBalance(),
                wallet.getVersion(),
                wallet.getUser() != null ? wallet.getUser().getId() : null,
                wallet.getGroup() != null ? wallet.getGroup().getId() : null,
                wallet.getGroup() != null ? wallet.getGroup().getName() : null
        );
    }
}
