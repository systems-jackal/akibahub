CREATE DATABASE IF NOT EXISTS akiba_audit;
USE akiba_audit;

CREATE TABLE IF NOT EXISTS ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    actor_id VARCHAR(36) NOT NULL,
    actor_email VARCHAR(255),
    resource_type VARCHAR(100),
    resource_id VARCHAR(36),
    group_id VARCHAR(36),
    amount DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'KES',
    metadata TEXT,
    ip_address VARCHAR(45),
    service_source VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Revoke dangerous privileges from app user
-- Run as root after creating akiba_audit_user:
-- GRANT SELECT, INSERT ON akiba_audit.ledger_entries TO 'akiba_audit_user'@'%';
-- REVOKE UPDATE, DELETE ON akiba_audit.ledger_entries FROM 'akiba_audit_user'@'%';

CREATE INDEX idx_ledger_actor ON ledger_entries(actor_id);
CREATE INDEX idx_ledger_group ON ledger_entries(group_id);
CREATE INDEX idx_ledger_resource ON ledger_entries(resource_id);
CREATE INDEX idx_ledger_event_type ON ledger_entries(event_type);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at);
