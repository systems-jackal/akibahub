package com.akibahub.shared;

import com.akibahub.shared.exception.BadRequestException;

import java.math.BigDecimal;

/**
 * Single choke point for validating any monetary amount that comes from a
 * client before it is allowed to touch a wallet balance or create a
 * transaction/ledger record.
 *
 * Why this exists as its own class instead of an inline "if" in each
 * service method: every place that previously checked balances directly
 * (WalletService, ProposalService) trusted the caller to have already sent
 * a sane, positive amount. Nothing enforced that assumption, so a client
 * could send a negative number and have it pass every existing check
 * (e.g. "is balance >= amount?" is true for almost any balance when amount
 * is negative), then get added instead of subtracted. Centralizing the
 * check means every money-moving code path validates amounts the same
 * way, and a future change to the rule (e.g. a new max limit) only needs
 * to happen in one place.
 */
public final class AmountValidator {

    // Defense-in-depth ceiling on a single transaction. This is not a
    // business/product limit (product may want per-user or per-group
    // limits later) - it exists purely so that even if every other
    // safeguard failed, one transfer can never claim an absurd amount.
    // Adjust to whatever makes sense for your real transaction limits.
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000");

    private AmountValidator() {
        // utility class, no instances
    }

    /**
     * Throws if the amount is missing, zero, negative, or above the sanity
     * ceiling. Call this BEFORE reading/mutating any wallet balance.
     */
    public static void requirePositive(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BadRequestException("Amount exceeds the maximum allowed per transaction");
        }
    }
}