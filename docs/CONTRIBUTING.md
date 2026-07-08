# 🤝 Contributing to Akiba Hub

We welcome contributions from all team members. These guidelines help ensure that Akiba Hub remains stable, secure, and maintainable throughout development.

---

# 🌿 Branching Model

The project follows a simple Git branching strategy.

| Branch | Purpose |
|---------|---------|
| `main` | Stable, production-ready code. **Never commit directly to this branch.** |
| `develop` | Integration branch where completed features are merged before production. |
| `feature/<short-description>` | Used for developing new features (e.g. `feature/harambee-campaigns`). |
| `hotfix/<issue-description>` | Used for urgent fixes to production issues. |

---

# 🔄 Development Workflow

1. Create a new feature branch from `develop`.
2. Develop your feature and commit regularly.
3. Push the branch to GitHub.
4. Open a Pull Request targeting `develop`.
5. Request a review from at least one teammate.
6. After approval, **Squash and Merge** the Pull Request.

---

# 📝 Commit Messages

The project follows the **Conventional Commits** specification.

## Format

```text
feat(scope): short description
fix(scope): short description
docs: update README
security(scope): description
refactor(scope): description
```

### Examples

```text
feat(harambee): add campaign creation endpoint
```

```text
fix(auth): return proper error for duplicate phone
```

```text
security(wallet): add rate limiting to deposit
```

---

# 💻 Code Style

## Java

- Follow standard Java coding conventions.
- Use **Lombok** to reduce boilerplate code.
- All API endpoints must return:

```java
ApiResponse<T>
```

---

## JavaScript

- Use **ES6+** syntax.
- Keep functions small, reusable, and modular.
- Use the existing `api.js` wrapper for all HTTP requests.

---

## CSS

- Use the existing class names and color variables.
- Avoid inline styles unless absolutely necessary.
- Keep styling responsive and consistent across all pages.

---

# 🧪 Testing

Before submitting any code:

- Write **unit tests** for all new service methods.
- Add **integration tests** for new API endpoints using `@SpringBootTest`.
- Run the full backend test suite before pushing.

```bash
mvn test
```

Before merging into `main`, ensure:

- The feature has been tested locally using Docker Compose.
- The GitHub Actions CI/CD pipeline passes successfully.

---

# ▶️ How to Test Locally

### 1. Start the full application stack

```bash
docker-compose up -d
```

### 2. Run the backend test suite

```bash
cd backend
mvn test
```

### 3. Verify critical user flows

Manually test important application features using either the frontend or the existing `curl` commands.

Examples include:

- User registration
- User login
- Wallet deposit
- Group creation
- Joining a group
- Creating proposals
- Voting on proposals
- Viewing transactions

---

# 🚀 Avoiding Production Breaks

To maintain application stability:

- Never merge directly into `main`.
- Always test new features in the `develop` branch first.
- Every change must go through a Pull Request.
- Ensure the GitHub Actions pipeline passes before merging.

> **Note**
>
> The CI/CD pipeline automatically deploys whenever changes are pushed to the `main` branch.
>
> If deployment issues occur, inspect the GitHub Actions logs immediately.

---

# 🔐 Secrets & Environment Variables

Sensitive information must never be committed to the repository.

## Local Development

Create a `.env` file in the project root (already included in `.gitignore`).

```text
PAYHERO_AUTH_TOKEN=...
PAYHERO_CHANNEL_ID=...
```

## Production

Production secrets are injected through:

- Docker Compose environment variables on the VPS
- GitHub Actions Secrets

### PayHero

The PayHero integration is prepared but the sandbox account is still pending activation.

Once credentials become available, store them as:

```text
PAYHERO_AUTH_TOKEN
PAYHERO_CHANNEL_ID
```

**Never hardcode credentials inside `application.yml` or source code.**

---

# 🛡️ Security Guidelines

When contributing to Akiba Hub:

- Always validate user input on the server.
- Ensure every financial operation is transactional.
- Record all financial actions in the immutable audit log.
- Apply proper authorization checks for all protected endpoints.
- Verify group membership and ownership before performing sensitive actions.
- Update rate-limiting rules whenever introducing new public endpoints.

If you discover a potential security vulnerability, report it privately to the project lead.

---

# ⚠️ Challenges & Lessons Learned

During development, several real-world challenges have shaped the project architecture.

## Database Port Conflicts

Kali Linux frequently runs its own MariaDB service on port **3306**.

To avoid conflicts:

- Stop the local MariaDB service, **or**
- Run the Docker MariaDB container on a different host port.

---

## Environment Variable Propagation

When running:

```bash
mvn spring-boot:run
```

environment variables must be exported within the same terminal session.

Using **Docker Compose** avoids this issue because environment variables are injected automatically.

---

## PayHero Sandbox Activation

The PayHero integration is fully implemented in code.

However, the sandbox merchant account still requires activation by PayHero before STK Push requests can be processed successfully.

This reinforced an important architectural lesson:

> External integrations should remain loosely coupled so that the platform's core functionality can continue evolving independently while third-party services are still being provisioned.