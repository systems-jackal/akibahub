package com.akibahub.service;

import com.akibahub.model.Proposal;
import com.akibahub.model.Vote;

import java.math.BigDecimal;
import java.util.List;

public interface GovernanceService {
    Proposal createProposal(Long groupId, Long proposerId, String title, String description, BigDecimal amount);
    Vote castVote(Long proposalId, Long voterId, Vote.VoteDecision decision);
    List<Proposal> getPendingProposals(Long groupId);
}