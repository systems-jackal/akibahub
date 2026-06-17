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

    public void deposit(Wallet wallet, BigDecimal amount, String reference) {

        // 1. Update wallet balance
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // 2. Save transaction
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        tx.setReference(reference);
        transactionRepository.save(tx);

        // 3. Write ledger (IMMUTABLE)
        LedgerEntry ledger = new LedgerEntry();
        ledger.setWallet(wallet);
        ledger.setAmount(amount);
        ledger.setAction("DEPOSIT");
        ledger.setBalanceAfter(wallet.getBalance());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }
}