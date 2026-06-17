package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PersonalFinanceService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public PersonalFinanceService(WalletRepository walletRepository,
                                  TransactionRepository transactionRepository,
                                  LedgerRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
    }

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
        ledger.setAmount(amount);
        ledger.setAction("PERSONAL_DEPOSIT");
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }

    public void withdraw(Wallet wallet, BigDecimal amount, String reference) {

        if (transactionRepository.existsByReference(reference)) return;

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

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
        ledger.setAmount(amount);
        ledger.setAction("PERSONAL_WITHDRAWAL");
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }
}