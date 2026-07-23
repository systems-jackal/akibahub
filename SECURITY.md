# Security

Akiba Hub takes financial integrity and user privacy seriously. This file explains how to report vulnerabilities and where policy lives.

## Policy and risk posture

- **[Security policy](docs/security/SECURITY_POLICY.md)** — asset classification, principles, do-not-change controls, payment modes
- **[Risk register](docs/security/risk-register.md)** — scored risks and mitigations
- **[Security docs index](docs/security/README.md)** — authentication, authorization, threat model, OWASP mapping

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security defects that could enable fund creation, account takeover, or PII exposure.

Instead, contact the maintainers privately (repository owner / `systems-jackal` on GitHub) with:

1. Description of the issue and affected endpoints or components
2. Steps to reproduce (or a proof of concept that does not target production balances)
3. Suggested impact and, if known, a fix approach

We aim to acknowledge reports within a few business days and will coordinate disclosure after a fix is available.

## Scope notes

- **In scope:** Authentication, authorization, wallet/ledger integrity, payment initiation/callback handling, secrets handling, XSS/CSRF against the hosted SPA, deployment misconfiguration that exposes secrets or the database.
- **Out of scope:** Denial of service against third-party networks; social engineering; issues that require physical access to an already-compromised host; collecting or “testing” real M-Pesa PINs (Akiba Hub never handles M-Pesa PINs).

## Production expectations

- Secrets via environment variables only (`JWT_SECRET`, database credentials, PayHero tokens when live)
- TLS terminated at the reverse proxy (Nginx on the VPS)
- Database not published on the public host interface in production Compose
