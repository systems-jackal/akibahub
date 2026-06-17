package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GroupFinanceService {

    private final GroupWithdrawalRepository groupWithdrawalRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public GroupFinanceService(GroupWithdrawalRepository groupWithdrawalRepository,
                               TransactionRepository transactionRepository,
                               LedgerRepository ledgerRepository) {
        this.groupWithdrawalRepository = groupWithdrawalRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
    }

    // =========================
    // REQUEST GROUP WITHDRAWAL
    // =========================
    public GroupWithdrawalRequest requestGroupWithdrawal(
            Group group,
            User user,
            BigDecimal amount,
            String reason
    ) {

        GroupWithdrawalRequest request = new GroupWithdrawalRequest();
        request.setGroup(group);
        request.setRequestedBy(user);
        request.setAmount(amount);
        request.setReason(reason);
        request.setApproved(false);
        request.setExecuted(false);

        return groupWithdrawalRepository.save(request);
    }

    // =========================
    // EXECUTE GROUP WITHDRAWAL
    // (ONLY AFTER APPROVAL)
    // =========================
    public void executeGroupWithdrawal(GroupWithdrawalRequest request, String reference) {

        if (request.isExecuted()) return;

        if (!request.isApproved()) {
            throw new RuntimeException("Request not approved");
        }

        request.setExecuted(true);
        groupWithdrawalRepository.save(request);

        Transaction tx = new Transaction();
        tx.setGroup(request.getGroup());
        tx.setAmount(request.getAmount());
        tx.setType(TransactionType.GROUP_WITHDRAWAL_EXECUTED);
        tx.setReference(reference);

        transactionRepository.save(tx);

        LedgerEntry ledger = new LedgerEntry();
        ledger.setGroup(request.getGroup());
        ledger.setAction(TransactionType.GROUP_WITHDRAWAL_EXECUTED.name());
        ledger.setAmount(request.getAmount());
        ledger.setReference(reference);

        ledgerRepository.save(ledger);
    }

    public void executeApprovedProposal(Proposal proposal) {

    GroupWithdrawalRequest request = new GroupWithdrawalRequest();
    request.setGroup(proposal.getGroup());
    request.setRequestedBy(proposal.getCreatedBy());
    request.setAmount(proposal.getAmount());
    request.setReason(proposal.getDescription());
    request.setApproved(true);
    request.setExecuted(false);

    executeGroupWithdrawal(request, "PROP-" + proposal.getId());
}
}