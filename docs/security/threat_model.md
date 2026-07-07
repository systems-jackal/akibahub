# Threat Model (STRIDE)

## Assets

- User credentials (phone number, password hash)
- Wallet balances and transaction history
- Group membership and governance records (proposals, votes)
- Audit log integrity

## Threat Analysis

| Threat Category        | Example Threat                                      | Mitigation in MVP                                      | Gaps / Future Work                                     |
|------------------------|-----------------------------------------------------|--------------------------------------------------------|--------------------------------------------------------|
| **Spoofing**           | Attacker impersonates a user by stealing JWT        | JWT signed with HMAC‑SHA256; token validation          | No token revocation; stolen token can be used until expiry |
| **Tampering**          | Modify wallet balance in transit/database            | BCrypt for passwords; database access restricted; transactions in services | No transport encryption (HTTP); DB not encrypted at rest |
| **Repudiation**        | User denies making a transaction                    | Immutable audit log records all actions with timestamps | Audit log not cryptographically verifiable             |
| **Information Disclosure** | Expose phone numbers via API responses            | Only authenticated endpoints return user data; `@JsonIgnore` on lazy fields prevents accidental serialization | API responses could be filtered; need proper DTOs      |
| **Denial of Service**  | Overwhelm API with registration/login requests      | No rate limiting implemented; Docker restart policy helps | Need rate limiting, circuit breakers                   |
| **Elevation of Privilege** | Non‑member votes on a proposal                  | Service‑layer checks enforce group membership           | No role separation; any authenticated user can create groups |

## Additional Risks

- **Dependency vulnerabilities:** Outdated libraries (Spring Boot, MariaDB driver) could be exploited. Mitigation: regular updates and OWASP Dependency‑Check in CI.
- **Container escape:** Docker breakout could compromise host. Mitigation: run containers as non‑root user (not yet implemented).