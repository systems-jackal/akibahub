CREATE DATABASE IF NOT EXISTS akiba_groups;
USE akiba_groups;

CREATE TABLE IF NOT EXISTS groups (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by VARCHAR(36) NOT NULL,
    group_wallet_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_contributed DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'KES',
    max_members INT DEFAULT 50,
    contribution_threshold INT DEFAULT 51,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS group_members (
    id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    user_email VARCHAR(255),
    display_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    is_active BOOLEAN DEFAULT TRUE,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_group_member (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS invite_codes (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    group_id VARCHAR(36) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    expires_at DATETIME NOT NULL,
    max_uses INT DEFAULT 10,
    use_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_members_group ON group_members(group_id);
CREATE INDEX idx_members_user ON group_members(user_id);
CREATE INDEX idx_invite_code ON invite_codes(code);
CREATE INDEX idx_invite_group ON invite_codes(group_id);
