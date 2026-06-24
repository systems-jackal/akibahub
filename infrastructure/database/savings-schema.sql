CREATE DATABASE IF NOT EXISTS akiba_savings;
USE akiba_savings;

CREATE TABLE IF NOT EXISTS wallets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_deposited DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_withdrawn DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'KES',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    wallet_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KES',
    payment_reference VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20),
    description TEXT,
    failure_reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_reference ON transactions(payment_reference);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_wallets_user ON wallets(user_id);
