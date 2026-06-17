package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsensusService {

    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final GroupFinanceService groupFinanceService;

    public ConsensusService(ProposalRepository proposalRepository,
                            VoteRepository voteRepository,
                            GroupFinanceService groupFinanceService) {
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.groupFinanceService = groupFinanceService;
    }

    // =========================
    // CREATE PROPOSAL
    // =========================
    public Proposal createProposal(Proposal proposal) {
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    // =========================
    // CAST VOTE
    // =========================
    public Vote vote(Vote vote) {
        Vote savedVote = voteRepository.save(vote);

        evaluateProposal(vote.getProposal().getId());

        return savedVote;
    }

    // =========================
    // CORE ENGINE
    // =========================
    private void evaluateProposal(Long proposalId) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow();

        List<Vote> votes = proposal.getVotes();

        long yes = votes.stream().filter(v -> v.getValue() == VoteValue.YES).count();
        long no = votes.stream().filter(v -> v.getValue() == VoteValue.NO).count();

        int total = votes.size();

        // THRESHOLD RULE (SIMPLE VERSION)
        if (yes > total / 2) {
            proposal.setStatus(ProposalStatus.APPROVED);
            proposalRepository.save(proposal);

            executeProposal(proposal);
        }

        if (no >= total / 2) {
            proposal.setStatus(ProposalStatus.REJECTED);
            proposalRepository.save(proposal);
        }
    }

    // =========================
    // EXECUTION ENGINE
    // =========================
    private void executeProposal(Proposal proposal) {

        try {
            groupFinanceService.executeApprovedProposal(proposal);
            proposal.setStatus(ProposalStatus.EXECUTED);
            proposalRepository.save(proposal);
        } catch (Exception e) {
            throw new RuntimeException("Execution failed: " + e.getMessage());
        }
    }
}