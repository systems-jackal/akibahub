# PayHero Integration – Preparation & Challenges

## Account Setup
- Created a PayHero sandbox account and obtained API credentials (Basic Auth token, Channel ID 10260).
- Registered a payment channel (National Bank) and topped up the sandbox wallet.

## Integration Completed
- Built `PayHeroService` to initiate STK Push deposits and process IPN callbacks.
- Created `PendingTransaction` entity to track in‑flight payments with idempotency.
- Added `PayHeroController` for the IPN callback endpoint (`POST /api/payments/callback`).
- Modified `WalletController` deposit endpoint to call PayHero and return external references.
- All code compiled and tested locally.

## Challenges Encountered
1. **Sandbox Activation**: PayHero returned "Merchant Account Inactive" despite topping up the wallet. This blocked live STK Push testing in the sandbox.
2. **Environment Variables**: The Spring Boot process needed the `PAYHERO_AUTH_TOKEN` and `PAYHERO_CHANNEL_ID` exported in the same terminal session; missing exports led to 401 errors.
3. **Local Port Conflict**: Kali's built‑in MariaDB service held port 3306, preventing the Docker MariaDB container from starting. Resolved by stopping the local service.
4. **Database Volume Wipe**: Repeated `docker-compose down -v` required re‑registering test users.

## Current Status
- **Code is complete and verified** (build success, endpoint logic correct).
- **Waiting on PayHero sandbox activation** to complete end‑to‑end testing with real STK Push.
- The integration has been reverted from the main branch to keep the codebase clean for the Harambee module.
- All integration code is preserved in the Git history and can be reapplied when the sandbox is active.

## Next Steps
- Contact PayHero support to activate the sandbox merchant account.
- Once activated, re‑apply the integration code and test with real sandbox numbers.
- Deploy to production with live PayHero credentials.
