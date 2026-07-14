package com.akibahub.ledger;

import com.akibahub.ledger.entity.LedgerEntry;
import com.akibahub.ledger.entity.Transfer;
import com.akibahub.ledger.entity.TransferRepository;
import com.akibahub.ledger.entity.LedgerEntryRepository;
import com.akibahub.shared.AmountValidator;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Append-only financial journal, run ALONGSIDE the existing Wallet.balance
 * field rather than replacing it - this is the "hybrid" approach: balance
 * stays a fast, authoritative field the rest of the app reads directly,
 * and this service writes the immutable record of *how* it got there.
 * That record is what lets you answer "what was the balance on date X"
 * or reconcile against M-Pesa later without recomputing anything, and it
 * gives you a tamper-evident trail independent of the mutable balance
 * column.
 *
 * IMPORTANT for callers: update and save the wallet's new balance BEFORE
 * calling either method here. balanceAfter is read directly off the
 * Wallet object you pass in, on the assumption it already reflects the
 * post-transaction value. Call this in the same @Transactional method
 * that mutates the balance, so the balance update and the journal entry
 * commit or roll back together.
 */
@Service
public class LedgerService {

    private final TransferRepository transferRepo;
    private final LedgerEntryRepository entryRepo;

    public LedgerService(TransferRepository transferRepo, LedgerEntryRepository entryRepo) {
        this.transferRepo = transferRepo;
        this.entryRepo = entryRepo;
    }

    /**
     * Records a genuine double-entry transfer between two wallets that
     * both exist inside Akiba Hub - a contribution (personal -> group) or
     * a proposal payout (group -> personal). Writes one DEBIT leg and one
     * CREDIT leg for the same amount, so the two rows always balance to
     * zero net.
     */
    @Transactional
    public Transfer recordInternalTransfer(Transfer.Type type, User initiatedBy, String reference,
                                            Wallet debitWallet, Wallet creditWallet, BigDecimal amount) {
        AmountValidator.requirePositive(amount);
        Transfer transfer = saveTransfer(type, initiatedBy, reference);
        writeEntry(transfer, debitWallet, LedgerEntry.Direction.DEBIT, amount);
        writeEntry(transfer, creditWallet, LedgerEntry.Direction.CREDIT, amount);
        return transfer;
    }

    /**
     * Records a single-leg entry for money crossing the boundary of the
     * system - a personal deposit or withdrawal against M-Pesa. This is
     * NOT strict double-entry: the other side (an M-Pesa/external
     * account) isn't modeled as a wallet in this system yet. That's the
     * natural next step once the M-Pesa integration is wired up - model
     * an EXTERNAL suspense wallet and make deposits/withdrawals real
     * two-legged transfers too, at which point this method goes away in
     * favor of recordInternalTransfer. For now this still gives every
     * wallet a complete, immutable audit trail, which is the gap this
     * migration is closing first.
     */
    @Transactional
    public Transfer recordExternalMovement(Transfer.Type type, User initiatedBy, String reference,
                                            Wallet wallet, LedgerEntry.Direction direction, BigDecimal amount) {
        AmountValidator.requirePositive(amount);
        Transfer transfer = saveTransfer(type, initiatedBy, reference);
        writeEntry(transfer, wallet, direction, amount);
        return transfer;
    }

    private Transfer saveTransfer(Transfer.Type type, User initiatedBy, String reference) {
        return transferRepo.save(Transfer.builder()
                .type(type)
                .status(Transfer.Status.COMPLETED)
                .initiatedBy(initiatedBy)
                .reference(reference)
                .build());
    }

    private void writeEntry(Transfer transfer, Wallet wallet, LedgerEntry.Direction direction, BigDecimal amount) {
        entryRepo.save(LedgerEntry.builder()
                .transfer(transfer)
                .wallet(wallet)
                .direction(direction)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .build());
    }
}