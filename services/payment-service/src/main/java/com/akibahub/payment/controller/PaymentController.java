package com.akibahub.payment.controller;

import com.akibahub.payment.dto.request.InitiatePaymentRequest;
import com.akibahub.payment.dto.request.PayHeroCallbackRequest;
import com.akibahub.payment.model.PaymentRecord;
import com.akibahub.payment.service.AuditPublisher;
import com.akibahub.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final AuditPublisher auditPublisher;

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> initiateDeposit(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody InitiatePaymentRequest request) {

        PaymentRecord record = paymentService.initiatePersonalDeposit(
                userId, userEmail, request.getPhoneNumber(), request.getAmount());

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
            @RequestBody PayHeroCallbackRequest callback) {

        log.info("PayHero callback received — ref: {} status: {}",
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
}
