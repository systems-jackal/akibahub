# Data Protection

## Sensitive Data

- **Passwords:** Hashed using BCrypt (strength 10). Never logged or returned in API responses.
- **Phone numbers:** Treated as PII. Only returned in auth responses and used as identity.
- **Financial data:** Wallet balances and transaction amounts are stored with `DECIMAL(19,4)` precision.

## Encryption at Rest

- The MariaDB volume is stored on the Docker host’s filesystem. For production, disk encryption (LUKS, cloud KMS) would be advisable.
- No column‑level encryption is used in the MVP.

## Encryption in Transit

- Currently the backend serves HTTP (port 8080) for development. For production, an Nginx reverse proxy with TLS termination is planned.
- Database traffic between backend and MariaDB containers runs on an internal Docker network, not exposed to the host or internet.

## Secrets Management

- `jwt.secret` and database credentials are passed via environment variables in `docker-compose.yml`.
- In production, these should come from a secrets manager (Docker secrets, HashiCorp Vault, environment injection from CI/CD).

## SQL Injection Prevention

- All database operations use Spring Data JPA repositories with parameterized queries. No raw SQL concatenation.
- Hibernate generates safe queries; custom `@Query` methods use named parameters.

## Logging

- Application logs do not contain passwords or sensitive data.
- The audit log stores event payloads as JSON; care must be taken to exclude secrets from those payloads.

## CORS

- Configured to allow all origins (`*`) for development. Must be restricted to trusted domains before production.

## Data Retention

- Audit log entries are never deleted (insert‑only). This may require retention policies for compliance (GDPR, etc.).

## Vulnerability Management

- Dependencies can be scanned with OWASP Dependency‑Check (to be added in CI).
- No known vulnerable components at the time of writing, but a regular update cycle is necessary.