# OWASP Top 10 (2021) Mapping

| # | Risk | How Akiba Hub Addresses It | Status / Notes |
|---|------|----------------------------|----------------|
| A01 | **Broken Access Control** | Spring Security with JWT; service‑layer checks for group membership and wallet ownership. | No role‑based access control yet. |
| A02 | **Cryptographic Failures** | Passwords hashed with BCrypt; JWT signed with HMAC‑SHA256. | Need to enforce HTTPS in production. |
| A03 | **Injection** | JPA parameterized queries prevent SQL injection. No raw SQL used. | No other injection vectors (LDAP, XPath) present. |
| A04 | **Insecure Design** | Security requirements considered during architecture (audit log, isolated network). | Missing threat modeling during initial design (now documented). |
| A05 | **Security Misconfiguration** | CORS restricted (currently wide open for dev); Docker containers use environment variables, not hardcoded secrets. | Need to disable default error stack traces and secure Swagger in production. |
| A06 | **Vulnerable and Outdated Components** | Spring Boot 3.3.0, MariaDB 10.11, recent libraries. | No automated dependency scanning; plan to add OWASP Dependency‑Check in CI. |
| A07 | **Identification and Authentication Failures** | Password strength validation (min 6 chars); JWT expiration. | No account lockout, no brute‑force protection. |
| A08 | **Software and Data Integrity Failures** | No CI/CD pipeline yet; manual builds. | Plan to introduce signed commits and verify JAR integrity. |
| A09 | **Security Logging and Monitoring Failures** | Comprehensive audit logging; application logs generated. | No centralized monitoring or alerting. |
| A10 | **Server‑Side Request Forgery (SSRF)** | No URLs are fetched from user input; low risk. | Future integration with PayHero may introduce SSRF risk if callbacks are not validated. |