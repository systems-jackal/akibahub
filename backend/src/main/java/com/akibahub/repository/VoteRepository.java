package com.akibahub.repository;

import com.akibahub.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByProposalIdAndVoterId(Long proposalId, Long voterId);
    long countByProposalIdAndDecision(Long proposalId, Vote.VoteDecision decision);
}