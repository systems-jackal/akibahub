# Security Overview

Akiba Hub is a community savings and governance platform built as a Spring Boot monolith. The MVP implements fundamental security controls to protect user data and financial operations.

## Key Security Features

- **Phone‑number based authentication** – Users register and log in using their phone number and password.
- **JWT (JSON Web Token)** – Stateless session management with HMAC‑SHA256 signed tokens.
- **BCrypt password hashing** – Passwords are never stored in plain text.
- **Role‑based access control** – Public endpoints (`/api/auth/**`) vs. authenticated endpoints.
- **Immutable audit log** – All critical actions are recorded in an insert‑only database table.
- **Network isolation** – Database and backend run on a dedicated Docker network, not exposed externally.
- **Environment‑based secrets** – Database credentials and JWT secret are injected via environment variables.

## Architecture (Security View)

```text
Client ── HTTPS ──> Nginx (future) ──> Spring Boot Backend ──> MariaDB (Docker network)