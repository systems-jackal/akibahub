package com.akibahub.audit.repository;

import com.akibahub.audit.model.EventType;
import com.akibahub.audit.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {

    Page<LedgerEntry> findByActorIdOrderByCreatedAtDesc(String actorId, Pageable pageable);

    Page<LedgerEntry> findByGroupIdOrderByCreatedAtDesc(String groupId, Pageable pageable);

    Page<LedgerEntry> findByResourceIdOrderByCreatedAtDesc(String resourceId, Pageable pageable);

    Page<LedgerEntry> findByEventTypeOrderByCreatedAtDesc(EventType eventType, Pageable pageable);

    List<LedgerEntry> findByGroupIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String groupId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT e FROM LedgerEntry e WHERE e.actorId = :userId " +
           "AND e.eventType IN :types ORDER BY e.createdAt DESC")
    Page<LedgerEntry> findByActorIdAndEventTypes(
            String userId, List<EventType> types, Pageable pageable);

    long countByGroupIdAndEventType(String groupId, EventType eventType);
}
