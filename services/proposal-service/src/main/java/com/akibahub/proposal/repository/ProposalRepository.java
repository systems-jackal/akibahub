package com.akibahub.proposal.repository;

import com.akibahub.proposal.model.Proposal;
import com.akibahub.proposal.model.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, String> {
    Page<Proposal> findByGroupIdOrderByCreatedAtDesc(String groupId, Pageable pageable);
    List<Proposal> findByStatusAndVotingDeadlineBefore(
            ProposalStatus status, LocalDateTime deadline);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Proposal> findWithLockById(String id);
}
