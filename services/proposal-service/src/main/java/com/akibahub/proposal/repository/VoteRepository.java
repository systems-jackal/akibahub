package com.akibahub.proposal.repository;

import com.akibahub.proposal.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {
    boolean existsByProposalIdAndVoterId(String proposalId, String voterId);
    Optional<Vote> findByProposalIdAndVoterId(String proposalId, String voterId);
    List<Vote> findByProposalId(String proposalId);
    long countByProposalId(String proposalId);
}
