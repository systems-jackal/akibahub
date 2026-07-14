-- 1. Optimistic locking on wallets.
--    Wallet balance updates today are read -> mutate in Java -> save,
--    with nothing preventing two concurrent requests (double-click
--    deposit, or a contribution racing a proposal payout) from both
--    reading the same starting balance and one of the updates getting
--    silently lost. A version column makes Hibernate detect that case
--    and throw OptimisticLockException instead of quietly losing money.
ALTER TABLE wallets ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. The ledger itself.
--    "transfers" is one row per money-movement event (a deposit, a
--    withdrawal, a contribution, a proposal payout). "ledger_entries" is
--    the actual journal: one row per wallet affected by that transfer,
--    insert-only, never updated or deleted. A genuine internal transfer
--    (e.g. contribution: personal -> group) writes exactly two balanced
--    rows - one DEBIT, one CREDIT, same amount. See LedgerService for
--    why deposits/withdrawals against M-Pesa are currently single-legged
--    rather than strict double-entry.
CREATE TABLE transfers (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    type          VARCHAR(30)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    initiated_by  BIGINT,
    reference     VARCHAR(255),
    created_at    DATETIME     NOT NULL,
    CONSTRAINT fk_transfers_initiated_by FOREIGN KEY (initiated_by) REFERENCES users(id)
);

CREATE TABLE ledger_entries (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id    BIGINT         NOT NULL,
    wallet_id      BIGINT         NOT NULL,
    direction      VARCHAR(10)    NOT NULL,
    amount         DECIMAL(19,4)  NOT NULL,
    balance_after  DECIMAL(19,4)  NOT NULL,
    created_at     DATETIME       NOT NULL,
    CONSTRAINT fk_ledger_entries_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id),
    CONSTRAINT fk_ledger_entries_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT chk_ledger_entries_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_ledger_entries_wallet ON ledger_entries(wallet_id);
CREATE INDEX idx_ledger_entries_transfer ON ledger_entries(transfer_id);

-- Recommended follow-up, done outside of Flyway (as a DBA/ops task, not
-- app code): once you have a dedicated application DB user, REVOKE
-- UPDATE and DELETE on ledger_entries for that user at the database
-- level. "Insert-only" should be enforced by the database's grants, not
-- just by application code being well-behaved.