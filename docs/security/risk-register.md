# Risk Register

*Last updated: 2026-07-14*

## Purpose and method

This register applies a standard qualitative risk assessment (Likelihood × Impact → Risk Level), the approach commonly taught alongside frameworks like ISO 27005 and NIST SP 800-30, to Akiba Hub specifically. Each risk is scored **before** mitigation (inherent risk) and **after** the mitigations actually implemented (residual risk), so the register shows genuine risk reduction rather than just listing controls.

**Likelihood:** Low (unlikely without specific effort) · Medium (plausible, would require some effort or luck) · High (likely without intervention, e.g. automatable or trivially discoverable)
**Impact:** Low (inconvenience, no financial/data loss) · Medium (limited financial loss or data exposure, recoverable) · High (direct financial loss at scale, regulatory exposure, or systemic trust failure)
**Risk Level:** Likelihood × Impact, mapped to Low / Medium / High / Critical using the standard 3×3 matrix (Low×Low = Low; High×High = Critical; etc.)

---

## Financial integrity risks

| ID | Risk | Likelihood (inherent) | Impact | Inherent Risk | Mitigation Implemented | Likelihood (residual) | Residual Risk | Status |
|---|---|---|---|---|---|---|---|---|
| R-01 | Negative-amount input causes a withdrawal to *increase* a balance instead of decrease it | High (trivial to trigger — just send a negative number) | High (direct, unlimited fund creation) | **Critical** | `AmountValidator.requirePositive()` enforced at every money-moving entry point, plus a redundant check at `executeWithdrawal` itself (defense in depth) | Low | **Low** | Closed |
| R-02 | Concurrent requests against the same wallet cause a lost-update race, corrupting balances | Medium (requires timing — double-click, or genuine concurrent load) | High (silent balance corruption, hard to detect after the fact) | **High** | `@Version` optimistic locking on `Wallet`; concurrent writers now get `OptimisticLockingFailureException` instead of silently overwriting each other | Low | **Low** | Closed |
| R-03 | A network retry or accidental double-submit (double-click) causes a deposit/withdrawal/vote to execute twice | High (common in real usage — slow networks, impatient users) | Medium (double-charge or double-vote, recoverable but damaging to trust) | **High** | Idempotency keys on deposit, withdraw, contribute, create-proposal, and vote endpoints | Low | **Low** | Closed (requires frontend to actually send the header — not yet wired into the UI, see R-11) |
| R-04 | Wallet balance has no independent audit trail; a bug or bad actor could alter it with no way to reconstruct history | High (the *original* architecture had no protection at all) | High (undermines the core promise of a savings ledger) | **High** | Double-entry ledger (`transfers` + `ledger_entries`), insert-only at the application layer | Low | **Medium** | Partially closed — insert-only is enforced by application code, not yet by database grants (see R-12) |
| R-05 | `deleteGroup` throws an unhandled foreign-key violation, leaking a raw database error to the client | Medium (requires a group with existing proposals) | Low (crash/confusing error, not a security breach) | Medium | Typed exception hierarchy + `DataIntegrityViolationException` handler returns a generic message with a logged reference ID | Low | **Low** | Closed |

## Access control & authentication risks

| ID | Risk | Likelihood (inherent) | Impact | Inherent Risk | Mitigation Implemented | Likelihood (residual) | Residual Risk | Status |
|---|---|---|---|---|---|---|---|---|
| R-06 | Any authenticated user can view any group's member list (names + phone numbers), details, or proposals, regardless of membership (IDOR) | High (no auth check existed at all on several endpoints) | Medium (privacy breach, not direct financial loss) | **High** | Membership checks (`requireMembership()`) enforced consistently on every group/proposal-scoped read | Low | **Low** | Closed |
| R-07 | Brute-force protection on login/register is bypassed by spoofing the `X-Forwarded-For` header | High (trivial — one HTTP header) | Medium (enables credential stuffing/brute force at scale) | **High** | Only a configured, trusted reverse-proxy address has its `X-Forwarded-For` header honored | Low | **Low** | Closed |
| R-08 | A stolen JWT remains valid for its full lifetime with no way to revoke it, even after the legitimate user notices and changes their password | High (was the *default* behavior of the original 1-hour token) | Medium (extended window for account takeover) | **High** | Access tokens shortened to 15 minutes; refresh tokens are revocable and revoked automatically on password change/logout | Low | **Medium** | Substantially reduced, not eliminated — a stolen *access* token is still valid for up to 15 minutes with no revocation mechanism; this is an accepted trade-off of stateless JWTs, not an oversight |
| R-09 | `/api/auth/me` reachable without any authentication token, throwing an unhandled NPE | Medium | Low (information/availability nuisance, not a breach) | Low-Medium | Endpoint now requires authentication like the rest of the API | Low | **Low** | Closed |
| R-10 | No multi-factor authentication for high-value actions (large withdrawals, password/phone number changes) | N/A (not implemented, so risk is present by omission) | High (single-factor compromise = full account compromise) | **High** | None yet | High | **High** | **Open — flagged for next phase** |

