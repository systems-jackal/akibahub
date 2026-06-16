package com.akibahub.service.impl;

import com.akibahub.model.LedgerEntry;
import com.akibahub.repository.LedgerEntryRepository;
import com.akibahub.service.LedgerService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LedgerServiceImpl implements LedgerService {
    private final LedgerEntryRepository ledgerRepository;

    public LedgerServiceImpl(LedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public void log(String action, String entityType, Long entityId, Map<String, Object> details, Long actorId, Long groupId) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        entry.setActorId(actorId);
        entry.setGroupId(groupId);
        ledgerRepository.save(entry);
    }
}