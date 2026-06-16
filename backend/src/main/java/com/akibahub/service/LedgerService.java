package com.akibahub.service;

import java.util.Map;

public interface LedgerService {
    void log(String action, String entityType, Long entityId, Map<String, Object> details, Long actorId, Long groupId);
}