package com.akibahub.idempotency;

import com.akibahub.idempotency.entity.IdempotencyRecord;
import com.akibahub.idempotency.entity.IdempotencyRecordRepository;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Makes a POST endpoint safely retryable via the Idempotency-Key header.
 *
 * Claim-then-execute: a placeholder row is inserted under the unique
 * (key, user) constraint BEFORE the money-moving action runs. Concurrent
 * retries with the same key hit the unique constraint and replay the
 * stored response instead of double-charging. Previously the action
 * committed first and the key was written afterward, so two parallel
 * retries could both miss the lookup and both mutate balances.
 */
@Service
public class IdempotencyService {

    private static final int PENDING_STATUS = 0;
    private static final String PENDING_BODY = "";

    private final IdempotencyRecordRepository repo;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> ResponseEntity<T> execute(String idempotencyKey, User user, Object requestBody,
                                          TypeReference<T> responseType, Supplier<ResponseEntity<T>> action) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        String requestHash = sha256(serialize(requestBody));

        Optional<IdempotencyRecord> existing = repo.findByIdempotencyKeyAndUserId(idempotencyKey, user.getId());
        if (existing.isPresent()) {
            return replayOrWait(existing.get(), requestHash, responseType);
        }

        // Claim the key first so a concurrent retry cannot also run action.
        IdempotencyRecord claim = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .userId(user.getId())
                .requestHash(requestHash)
                .responseStatus(PENDING_STATUS)
                .responseBody(PENDING_BODY)
                .build();
        try {
            repo.saveAndFlush(claim);
        } catch (DataIntegrityViolationException e) {
            IdempotencyRecord winner = repo.findByIdempotencyKeyAndUserId(idempotencyKey, user.getId())
                    .orElseThrow(() -> e);
            return replayOrWait(winner, requestHash, responseType);
        }

        ResponseEntity<T> response;
        try {
            response = action.get();
        } catch (RuntimeException e) {
            // Outer @Transactional will roll the pending claim back with
            // the failed money move; rethrow so GlobalExceptionHandler runs.
            throw e;
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            claim.setResponseStatus(response.getStatusCode().value());
            claim.setResponseBody(serialize(response.getBody()));
            repo.save(claim);
        } else {
            // Failed business responses should not be cached forever —
            // drop the claim so a corrected retry can proceed.
            repo.delete(claim);
        }

        return response;
    }

    private <T> ResponseEntity<T> replayOrWait(IdempotencyRecord record, String requestHash,
                                                TypeReference<T> responseType) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new ConflictException(
                    "This Idempotency-Key was already used for a different request");
        }
        if (record.getResponseStatus() == PENDING_STATUS) {
            // Another request holds the claim and is still executing.
            throw new ConflictException("A request with this Idempotency-Key is already in progress");
        }
        T cachedBody = deserialize(record.getResponseBody(), responseType);
        return ResponseEntity.status(record.getResponseStatus()).body(cachedBody);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotency payload", e);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize cached idempotent response", e);
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
