package com.akibahub.service.impl;

import com.akibahub.model.PersonalWallet;
import com.akibahub.model.Transaction;
import com.akibahub.repository.PersonalWalletRepository;
import com.akibahub.repository.TransactionRepository;
import com.akibahub.service.LedgerService;
import com.akibahub.service.PaymentService;
import com.akibahub.service.PersonalSavingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PersonalSavingsServiceImpl implements PersonalSavingsService {
    private final PersonalWalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentService paymentService;
    private final LedgerService ledgerService;

    public PersonalSavingsServiceImpl(PersonalWalletRepository walletRepository,
                                      TransactionRepository transactionRepository,
                                      PaymentService paymentService,
                                      LedgerService ledgerService) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.paymentService = paymentService;
        this.ledgerService = ledgerService;
    }

    @Override
    @Transactional
    public PersonalWallet deposit(Long userId, BigDecimal amount) {
        PersonalWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        String ref = paymentService.processDeposit(userId, amount);
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType("DEPOSIT");
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setStatus("COMPLETED");
        tx.setPayheroReference(ref);
        tx.setDescription("Personal deposit");
        transactionRepository.save(tx);

        ledgerService.log("PERSONAL_DEPOSIT", "TRANSACTION", tx.getId(),
                Map.of("amount", amount, "reference", ref), userId, null);
        return wallet;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(PersonalWallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}