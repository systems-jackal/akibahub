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

    // 1. Prevent duplicate processing (IDEMPOTENCY)
    if (transactionRepository.existsByReference(reference)) {
        return; // already processed
    }

    // 2. Update wallet
    wallet.setBalance(wallet.getBalance().add(amount));
    walletRepository.save(wallet);

    // 3. Transaction record
    Transaction tx = new Transaction();
    tx.setWallet(wallet);
    tx.setAmount(amount);
    tx.setType(TransactionType.DEPOSIT);
    tx.setReference(reference);
    transactionRepository.save(tx);

    // 4. Ledger (immutable truth)
    LedgerEntry ledger = new LedgerEntry();
    ledger.setWallet(wallet);
    ledger.setAction("DEPOSIT");
    ledger.setAmount(amount);
    ledger.setBalanceAfter(wallet.getBalance());
    ledger.setReference(reference);

    ledgerRepository.save(ledger);
}
}