# Changelog

## [Unreleased]

### Added
- **ID‑based authentication**: users can register with ID number and login with phone or ID.
- **Consistent API response envelope** (`ApiResponse<T>`) across all endpoints.
- **Dashboard aggregate endpoint** (`GET /api/dashboard`) for faster page loads.
- **Personal wallet withdrawal** endpoint (`POST /api/wallets/me/personal/withdraw`).
- **Group management**: edit group details, delete group (owner only), group statistics.
- **Group invite codes**: 6‑character alphanumeric codes generated on group creation; join by code.
- **Group rules**: group creators can set rules/terms when creating a group, visible to members.
- **Member count with toggle**: group members can see the total count without revealing the full list unless desired.
- **Proposal detail, edit, and delete** endpoints.
- **Transaction history with filtering** (`GET /api/transactions/me` with type, group, date range).
- **User profile update** (name, phone) and **password change** endpoints.
- **Complete frontend rebuild**:
  - Multi‑page application with persistent sidebar navigation.
  - New color scheme: Emerald Dynasty (Emerald Place, Champagne Gold, Ivory Silk).
  - Adaptive dashboard: shows personal savings only when no groups, expands to group cards once joined.
  - Dedicated pages for Wallet, Groups, Group Detail, Proposals, Transactions, Settings.
  - Phone input with +254 prefix and auto‑formatting; password visibility toggle.
  - Professional, clean language throughout the UI.

### Changed
- Backend container now runs as non‑root user `akibahub` (security hardening).
- Group membership enforcement on all group‑scoped endpoints.
- Frontend API layer handles the new `ApiResponse` wrapper and provides clear error messages.

### Security
- Rate limiting on `/api/auth/**` endpoints (bucket4j) to prevent brute‑force attacks.
- Disabled stack traces and exception details in API error responses.
- Disabled `spring.jpa.open-in-view` to prevent potential data leaks.
- CORS restricted to `https://akiba.unitybridge.dev` (production domain).
- Content‑Security‑Policy header added (Nginx) with strict directives.
- HSTS enforced (Nginx) to ensure HTTPS.

### Fixed
- Hibernate proxy serialization errors by adding `@JsonIgnore` on lazy‑loaded fields.
- Validation exceptions now return proper 400 responses with field‑level details.
- Dashboard no longer shows all groups – only groups the user belongs to.
- Proposals now appear on the dashboard for group members.

---

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