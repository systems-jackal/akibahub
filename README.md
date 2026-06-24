<div align="center">

```text
 █████╗ ██╗  ██╗██╗██████╗  █████╗     ██╗  ██╗██╗   ██╗██████╗
██╔══██╗██║ ██╔╝██║██╔══██╗██╔══██╗    ██║  ██║██║   ██║██╔══██╗
███████║█████╔╝ ██║██████╔╝███████║    ███████║██║   ██║██████╔╝
██╔══██║██╔═██╗ ██║██╔══██╗██╔══██║    ██╔══██║██║   ██║██╔══██╗
██║  ██║██║  ██╗██║██████╔╝██║  ██║    ██║  ██║╚██████╔╝██████╔╝
╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═════╝ ╚═╝  ╚═╝    ╚═╝  ╚═╝ ╚═════╝ ╚═════╝
```

# AKIBA HUB

### Secure Community Savings & Governance Platform

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.11-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-25.x-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

**Distributed Savings • Democratic Governance • Immutable Auditing**

*Built for community savings groups, investment clubs, and digital chamas that require transparency, accountability, and secure financial operations.*

</div>

---

# Overview

Akiba Hub is a cloud-native financial collaboration platform that enables users and groups to:

- Manage personal savings wallets
- Create and administer savings groups
- Propose and vote on withdrawals
- Process contributions through PayHero
- Maintain immutable financial records
- Operate through a secure microservices ecosystem

The platform is currently transitioning from a monolithic architecture to a fully distributed microservices platform with automated SecDevOps workflows.

---

# Architecture

```text
Browser / Mobile Client
           │
           ▼
     Nginx (SSL)
           │
           ▼
     API Gateway
           │
 ┌─────────┼─────────┐
 │         │         │
 ▼         ▼         ▼

Auth    Savings    Group
Service Service   Service

 │         │         │
 └────┬────┴────┬────┘
      │         │

      ▼         ▼

 Payment   Proposal
 Service   Service

      │
      ▼

 Audit Service
(Immutable Ledger)
```

---

# Core Services

| Service | Responsibility |
|----------|---------------|
| Auth Service | Google OAuth2, JWT issuance, token validation |
| API Gateway | Routing, authorization, rate limiting |
| Savings Service | Personal wallet management |
| Group Service | Group lifecycle and membership |
| Payment Service | PayHero integration and callback processing |
| Proposal Service | Voting, consensus, withdrawal approvals |
| Audit Service | Immutable financial ledger |

---

# Technology Stack

| Layer | Technology |
|---------|------------|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Java 21, Spring Boot 3.3 |
| Security | Spring Security, OAuth2, JWT |
| Database | MariaDB |
| Messaging | RabbitMQ (CloudAMQP) |
| Payments | PayHero |
| Infrastructure | Docker, Nginx |
| CI/CD | GitHub Actions |
| Hosting | Ubuntu 24.04 VPS |

---

# Repository Structure

```text
akibahub/
│
├── services/
│   ├── auth-service/
│   ├── api-gateway/
│   ├── savings-service/
│   ├── group-service/
│   ├── payment-service/
│   ├── proposal-service/
│   └── audit-service/
│
├── frontend/
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── docker-compose.dev.yml
│   ├── nginx/
│   └── database/
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── DATABASE.md
│   └── DEPLOYMENT.md
│
└── .github/
    └── workflows/
```

---

# Security Architecture

### Authentication Flow

```text
Google OAuth2
      │
      ▼
 Auth Service
      │
      ▼
 JWT Token
      │
      ▼
 API Gateway
      │
      ▼
 Microservices
```

### Security Controls

- OAuth2 Authentication
- JWT Access Tokens
- Refresh Tokens
- HTTPS Enforcement
- Gateway Rate Limiting
- OWASP Dependency Scanning
- Environment-Based Secrets
- Internal Service Isolation
- Audit Log Immutability

---

# Audit-First Design

Every financial and governance action is recorded in an immutable ledger.

### Recorded Events

- AUTH_LOGIN
- GROUP_CREATED
- MEMBER_JOINED
- PERSONAL_DEPOSIT
- GROUP_CONTRIBUTION
- PROPOSAL_CREATED
- VOTE_CAST
- PROPOSAL_APPROVED
- WITHDRAWAL_EXECUTED

### Ledger Rules

```text
INSERT  ✓ Allowed
UPDATE  ✗ Forbidden
DELETE  ✗ Forbidden
```

---

# CI/CD Pipeline

```text
Push
 │
 ▼
Build
 │
 ▼
Test
 │
 ▼
OWASP Scan
 │
 ▼
Docker Build
 │
 ▼
Push to GHCR
 │
 ▼
Deploy to VPS
 │
 ▼
Health Checks
```

---

# Development

### Start Development Environment

```bash
cd infrastructure

docker-compose -f docker-compose.dev.yml up --build
```

### Run Individual Service

```bash
cd services/auth-service

mvn spring-boot:run
```

### Run Tests

```bash
mvn clean test
```

---

# Roadmap

- [x] Architecture Redesign
- [x] Microservices Planning
- [ ] Auth Service Extraction
- [ ] Audit Service Deployment
- [ ] API Gateway Integration
- [ ] Payment Service Isolation
- [ ] RabbitMQ Event Integration
- [ ] CI/CD Automation
- [ ] End-to-End Testing
- [ ] Production Launch

---

# Engineering Standards

```text
feat(service): new feature
fix(service): bug fix
refactor(service): code improvement
test(service): tests
security(service): security enhancement
docs: documentation update
```

---

# License

Internal / Confidential

© Akiba Hub Platform

---

<div align="center">

### Transparent Savings • Democratic Decisions • Secure Finance

Built for modern digital chamas.

</div>