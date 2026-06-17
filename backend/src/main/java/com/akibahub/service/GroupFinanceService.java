package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GroupFinanceService {

    private final TransactionRepository transactionRepository;
    private final GroupWithdrawalRepository withdrawalRepository;

    public GroupFinanceService(TransactionRepository transactionRepository,
                               GroupWithdrawalRepository withdrawalRepository) {
        this.transactionRepository = transactionRepository;
        this.withdrawalRepository = withdrawalRepository;
    }

    // CONTRIBUTION (money into group)
    public void contribute(Wallet wallet, Group group, BigDecimal amount, String reference) {

        if (transactionRepository.existsByReference(reference)) return;

        wallet.setBalance(wallet.getBalance().subtract(amount));

        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setGroup(group);
        tx.setAmount(amount);
        tx.setType(TransactionType.GROUP_CONTRIBUTION);
        tx.setReference(reference);

        transactionRepository.save(tx);
    }

    // REQUEST WITHDRAWAL (NO EXECUTION HERE)
    public GroupWithdrawalRequest requestWithdrawal(Group group,
                                                    User user,
                                                    BigDecimal amount,
                                                    String reason) {

        GroupWithdrawalRequest req = new GroupWithdrawalRequest();
        req.setGroup(group);
        req.setRequestedBy(user);
        req.setAmount(amount);
        req.setReason(reason);
        req.setApproved(false);
        req.setExecuted(false);

        return withdrawalRepository.save(req);
    }
}