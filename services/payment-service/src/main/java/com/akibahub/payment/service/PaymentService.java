package com.akibahub.payment.service;

import com.akibahub.payment.model.*;
import com.akibahub.payment.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final PayHeroClient payHeroClient;
    private final PaymentEventPublisher eventPublisher;
    private final AuditPublisher auditPublisher;

    @Transactional
    public PaymentRecord initiatePersonalDeposit(String userId, String userEmail,
                                                  String phoneNumber, BigDecimal amount) {
        String internalRef = UUID.randomUUID().toString();

        PaymentRecord record = PaymentRecord.builder()
                .userId(userId)
                .userEmail(userEmail)
                .phoneNumber(phoneNumber)
                .amount(amount)
                .type(PaymentType.PERSONAL_DEPOSIT)
                .internalReference(internalRef)
                .build();

        PaymentRecord saved = paymentRecordRepository.save(record);

        try {
            String payheroRef = payHeroClient.initiateStkPush(
                    phoneNumber, amount, internalRef,
                    "Akiba Hub personal savings deposit");
            saved.setPayheroReference(payheroRef);
            paymentRecordRepository.save(saved);

            auditPublisher.publish("PAYMENT_INITIATED", userId, userEmail,
                    saved.getId(), null, amount, "ref:" + internalRef);

        } catch (Exception e) {
            saved.setStatus(PaymentStatus.FAILED);
            saved.setFailureReason(e.getMessage());
            paymentRecordRepository.save(saved);
            auditPublisher.publish("PAYMENT_FAILED", userId, userEmail,
                    saved.getId(), null, amount, e.getMessage());
            throw e;
        }

        return saved;
    }

    @Transactional
    public PaymentRecord initiateGroupContribution(String userId, String userEmail,
                                                    String phoneNumber, BigDecimal amount,
                                                    String groupId) {
        String internalRef = UUID.randomUUID().toString();

        PaymentRecord record = PaymentRecord.builder()
                .userId(userId)
                .userEmail(userEmail)
                .phoneNumber(phoneNumber)
                .amount(amount)
                .type(PaymentType.GROUP_CONTRIBUTION)
                .internalReference(internalRef)
                .groupId(groupId)
                .build();

        PaymentRecord saved = paymentRecordRepository.save(record);

        try {
            String payheroRef = payHeroClient.initiateStkPush(
                    phoneNumber, amount, internalRef,
                    "Akiba Hub group contribution");
            saved.setPayheroReference(payheroRef);
            paymentRecordRepository.save(saved);

            auditPublisher.publish("PAYMENT_INITIATED", userId, userEmail,
                    saved.getId(), groupId, amount, "ref:" + internalRef);

        } catch (Exception e) {
            saved.setStatus(PaymentStatus.FAILED);
            saved.setFailureReason(e.getMessage());
            paymentRecordRepository.save(saved);
            throw e;
        }

        return saved;
    }

    @Transactional
    public void handleCallback(String internalReference, boolean success,
                               String payheroReference, String failureReason) {
        // Idempotency — ignore duplicate callbacks
        if (paymentRecordRepository.existsByInternalReferenceAndStatus(
                internalReference, PaymentStatus.COMPLETED)) {
            log.warn("Duplicate callback ignored for ref: {}", internalReference);
            return;
        }

        PaymentRecord record = paymentRecordRepository
                .findByInternalReference(internalReference)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + internalReference));

        record.setCallbackReceivedAt(LocalDateTime.now());

        if (success) {
            record.setStatus(PaymentStatus.COMPLETED);
            if (payheroReference != null) {
                record.setPayheroReference(payheroReference);
            }
            paymentRecordRepository.save(record);
            eventPublisher.publishPaymentCompleted(record);
            auditPublisher.publish("PAYMENT_COMPLETED",
                    record.getUserId(), record.getUserEmail(),
                    record.getId(), record.getGroupId(),
                    record.getAmount(), "ref:" + internalReference);

            log.info("Payment completed — ref: {} amount: {}",
                    internalReference, record.getAmount());
        } else {
            record.setStatus(PaymentStatus.FAILED);
            record.setFailureReason(failureReason);
            paymentRecordRepository.save(record);
            eventPublisher.publishPaymentFailed(record);
            auditPublisher.publish("PAYMENT_FAILED",
                    record.getUserId(), record.getUserEmail(),
                    record.getId(), record.getGroupId(),
                    record.getAmount(), "reason:" + failureReason);

            log.warn("Payment failed — ref: {} reason: {}", internalReference, failureReason);
        }
    }
}
