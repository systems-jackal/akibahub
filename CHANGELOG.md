[0.2.0] - 2026-07-14 — Security Hardening & Ledger Redesign

This release follows a full security audit of the 0.1.0 MVP and addresses every finding, plus a structural redesign of how money is recorded. See docs/security/Security-Audit-Report.md, docs/security/risk-register.md, and docs/architecture/AkibaHub-redesign.md for full detail and reasoning behind each change.

Security — Critical fixes


Fixed a live financial exploit: none of deposit, withdraw, contribute, or proposal amount validated that the value was positive. A negative withdrawal amount passed the balance check and was added to the balance instead of subtracted (AmountValidator introduced, enforced at every money-moving entry point, including a redundant defense-in-depth check at the exact point funds actually move).
Fixed a rate-limiter bypass: RateLimitFilter trusted the client-supplied X-Forwarded-For header unconditionally, so brute-force protection on /api/auth/** was defeated by spoofing that header on every request. Now only trusted, configured reverse-proxy addresses (security.trusted-proxies) have their X-Forwarded-For header honored.
Fixed stored XSS: dashboard.js, proposals.js, transactions.js, and wallet.js rendered user-controlled strings (proposal titles, transaction references) into innerHTML unescaped, while group.js/groups.js correctly escaped. escapeHtml() consolidated into utils.js as the single shared implementation and applied everywhere user content is rendered.
Fixed broken access control (IDOR): GET /api/groups/{id}, GET /api/groups/{id}/members, GET /api/groups/{id}/proposals, and GET /api/proposals/{id} had no membership check — any authenticated user (not just group members) could view any group's details, member list (names + phone numbers), and financial proposals. All four now enforce membership.
Fixed /api/auth/me being reachable without authentication: /api/auth/** was previously permitAll() in its entirety; only /register, /login, and /refresh are public now.
Fixed RateLimitFilter/JwtAuthenticationFilter ordering bug: SecurityConfig claimed rate limiting ran first but the filters were actually registered in the opposite order, so JWT processing silently ran before rate limiting.


Added — Ledger & financial integrity


Double-entry ledger (transfers + ledger_entries tables, insert-only): every deposit, withdrawal, contribution, and proposal payout now writes an immutable journal entry alongside the existing Wallet.balance field (hybrid model — balance stays the fast/authoritative read, the ledger is the source of truth for history/reconciliation/audit).
Optimistic locking on Wallet (@Version): prevents lost-update races when two requests modify the same wallet concurrently.
Idempotency keys: Idempotency-Key header support on deposit, withdraw, contribute, create-proposal, and vote endpoints. A retried request with the same key replays the original response instead of re-executing the action.
Flyway database migrations introduced (V1–V4); spring.jpa.hibernate.ddl-auto changed from update to validate — schema changes are now version-controlled and reviewable, not silently auto-generated on boot.


Added — Authentication


Refresh tokens: opaque, server-side, hashed, rotated on every use, individually revocable. Access token lifetime reduced from 1 hour to 15 minutes now that refresh tokens cover session renewal.
POST /api/auth/refresh and POST /api/auth/logout endpoints.
Password change now revokes all refresh tokens for the user, closing most of the window a stolen token would otherwise remain useful.
JWT subject changed from phone number to immutable user ID — previously, changing your phone number (a supported profile edit) silently broke your own existing session.


Changed — Error handling


Introduced a typed exception hierarchy (NotFoundException → 404, ForbiddenException → 403, ConflictException → 409, BadRequestException → 400) replacing generic RuntimeException throughout GroupService, ProposalService, and WalletService.
GlobalExceptionHandler no longer leaks raw exception messages for unexpected failures (NullPointerException, DataIntegrityViolationException, OptimisticLockingFailureException) — these are now logged server-side with a short reference ID and return a generic message to the client.


Fixed — Frontend


Frontend had no refresh-token handling at all prior to this release, which combined with the new 15-minute access token would have logged every user out every 15 minutes. api.js now stores and silently refreshes tokens on any 401, with in-flight request de-duplication.
logout() now calls the server-side /api/auth/logout endpoint to revoke refresh tokens, instead of only clearing local storage.
api.js's joinGroup() helper called an endpoint that never existed; createGroup() was missing the rules field. Both fixed and group.js/groups.js migrated off ad hoc raw fetch() calls onto the shared, refresh-aware apiFetch().
Restored the ability to vote NO on a proposal in group.js — every vote button across the app was previously hardcoded to submit YES only, despite the backend always supporting NO.
Removed frontend/js/app.js — an unreferenced, out-of-date duplicate of api.js with several of the same bugs already fixed elsewhere.


Added — Responsive design


New breakpoint (769px–1024px): sidebar collapses to a persistent icon rail (rather than the mobile off-canvas pattern) for tablet/small-laptop viewports, closing a gap where nothing was previously defined between the 768px mobile breakpoint and the 1024px desktop breakpoint.



[Unreleased]

Added


ID‑based authentication: users can register with ID number and login with phone or ID.
Consistent API response envelope (ApiResponse<T>) across all endpoints.
Dashboard aggregate endpoint (GET /api/dashboard) for faster page loads.
Personal wallet withdrawal endpoint (POST /api/wallets/me/personal/withdraw).
Group management: edit group details, delete group (owner only), group statistics.
Group invite codes: 6‑character alphanumeric codes generated on group creation; join by code.
Group rules: group creators can set rules/terms when creating a group, visible to members.
Member count with toggle: group members can see the total count without revealing the full list unless desired.
Proposal detail, edit, and delete endpoints.
Transaction history with filtering (GET /api/transactions/me with type, group, date range).
User profile update (name, phone) and password change endpoints.
Complete frontend rebuild:

Multi‑page application with persistent sidebar navigation.
New color scheme: Emerald Dynasty (Emerald Place, Champagne Gold, Ivory Silk).
Adaptive dashboard: shows personal savings only when no groups, expands to group cards once joined.
Dedicated pages for Wallet, Groups, Group Detail, Proposals, Transactions, Settings.
Phone input with +254 prefix and auto‑formatting; password visibility toggle.
Professional, clean language throughout the UI.





Changed


Backend container now runs as non‑root user akibahub (security hardening).
Group membership enforcement on all group‑scoped endpoints.
Frontend API layer handles the new ApiResponse wrapper and provides clear error messages.


Security


Rate limiting on /api/auth/** endpoints (bucket4j) to prevent brute‑force attacks.
Disabled stack traces and exception details in API error responses.
Disabled spring.jpa.open-in-view to prevent potential data leaks.
CORS restricted to https://akiba.unitybridge.dev (production domain).
Content‑Security‑Policy header added (Nginx) with strict directives.
HSTS enforced (Nginx) to ensure HTTPS.


Fixed


Hibernate proxy serialization errors by adding @JsonIgnore on lazy‑loaded fields.
Validation exceptions now return proper 400 responses with field‑level details.
Dashboard no longer shows all groups – only groups the user belongs to.
Proposals now appear on the dashboard for group members.



[0.1.0] - 2026-07-07

Added


MVP with phone‑number authentication (JWT)
Personal wallet management
Group creation and membership
Democratic proposal / voting system
Immutable audit log
Docker Compose setup with environment‑based configuration
Comprehensive functional & security test suite (34 tests)
Security documentation (threat model, OWASP mapping)


Fixed


Hibernate proxy serialization errors by adding @JsonIgnore
Validation exceptions now return proper 400 responses


Security


JWT signature validation, BCrypt password hashing
Input validation, SQL injection blocked
Good security headers (X‑Content‑Type‑Options, X‑Frame‑Options)
CORS currently permissive (restricted in production)