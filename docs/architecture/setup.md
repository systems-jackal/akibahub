# 🚀 Local Development Setup

## Prerequisites

Before running Akiba Hub locally, ensure the following software is installed:

- **Java 21 JDK**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Git**

---

# 📥 Clone the Repository

Clone the project from GitHub and navigate into the project directory.

```bash
git clone https://github.com/systems-jackal/akibahub.git
cd akibahub
```

---

# 📁 Local Environment Structure

Understanding the project layout makes it easier to know where every feature belongs.

```text
akibahub/
├── backend/                         Spring Boot monolith
│   ├── src/main/java/com/akibahub/
│   │   ├── audit/                   Immutable event logging
│   │   ├── auth/                    Authentication (JWT, login, register)
│   │   ├── config/                  Security, JWT filter, rate limiting
│   │   ├── dashboard/               Aggregated dashboard data
│   │   ├── group/                   Groups, members, wallets, invitations
│   │   ├── proposal/                Proposals, voting, execution
│   │   ├── shared/                  ApiResponse wrapper, exception handling
│   │   ├── transaction/             Transaction history with filters
│   │   ├── user/                    User entity, profile, password
│   │   └── wallet/                  Personal & group wallets, deposits, withdrawals
│   │
│   ├── src/main/resources/
│   │   └── application.yml          Main configuration (database, JWT, rate limits)
│   │
│   └── pom.xml
│
├── frontend/                        Static web client (HTML/CSS/JavaScript)
│   ├── css/
│   ├── js/
│   │   ├── api.js                   Shared API wrapper (JWT + fetch)
│   │   ├── ui.js                    UI helper functions
│   │   ├── utils.js                 Currency/date formatting utilities
│   │   └── pages/                   One JavaScript file per page
│   │
│   └── *.html                       Application pages
│
├── docker-compose.yml               Docker services
├── .gitignore
├── .env.example                     Environment variable template
├── CHANGELOG.md
└── docs/                            Project documentation
```

> **Tip**
>
> Spend a few minutes familiarizing yourself with the folder structure before writing code. It will make navigating the project much easier.

---

# 🗄️ Start MariaDB

Start the MariaDB container.

```bash
docker-compose up -d mariadb
```

Verify that the database is healthy.

```bash
docker-compose ps
```

---

# 🔨 Build the Backend

Navigate to the backend project and build it.

```bash
cd backend
mvn clean package -DskipTests
```

---

# ▶️ Run the Backend

Start the Spring Boot application.

```bash
mvn spring-boot:run
```

Alternatively, run the complete application stack using Docker Compose.

```bash
docker-compose up -d
```

---

# 🌐 Access the Application

| Service | URL |
|----------|-----|
| **Backend API** | `http://localhost:8080` |
| **Frontend** | `http://localhost:8080` |

> **Development Note**
>
> In production, the frontend is served through **Nginx**.
>
> During development, you may:
>
> - Open `frontend/index.html` directly in your browser, or
> - Serve the frontend using a lightweight HTTP server.
>
> For the closest production experience, install Nginx locally and configure it to proxy API requests to `localhost:8080`.

---

# ⚙️ Environment Variables

Some features (such as **PayHero**) require environment variables.

Create a `.env` file in the project root using the provided `.env.example` template.

```text
PAYHERO_AUTH_TOKEN=your_token_here
PAYHERO_CHANNEL_ID=your_channel_id_here
```

When running with Docker Compose, these variables are automatically injected.

If running Spring Boot directly, export them first.

```bash
export PAYHERO_AUTH_TOKEN="Basic ..."
export PAYHERO_CHANNEL_ID="123"
```

> **Important**
>
> Never commit secrets to Git.
>
> Never hardcode credentials inside `application.yml` or any Java source file.

---

# 🧪 Testing the API

The API can be tested using:

- curl
- Postman
- Insomnia
- Any REST client

## Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "phoneNumber":"+254711111111",
        "password":"secret12",
        "fullName":"Alice",
        "idNumber":"12345678"
      }'
```

After registering successfully:

1. Log in.
2. Obtain a JWT token.
3. Include the token in the `Authorization: Bearer <token>` header when testing protected endpoints.

---

# 🛡️ Avoiding Production Breaks

Before pushing code, always follow these practices.

### ✅ Test Locally

Start the complete application.

```bash
docker-compose up -d
```

Verify your feature manually using the frontend or API.

---

### ✅ Run the Test Suite

```bash
cd backend
mvn test
```

All tests should pass before pushing.

---

### ✅ Use the Correct Branch

- Develop features on feature branches.
- Merge into `develop` first.
- Never push feature work directly to `main`.

---

### ✅ Open a Pull Request

Every change should be reviewed by at least one teammate before merging.

---

### ✅ Monitor CI/CD

GitHub Actions automatically:

- Builds the project
- Runs tests
- Creates Docker images
- Deploys to production

If the pipeline fails, investigate and resolve the issue before merging.

---

# 🧩 Common Development Challenges

| Challenge | Solution |
|-----------|----------|
| **Port 3306 already in use** | Your machine may already be running MariaDB or MySQL. Stop the local service (`sudo systemctl stop mariadb`) or change Docker's host port (e.g. `3307:3306`) and update `application.yml`. |
| **Environment variables not detected by `mvn spring-boot:run`** | Export the variables in the same terminal before starting Spring Boot, or use Docker Compose which loads `.env` automatically. |
| **PayHero sandbox returns `Merchant Account Inactive`** | The PayHero sandbox merchant account requires manual activation by PayHero. The integration code is complete; only the merchant account activation is pending. Store credentials in `.env` but never commit them. |
| **Database contains old test data** | Remove the existing Docker volume and recreate the database using `docker-compose down -v`, then start the containers again. |
| **Frontend encounters CORS errors** | During development, the backend allows broader CORS access. If using a different frontend port, update the CORS configuration temporarily in `SecurityConfig.java`. Production should remain restricted to the official domain. |

---

# 📌 Development Checklist

Before submitting your work, verify the following:

- ✅ Project builds successfully.
- ✅ Backend starts without errors.
- ✅ Database migrations complete successfully.
- ✅ All tests pass.
- ✅ Feature has been manually tested.
- ✅ No secrets or credentials have been committed.
- ✅ Pull Request targets the `develop` branch.
- ✅ CI/CD pipeline passes successfully.