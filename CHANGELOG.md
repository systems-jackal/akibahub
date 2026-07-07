# Changelog

## [0.1.0] - 2026-07-07
### Added
- MVP with phone‑number authentication (JWT)
- Personal wallet management
- Group creation and membership
- Democratic proposal / voting system
- Immutable audit log
- Docker Compose setup with environment‑based configuration
- Comprehensive functional & security test suite (34 tests)
- Security documentation (threat model, OWASP mapping)

### Fixed
- Hibernate proxy serialization errors by adding @JsonIgnore
- Validation exceptions now return proper 400 responses

### Security
- JWT signature validation, BCrypt password hashing
- Input validation, SQL injection blocked
- Good security headers (X‑Content‑Type‑Options, X‑Frame‑Options)
- CORS currently permissive (restricted in production)


## [Unreleased]
### Changed
- Backend container now runs as non‑root user `akibahub` (security hardening)

### Added
- Rate limiting on `/api/auth/**` endpoints (bucket4j) to prevent brute‑force attacks
