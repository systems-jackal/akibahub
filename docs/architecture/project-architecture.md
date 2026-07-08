# 🏗️ Architecture

## Overview

Akiba Hub is a student financial companion platform that provides **personal savings**, **group savings (chamas)**, **democratic governance**, and **community fundraising (Harambee)**.

The application follows a **monolithic Spring Boot backend** with a **Vanilla JavaScript frontend**, currently deployed on a **single VPS**.

---

# 💻 Technology Stack

| Layer | Technology |
|--------|------------|
| **Backend** | Java 21, Spring Boot 3.3, Spring Security, JPA/Hibernate |
| **Database** | MariaDB 10.11 |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Security** | JWT (access tokens), BCrypt, rate limiting (bucket4j) |
| **Payments** | PayHero *(integration prepared, pending sandbox activation)* |
| **Deployment** | Docker, Docker Compose, Nginx, Let's Encrypt |
| **CI/CD** | GitHub Actions *(builds, tests, pushes Docker image, deploys to VPS)* |

---

# 🌐 System Diagram

```text
                     Browser / Mobile Client
                              │
                              ▼
                 Nginx (Reverse Proxy + SSL)
                              │
          ┌───────────────────┴───────────────────┐
          │                                       │
          ▼                                       ▼
 /api/* → Spring Boot Backend (:8080)      /* → Static Frontend
                                           (/var/www/akibahub/frontend)
```

---

# 📦 Backend Package Structure

```text
com.akibahub
│
├── AkibaHubApplication.java
├── audit
│   └── Immutable event logging
│
├── auth
│   └── Authentication (JWT, login, register)
│
├── config
│   └── Security, JWT filter, rate limiting
│
├── dashboard
│   └── Aggregated dashboard data
│
├── group
│   └── Groups, members, wallets, invitations
│
├── proposal
│   └── Proposals, voting, execution
│
├── shared
│   └── ApiResponse wrapper, exception handling
│
├── transaction
│   └── Transaction history with filters
│
├── user
│   └── User entity, profile, password
│
└── wallet
    └── Personal & group wallets, deposits, withdrawals, contributions
```

---

# ⚙️ Key Design Decisions

- **Monolith first** – simple to develop and deploy; extraction into microservices planned for future scaling.
- **ApiResponse wrapper** – every endpoint returns `{ success, message, data }` for consistency.
- **Membership-based access** – all group-scoped operations check that the user is a member.
- **Immutable audit log** – every financial and governance action is recorded as an insert-only event.
- **Rate limiting** – applied to `/api/auth/**` to prevent brute-force attacks.
- **Non-root Docker user** – backend container runs as `akibahub`.