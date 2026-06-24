CREATE DATABASE IF NOT EXISTS akiba_proposals;
USE akiba_proposals;

CREATE TABLE IF NOT EXISTS proposals (
    id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    creator_email VARCHAR(255),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KES',
    recipient_phone VARCHAR(20),
    recipient_description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    threshold_percent INT NOT NULL DEFAULT 51,
    total_eligible_voters INT DEFAULT 0,
    yes_count INT DEFAULT 0,
    no_count INT DEFAULT 0,
    abstain_count INT DEFAULT 0,
    voting_deadline DATETIME NOT NULL,
    executed_at DATETIME,
    execution_reference VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS votes (
    id VARCHAR(36) PRIMARY KEY,
    proposal_id VARCHAR(36) NOT NULL,
    voter_id VARCHAR(36) NOT NULL,
    voter_email VARCHAR(255),
    value VARCHAR(10) NOT NULL,
    cast_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_proposal_voter (proposal_id, voter_id),
    FOREIGN KEY (proposal_id) REFERENCES proposals(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_proposals_group ON proposals(group_id);
CREATE INDEX idx_proposals_status ON proposals(status);
CREATE INDEX idx_proposals_deadline ON proposals(voting_deadline);
CREATE INDEX idx_votes_proposal ON votes(proposal_id);
CREATE INDEX idx_votes_voter ON votes(voter_id);
