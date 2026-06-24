package com.akibahub.proposal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class PaymentServiceClient {

    private final WebClient paymentWebClient;
    private final String internalKey;

    public PaymentServiceClient(@Qualifier("paymentWebClient") WebClient paymentWebClient,
                                @Value("${services.internal-key}") String internalKey) {
        this.paymentWebClient = paymentWebClient;
        this.internalKey = internalKey;
    }

    public String initiateGroupWithdrawal(String groupId, BigDecimal amount,
                                          String recipientPhone, String proposalId) {
        Map<String, Object> payload = Map.of(
                "groupId", groupId,
                "amount", amount,
                "phoneNumber", recipientPhone,
                "proposalId", proposalId
        );

        try {
            Map<?, ?> response = paymentWebClient.post()
                    .uri("/payments/internal/group-withdrawal")
                    .header("X-Service-Key", internalKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("reference")) {
                return response.get("reference").toString();
            }
            throw new RuntimeException("No reference returned from payment-service");
        } catch (Exception e) {
            log.error("Group withdrawal failed: {}", e.getMessage());
            throw new RuntimeException("Withdrawal execution failed: " + e.getMessage());
        }
    }
}
