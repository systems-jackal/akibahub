package com.akibahub.repository;

import com.akibahub.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByGroupIdAndStatus(Long groupId, Proposal.ProposalStatus status);
    List<Proposal> findByGroupId(Long groupId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Proposal p SET p.status = 'REJECTED' WHERE p.status = 'PENDING' AND p.expiresAt < :now")
    int rejectExpiredProposals(@Param("now") LocalDateTime now);
}