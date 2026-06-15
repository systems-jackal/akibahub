package com.akibahub.service;

import com.akibahub.model.Proposal;
import com.akibahub.model.Vote;

public interface ConsensusService {

    Proposal createProposal(Proposal proposal);

    Vote castVote(Long proposalId, Long userId, boolean approve);

    Proposal evaluateProposal(Long proposalId);
}