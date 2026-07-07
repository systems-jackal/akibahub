# Authorization & Access Control

## Model

The MVP does not implement fine‑grained roles (admin, member, etc.). Authorization is simplified to two levels:

- **Public endpoints** – accessible without authentication.
- **Authenticated endpoints** – require a valid JWT.

## Public Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`

All other endpoints require authentication.

## Enforcement

Spring Security’s `SecurityFilterChain` defines:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated()
)