<div align="center">

<img src="./assets/banner.svg" alt="Akiba Hub" width="100%" />

<br/>

![Java](https://img.shields.io/badge/Java-21-A67C3D?style=for-the-badge&logo=openjdk&logoColor=F7E7CE)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-C9A15A?style=for-the-badge&logo=springboot&logoColor=F7E7CE)
![MariaDB](https://img.shields.io/badge/MariaDB-10.11-8A6D46?style=for-the-badge&logo=mariadb&logoColor=F7E7CE)
![JWT](https://img.shields.io/badge/JWT-Authentication-B8860B?style=for-the-badge&logo=jsonwebtokens&logoColor=F7E7CE)
![Docker](https://img.shields.io/badge/Docker-Containerized-A67C3D?style=for-the-badge&logo=docker&logoColor=F7E7CE)
![Nginx](https://img.shields.io/badge/Nginx-Reverse_Proxy-C9A15A?style=for-the-badge&logo=nginx&logoColor=F7E7CE)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI/CD-8A6D46?style=for-the-badge&logo=githubactions&logoColor=F7E7CE)
![M-Pesa](https://img.shields.io/badge/M--Pesa-PayHero_STK-B8860B?style=for-the-badge&logoColor=F7E7CE)

<img src="./assets/divider.svg" width="70%" alt="" />

### *Empowering communities through transparent digital savings, democratic governance, and secure mobile payments.*

</div>

<br/>

## 📖 Overview

**Akiba Hub** is a secure financial management platform that enables individuals and groups to manage their savings digitally while maintaining transparency and accountability.

The platform combines **personal wallets**, **group savings**, **proposal-based withdrawals**, **community fundraising**, and **M‑Pesa integration** into one unified system.

Unlike traditional savings applications, Akiba Hub emphasizes **democratic governance** — shared funds can only be withdrawn after member approval.

The platform is currently implemented as a **Spring Boot monolithic application**, keeping development, deployment, and maintenance simple while remaining scalable for future enhancements.

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## ✨ Core Features

<table>
<tr>
<td width="50%" valign="top">

### 👤 Personal Savings
- Secure personal wallet
- Deposit funds via M‑Pesa STK Push
- Withdraw available balance
- Full transaction history
- Real-time balance tracking

</td>
<td width="50%" valign="top">

### 👥 Chama (Group Savings)
- Create savings groups
- Invite members
- Join existing groups
- Shared group wallet
- Member management & statistics

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🗳️ Democratic Governance
- Create withdrawal proposals
- Vote **YES** or **NO**
- Automatic approval / rejection
- Full proposal history
- Transparent decision making

</td>
<td width="50%" valign="top">

### 🎗️ Harambee
- Create fundraising campaigns
- Share campaign links
- Receive community contributions
- Track fundraising progress
- View donor history

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 💳 Mobile Payments
PayHero-oriented **M‑Pesa STK** flow (demo mode by default)
- STK-style initiate → PENDING
- Labeled phone PIN simulator for demos
- Verify-then-credit (demo complete / future IPN)
- Status polling + callback contract
- Sandbox-ready env vars for live PayHero

</td>
<td width="50%" valign="top">

### 📊 Dashboard
- Personal balance
- Group balances
- Pending proposals
- Recent transactions
- Active Harambee campaigns

</td>
</tr>
</table>

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

<div align="center">
<img src="./assets/seal.svg" width="56" alt="" />

### 🔐 Security, By Design

</div>

| | |
|---|---|
| 🔑 | JWT + revocable refresh tokens |
| 🔒 | BCrypt password hashing |
| 🚦 | Trusted-proxy-aware rate limiting |
| 🛂 | Membership checks on group-scoped data |
| 📜 | Append-only ledger + audit logging |
| 🧱 | Fail-closed secrets · Idempotency-Key on money ops |

See [SECURITY.md](SECURITY.md) and [docs/security/SECURITY_POLICY.md](docs/security/SECURITY_POLICY.md).

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🛠 Technology Stack

<div align="center">

| Layer | Technology |
|:---|:---|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Backend** | Java 21 |
| **Framework** | Spring Boot 3.3 |
| **Security** | Spring Security + JWT |
| **Database** | MariaDB 10.11 |
| **ORM** | Spring Data JPA / Hibernate |
| **Payments** | PayHero STK contract (demo mode + sandbox-ready) |
| **Build Tool** | Maven |
| **Reverse Proxy** | Nginx |
| **Containerization** | Docker & Docker Compose |
| **CI/CD** | GitHub Actions |
| **Hosting** | Ubuntu VPS |

</div>

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🏗 System Architecture

```text
                        Browser
                           │
                           ▼
                    HTML • CSS • JS
                           │
                           ▼
                     Nginx Reverse Proxy
                           │
        ┌──────────────────┴──────────────────┐
        │                                      │
        ▼                                      ▼
 Static Frontend                    Spring Boot Backend
                                          │
        ┌─────────────────────────────────┼────────────────────────────┐
        │                                 │                            │
        ▼                                 ▼                            ▼
 Authentication                    Business Logic                 Audit Logging
      │                                 │                            │
      └─────────────────────────────────┼────────────────────────────┘
                                        │
                                        ▼
                                  MariaDB Database
                                        │
                                        ▼
                           PayHero / M-Pesa STK
                                        │
                                        ▼
                                   M-Pesa Network
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 📂 Project Structure

```text
akibahub/
│
├── backend/
│   ├── src/main/java/com/akibahub/
│   │
│   ├── auth/
│   ├── user/
│   ├── wallet/
│   ├── group/
│   ├── proposal/
│   ├── harambee/
│   ├── payment/
│   ├── transaction/
│   ├── audit/
│   ├── dashboard/
│   ├── config/
│   └── shared/
│
├── frontend/
│   ├── css/
│   ├── js/
│   ├── images/
│   └── *.html
│
├── assets/
│   ├── banner.svg
│   ├── divider.svg
│   └── seal.svg
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── DATABASE.md
│   ├── DEVELOPMENT.md
│   └── CONTRIBUTING.md
│
├── docker-compose.yml
├── Dockerfile
├── nginx.conf
├── .env.example
└── README.md
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🎯 Design Principles

- Separation of concerns
- Layered architecture
- RESTful API design
- Stateless authentication
- Transactional financial operations
- Immutable audit records
- Secure secret management
- Mobile-first user experience

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

<div align="center">

## 🌟 Highlights

✔ Personal Savings Wallet &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Group Savings (Chamas) &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Democratic Voting
✔ Proposal Management &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Harambee Campaigns &nbsp;&nbsp;·&nbsp;&nbsp; ✔ M‑Pesa STK (demo + PayHero-ready)
✔ Secure JWT + Refresh Tokens &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Append-only Ledger &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Docker Deployment &nbsp;&nbsp;·&nbsp;&nbsp; ✔ GitHub Actions CI/CD

</div>

<div align="center"><img src="./assets/divider.svg" width="100%" alt="" /></div>

## 🚀 Getting Started

### Prerequisites

| Software | Version |
|:---|:---|
| Java JDK | 21+ |
| Maven | 3.9+ |
| Docker | Latest |
| Docker Compose | Latest |
| Git | Latest |
| MariaDB | 10.11+ (optional if using Docker) |

Verify your installation:

```bash
java -version
mvn -version
docker --version
docker compose version
git --version
```

### Clone the Repository

```bash
git clone https://github.com/systems-jackal/akibahub.git
cd akibahub
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## ⚙️ Environment Variables

Create a `.env` file in the project root (copy from [`.env.example`](.env.example)).

```env
# Database (Compose / Spring)
MYSQL_ROOT_PASSWORD=change-me
MYSQL_DATABASE=akibahub
MYSQL_PORT=3306
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=change-me

# Backend
APP_PORT=8080
JWT_SECRET=replace-with-a-long-random-alphanumeric-string

# Payments (demo by default — never collect M-Pesa PIN in-app)
PAYMENTS_MODE=demo
APP_BASE_URL=http://localhost:8080
# Future live PayHero:
# PAYHERO_AUTH_TOKEN=
# PAYHERO_CHANNEL_ID=
```

> ⚠️ Never commit your `.env` file to Git. Production must set strong `JWT_SECRET` and DB passwords — the app fails closed without them.

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🗄 Database Setup

```bash
docker compose up -d mariadb
docker compose ps
```

Expected output:

```text
NAME              STATUS
mariadb           running
```

## 🔨 Build the Backend

```bash
cd backend
mvn clean package

# Skip tests if necessary
mvn clean package -DskipTests
```

## ▶️ Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using Docker Compose
docker compose up --build
```

## 🌐 Access the Application

| Service | URL |
|:---|:---|
| Frontend | http://localhost |
| Backend API | http://localhost:8080 |
| Health Endpoint | http://localhost:8080/health |

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test
mvn test -Dtest=WalletServiceTest

# Generate a test report
mvn surefire-report:report
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 📱 M‑Pesa / PayHero Integration

Akiba Hub uses a **PayHero-oriented STK Push contract**. Default mode is **`demo`** for presentation: initiate leaves a pending payment, the UI shows a labeled phone PIN simulator, and credit happens only after demo-complete (or a future verified IPN). See [docs/architecture/payhero-integration.md](docs/architecture/payhero-integration.md).

### Payment Flow (demo)

```text
User (wallet UI)
 │
 ▼
POST /api/wallets/me/personal/deposit  (+ Idempotency-Key)
 │
 ▼
PENDING payment (no credit yet)
 │
 ▼
Waiting UI + SIMULATION STK PIN overlay
 │  (PIN never sent to Akiba Hub)
 ▼
POST /api/payments/demo/complete
 │
 ▼
Ledger credit + COMPLETED
```

### PayHero configuration (live, future)

```text
PAYMENTS_MODE=live
PAYHERO_AUTH_TOKEN
PAYHERO_CHANNEL_ID
APP_BASE_URL
```

Demo mode needs only `PAYMENTS_MODE=demo` (default). M-Pesa PIN is never collected by Akiba Hub.

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🔐 Authentication

Akiba Hub uses **JWT Authentication**.

```text
Register → Login → Receive JWT Token → Store Token
→ Send Token in Authorization Header → Access Protected Endpoints
```

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🐳 Docker Deployment

```bash
# Start all services
docker compose up --build

# Stop services
docker compose down

# Remove volumes
docker compose down -v

# View logs
docker compose logs -f
```

## ☁️ Production Deployment

```text
Internet
     │
     ▼
Nginx Reverse Proxy
     │
     ▼
Spring Boot Application
     │
     ▼
MariaDB
     │
     ▼
PayHero / M-Pesa (when live)
```

SSL certificates are managed using **Let's Encrypt**. Deployment is automated through **GitHub Actions**.

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 📊 Logging & Monitoring

The application records:

- User authentication
- Wallet deposits & withdrawals
- Group contributions
- Proposal creation & voting activity
- Harambee contributions
- Payment callbacks
- System errors

```bash
docker compose logs
# or
journalctl
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🧰 Useful Commands

| Action | Command |
|:---|:---|
| Build project | `mvn clean package` |
| Run application | `mvn spring-boot:run` |
| Run tests | `mvn test` |
| Start Docker | `docker compose up` |
| Stop Docker | `docker compose down` |
| View logs | `docker compose logs -f` |

<div align="center"><img src="./assets/divider.svg" width="100%" alt="" /></div>

## 📚 API Overview

All API endpoints return a consistent response structure.

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {}
}
```

Errors follow the same format.

```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

### 🔑 Authentication Endpoints

| Method | Endpoint | Description |
|:---|:---|:---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user |
| GET | `/api/auth/me` | Retrieve the authenticated user's profile |

### 👤 Wallet Endpoints

| Method | Endpoint | Description |
|:---|:---|:---|
| GET | `/api/wallets/me` | Retrieve user wallets |
| POST | `/api/wallets/me/personal/deposit` | Deposit funds |
| POST | `/api/wallets/me/personal/withdraw` | Withdraw funds |
| POST | `/api/wallets/groups/{groupId}/contribute` | Contribute to a group wallet |

### 👥 Group Endpoints

| Method | Endpoint |
|:---|:---|
| POST | `/api/groups` |
| GET | `/api/groups/my` |
| GET | `/api/groups/{groupId}` |
| PUT | `/api/groups/{groupId}` |
| DELETE | `/api/groups/{groupId}` |
| POST | `/api/groups/{groupId}/join` |
| POST | `/api/groups/{groupId}/invite` |

### 🗳️ Proposal Endpoints

| Method | Endpoint |
|:---|:---|
| POST | `/api/groups/{groupId}/proposals` |
| GET | `/api/groups/{groupId}/proposals` |
| GET | `/api/proposals/{proposalId}` |
| POST | `/api/proposals/{proposalId}/vote` |
| PUT | `/api/proposals/{proposalId}` |
| DELETE | `/api/proposals/{proposalId}` |

### 🎗️ Harambee Endpoints *(Planned)*

| Method | Endpoint |
|:---|:---|
| POST | `/api/harambee` |
| GET | `/api/harambee` |
| GET | `/api/harambee/{campaignId}` |
| POST | `/api/harambee/{campaignId}/contribute` |
| GET | `/api/harambee/{campaignId}/contributors` |

### 💳 Payment Endpoints

| Method | Endpoint |
|:---|:---|
| POST | `/api/wallets/me/personal/deposit` (initiate PENDING) |
| GET | `/api/payments/status/{reference}` |
| POST | `/api/payments/demo/complete` (demo mode only) |
| POST | `/api/payments/callback` (IPN stub / future live) |

<div align="center"><img src="./assets/divider.svg" width="100%" alt="" /></div>

## 🗄 Database Overview

The application uses **MariaDB 10.11**.

| Table | Purpose |
|:---|:---|
| `users` | Registered users |
| `wallets` | Personal & group wallets |
| `transactions` | Financial transactions |
| `groups_table` | Savings groups |
| `group_members` | Group memberships |
| `proposals` | Withdrawal proposals |
| `votes` | Proposal voting |
| `harambee_campaigns` | Fundraising campaigns *(planned)* |
| `harambee_contributions` | Campaign contributions *(planned)* |
| `audit_log` | Immutable audit records |

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 📜 Audit Logging

Every critical system event is permanently recorded — user registration, login, wallet deposits/withdrawals, group creation, membership changes, proposal creation and voting, Harambee contributions, M‑Pesa callbacks, and transaction completions.

**Audit records are append-only and cannot be modified.**

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

<div align="center">
<img src="./assets/seal.svg" width="56" alt="" />

## 🔒 Security Features

</div>

Implemented protections include:

- JWT Authentication + revocable refresh tokens
- BCrypt password hashing
- Spring Security
- Protected REST APIs
- Membership checks on group-scoped data
- Append-only ledger + audit logging
- Amount validation + Idempotency-Key on money ops
- Input validation
- Fail-closed environment secrets
- Docker container isolation (non-root)

**Planned enhancements (real-money launch):**

- Multi-factor / step-up authentication (R-10)
- KYC/AML (R-18)
- Live PayHero STK + verified IPN
- Broader automated test coverage and monitoring

See [docs/security/SECURITY_POLICY.md](docs/security/SECURITY_POLICY.md).

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🧪 Testing

Testing includes unit tests for the deposit money path, manual API testing (see `docs/testing/functional_tests.md`), Docker testing, authentication testing, wallet transaction testing, and proposal workflow testing. Live PayHero sandbox testing remains pending merchant activation.

```bash
mvn test
```

<div align="center"><img src="./assets/divider.svg" width="100%" alt="" /></div>

## 🛣 Roadmap

<table>
<tr><td width="20%"><b>Phase 1</b><br/>Foundation ✅</td>
<td>User Registration · Authentication · JWT Security · Personal Wallet · Group Wallet · Transactions · Docker Deployment</td></tr>
<tr><td width="20%"><b>Phase 2</b><br/>Governance 🚧</td>
<td>Group Creation · Invitations · Withdrawal Proposals · Voting System · Dashboard · Notifications</td></tr>
<tr><td width="20%"><b>Phase 3</b><br/>Payments 🚧</td>
<td>Demo STK flow ✅ · Pending payments · Status poll · Callback contract · Live PayHero STK · Reconciliation</td></tr>
<tr><td width="20%"><b>Phase 4</b><br/>Harambee 🚧</td>
<td>Campaign Creation · Public Campaign Sharing · Community Contributions · Progress Tracking · Campaign Analytics</td></tr>
<tr><td width="20%"><b>Phase 5</b><br/>Future ✨</td>
<td>Mobile Application · QR Code Payments · Scheduled Contributions · Savings Goals · AI Financial Insights · SACCO Integration · Bank Transfers · Multi-Currency Support</td></tr>
</table>

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🤝 Contributing

Contributions are welcome.

```text
feature/*  →  develop  →  Pull Request  →  Code Review  →  main
```

**Commit convention:**

```text
feat(wallet): add deposit endpoint
fix(auth): resolve JWT expiration issue
docs(readme): update documentation
refactor(group): improve membership validation
test(wallet): add deposit integration tests
security(auth): strengthen password policy
```

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 📖 Documentation

Project documentation is available inside the `docs/` directory:

`ARCHITECTURE.md` · `API.md` · `DATABASE.md` · `DEVELOPMENT.md` · `CONTRIBUTING.md`

## 🌍 Deployment

Akiba Hub is designed to run on Ubuntu VPS, Docker, Docker Compose, Nginx, MariaDB, GitHub Actions, and Let's Encrypt SSL.

```text
Git Push → GitHub Actions → Build → Run Tests → Build Docker Image → Deploy to VPS → Health Check
```

<div align="center"><img src="./assets/divider.svg" width="100%" alt="" /></div>

<div align="center">

### 👨‍💻 Authors

**Akiba Hub Development Team**

*Developed as a modern financial technology platform focused on secure digital savings, transparent governance, and community fundraising.*

<br/>

### 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for more information.

<br/>

### ⭐ Support

If you find this project useful — star the repository, fork the project, submit improvements, report issues, or suggest new features.

<br/>

<img src="./assets/divider.svg" width="70%" alt="" />

<br/>

<img src="./assets/seal.svg" width="48" alt="" />

## 🏦 Akiba Hub

### *Save Together • Decide Together • Grow Together*

**Building transparent digital financial communities through secure technology.**

<br/>

![Java](https://img.shields.io/badge/Java-21-A67C3D?style=flat-square&logoColor=F7E7CE)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-C9A15A?style=flat-square&logoColor=F7E7CE)
![MariaDB](https://img.shields.io/badge/MariaDB-8A6D46?style=flat-square&logoColor=F7E7CE)
![Docker](https://img.shields.io/badge/Docker-A67C3D?style=flat-square&logoColor=F7E7CE)
![JWT](https://img.shields.io/badge/JWT-B8860B?style=flat-square&logoColor=F7E7CE)
![PayHero STK](https://img.shields.io/badge/PayHero_STK-C9A15A?style=flat-square&logoColor=F7E7CE)

</div>