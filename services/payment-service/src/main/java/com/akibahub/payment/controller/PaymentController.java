package com.akibahub.payment.controller;

import com.akibahub.payment.dto.request.InitiatePaymentRequest;
import com.akibahub.payment.dto.request.PayHeroCallbackRequest;
import com.akibahub.payment.model.PaymentRecord;
import com.akibahub.payment.service.AuditPublisher;
import com.akibahub.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final AuditPublisher auditPublisher;

    @Value("${payhero.callback.secret:}")
    private String callbackSecret;

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> initiateDeposit(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody InitiatePaymentRequest request) {

        PaymentRecord record = paymentService.initiatePersonalDeposit(
                userId, userEmail,
                request.getPhoneNumber(),
                request.getAmount());

        return ResponseEntity.accepted().body(Map.of(
                "message", "STK push sent. Complete payment on your phone.",
                "reference", record.getInternalReference(),
                "amount", record.getAmount().toString(),
                "currency", "KES"
        ));
    }

    @PostMapping("/contribute")
    public ResponseEntity<Map<String, String>> initiateGroupContribution(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody InitiatePaymentRequest request) {

        if (request.getGroupId() == null || request.getGroupId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "groupId is required for group contributions"));
        }

        PaymentRecord record = paymentService.initiateGroupContribution(
                userId, userEmail,
                request.getPhoneNumber(),
                request.getAmount(),
                request.getGroupId());

        return ResponseEntity.accepted().body(Map.of(
                "message", "STK push sent. Complete payment on your phone.",
                "reference", record.getInternalReference(),
                "groupId", record.getGroupId(),
                "amount", record.getAmount().toString(),
                "currency", "KES"
        ));
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-PayHero-Signature", required = false) String signature,
            HttpServletRequest request) {

        // Verify HMAC signature if secret is configured
        if (callbackSecret != null && !callbackSecret.isBlank()) {
            if (signature == null || !verifySignature(rawBody, signature)) {
                log.warn("PayHero callback rejected — invalid signature from {}",
                        request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }
        }

        // Parse the body manually after verification
        PayHeroCallbackRequest callback = parseCallback(rawBody);

        log.info("PayHero callback verified — ref: {} status: {}",
                callback.getExternalReference(), callback.getStatus());

        auditPublisher.publish(
                "PAYMENT_CALLBACK_RECEIVED",
                null, null,
                callback.getExternalReference(),
                null, null,
                "status:" + callback.getStatus());

        paymentService.handleCallback(
                callback.getExternalReference(),
                callback.isSuccessful(),
                callback.getReference(),
                callback.getFailureReason());

        return ResponseEntity.ok(Map.of("status", "received"));
    }

    // Internal — called by proposal-service for group withdrawals
    @PostMapping("/internal/group-withdrawal")
    public ResponseEntity<Map<String, String>> groupWithdrawal(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestBody Map<String, Object> body) {

        String expectedKey = System.getenv("INTERNAL_SERVICE_KEY");
        if (expectedKey == null || !expectedKey.equals(serviceKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid service key"));
        }

        String groupId    = (String) body.get("groupId");
        String phone      = (String) body.get("phoneNumber");
        String proposalId = (String) body.get("proposalId");
        java.math.BigDecimal amount =
                new java.math.BigDecimal(body.get("amount").toString());

        PaymentRecord record = paymentService.initiateGroupContribution(
                "SYSTEM", "system@akibahub.internal",
                phone, amount, groupId);

        return ResponseEntity.ok(Map.of(
                "reference", record.getInternalReference(),
                "proposalId", proposalId
        ));
    }

    private boolean verifySignature(String payload, String receivedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    callbackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(receivedSignature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private PayHeroCallbackRequest parseCallback(String rawBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(rawBody, PayHeroCallbackRequest.class);
        } catch (Exception e) {
            log.error("Failed to parse callback body: {}", e.getMessage());
            return new PayHeroCallbackRequest();
        }
    }
}
