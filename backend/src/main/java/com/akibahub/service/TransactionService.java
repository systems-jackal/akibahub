package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              LedgerRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
    }

    // =========================
    // PERSONAL DEPOSIT
    // =========================
    public void deposit(Wallet wallet, BigDecimal amount, String reference) {

        if (transactionRepository.existsByReference(reference)) return;

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.PERSONAL_DEPOSIT);
        tx.setReference(reference);
        transactionRepository.save(tx);

        LedgerEntry ledger = new LedgerEntry();
        ledger.setUser(wallet.getUser());
        ledger.setAction(TransactionType.PERSONAL_DEPOSIT.name());
        ledger.setAmount(amount);
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }

    // =========================
    // PERSONAL WITHDRAWAL
    // =========================
    public void withdraw(Wallet wallet, BigDecimal amount, String reference) {

        if (transactionRepository.existsByReference(reference)) return;

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.PERSONAL_WITHDRAWAL);
        tx.setReference(reference);
        transactionRepository.save(tx);

        LedgerEntry ledger = new LedgerEntry();
        ledger.setUser(wallet.getUser());
        ledger.setAction(TransactionType.PERSONAL_WITHDRAWAL.name());
        ledger.setAmount(amount);
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }
}