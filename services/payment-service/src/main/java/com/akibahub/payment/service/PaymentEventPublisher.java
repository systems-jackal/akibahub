package com.akibahub.payment.service;

import com.akibahub.payment.model.PaymentRecord;
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
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${payment.events.exchange}")
    private String exchange;

    @Value("${payment.events.savings-routing-key}")
    private String savingsRoutingKey;

    @Value("${payment.events.group-routing-key}")
    private String groupRoutingKey;

    public void publishPaymentCompleted(PaymentRecord record) {
        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", record.getId());
        event.put("userId", record.getUserId());
        event.put("userEmail", record.getUserEmail());
        event.put("amount", record.getAmount());
        event.put("currency", record.getCurrency());
        event.put("internalReference", record.getInternalReference());
        event.put("groupId", record.getGroupId());
        event.put("paymentType", record.getType().name());
        event.put("status", "COMPLETED");

        String routingKey = record.getGroupId() != null
                ? groupRoutingKey : savingsRoutingKey;

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Payment completed event published — ref: {}",
                    record.getInternalReference());
        } catch (Exception e) {
            log.error("Failed to publish payment completed event: {}", e.getMessage());
        }
    }

    public void publishPaymentFailed(PaymentRecord record) {
        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", record.getId());
        event.put("userId", record.getUserId());
        event.put("internalReference", record.getInternalReference());
        event.put("groupId", record.getGroupId());
        event.put("paymentType", record.getType().name());
        event.put("reason", record.getFailureReason());
        event.put("status", "FAILED");

        String routingKey = record.getGroupId() != null
                ? groupRoutingKey : savingsRoutingKey;

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish payment failed event: {}", e.getMessage());
        }
    }
}
