# Akiba Hub Security Policy

*Last updated: 2026-07-23*

This policy defines how Akiba Hub classifies assets, which principles govern money and identity paths, and which controls must not be weakened casually. It is the presentation-facing companion to the [risk register](risk-register.md) and [threat model](threat_model.md).

## Asset classification

| Class | Examples | Handling |
|-------|----------|----------|
| **Critical secrets** | `JWT_SECRET`, DB passwords, `PAYHERO_AUTH_TOKEN` | Env-only; never commit; fail closed if missing in production |
| **Financial integrity** | Wallet balances, `transfers`, `ledger_entries`, pending payments | Append-only journal; credit only after verified payment event; idempotent money ops |
| **PII** | Phone numbers, names, national ID | Authenticated access; membership checks on group-scoped reads; no PII in public logs |
| **Session artifacts** | Access JWTs, refresh tokens | Short-lived access tokens; refresh tokens hashed at rest; revoke on logout/password change |
| **Payment references** | STK external refs, callback payloads | Correlate to pending rows; never treat client claims as proof of payment |
| **M-Pesa PIN** | PIN entered on the customer's phone | **Never collected, transmitted, or stored by Akiba Hub** |

## Security principles

1. **Least privilege** — Authenticated by default; public routes are an explicit allowlist (`/api/auth/register`, `/login`, `/refresh`, `/health`, payment callback stub only).
2. **Fail-closed secrets** — Production must not boot with checked-in default JWT or DB credentials.
3. **Never store M-Pesa PIN** — App account password ≠ M-Pesa PIN. Demo STK overlays are labeled simulations and do not represent real PIN capture.
4. **Verify-then-credit** — Deposit initiation creates a `PENDING` payment; wallet credit happens only after a confirmed event (demo complete or verified PayHero IPN).
5. **Append-only ledger** — Do not update or delete `ledger_entries`; extend with new transfers/statuses instead.
6. **Idempotent money operations** — Money-moving POSTs require `Idempotency-Key`; claim-then-execute, never execute-then-store.
7. **Trust boundaries** — Honor `X-Forwarded-For` only from configured trusted proxies.
8. **Defense in depth on amounts** — `AmountValidator.requirePositive()` at every money entry point.

## Do-not-change controls

These were hardened deliberately; regressions are high-impact:

| Control | Why it stays |
|---------|----------------|
| JWT subject = user id (not phone) | Phone changes must not invalidate identity |
| Rate-limit filter before JWT filter | Auth brute-force must apply before token work |
| Narrow `permitAll` (not `/api/auth/**`) | Prevents unauthenticated access to `/me` and siblings |
| Refresh tokens hashed + revoked on password change | Session kill on credential change |
| Flyway + `ddl-auto: validate` | Schema changes are reviewed, not silent |
| Wallet `@Version` optimistic locking | Prevents lost-update races |
| Membership checks on group/proposal reads | Closes IDOR on member PII and proposals |
| Trusted-proxy-aware XFF | Prevents rate-limit bypass |

## Payment modes

| Mode | Behavior |
|------|----------|
| `demo` (default for presentation) | STK initiate → PENDING; labeled phone PIN simulator; `POST /api/payments/demo/complete` credits once |
| `live` (future) | PayHero STK + signature-verified callback; demo-complete disabled |

Callback URL contract (sandbox/production): `POST /api/payments/callback` with PayHero env vars `PAYHERO_AUTH_TOKEN`, `PAYHERO_CHANNEL_ID`, `APP_BASE_URL`.

## Launch blockers (real money)

Documented as open in the risk register — not optional for a production money launch:

- **R-10** Multi-factor / step-up for high-value actions
- **R-16** Automated regression suite for ledger and payments (partially addressed for deposit path)
- **R-18** KYC/AML under Kenyan POCAMLA/CBK expectations

## Reporting

See the root [SECURITY.md](../../SECURITY.md) for how to report vulnerabilities.
