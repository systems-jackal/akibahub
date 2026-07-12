# Akiba Hub — Proposed Redesign for Fintech-Grade Standards

This is a target-state proposal, not a rewrite mandate. Treat it as a menu: some of it (ledger model, API conventions, idempotency) you should do soon regardless of scale; some of it (microservices, multi-region) only makes sense once you have the user volume or regulatory pressure to justify the complexity. Where relevant, this assumes you're operating in Kenya with M-Pesa as the payment rail, since that shapes several decisions below.

---

## 1. Guiding Principles

A fintech system is judged differently from a normal CRUD app. The four things that matter most:

1. **Money is never mutated, only recorded.** Balances are a *derived view* of an immutable transaction ledger, not a field you increment/decrement in place.
2. **Every state-changing request is idempotent.** Networks retry. Users double-tap. M-Pesa sends duplicate callbacks. If a repeated request can double-charge someone, the design is wrong.
3. **Authorization is deny-by-default and checked at the data layer, not just the controller.** IDORs (like the ones in the audit) happen when this discipline slips.
4. **Every privileged action is traceable to a person, a time, and a reason**, and that trail can't be edited by the app itself.

Everything below serves one of these four goals.

---

## 2. System Architecture

### 2.1 Keep it a modular monolith — for now
Given current scale, microservices would add operational cost (service discovery, distributed tracing, network failure modes, deployment complexity) without a corresponding benefit. The right move is **tightening the module boundaries you already have** so a future split is easy if you ever need it:

```
com.akibahub
 ├── identity/       (auth, users, sessions, MFA)
 ├── ledger/         (accounts, entries, transfers — the money core)
 ├── groups/         (chama, membership, roles)
 ├── governance/     (proposals, voting)
 ├── payments/       (M-Pesa integration, webhooks, reconciliation)
 ├── notifications/  (SMS/email/push)
 └── platform/       (audit, rate-limiting, shared kernel, config)
```

Rules to enforce (with ArchUnit tests, not just convention):
- `ledger` has no dependency on `groups` or `governance` — it only knows about "accounts" and "entries," not what a group or a proposal is. Group and proposal-driven transfers call *into* the ledger, never the reverse.
- No module reaches into another module's repositories directly; cross-module calls go through a public service interface.
- `payments` is the only module allowed to talk to M-Pesa's Daraja API.

This alone would have prevented several of the audit findings (e.g., wallet balance mutation logic duplicated across `WalletService` and `ProposalService`).

### 2.2 When to actually split into services
Split `payments` out first if/when you need independent scaling or separate on-call ownership for the M-Pesa integration (webhook volume, retry logic, and reconciliation jobs tend to be the first thing that outgrows a monolith). Everything else can wait.

### 2.3 Environments
Three real environments, not one config file pretending to be all of them:
- **dev** — local Docker Compose, seeded fake data, relaxed CORS, `ddl-auto: validate` against Flyway-managed schema (never `update`, even in dev — you want migrations exercised from day one).
- **staging** — mirrors prod topology, uses M-Pesa's sandbox, receives every deploy before prod.
- **prod** — locked down, secrets from a vault, no direct DB access from developer machines.

---

## 3. Data Model: Double-Entry Ledger

This is the single highest-leverage change. Today, `Wallet.balance` is a mutable integer field that services increment/decrement directly. That pattern is why the negative-amount bug from the audit was even possible, why there's no way to reconstruct "what was the balance on March 3rd," and why reconciliation against M-Pesa will be painful.

### 3.1 Proposed schema

