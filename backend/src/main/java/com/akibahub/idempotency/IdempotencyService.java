package com.akibahub.idempotency;

import com.akibahub.idempotency.entity.IdempotencyRecord;
import com.akibahub.idempotency.entity.IdempotencyRecordRepository;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Makes a POST endpoint safely retryable. The client generates a random
 * key (a UUID is fine) and sends it as the "Idempotency-Key" header on a
 * request. If that exact request is retried - a network timeout makes
 * the client resend it, a user double-taps "Contribute" before the first
 * tap's response comes back - the SAME response is replayed instead of
 * the action running twice.
 *
 * This only wraps the success path. If the wrapped action throws (e.g.
 * insufficient balance), nothing is recorded, and the exception
 * propagates as normal - a request that failed for a business reason
 * should be evaluated fresh next time, not have that failure cached and
 * replayed forever.
 *
 * Callers that don't send a key at all still work exactly as before -
 * the key is opt-in from the client's side, not mandatory. Making it
 * mandatory is a reasonable next step once your frontend is updated to
 * always send one.
 */
@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repo;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    /**
     * @param idempotencyKey the client-supplied key, or null/blank if the
     *                       client didn't send one (in which case the
     *                       action just runs normally, no caching)
     * @param user           whose request this is - keys are scoped per
     *                       user, so two different users can't collide on
     *                       the same client-generated key
     * @param requestBody    the parsed request body, hashed to detect a
     *                       key being reused for a genuinely different
     *                       request
     * @param responseType   Jackson TypeReference describing the response
     *                       body's exact generic type, e.g.
     *                       {@code new TypeReference<ApiResponse<Wallet>>() {}}
     * @param action         the real work to perform on a cache miss
     */
    public <T> ResponseEntity<T> execute(String idempotencyKey, User user, Object requestBody,
                                          TypeReference<T> responseType, Supplier<ResponseEntity<T>> action) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return action.get();
        }

        String requestHash = sha256(serialize(requestBody));

        Optional<IdempotencyRecord> existing = repo.findByIdempotencyKeyAndUserId(idempotencyKey, user.getId());
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                // Same key, different request body - this is a client bug
                // (keys should be generated fresh per logical operation),
                // not something safe to silently paper over by either
                // replaying the old response or re-running the new one.
                throw new ConflictException(
                        "This Idempotency-Key was already used for a different request");
            }
            T cachedBody = deserialize(record.getResponseBody(), responseType);
            return ResponseEntity.status(record.getResponseStatus()).body(cachedBody);
        }

        ResponseEntity<T> response = action.get();

        // Only cache clean, successful responses. A 2xx from here means
        // the action fully committed, so it's safe to replay verbatim.
        if (response.getStatusCode().is2xxSuccessful()) {
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .userId(user.getId())
                    .requestHash(requestHash)
                    .responseStatus(response.getStatusCode().value())
                    .responseBody(serialize(response.getBody()))
                    .build();
            repo.save(record);
        }

        return response;
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
            // SHA-256 is always available on any standard JVM - this
            // branch is unreachable in practice.
            throw new IllegalStateException(e);
        }
    }
}