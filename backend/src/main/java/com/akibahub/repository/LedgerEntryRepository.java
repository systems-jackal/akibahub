package com.akibahub.repository;

import com.akibahub.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByActorIdOrderByCreatedAtDesc(Long actorId);
    List<LedgerEntry> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}