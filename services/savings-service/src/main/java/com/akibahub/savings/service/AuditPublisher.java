package com.akibahub.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${audit.exchange}")
    private String exchange;

    @Value("${audit.routing-key}")
    private String routingKey;

    public void publish(String eventType, String actorId, String actorEmail,
                        String resourceType, String resourceId,
                        BigDecimal amount, String metadata) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("actorId", actorId);
        event.put("actorEmail", actorEmail);
        event.put("resourceType", resourceType);
        event.put("resourceId", resourceId);
        event.put("amount", amount);
        event.put("currency", "KES");
        event.put("metadata", metadata);
        event.put("serviceSource", "savings-service");

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.debug("Audit event published: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {} — {}", eventType, e.getMessage());
        }
    }
}
