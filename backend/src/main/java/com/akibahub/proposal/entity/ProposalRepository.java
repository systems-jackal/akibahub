package com.akibahub.proposal.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByGroupId(Long groupId);
    List<Proposal> findByGroupIdIn(List<Long> groupIds);
    long countByStatus(Proposal.ProposalStatus status);
}