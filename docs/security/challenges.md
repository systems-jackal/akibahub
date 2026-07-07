# Challenges & Lessons Learned

During the development of the Akiba Hub MVP, we encountered several technical obstacles. This document outlines the key challenges and how they were resolved.

## 1. Hibernate Proxy Serialization Errors

**Problem:** Returning JPA entities (with lazy‑loaded relationships) directly from controllers caused `com.fasterxml.jackson.databind.exc.InvalidDefinitionException` with `ByteBuddyInterceptor`. For example, the `Wallet` entity’s `user` and `group` fields were lazy proxies that Jackson could not serialize.

**Solution:** Added `@JsonIgnore` on all lazy‑loaded `@ManyToOne` fields across `Wallet`, `Transaction`, `Group`, `GroupMember`, `Proposal`, and `Vote` entities. This breaks the serialization of circular references and ensures only the owning side’s ID is needed (not the full object).

**Lesson:** Always use DTOs (Data Transfer Objects) for API responses rather than exposing entities directly. This prevents lazy‑loading issues and keeps a clean separation of concerns.

## 2. Security Configuration Not Being Picked Up by Docker

**Problem:** After modifying `SecurityConfig.java` to permit Swagger UI paths, the running container still returned `403 Forbidden` for `/v3/api-docs` and `/swagger-ui.html`. The change was not reflected.

**Root Cause:** The Docker image was being built from an old JAR because `mvn package` had not been re‑run before `docker‑compose up --build`. Moreover, Docker’s build cache sometimes used an intermediate layer with the old compiled classes.

**Solution:** Used `mvn clean package -DskipTests` to rebuild the JAR, then `docker‑compose build --no-cache backend` to force a full image rebuild. Verified the JAR content with `jar tvf`.

**Lesson:** In a development workflow, ensure a full clean build pipeline. Using a volume mount for `target/` or building inside the container can reduce cache issues.

## 3. Swagger UI 403 (Spring Security Blocking API Docs)

**Problem:** Even with correct security configuration, Swagger UI could not load because the `/v3/api-docs` endpoints were blocked.

**Root Cause:** The Spring Security `requestMatchers` list missed some paths required by Springdoc OpenAPI (e.g., `/v3/api-docs`, `/v3/api-docs/swagger-config`, `/swagger-resources/**`). After adding all necessary patterns, the issue persisted until a separate `SwaggerSecurityConfig` was created with `@Order(1)` to bypass security for those paths.

**Final Decision:** Swagger UI was removed from the MVP to simplify the security configuration; testing is done via `curl`. In production, API documentation should be secured behind authentication or disabled entirely.

**Lesson:** When integrating libraries that serve static content, carefully inspect all network requests to determine the exact URL patterns that need to be permitted.

## 4. Container Port Not Accessible from Browser

**Problem:** After starting the containers, `http://localhost:8080` was not reachable from the browser, although `docker ps` showed port mapping.

**Diagnosis:** `curl` from the host also failed. The issue was that the browser was forcing HTTPS or the proxy settings interfered. Additionally, the first attempt missed the healthcheck dependency, so the backend started before MariaDB was ready, causing connection failures and occasional crashes.

**Solution:** Added a healthcheck for the MariaDB service and made the backend depend on `service_healthy`. Used `curl -I` to test connectivity. For browser issues, used a private window and explicitly typed `http://`.

**Lesson:** Docker Compose health checks are essential for multi‑service applications. Always verify connectivity with command‑line tools before assuming the browser is the problem.

## 5. Database State Persistence Between Tests

**Problem:** During repeated testing, user registration would fail with “Phone number already registered” because the database volume was not removed between runs.

**Solution:** Used `docker‑compose down -v` to delete the volume and start fresh. For automated testing, a test‑specific profile with an in‑memory database (H2) would be preferable.

**Lesson:** Clean‑up scripts or tear‑down routines are necessary for reproducible test environments.

## 6. Token Extraction in Shell Scripts

**Problem:** Parsing the JWT from the registration JSON response using `grep -o` and `cut` worked inconsistently across different terminal states (e.g., when the token contained special characters).

**Solution:** Eventually used manual token copying and assignment to shell variables to avoid parsing errors. For a more robust approach, `jq` would be recommended (though not installed by default).

**Lesson:** For quick MVP testing, manual steps are acceptable, but for integration tests, use a proper scripting language (Python, Bash with `jq`) to handle JSON parsing.

These challenges provided valuable insight into real‑world development and security hardening, reinforcing the importance of rigorous testing and automation.