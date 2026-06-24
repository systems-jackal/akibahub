package com.akibahub.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
                        String groupId, String metadata) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("actorId", actorId);
        event.put("actorEmail", actorEmail);
        event.put("resourceType", resourceType);
        event.put("resourceId", resourceId);
        event.put("groupId", groupId);
        event.put("metadata", metadata);
        event.put("serviceSource", "group-service");

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {} — {}", eventType, e.getMessage());
        }
    }
}
