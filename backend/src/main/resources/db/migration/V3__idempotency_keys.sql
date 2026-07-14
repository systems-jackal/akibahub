-- Stores one row per (Idempotency-Key, user) pair, along with the
-- response that was returned the first time that request was processed.
-- A retried request with the same key and the same user gets the exact
-- same response replayed back, instead of re-executing the action (and,
-- for money-moving endpoints, potentially double-charging or
-- double-crediting someone).
CREATE TABLE idempotency_keys (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key   VARCHAR(64)   NOT NULL,
    user_id           BIGINT        NOT NULL,
    request_hash      VARCHAR(64)   NOT NULL,
    response_status   INT           NOT NULL,
    response_body     TEXT          NOT NULL,
    created_at        DATETIME      NOT NULL,
    CONSTRAINT uk_idempotency_key_user UNIQUE (idempotency_key, user_id),
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Scoped by user as well as key: two different users could each
-- independently choose the same UUID for their own client-generated key
-- (extremely unlikely, but the key is client-supplied, so we shouldn't
-- assume global uniqueness) - scoping to (key, user) means that's not a
-- collision.
CREATE INDEX idx_idempotency_created_at ON idempotency_keys(created_at);