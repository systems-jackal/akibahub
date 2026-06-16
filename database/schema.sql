-- =============================================
-- AKIBA HUB V2 - MARIADB SCHEMA
-- =============================================

CREATE DATABASE IF NOT EXISTS akiba_hub_v2;
USE akiba_hub_v2;

-- 1. USERS
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NULL,
    provider VARCHAR(20) DEFAULT 'LOCAL',
    provider_id VARCHAR(255) NULL,
    profile_pic VARCHAR(500) NULL,
    phone_number VARCHAR(20) NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    updated_at TIMESTAMP DEFAULT current_timestamp() ON UPDATE current_timestamp()
);

-- 2. PERSONAL WALLETS
CREATE TABLE personal_wallets (
    user_id BIGINT PRIMARY KEY,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. SAVINGS GROUPS
CREATE TABLE groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    admin_id BIGINT NOT NULL,
    total_balance DECIMAL(15, 2) DEFAULT 0.00,
    monthly_contribution DECIMAL(10, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. GROUP MEMBERS
CREATE TABLE group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT current_timestamp(),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_membership (group_id, user_id)
);

-- 5. INVITE CODES
CREATE TABLE invite_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    group_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    max_uses INT DEFAULT 10,
    used_count INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 6. TRANSACTIONS
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    payhero_reference VARCHAR(255) NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- 7. PROPOSALS (Consensus)
CREATE TABLE proposals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    proposer_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    yes_votes INT DEFAULT 0,
    no_votes INT DEFAULT 0,
    abstain_votes INT DEFAULT 0,
    required_yes_votes INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    executed_at TIMESTAMP NULL,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (proposer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 8. VOTES
CREATE TABLE votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proposal_id BIGINT NOT NULL,
    voter_id BIGINT NOT NULL,
    decision ENUM('YES', 'NO', 'ABSTAIN') NOT NULL,
    voted_at TIMESTAMP DEFAULT current_timestamp(),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id) ON DELETE CASCADE,
    FOREIGN KEY (voter_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_vote (proposal_id, voter_id)
);

-- 9. LEDGER ENTRIES
CREATE TABLE ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    details JSON NULL,
    actor_id BIGINT NOT NULL,
    group_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp(),
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- SEED DATA
INSERT INTO users (email, name, provider) VALUES ('admin@akiba.com', 'System Admin', 'LOCAL');
INSERT INTO personal_wallets (user_id, balance) VALUES (1, 1000.00);
INSERT INTO groups (name, description, admin_id) VALUES ('Demo Chama', 'Test group with consensus', 1);
INSERT INTO group_members (group_id, user_id, role) VALUES (1, 1, 'ADMIN');