<div align="center">

```
 █████╗ ██╗  ██╗██╗██████╗  █████╗     ██╗  ██╗██╗   ██╗██████╗
██╔══██╗██║ ██╔╝██║██╔══██╗██╔══██╗    ██║  ██║██║   ██║██╔══██╗
███████║█████╔╝ ██║██████╔╝███████║    ███████║██║   ██║██████╔╝
██╔══██║██╔═██╗ ██║██╔══██╗██╔══██║    ██╔══██║██║   ██║██╔══██╗
██║  ██║██║  ██╗██║██████╔╝██║  ██║    ██║  ██║╚██████╔╝██████╔╝
╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═════╝ ╚═╝  ╚═╝   ╚═╝  ╚═╝ ╚═════╝ ╚═════╝
```

**Secure Student Chama Management System**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=spring-boot)
![MySQL](https://img.shields.io/badge/Aiven_MySQL-4479A1?style=for-the-badge&logo=mysql)
![JWT](https://img.shields.io/badge/JWT_HS512-000000?style=for-the-badge&logo=JSON%20web%20tokens)

*Digitizing informal student savings groups (chamas) — secure, transparent, and accountable.*

</div>

---

## What is AkibaHub?

AkibaHub is a REST API backend for managing student savings groups (*chamas*) at Kenyan universities.
It handles group creation, M-Pesa deposits via PayHero, multi-signature proposal voting, and a tamper-evident audit ledger — all behind Google OAuth2 + JWT authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.5 |
| Security | Google OAuth2, JWT (HS512), BCrypt |
| Database | Aiven MySQL (cloud-managed) |
| Payments | PayHero API (M-Pesa STK push) |
| Build | Maven, multi-stage Docker |
| Hosting | Render (backend), Vercel (frontend) |

---

## Architecture

```
Google OAuth2
     │
     ▼
AuthController ──► AuthService ──► JwtUtil ──► JWT token
                                     │
                                     ▼
All other endpoints ◄── JwtAuthFilter (OncePerRequestFilter)
     │
     ├── GroupController     ──► GroupService
     ├── TransactionController──► TransactionService ──► PayHeroService
     ├── ProposalController  ──► ConsensusService ──► LedgerRepository
     ├── LedgerController    ──► ConsensusService
     └── WebhookController   ──► (PayHero payment callbacks)
```

---

## API Reference

All endpoints require `Authorization: Bearer <token>` except `/api/v1/auth/**`.

### Auth
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/auth/oauth2/callback/google` | Google OAuth2 callback — returns JWT |

### Users
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/users/me` | Authenticated user profile |

### Groups
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/groups/create` | Create a new group |
| `POST` | `/api/v1/groups/join` | Join via invite code |
| `GET` | `/api/v1/groups/my-groups` | List your groups |
| `GET` | `/api/v1/groups/{id}` | Group details (members only) |

### Transactions
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/transactions/deposit` | Initiate M-Pesa STK push |
| `GET` | `/api/v1/transactions/user` | Your transaction history |
| `GET` | `/api/v1/transactions/group/{id}` | Group transactions (members only) |

### Proposals & Voting
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/proposals/create` | Create a proposal |
| `POST` | `/api/v1/proposals/{id}/vote` | Cast a vote (once per user per proposal) |
| `GET` | `/api/v1/proposals/group/{id}` | List group proposals |

### Ledger
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/ledger/group/{id}` | SHA-256 hash-chained audit log |

---

## Local Development

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+ (or use [Aiven free tier](https://aiven.io))
- A Google OAuth2 app ([console.cloud.google.com](https://console.cloud.google.com))
- A PayHero account ([payhero.co.ke](https://payhero.co.ke))

### 1. Clone

```bash
git clone https://github.com/systems-jackal/akibahub.git
cd akibahub
```

### 2. Configure environment

```bash
cp .env.example .env
# Fill in all values in .env
```

### 3. Run

```bash
cd backend
mvn spring-boot:run
# API available at http://localhost:8080
```

### 4. Test

```bash
mvn test
```

---

## Docker

```bash
cd backend
docker build -t akibahub:latest .
docker run -p 8080:8080 --env-file ../.env akibahub:latest
```

The image uses a **multi-stage build**: Maven compiles in stage 1, only the JRE + JAR ship in stage 2 (~180 MB vs ~600 MB).
The container runs as a **non-root user** (`akiba`).

---

## Deploying to Render

1. Push to GitHub.
2. Create a new **Web Service** on [render.com](https://render.com).
3. Set root directory to `backend/`, Docker build command auto-detected.
4. Add all variables from `.env.example` in Render's Environment tab.
5. Deploy.

---

## Security Design

| Concern | Solution |
|---|---|
| Authentication | Google OAuth2 → JWT (HS512, 512-bit key enforced at startup) |
| Authorization | `JwtAuthFilter` on every request; `@AuthenticationPrincipal` in controllers |
| CORS | Centralised `CorsConfigurationSource`; allowed origins from env var |
| Input validation | Bean Validation (`@Valid`) on all request DTOs |
| User enumeration | Generic 401 response for all auth failures |
| IDOR | All data access scoped to `@AuthenticationPrincipal User` |
| Duplicate votes | DB unique constraint `(proposalId, userId)` + service-layer check |
| Ledger integrity | SHA-256 hash chain (`hash = SHA256(previousHash + content)`) |
| Secrets | All via environment variables; `.env` in `.gitignore` |
| Container | Non-root user, JRE-only image (no Maven/source in production) |
| Money | `BigDecimal` (scale 4) everywhere; never `double` or `float` |

---

## Project Structure

```
akibahub/
├── .env.example
├── .gitignore
├── README.md
└── backend/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/com/akibahub/
        │   ├── AkibaHubApplication.java
        │   ├── config/
        │   │   └── SecurityConfig.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── GroupController.java
        │   │   ├── LedgerController.java
        │   │   ├── ProposalController.java
        │   │   ├── TransactionController.java
        │   │   ├── UserController.java
        │   │   └── WebhookController.java
        │   ├── dto/
        │   │   ├── request/
        │   │   └── response/
        │   ├── exception/
        │   │   └── GlobalExceptionHandler.java
        │   ├── model/
        │   ├── repository/
        │   ├── security/
        │   │   ├── JwtAuthFilter.java
        │   │   └── JwtUtil.java
        │   ├── service/
        │   │   └── impl/
        │   └── util/
        │       ├── HashUtil.java
        │       └── InviteCodeGenerator.java
        └── main/resources/
            └── application.properties
```

---

## Roadmap

- [ ] SMS/email notifications on contribution
- [ ] Group loan request module
- [ ] Admin analytics dashboard
- [ ] PayHero webhook signature verification
- [ ] Mobile app (Android / Flutter)

---

<div align="center">
Built for Kenyan students · Powered by Unity Bridge
</div>