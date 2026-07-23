-- Pending M-Pesa / PayHero deposits (verify-then-credit).
-- Status values: PENDING, COMPLETED, FAILED, EXPIRED, CANCELLED

CREATE TABLE IF NOT EXISTS pending_payments (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference     VARCHAR(64)  NOT NULL,
    user_id       BIGINT       NOT NULL,
    wallet_id     BIGINT       NOT NULL,
    amount        DECIMAL(19, 2) NOT NULL,
    phone         VARCHAR(20)  NULL,
    status        VARCHAR(20)  NOT NULL,
    expires_at    DATETIME(6)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    completed_at  DATETIME(6)  NULL,
    CONSTRAINT uk_pending_payments_reference UNIQUE (reference),
    CONSTRAINT fk_pending_payments_user   FOREIGN KEY (user_id)   REFERENCES users(id),
    CONSTRAINT fk_pending_payments_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    INDEX idx_pending_payments_user (user_id),
    INDEX idx_pending_payments_status (status)
);
