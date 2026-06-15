package com.akibahub.repository;

import com.akibahub.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByProposalId(Long proposalId);
}