package com.akibahub.proposal.service;

import com.akibahub.proposal.model.*;
import com.akibahub.proposal.repository.ProposalRepository;
import com.akibahub.proposal.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsensusEngine {

    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final AuditPublisher auditPublisher;
    private final PaymentServiceClient paymentServiceClient;

    @Transactional
    public void evaluateAfterVote(String proposalId) {
        Proposal proposal = proposalRepository.findWithLockById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.ACTIVE) return;

        if (proposal.hasReachedThreshold()) {
            approveAndExecute(proposal);
        } else if (proposal.hasBeenRejected()) {
            reject(proposal);
        }
        // else — still active, waiting for more votes
    }

    @Transactional
    public void approveAndExecute(Proposal proposal) {
        proposal.setStatus(ProposalStatus.APPROVED);
        proposalRepository.save(proposal);

        auditPublisher.publish("PROPOSAL_APPROVED",
                proposal.getCreatedBy(), proposal.getCreatorEmail(),
                proposal.getId(), proposal.getGroupId(),
                proposal.getAmount(), "yes:" + proposal.getYesCount());

        log.info("Proposal approved: {} — executing withdrawal", proposal.getId());

        try {
            String executionRef = paymentServiceClient.initiateGroupWithdrawal(
                    proposal.getGroupId(),
                    proposal.getAmount(),
                    proposal.getRecipientPhone(),
                    proposal.getId());

            proposal.setStatus(ProposalStatus.EXECUTED);
            proposal.setExecutedAt(LocalDateTime.now());
            proposal.setExecutionReference(executionRef);
            proposalRepository.save(proposal);

            auditPublisher.publish("WITHDRAWAL_INITIATED",
                    proposal.getCreatedBy(), null,
                    proposal.getId(), proposal.getGroupId(),
                    proposal.getAmount(), "ref:" + executionRef);

        } catch (Exception e) {
            log.error("Withdrawal execution failed for proposal {}: {}",
                    proposal.getId(), e.getMessage());
            // Proposal stays APPROVED — manual intervention required
            auditPublisher.publish("WITHDRAWAL_FAILED",
                    proposal.getCreatedBy(), null,
                    proposal.getId(), proposal.getGroupId(),
                    proposal.getAmount(), "error:" + e.getMessage());
        }
    }

    @Transactional
    public void reject(Proposal proposal) {
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);

        auditPublisher.publish("PROPOSAL_REJECTED",
                proposal.getCreatedBy(), proposal.getCreatorEmail(),
                proposal.getId(), proposal.getGroupId(),
                proposal.getAmount(), "no:" + proposal.getNoCount());

        log.info("Proposal rejected: {}", proposal.getId());
    }

    // Runs every hour — expires proposals past their voting deadline
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void expireOverdueProposals() {
        List<Proposal> overdue = proposalRepository
                .findByStatusAndVotingDeadlineBefore(
                        ProposalStatus.ACTIVE, LocalDateTime.now());

        for (Proposal proposal : overdue) {
            if (proposal.hasReachedThreshold()) {
                approveAndExecute(proposal);
            } else {
                proposal.setStatus(ProposalStatus.EXPIRED);
                proposalRepository.save(proposal);

                auditPublisher.publish("PROPOSAL_EXPIRED",
                        proposal.getCreatedBy(), null,
                        proposal.getId(), proposal.getGroupId(),
                        proposal.getAmount(), "expired_at:" + LocalDateTime.now());

                log.info("Proposal expired: {}", proposal.getId());
            }
        }
    }
}
