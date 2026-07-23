# PayHero Integration

## Status: DEMO / SIMULATOR + SANDBOX-READY CONTRACT

Akiba Hub deposits use an async STK-style flow. The default runtime mode is **`payments.mode=demo`** for presentation: initiate creates a `PENDING` payment, the UI shows a labeled phone PIN simulator, and credit happens only via `POST /api/payments/demo/complete`.

Live PayHero HTTP calls are **not** enabled in this cut. Env vars and the callback route document the production contract so sandbox activation does not require another redesign.

## Current endpoints

| Method | Path | Auth | Role |
|--------|------|------|------|
| POST | `/api/wallets/me/personal/deposit` | JWT + `Idempotency-Key` | Initiate STK / pending deposit (no credit) |
| GET | `/api/payments/status/{reference}` | JWT | Poll pending/completed/failed |
| POST | `/api/payments/demo/complete` | JWT | Demo-only: simulate successful IPN and credit once |
| POST | `/api/payments/callback` | Public (stub) | Production IPN contract; rejects unverified payloads until live mode |

## Demo flow (presentation)

1. User confirms amount and phone in the wallet UI.
2. Backend persists a `PENDING` payment and returns `{ reference, status, expiresAt }`.
3. UI shows “Check your phone” and a **SIMULATION** STK dialog (M-Pesa PIN is never sent to Akiba Hub APIs).
4. On simulator confirm, client calls `POST /api/payments/demo/complete` with the reference.
5. Backend credits the personal wallet once via the ledger external-movement path and marks the payment `COMPLETED`.

## Prerequisites for live / sandbox (future)

1. PayHero sandbox account **Active** (not “Merchant Account Inactive”).
2. Sandbox wallet topped up from the PayHero dashboard.
3. Set environment variables:
   - `PAYMENTS_MODE=live` (when implemented)
   - `PAYHERO_AUTH_TOKEN`
   - `PAYHERO_CHANNEL_ID`
   - `APP_BASE_URL` (e.g. `https://akiba.unitybridge.dev`)
4. Callback URL: `https://akiba.unitybridge.dev/api/payments/callback`

## Security rules

- Never collect or store the customer’s M-Pesa PIN in the app.
- Credit only after a verified payment event (demo complete or signature-verified IPN).
- Require `Idempotency-Key` on deposit initiation; claim-then-execute.
- See [SECURITY_POLICY.md](../security/SECURITY_POLICY.md).
