package com.akibahub.service.impl;

import com.akibahub.model.Proposal;
import com.akibahub.model.Vote;
import com.akibahub.repository.GroupMemberRepository;
import com.akibahub.repository.ProposalRepository;
import com.akibahub.repository.VoteRepository;
import com.akibahub.service.GovernanceService;
import com.akibahub.service.LedgerService;
import com.akibahub.service.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GovernanceServiceImpl implements GovernanceService {
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final GroupMemberRepository memberRepository;
    private final PaymentService paymentService;
    private final LedgerService ledgerService;

    public GovernanceServiceImpl(ProposalRepository proposalRepository,
                                 VoteRepository voteRepository,
                                 GroupMemberRepository memberRepository,
                                 PaymentService paymentService,
                                 LedgerService ledgerService) {
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.memberRepository = memberRepository;
        this.paymentService = paymentService;
        this.ledgerService = ledgerService;
    }

    @Override
    @Transactional
    public Proposal createProposal(Long groupId, Long proposerId, String title, String description, BigDecimal amount) {
        long activeMembers = memberRepository.countByGroupIdAndIsActiveTrue(groupId);
        int requiredYes = (int) Math.ceil(activeMembers / 2.0);

        Proposal proposal = new Proposal();
        proposal.setGroupId(groupId);
        proposal.setProposerId(proposerId);
        proposal.setTitle(title);
        proposal.setDescription(description);
        proposal.setAmount(amount);
        proposal.setRequiredYesVotes(requiredYes);
        proposal.setExpiresAt(LocalDateTime.now().plusHours(48));
        proposal = proposalRepository.save(proposal);

        ledgerService.log("PROPOSAL_CREATED", "PROPOSAL", proposal.getId(),
                Map.of("amount", amount, "requiredYes", requiredYes), proposerId, groupId);
        return proposal;
    }

    @Override
    @Transactional
    public Vote castVote(Long proposalId, Long voterId, Vote.VoteDecision decision) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if (proposal.getStatus() != Proposal.ProposalStatus.PENDING) {
            throw new RuntimeException("Proposal is no longer pending");
        }
        if (proposal.getExpiresAt().isBefore(LocalDateTime.now())) {
            proposal.setStatus(Proposal.ProposalStatus.REJECTED);
            proposalRepository.save(proposal);
            throw new RuntimeException("Proposal expired");
        }

        if (voteRepository.findByProposalIdAndVoterId(proposalId, voterId).isPresent()) {
            throw new RuntimeException("Already voted");
        }

        Vote vote = new Vote();
        vote.setProposalId(proposalId);
        vote.setVoterId(voterId);
        vote.setDecision(decision);
        vote = voteRepository.save(vote);

        switch (decision) {
            case YES -> proposal.setYesVotes(proposal.getYesVotes() + 1);
            case NO -> proposal.setNoVotes(proposal.getNoVotes() + 1);
            case ABSTAIN -> proposal.setAbstainVotes(proposal.getAbstainVotes() + 1);
        }
        proposalRepository.save(proposal);

        // Check consensus
        if (proposal.getYesVotes() >= proposal.getRequiredYesVotes()) {
            executeProposal(proposal);
        } else {
            long activeMembers = memberRepository.countByGroupIdAndIsActiveTrue(proposal.getGroupId());
            int requiredYes = (int) Math.ceil(activeMembers / 2.0);
            if (proposal.getNoVotes() >= requiredYes) {
                proposal.setStatus(Proposal.ProposalStatus.REJECTED);
                proposalRepository.save(proposal);
            }
        }
        return vote;
    }

    @Transactional
    public void executeProposal(Proposal proposal) {
        try {
            String ref = paymentService.processWithdrawal(proposal.getProposerId(), proposal.getAmount());
            proposal.setStatus(Proposal.ProposalStatus.EXECUTED);
            proposal.setExecutedAt(LocalDateTime.now());
            proposalRepository.save(proposal);

            ledgerService.log("PROPOSAL_EXECUTED", "PROPOSAL", proposal.getId(),
                    Map.of("amount", proposal.getAmount(), "ref", ref),
                    proposal.getProposerId(), proposal.getGroupId());
        } catch (Exception e) {
            ledgerService.log("PROPOSAL_EXECUTION_FAILED", "PROPOSAL", proposal.getId(),
                    Map.of("error", e.getMessage()),
                    proposal.getProposerId(), proposal.getGroupId());
            throw new RuntimeException("Payment failed");
        }
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void rejectExpiredProposals() {
        int count = proposalRepository.rejectExpiredProposals(LocalDateTime.now());
        if (count > 0) System.out.println("Rejected " + count + " expired proposals.");
    }

    @Override
    public List<Proposal> getPendingProposals(Long groupId) {
        return proposalRepository.findByGroupIdAndStatus(groupId, Proposal.ProposalStatus.PENDING);
    }
}