```sql
accounts
  id            BIGINT PK
  owner_type    ENUM('USER','GROUP')
  owner_id      BIGINT
  account_type  ENUM('PERSONAL','GROUP_SAVINGS')
  currency      CHAR(3)              -- 'KES', future-proofs multi-currency
  status        ENUM('ACTIVE','FROZEN','CLOSED')
  created_at    DATETIME

ledger_entries
  id              BIGINT PK
  transfer_id     BIGINT FK -> transfers.id
  account_id      BIGINT FK -> accounts.id
  direction       ENUM('DEBIT','CREDIT')
  amount          DECIMAL(19,4)      -- always positive; direction carries the sign
  balance_after   DECIMAL(19,4)      -- snapshot, makes audits/statements O(1)
  created_at      DATETIME
  -- ledger_entries is INSERT-ONLY. No UPDATE, no DELETE. Enforced by DB grants, not just app code.

transfers
  id              BIGINT PK
  type            ENUM('DEPOSIT','WITHDRAWAL','CONTRIBUTION','PROPOSAL_PAYOUT','MPESA_TOPUP', ...)
  status          ENUM('PENDING','COMPLETED','FAILED','REVERSED')
  idempotency_key VARCHAR(64) UNIQUE
  initiated_by    BIGINT FK -> users.id
  reference       VARCHAR(255)
  external_ref    VARCHAR(255)       -- M-Pesa transaction ID, etc.
  created_at      DATETIME
```

Every money movement creates **exactly one `transfers` row and at least two balanced `ledger_entries` rows** (a debit and a credit that sum to zero) inside a single DB transaction. A "current balance" is either:
- a materialized `balance_after` on the account's most recent entry (fast path, read directly), or
- `SUM(credits) - SUM(debits)` recomputed from entries (slow path, used for reconciliation/audits to catch drift).

