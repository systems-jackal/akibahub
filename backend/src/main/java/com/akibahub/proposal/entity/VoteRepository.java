package com.akibahub.proposal.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByProposalIdAndUserId(Long proposalId, Long userId);
    long countByProposalIdAndVote(Long proposalId, Vote.VoteValue vote);
}