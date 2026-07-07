# Immutable Audit Trail

## Design

Every critical action is recorded in an **insert‑only** `audit_log` table. The table schema:

| Column      | Type         | Description                       |
|-------------|--------------|-----------------------------------|
| id          | BIGINT       | Primary key                       |
| event_type  | VARCHAR(255) | Type of event (e.g., USER_REGISTERED) |
| payload     | TEXT         | JSON‑serialized snapshot of affected entity or details |
| created_at  | DATETIME(6)  | Timestamp of the event            |

**Insert rule enforced at application level:** There are no update/delete endpoints for this table; the repository only extends `JpaRepository`.

## Recorded Events

| Event              | Trigger                                         |
|--------------------|-------------------------------------------------|
| USER_REGISTERED    | New user registration                           |
| USER_LOGGED_IN     | Successful login                                |
| GROUP_CREATED      | Group created by a user                         |
| MEMBER_JOINED      | User joins a group                              |
| PERSONAL_DEPOSIT   | Deposit into personal wallet                    |
| GROUP_CONTRIBUTION | Transfer from personal to group wallet          |
| PROPOSAL_CREATED   | New withdrawal proposal                         |
| VOTE_CAST          | A member votes on a proposal                    |
| PROPOSAL_APPROVED  | Proposal meets majority and is executed         |
| PROPOSAL_REJECTED  | Proposal rejected by majority                   |

## Integrity

- No cryptographic chaining (hash chain) is used yet. A future enhancement could store `previous_hash` to create an append‑only Merkle tree.
- Database‑level permissions can be restricted to prevent `UPDATE`/`DELETE` by the application user.

## Retention & Monitoring

- Logs are stored indefinitely in the database. For long‑term retention, export to an external log management system.
- Alerts can be set up on suspicious patterns (e.g., multiple failed login attempts) by querying the audit log.

## Limitations

- The audit log is not tamper‑proof at the database level; a DBA could modify it. For true immutability, a blockchain‑inspired hash chain or write‑once storage could be used.
- Payloads may contain sensitive data; care must be taken to exclude secrets.