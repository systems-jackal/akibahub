<div align="center">

```
 █████╗ ██╗  ██╗██╗██████╗  █████╗     ██╗  ██╗██╗   ██╗██████╗
██╔══██╗██║ ██╔╝██║██╔══██╗██╔══██╗    ██║  ██║██║   ██║██╔══██╗
███████║█████╔╝ ██║██████╔╝███████║    ███████║██║   ██║██████╔╝
██╔══██║██╔═██╗ ██║██╔══██╗██╔══██║    ██╔══██║██║   ██║██╔══██╗
██║  ██║██║  ██╗██║██████╔╝██║  ██║    ██║  ██║╚██████╔╝██████╔╝
╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═════╝ ╚═╝  ╚═╝    ╚═╝  ╚═╝ ╚═════╝ ╚═════╝
```

**Secure Student Chama Management System**

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

*Digitizing informal student savings groups (Chamas) across Kenyan institutions — with security, transparency, and accountability at the core.*

</div>

---

## 🧩 What Is Akiba Hub?

**Akiba Hub** is a web-based platform that transforms how university and college students manage their savings groups (*chamas*). Instead of relying on WhatsApp messages, M-Pesa screenshots, and manual spreadsheets, Akiba Hub centralizes everything: contributions, group management, transaction history, and access control — securely and in real time.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| 🖥️ Frontend | HTML5, CSS3, Vanilla JavaScript |
| ⚙️ Backend | Java 17 + Spring Boot |
| 🗄️ Database | MySQL |
| 💳 Payments | PayHero API |
| 🔐 Security | JWT + bcrypt + HTTPS |
| 🔁 Version Control | Git & GitHub |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        CLIENT                           │
│          HTML / CSS / Vanilla JavaScript                │
│    index.html  |  dashboard.html  |  group.html         │
└────────────────────────┬────────────────────────────────┘
                         │  HTTPS / REST
┌────────────────────────▼────────────────────────────────┐
│                     BACKEND                             │
│                  Spring Boot API                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │Controller│→ │ Service  │→ │Repository│              │
│  └──────────┘  └──────────┘  └────┬─────┘              │
│  ┌──────────────────────────┐      │                    │
│  │  JWT + bcrypt + CORS     │      │                    │
│  └──────────────────────────┘      │                    │
└───────────────────────────────┬────┘                    │
                                │                         │
          ┌─────────────────────▼──────────────────────┐  │
          │                  MySQL                     │  │
          │   Users | Groups | Transactions | Codes    │  │
          └────────────────────────────────────────────┘  │
                                                          │
┌─────────────────────────────────────────────────────────┘
│                  PayHero API
│         External Payment Gateway Integration
└─────────────────────────────────────────────────────────
```

---

## 📁 Project Structure

```
akiba-hub/
│
├── backend/
│   │
│   ├── pom.xml
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── akibahub/
│           │           │
│           │           ├── AkibaHubApplication.java
│           │           │
│           │           ├── entity/
│           │           │
│           │           ├── repository/
│           │           │
│           │           ├── controller/
│           │           │
│           │           └── config/
│           │
│           └── resources/
│               │
│               └── application.properties
│
│
├── frontend/
│   │
│   ├── pages/
│   │   │
│   │   ├── index.html
│   │   ├── login.html
│   │   ├── register.html
│   │   ├── dashboard.html
│   │   ├── groups.html
│   │   └── personal.html
│   │
│   ├── css/
│   │   │
│   │   └── styles.css
│   │
│   ├── js/
│   │   │
│   │   ├── api.js
│   │   ├── auth.js
│   │   ├── groups.js
│   │   └── savings.js
│   │
│   └── assets/
│
│
├── database/
│   │
│   └── schema.sql
│
│
├── docs/
│   │
│   ├── API.md
│   └── SETUP.md
│
│
├── .gitignore
│
├── README.md
│
└── .git/

---

## ✨ Core Features

- 🔐 **Secure Auth** — Registration & login with bcrypt-hashed passwords and JWT sessions
- 💰 **Savings Modes** — Choose between personal savings or joining a group (chama)
- 👥 **Group Management** — Admins create and manage groups; members join via invite codes
- 🎟️ **Invite Codes** — Unique, validated codes control who accesses each group
- 📲 **PayHero Integration** — Real-time M-Pesa-style contributions via PayHero API
- 📊 **Transaction History** — Every contribution is automatically recorded and viewable

---

## 🔒 Security Design

```
User Password  ──►  bcrypt hash  ──►  Stored in DB
                                            │
Login Request  ──►  Validate hash  ──►  Issue JWT Token
                                            │
API Requests   ──►  Verify JWT  ──►  Authorize or Reject
                                            │
All Traffic    ──►  HTTPS only  ──►  Encrypted in Transit
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL 8+
- A PayHero API account

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/akiba-hub.git
cd akiba-hub
```

### 2. Configure the Database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

### 3. Set Environment Variables

Update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/akiba_hub
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

jwt.secret=YOUR_JWT_SECRET
payhero.api.key=YOUR_PAYHERO_KEY
```

### 4. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

### 5. Open the Frontend

Open `frontend/index.html` in your browser, or serve it with any static file server.

---

## 👤 User Roles

| Role | Permissions |
|---|---|
| **Student** | Register, log in, save personally or join a group |
| **Group Admin** | Create groups, generate invite codes, view all contributions |
| **Member** | Join via invite code, contribute, view group history |

---

## 📌 Roadmap

- [ ] Email/SMS contribution notifications
- [ ] Group loan request module
- [ ] Admin analytics dashboard
- [ ] Mobile app (Android)
- [ ] Multi-institution support

---

## 🤝 Contributing

Pull requests are welcome. For major changes, open an issue first to discuss what you'd like to change.

```bash
git checkout -b feature/your-feature-name
git commit -m "feat: describe your change"
git push origin feature/your-feature-name
```

---

## 📄 License

This project is licensed under the **MIT License**.

---

<div align="center">

Built with ❤️ for Kenyan students · Powered by Spring Boot & PayHero

</div>