## Operational / deployment risks

| ID | Risk | Likelihood (inherent) | Impact | Inherent Risk | Mitigation Implemented | Likelihood (residual) | Residual Risk | Status |
|---|---|---|---|---|---|---|---|---|
| R-11 | Frontend doesn't yet send `Idempotency-Key` headers, so the backend protection in R-03 isn't actually exercised by real user traffic yet | High (currently true for 100% of frontend traffic) | Medium | Medium-High | Backend support exists and is ready | N/A | **Medium-High** | **Open — frontend wiring pending** |
| R-12 | `ledger_entries` insert-only guarantee is enforced only by application code discipline, not database grants — a compromised app server or a future bug could still mutate history | Low (requires app-layer compromise or a coding mistake) | High (undermines the ledger's entire purpose as an immutable record) | Medium | None at the database-grant level yet | Low | **Medium** | **Open — recommended: `REVOKE UPDATE, DELETE` on `ledger_entries` for the application DB user** |
| R-13 | Weak default secrets (`root`/`root`, a literal JWT secret) are committed to `application.yml` in source control | Medium (requires a misconfigured deployment environment that doesn't override the env var) | High (full compromise if ever actually used) | **High** | None yet — production compose file requires the env var with no fallback, but the *checked-in default* itself remains a bad practice regardless | Medium | **High** | **Open** |
| R-14 | Database port (3306) published to the host in `docker-compose.prod.yml` | Medium (depends on host firewall/network exposure) | Medium (unnecessary attack surface — direct DB access attempts) | Medium | None yet | Medium | **Medium** | **Open** |
| R-15 | CI declares a dependency vulnerability scanner (`dependency-check-maven`) but never actually runs it | High (currently true on every build) | Medium (unknown vulnerable dependencies could ship undetected) | Medium-High | None — plugin declared but not wired into `deploy.yml` | High | **Medium-High** | **Open** |
| R-16 | No automated test suite exists (`backend/src/test` doesn't exist); CI's "test" step trivially passes with nothing to run | High | High (regressions in ledger/vote logic could reach production undetected) | **High** | Manual functional test suite exists and is documented (`docs/testing/functional_tests.md`, 34 documented manual test cases) but is not automated, not re-run on every change, and cannot catch a regression introduced after it was last executed | High | **High** | **Open — highest-priority remaining gap** |
| R-17 | No centralized monitoring/alerting; an anomaly (ledger imbalance, failed-login spike) would be discovered via user complaint, not a system alert | High | High (delayed detection directly increases breach cost per industry data — see `industry-benchmark-comparison.md`) | **High** | Structured audit logging exists; no aggregation, metrics, or alerting layer | High | **High** | **Open** |

## Regulatory / product risks

| ID | Risk | Likelihood (inherent) | Impact | Inherent Risk | Mitigation Implemented | Residual Risk | Status |
|---|---|---|---|---|---|---|---|
| R-18 | No KYC/AML process — ID number uniqueness is validated but not verified against any real identity source or sanctions list | High (currently zero verification) | High (regulatory non-compliance under Kenya's POCAMLA/CBK expectations for financial services; enables fraud/money laundering) | **Critical** | None yet | **Critical** | **Open — blocking for real-money launch** |
| R-19 | M-Pesa/Payhero integration exists only as a design document, not implemented code | N/A | N/A (not a security risk per se, but means the product cannot yet move real money at all) | N/A | Design documented (`payhero-integration.md`) | N/A | **Open — product-completeness gap, not a security defect** |

---

## Summary: risk posture at a glance

- **9 risks closed** through this hardening pass (R-01, R-02, R-03 partial, R-04 partial, R-05, R-06, R-07, R-08 substantially, R-09).
- **8 risks remain open**, of which **3 are Critical/High-and-blocking** for a real-money launch: no KYC/AML (R-18), no automated tests (R-16), no MFA (R-10).
- The pattern across closed risks: every one was fixed by adding a **structural** control (validation, locking, membership checks, typed exceptions) rather than a one-off patch — which is why several also incidentally closed related bugs (see `CHANGELOG.md` [0.2.0]).
- The pattern across open risks: none of them are *unknown* — every open item here has a specific, named next action. That's a meaningfully different position than "we haven't looked yet."