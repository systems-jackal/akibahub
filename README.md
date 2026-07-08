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
![M-Pesa](https://img.shields.io/badge/M--Pesa-Daraja_API-B8860B?style=for-the-badge&logoColor=F7E7CE)

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
Integrated with the **Safaricom Daraja API**
- M‑Pesa STK Push
- Callback handling
- Automatic wallet updates
- Transaction reconciliation
- Sandbox & production support

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
| 🔑 | JWT Authentication |
| 🔒 | BCrypt password hashing |
| 🚦 | Rate limiting |
| 🛂 | Role-based authorization |
| 📜 | Immutable audit logging |
| 🧱 | Secure, isolated API endpoints |

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
| **Payments** | Safaricom Daraja API |
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
                           Safaricom Daraja API
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
✔ Proposal Management &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Harambee Campaigns &nbsp;&nbsp;·&nbsp;&nbsp; ✔ M‑Pesa Daraja Integration
✔ Secure JWT Authentication &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Immutable Audit Logs &nbsp;&nbsp;·&nbsp;&nbsp; ✔ Docker Deployment &nbsp;&nbsp;·&nbsp;&nbsp; ✔ GitHub Actions CI/CD

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

Create a `.env` file in the project root.

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=akibahub
DB_USERNAME=root
DB_PASSWORD=password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000

# Daraja API
MPESA_ENVIRONMENT=sandbox
MPESA_CONSUMER_KEY=
MPESA_CONSUMER_SECRET=
MPESA_SHORTCODE=
MPESA_PASSKEY=
MPESA_CALLBACK_URL=https://your-domain.com/api/payments/callback

# Application
SERVER_PORT=8080
```

> ⚠️ Never commit your `.env` file to Git.

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

## 📱 Daraja API Integration

Akiba Hub uses the **Safaricom Daraja API** to process M‑Pesa payments.

- OAuth authentication
- STK Push requests
- Payment callback processing
- Automatic wallet updates
- Transaction recording
- Sandbox testing
- Production-ready configuration

### Payment Flow

```text
User
 │
 ▼
Frontend
 │
 ▼
Spring Boot Backend
 │
 │ Request OAuth Token
 ▼
Safaricom Daraja API
 │
 │ STK Push
 ▼
Customer Phone
 │
 │ Enter M-Pesa PIN
 ▼
Safaricom
 │
 │ Payment Callback
 ▼
Backend Callback Endpoint
 │
 ▼
Wallet Service
 │
 ▼
Transaction Service
 │
 ▼
Audit Log
```

### Daraja Configuration

```text
MPESA_CONSUMER_KEY
MPESA_CONSUMER_SECRET
MPESA_SHORTCODE
MPESA_PASSKEY
MPESA_CALLBACK_URL
MPESA_ENVIRONMENT
```

Supported environments: `sandbox` · `production`

Changing from sandbox to production only requires updating these values.

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
Safaricom Daraja API
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
| POST | `/api/payments/stkpush` |
| POST | `/api/payments/callback` |
| GET | `/api/payments/status/{reference}` |

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

- JWT Authentication
- BCrypt password hashing
- Spring Security
- Protected REST APIs
- Role-based authorization
- Immutable audit logging
- Transaction validation
- Input validation
- Secure environment variables
- Docker container isolation

**Planned enhancements:**

- Two-Factor Authentication (2FA)
- Device recognition
- Login notifications
- Account lockout
- Refresh tokens

<div align="center"><img src="./assets/divider.svg" width="70%" alt="" /></div>

## 🧪 Testing

Testing includes unit tests, integration tests, manual API testing, Docker testing, authentication testing, wallet transaction testing, proposal workflow testing, and Daraja sandbox testing.

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
<td>Daraja OAuth · STK Push · Callback Processing · Wallet Synchronization · Transaction Reconciliation</td></tr>
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
![Daraja API](https://img.shields.io/badge/Safaricom_Daraja_API-C9A15A?style=flat-square&logoColor=F7E7CE)

</div>