package com.akibahub.ledger.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    // Full point-in-time history for a wallet, oldest first - the thing
    // Wallet.balance alone could never give you.
    List<LedgerEntry> findByWalletIdOrderByCreatedAtAsc(Long walletId);
}