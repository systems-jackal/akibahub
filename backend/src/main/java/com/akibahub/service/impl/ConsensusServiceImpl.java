package com.akibahub.service.impl;

import com.akibahub.model.*;
import com.akibahub.repository.*;
import com.akibahub.service.ConsensusService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsensusServiceImpl implements ConsensusService {

    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final LedgerRepository ledgerRepository;

    public ConsensusServiceImpl(
            ProposalRepository proposalRepository,
            VoteRepository voteRepository,
            LedgerRepository ledgerRepository
    ) {
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public Proposal createProposal(Proposal proposal) {
        return proposalRepository.save(proposal);
    }

    @Override
    public Vote castVote(Long proposalId, Long userId, boolean approve) {

        Vote vote = Vote.builder()
                .proposalId(proposalId)
                .userId(userId)
                .approve(approve)
                .build();

        Vote saved = voteRepository.save(vote);

        evaluateProposal(proposalId);

        return saved;
    }

    @Override
    public Proposal evaluateProposal(Long proposalId) {

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        List<Vote> votes = voteRepository.findByProposalId(proposalId);

        long approvals = votes.stream().filter(Vote::isApprove).count();

        proposal.setCurrentApprovals((int) approvals);

        if (approvals >= proposal.getRequiredApprovals()) {
            proposal.setStatus(ProposalStatus.APPROVED);

            LedgerEntry entry = LedgerEntry.builder()
                    .groupId(proposal.getGroupId())
                    .proposalId(proposal.getId())
                    .action("APPROVED: " + proposal.getTitle())
                    .hash(String.valueOf(proposal.hashCode()))
                    .previousHash("GENESIS")
                    .build();

            ledgerRepository.save(entry);
        }

        return proposalRepository.save(proposal);
    }
}