package com.akibahub.audit;

import com.akibahub.audit.entity.AuditLog;
import com.akibahub.audit.entity.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepo;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepo, ObjectMapper objectMapper) {
        this.auditLogRepo = auditLogRepo;
        this.objectMapper = objectMapper;
    }

    public void logEvent(String eventType, Object details) {
        String json;
        try {
            json = objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            json = details.toString();
        }
        AuditLog log = AuditLog.builder().eventType(eventType).payload(json).build();
        auditLogRepo.save(log);
    }
}