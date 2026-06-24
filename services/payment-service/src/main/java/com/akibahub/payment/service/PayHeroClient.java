package com.akibahub.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayHeroClient {

    private final WebClient payheroWebClient;

    @Value("${payhero.api.channel-id}")
    private String channelId;

    @Value("${payhero.api.callback-url}")
    private String callbackUrl;

    public String initiateStkPush(String phoneNumber, BigDecimal amount,
                                  String reference, String description) {
        Map<String, Object> payload = Map.of(
                "amount", amount,
                "phone_number", normalizePhone(phoneNumber),
                "channel_id", channelId,
                "provider", "m-pesa",
                "external_reference", reference,
                "description", description,
                "callback_url", callbackUrl
        );

        try {
            Map<?, ?> response = payheroWebClient.post()
                    .uri("/payments")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("reference")) {
                String payheroRef = response.get("reference").toString();
                log.info("STK push initiated — ref: {}", payheroRef);
                return payheroRef;
            }
            throw new RuntimeException("PayHero did not return a reference");
        } catch (Exception e) {
            log.error("STK push failed for ref {}: {}", reference, e.getMessage());
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        // Normalize to 254XXXXXXXXX format
        phone = phone.trim().replaceAll("\\s+", "");
        if (phone.startsWith("+")) phone = phone.substring(1);
        if (phone.startsWith("07") || phone.startsWith("01")) {
            phone = "254" + phone.substring(1);
        }
        return phone;
    }
}
