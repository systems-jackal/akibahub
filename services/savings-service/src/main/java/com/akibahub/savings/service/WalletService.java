package com.akibahub.savings.service;

import com.akibahub.savings.model.*;
import com.akibahub.savings.repository.TransactionRepository;
import com.akibahub.savings.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditPublisher auditPublisher;

    @Transactional
    public Wallet getOrCreateWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet wallet = Wallet.builder()
                            .userId(userId)
                            .build();
                    log.info("Created new wallet for user: {}", userId);
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public Transaction initiateDeposit(String userId, String userEmail,
                                       BigDecimal amount, String phoneNumber,
                                       String paymentReference) {
        Wallet wallet = getOrCreateWallet(userId);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .walletId(wallet.getId())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .phoneNumber(phoneNumber)
                .paymentReference(paymentReference)
                .description("Personal savings deposit via M-Pesa")
                .build();

        Transaction saved = transactionRepository.save(transaction);

        auditPublisher.publish(
                "PERSONAL_DEPOSIT_INITIATED",
                userId, userEmail,
                "transaction", saved.getId(),
                amount, "ref:" + paymentReference);

        return saved;
    }

    @Transactional
    public void completeDeposit(String paymentReference, BigDecimal amount) {
        // Idempotency check
        if (transactionRepository.existsByPaymentReferenceAndStatus(
                paymentReference, TransactionStatus.COMPLETED)) {
            log.warn("Duplicate callback ignored for ref: {}", paymentReference);
            return;
        }

        Transaction transaction = transactionRepository
                .findByPaymentReference(paymentReference)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + paymentReference));

        Wallet wallet = walletRepository
                .findWithLockByUserId(transaction.getUserId())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        wallet.credit(amount);
        walletRepository.save(wallet);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        auditPublisher.publish(
                "PERSONAL_DEPOSIT_COMPLETED",
                transaction.getUserId(), null,
                "transaction", transaction.getId(),
                amount, "ref:" + paymentReference);

        log.info("Deposit completed for user: {} amount: {}", transaction.getUserId(), amount);
    }

    @Transactional
    public void failDeposit(String paymentReference, String reason) {
        transactionRepository.findByPaymentReference(paymentReference)
                .ifPresent(transaction -> {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setFailureReason(reason);
                    transactionRepository.save(transaction);

                    auditPublisher.publish(
                            "PERSONAL_DEPOSIT_FAILED",
                            transaction.getUserId(), null,
                            "transaction", transaction.getId(),
                            transaction.getAmount(), "reason:" + reason);
                });
    }

    @Transactional(readOnly = true)
    public Wallet getBalance(String userId) {
        return getOrCreateWallet(userId);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistory(String userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
