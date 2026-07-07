# Authentication Design

## Overview

Users authenticate with their **phone number** and a **password**. On successful registration or login, a signed JWT is issued. The token must be sent in the `Authorization: Bearer <token>` header for all protected requests.

## Registration Flow

1. Client sends `POST /api/auth/register` with `phoneNumber`, `password`, and `fullName`.
2. Server validates input (phone format: `^\+?[0-9]{7,15}$`, password min 6 chars).
3. If phone number already exists → 400 error.
4. Password is hashed with **BCrypt** (strength 10).
5. A new `User` entity is persisted, and a personal `Wallet` is created automatically.
6. Server returns a JWT containing the phone number as subject.

## Login Flow

1. Client sends `POST /api/auth/login` with `phoneNumber` and `password`.
2. Server fetches user by phone number; if not found → 401.
3. BCrypt verifies the provided password against the stored hash.
4. On success, a new JWT is issued and returned.

## JWT Specification

- **Signing algorithm:** HMAC‑SHA256 (`HS256`)
- **Claims:**
  - `sub`: phone number (e.g., `+254711111111`)
  - `iat`: issued at timestamp
  - `exp`: expiration timestamp
- **Expiration:** 1 hour (`jwt.expiration=3600000`)
- **Secret key:** Configured via `jwt.secret` property (currently a placeholder; must be rotated in production)

## Token Validation

- `JwtAuthenticationFilter` extracts the token from the `Authorization` header.
- The token is parsed and verified using the secret key.
- If valid, the user is loaded from the database and set in the security context.
- No roles/authorities are assigned (all authenticated users are equal).

## Security Considerations

- **Brute‑force protection:** None implemented yet. Rate limiting on `/api/auth/**` should be added.
- **Account lockout:** Not present. A possible future enhancement.
- **Token storage:** Recommended to store tokens in memory (e.g., JavaScript variable) rather than `localStorage` to mitigate XSS risks.
- **No refresh tokens:** Users must re‑authenticate after token expiry.
- **Secret management:** The JWT secret must be injected from a secure vault (e.g., Docker secrets, HashiCorp Vault) in production.

## Planned Improvements

- Add refresh token rotation.
- Implement multi‑factor authentication (OTP via SMS) for sensitive operations (withdrawals).
- Integrate OAuth2 with Google for alternative login.