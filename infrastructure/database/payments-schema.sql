CREATE DATABASE IF NOT EXISTS akiba_payments;
USE akiba_payments;

CREATE TABLE IF NOT EXISTS payment_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    user_email VARCHAR(255),
    phone_number VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KES',
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    internal_reference VARCHAR(255) NOT NULL UNIQUE,
    payhero_reference VARCHAR(255),
    group_id VARCHAR(36),
    failure_reason TEXT,
    callback_received_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_payment_user ON payment_records(user_id);
CREATE INDEX idx_payment_internal_ref ON payment_records(internal_reference);
CREATE INDEX idx_payment_payhero_ref ON payment_records(payhero_reference);
CREATE INDEX idx_payment_status ON payment_records(status);
CREATE INDEX idx_payment_group ON payment_records(group_id);
