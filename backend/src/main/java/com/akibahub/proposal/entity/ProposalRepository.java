package com.akibahub.proposal.entity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByGroupId(Long groupId);
    List<Proposal> findByGroupIdIn(List<Long> groupIds);
    long countByGroupId(Long groupId);

    /**
     * Locks the proposal row for the duration of the surrounding
     * transaction so two concurrent YES votes that both cross majority
     * cannot both run executeWithdrawal (double payout).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Proposal p WHERE p.id = :id")
    Optional<Proposal> findByIdForUpdate(@Param("id") Long id);
}
