package com.akibahub.audit.service;

import com.akibahub.audit.dto.request.LedgerEventRequest;
import com.akibahub.audit.dto.response.LedgerEntryResponse;
import com.akibahub.audit.model.LedgerEntry;
import com.akibahub.audit.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public LedgerEntryResponse record(LedgerEventRequest request) {
        LedgerEntry entry = LedgerEntry.builder()
                .eventType(request.getEventType())
                .actorId(request.getActorId())
                .actorEmail(request.getActorEmail())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .groupId(request.getGroupId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "KES")
                .metadata(request.getMetadata())
                .ipAddress(request.getIpAddress())
                .serviceSource(request.getServiceSource())
                .build();

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        log.info("Ledger entry recorded: {} by {} from {}",
                saved.getEventType(), saved.getActorId(), saved.getServiceSource());
        return LedgerEntryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getByUser(String userId, Pageable pageable) {
        return ledgerEntryRepository
                .findByActorIdOrderByCreatedAtDesc(userId, pageable)
                .map(LedgerEntryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getByGroup(String groupId, Pageable pageable) {
        return ledgerEntryRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(LedgerEntryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getByResource(String resourceId, Pageable pageable) {
        return ledgerEntryRepository
                .findByResourceIdOrderByCreatedAtDesc(resourceId, pageable)
                .map(LedgerEntryResponse::from);
    }
}
