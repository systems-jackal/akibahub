-- Baseline: reproduces the schema exactly as Hibernate's ddl-auto:update
-- has been creating it, so that on an already-running dev/staging
-- database (baselined at version 0 via baseline-on-migrate), this
-- migration is effectively a no-op check, and on a brand new database
-- it creates everything from scratch.

CREATE TABLE users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number   VARCHAR(15)  NOT NULL,
    id_number      VARCHAR(8)   NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(255) NOT NULL,
    created_at     DATETIME     NOT NULL,
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
    CONSTRAINT uk_users_id_number UNIQUE (id_number)
);

CREATE TABLE groups_table (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    rules        VARCHAR(500),
    invite_code  VARCHAR(6),
    created_by   BIGINT       NOT NULL,
    created_at   DATETIME     NOT NULL,
    CONSTRAINT uk_groups_invite_code UNIQUE (invite_code),
    CONSTRAINT fk_groups_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE group_members (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id   BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    joined_at  DATETIME NOT NULL,
    CONSTRAINT uk_group_members_group_user UNIQUE (group_id, user_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups_table(id),
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE wallets (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT,
    group_id BIGINT,
    type     VARCHAR(20)     NOT NULL,
    balance  DECIMAL(19,4)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wallets_group FOREIGN KEY (group_id) REFERENCES groups_table(id)
);

CREATE TABLE transactions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id  BIGINT         NOT NULL,
    amount     DECIMAL(19,4)  NOT NULL,
    type       VARCHAR(20)    NOT NULL,
    reference  VARCHAR(255),
    timestamp  DATETIME       NOT NULL,
    CONSTRAINT fk_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE TABLE proposals (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id     BIGINT         NOT NULL,
    title        VARCHAR(255)   NOT NULL,
    description  VARCHAR(255),
    amount       DECIMAL(19,4)  NOT NULL,
    created_by   BIGINT         NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    created_at   DATETIME       NOT NULL,
    CONSTRAINT fk_proposals_group FOREIGN KEY (group_id) REFERENCES groups_table(id),
    CONSTRAINT fk_proposals_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE votes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    proposal_id  BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    vote         VARCHAR(10) NOT NULL,
    CONSTRAINT uk_votes_proposal_user UNIQUE (proposal_id, user_id),
    CONSTRAINT fk_votes_proposal FOREIGN KEY (proposal_id) REFERENCES proposals(id),
    CONSTRAINT fk_votes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type  VARCHAR(255) NOT NULL,
    payload     TEXT,
    created_at  DATETIME     NOT NULL
);