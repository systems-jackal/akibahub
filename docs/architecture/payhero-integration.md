# PayHero Integration

## Status: CODE-COMPLETE (pending sandbox account activation)

The Akiba Hub backend fully integrates PayHero for MPESA deposits.
- STK Push initiation endpoint: POST /api/wallets/me/personal/deposit
- IPN callback endpoint: POST /api/payments/callback
- PendingTransaction entity tracks all in‑flight payments
- All deposit events are audit‑logged

## Prerequisites for Live Testing
1. PayHero sandbox account must be **Active** (not "Merchant Account Inactive").
2. The sandbox wallet must have a small balance (top‑up from dashboard).
3. Use the Basic Auth token and Channel ID from the dashboard.

## Test Flow
1. Register user → login → initiate deposit (STK Push).
2. If sandbox supports live STK, the user's phone will receive an MPESA prompt.
3. Simulate the IPN callback with the ExternalReference to credit the wallet.
4. Wallet balance updates on successful callback.

## Production Deployment
- Set `PAYHERO_AUTH_TOKEN`, `PAYHERO_CHANNEL_ID`, and `APP_BASE_URL` as environment variables.
- The callback URL will be `https://akiba.unitybridge.dev/api/payments/callback`.