Benefits this buys you immediately:
- **The negative-amount bug becomes structurally impossible** — `amount` is validated positive at insert, and direction is a separate enum, not a sign.
- **Full point-in-time history for free** — "what was my balance on any given date" is a query, not a feature you have to build.
- **Reconciliation against M-Pesa is a diff of two ledgers**, not a manual balance check.
- **Regulators/auditors expect this shape.** Any real fintech audit (CBK, or an investor's diligence) will ask "show me your ledger," and "we mutate an integer column" is not an acceptable answer.

### 3.2 Concurrency
Every transfer is one `SERIALIZABLE`-or-`REPEATABLE READ` DB transaction that inserts the `transfers` row and both `ledger_entries` rows together, with `SELECT ... FOR UPDATE` on the account rows involved, ordered consistently by `account_id` to avoid deadlocks between two transfers touching the same pair of accounts in opposite order.

### 3.3 Money type
Keep `DECIMAL(19,4)`, never floating point — already correct in the current schema, keep doing this everywhere new code touches money.

---

## 4. Payments Integration (M-Pesa) — Reliability Patterns

This is where most fintech incidents actually happen, and the current `docs/architecture/payhero-integration.md` design should be hardened with:

1. **Idempotency keys on every STK Push initiation.** Client generates a UUID, sends it as `Idempotency-Key`; server stores it on the `transfers` row before calling Daraja, and any retry with the same key returns the original result instead of double-initiating.
2. **Webhook signature verification.** Don't trust an inbound M-Pesa callback just because it hit your endpoint — verify it's genuinely from Safaricom (IP allowlist + payload validation) and match it to a `PENDING` transfer by its checkout request ID before mutating anything.
3. **Outbox pattern for callback processing.** Write the raw callback payload to an `inbox` table first (fast, always succeeds), process it asynchronously, and make processing idempotent by transfer ID — so a duplicate callback (M-Pesa does send these) is a no-op, not a double-credit.
4. **Reconciliation job.** A scheduled job that pulls M-Pesa's transaction statement and diffs it against your `transfers` table, flagging anything that's `PENDING` past a timeout or present on one side but not the other.
5. **Explicit state machine for transfers:** `PENDING → COMPLETED` or `PENDING → FAILED`, with `REVERSED` as a distinct state for manual corrections — never silently re-mutate a `COMPLETED` transfer.

---

## 5. API Design

### 5.1 Versioning & structure
Prefix everything with `/api/v1/...` from the start — you have no versioning today, which means any breaking change forces every client (including your own frontend) to update in lockstep. Cheap to add now, expensive to retrofit later.

### 5.2 Resource-oriented, consistent verbs
Current API is already mostly RESTful; tighten a few things:

| Current | Issue | Proposed |
|---|---|---|
| `POST /api/wallets/me/personal/deposit` | Verb in the URL | `POST /api/v1/accounts/{id}/transfers` with `{"type":"DEPOSIT","amount":...}` |
| `POST /api/groups/{id}/invite` triggered on page load | State-changing GET-like usage | `GET /api/v1/groups/{id}/invite-code` (read existing) + `POST /api/v1/groups/{id}/invite-code:rotate` (explicit regen) |
| `PUT /api/users/me` with raw `Map<String,String>` | No schema, no validation | Typed `UpdateProfileRequest` DTO, `PATCH` semantics (partial update) instead of `PUT` |

### 5.3 Idempotency as a first-class API concept
Any `POST` that moves money or creates a governance action (deposit, withdraw, contribute, create proposal, cast vote) should accept an `Idempotency-Key` header. Server stores `(key, request_hash, response)` and replays the stored response on a retry with the same key instead of re-executing — this is standard practice at Stripe/Paystack/M-Pesa-integrated fintechs and directly prevents double-submit bugs like the ones in the current frontend (serial awaits, double-click on "Contribute").

### 5.4 Consistent error envelope
Keep the existing `ApiResponse<T>` shape, but standardize error bodies with a machine-readable code, not just a human message:

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_FUNDS",
    "message": "Your personal wallet balance is too low for this withdrawal.",
    "traceId": "a1b2c3d4"
  }
}
```

`traceId` ties back to server-side structured logs so support/engineering can pull the exact request without needing the user to reproduce anything. `code` lets the frontend branch on error type without string-matching `message`.

### 5.5 Pagination & filtering
Every list endpoint (`/transactions`, `/proposals`, `/groups/{id}/members`) should support `?page=&size=&sort=` with a consistent envelope (`{ items, page, size, totalItems, totalPages }`) — today everything returns the full result set unbounded.

### 5.6 OpenAPI as the source of truth
`springdoc-openapi` is already a dependency — actually publish and version the generated spec, and consider generating the frontend's `api.js` client from it (or at least validating requests/responses against it in CI) so frontend and backend can't silently drift apart, which is already happening today (`api.js` vs. the dead `app.js`).

---

## 6. AuthN / AuthZ

### 6.1 Authentication
- Move JWT subject from **phone number** (mutable) to **immutable numeric user ID**.
- Add a short-lived **access token** (10–15 min) + a longer-lived, rotating **refresh token** stored server-side (hashed) so tokens can actually be revoked — solves the "no logout/invalidate on password change" gap from the audit.
- Add optional **step-up authentication (OTP via SMS)** for high-risk actions specifically: large withdrawals, proposal creation above a threshold, changing phone number/password. This is standard for Kenyan fintech (M-Pesa itself does this) and something your own docs already flag as a planned improvement.
- Store tokens client-side in memory + a short-lived httpOnly cookie for refresh, not `localStorage`, closing the XSS-to-takeover path identified in the audit.

### 6.2 Authorization
Introduce real roles, even if only two exist today (`MEMBER`, `GROUP_ADMIN`) plus a resource-scoped check pattern:
- Every group-scoped endpoint uses a shared `@PreAuthorize` expression or a method-level guard (`groupAccessGuard.requireMember(groupId, principal)`) rather than ad hoc `if (!membership...) throw` scattered per-method — this is exactly what's inconsistent today (stats checks membership, member-list doesn't).
- Add a `SUPER_ADMIN`/`SUPPORT` role for internal staff with its own audited endpoints, separate from member-facing ones, once you need customer support tooling.

### 6.3 Rate limiting, correctly
Rate limit by authenticated user ID where a token is present (not just IP), in addition to fixing the `X-Forwarded-For` trust issue from the audit — IP-only limiting is easy to spread across many source IPs, but a user ID-based limit follows the account.

---

## 7. Security & Compliance Baseline

For a Kenyan fintech handling personal financial data, the practical compliance targets are:

- **Kenya Data Protection Act (2019):** you're processing ID numbers, phone numbers, and financial data — this is "sensitive personal data" under the Act. You need a documented data retention policy, a registered Data Protection Officer if scale requires it, and demonstrable consent/purpose limitation for what you collect.
- **CBK / PSP guidelines**, if you ever hold float directly rather than just orchestrating M-Pesa transfers — worth a legal read before scaling, since "holding pooled customer funds" triggers different regulatory treatment than "facilitating transfers."
- **PCI-DSS** only becomes relevant if you ever touch card data directly; if payments stay M-Pesa-only, you're mostly exempt, but keep this in mind if you add card top-ups later.

Concrete engineering baseline to get there:
1. **Secrets management** — move JWT secret, DB creds, M-Pesa API keys out of env vars-with-defaults into a real secrets store (Docker Secrets, AWS Secrets Manager, HashiCorp Vault, or even a `.env` that's never given a fallback in `application.yml`, per the audit).
2. **Encryption at rest** for the database volume, and TLS everywhere in transit (already implied by the Nginx reverse proxy — confirm HSTS is enforced there).
3. **PII minimization in logs** — audit log payloads currently JSON-serialize entire entities; make sure password hashes, tokens, and full ID numbers are never written to `audit_log` or application logs. Mask ID numbers (e.g., store/display only last 4 digits) anywhere they don't need to be shown in full.
4. **Tamper-evident audit trail** — your own docs already note the audit log isn't cryptographically protected. Add a hash-chain column (`prev_hash`, `this_hash = hash(prev_hash + payload)`) so tampering is at least *detectable*, even without full blockchain-style immutability.
5. **Dependency & image scanning in CI** — you already have `dependency-check-maven` as a plugin but it isn't wired into the pipeline; actually run it, plus a container scan (Trivy/Grype) before push.
6. **Structured logging + centralized log aggregation** (even a simple ELK/Loki stack) so a `traceId` (see §5.4) can be searched across services during an incident.

---

## 8. Observability

Currently there's a `/health` endpoint and nothing else. Minimum viable observability for a money-moving system:

- **Metrics:** request rate/latency/error-rate per endpoint (Micrometer + Prometheus is a two-dependency addition to a Spring Boot app), plus business metrics — total ledger volume/day, failed M-Pesa callbacks, proposal approval rate.
- **Structured logs** with a request-scoped `traceId`/`spanId` (Spring Cloud Sleuth or Micrometer Tracing), propagated into the error envelope from §5.4.
- **Alerting** on the things that actually matter for a fintech: ledger imbalance (sum of all entries ≠ 0), M-Pesa callback failure rate spike, auth failure rate spike (credential stuffing signal), and rate-limiter bypass patterns (many requests, many distinct claimed IPs, same underlying TCP source).

---

## 9. Testing Strategy

- **Unit tests** on ledger arithmetic and vote-tallying logic specifically — these are the two places where an off-by-one or sign error costs real money or breaks governance.
- **Integration tests** against a real (Testcontainers) MariaDB instance for every money-moving flow, including the concurrency scenario from §3.2 (two simultaneous transfers on the same account).
- **Contract tests** against the OpenAPI spec so frontend/backend can't silently drift.
- **A dedicated "adversarial" test suite** that specifically re-tests every item in the audit report (negative amounts, IDOR on group endpoints, rate-limit bypass via spoofed headers) as regression tests, so these classes of bug can't come back unnoticed.

---

## 10. Suggested Sequencing

This is a lot — realistically, layer it in roughly this order, building on the bug-fix work already in progress:

1. Ship the audit fixes (already scoped).
2. Introduce the double-entry ledger model (§3) — this is the biggest structural win and makes several other items easier (reconciliation, point-in-time balances, regulator-readiness).
3. Idempotency keys on money-moving and governance endpoints (§3.4, §5.3) — directly prevents double-submit bugs, cheap to add once the ledger exists.
4. AuthN/AuthZ hardening (§6) — refresh tokens, immutable JWT subject, consistent membership guards.
5. API conventions (§5) — versioning, error envelope, pagination — do this alongside the frontend/UI refresh you mentioned, since the frontend will need to adapt anyway.
6. Observability + compliance baseline (§7, §8) — ongoing, not a single milestone.

Happy to turn any single section here into an actual implementation plan (migrations, code skeletons, etc.) whenever you're ready to start on it.