package com.akibahub.audit.service;

import com.akibahub.audit.dto.request.LedgerEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final LedgerService ledgerService;

    @RabbitListener(queues = "${audit.queue.name}")
    public void handleAuditEvent(LedgerEventRequest event) {
        try {
            ledgerService.record(event);
        } catch (Exception e) {
            log.error("Failed to record audit event: {} — {}",
                    event.getEventType(), e.getMessage());
            throw e;
        }
    }
}
