# Akiba Hub — Full Audit Report
**Scope:** backend (Spring Boot), frontend (vanilla JS), infrastructure (Docker/CI-CD)
**Severity key:** 🔴 Critical &nbsp; 🟠 High &nbsp; 🟡 Medium &nbsp; 🔵 Low / Refactor

---

## 1. Backend — Auth & Security

### 🔴 1.1 Rate limiter trusts `X-Forwarded-For` blindly → trivially bypassed
`RateLimitFilter.getClientIP()` takes the first value of the client-supplied `X-Forwarded-For` header with no validation that the request actually came through a trusted proxy. Any caller can set `X-Forwarded-For: <random-value>` on every request and get a fresh rate-limit bucket every time, completely defeating brute-force protection on `/api/auth/login` and `/register`.
**Fix:** only trust `X-Forwarded-For` when the request comes from a known reverse-proxy IP (configure `server.forward-headers-strategy` / trusted proxies), otherwise use `request.getRemoteAddr()`.

### 🔴 1.2 Negative-amount financial bypass in wallet & proposal logic
None of `depositToPersonal`, `withdrawFromPersonal`, `contributeToGroup`, or proposal `amount` validate that the value is **positive**. Example exploit:
```
POST /api/wallets/me/personal/withdraw   { "amount": -1000000 }
```
`balance.compareTo(amount) < 0` will pass (balance ≥ a huge negative number), then `balance.subtract(-1000000)` **adds** a million to the balance instead of subtracting. The same flaw lets a "contribution" of a negative amount drain a group wallet into a personal one, and a proposal with a negative amount will inflate the group wallet instead of debiting it once "approved."
**Fix:** validate `amount != null && amount.compareTo(BigDecimal.ZERO) > 0` (and probably a sane upper bound) at the DTO level with `@Positive`/`@DecimalMin`, enforced everywhere money moves.

### 🟠 1.3 Broken access control (IDOR) on group/proposal read endpoints
- `GET /api/groups/{groupId}` — no membership check, any authenticated user can view any group.
- `GET /api/groups/{groupId}/members` — **no auth/membership check at all**, leaks full names & phone numbers of any group's members to any authenticated stranger.
- `GET /api/groups/{groupId}/proposals` and `GET /api/proposals/{proposalId}` — no membership check, financial withdrawal requests of any group are readable by any logged-in user.

Compare with `getGroupStats()`, which *does* check membership correctly — the inconsistency confirms this is an oversight rather than intentional design.
**Fix:** add a membership check (`memberRepo.findByGroupIdAndUserId(...)`) to every group-scoped read, not just stats.

### 🟠 1.4 `GlobalExceptionHandler` leaks internal exception messages
Every `RuntimeException` (including `NullPointerException`, `DataIntegrityViolationException`, etc.) is caught generically and `ex.getMessage()` is sent straight to the client as a 400. This means DB constraint errors, NPEs, and other internal failures can leak schema/implementation details to end users, and everything gets flattened to HTTP 400 even when 401/403/404 would be correct.
**Fix:** introduce a small hierarchy of business exceptions (`NotFoundException`, `ForbiddenException`, `ValidationException`, etc.) mapped to correct HTTP codes; add a catch‑all `Exception.class` handler that logs the real error server-side and returns a generic "internal error" message to the client.

### 🟠 1.5 `/api/auth/me` is reachable unauthenticated and NPEs
`/api/auth/**` is `permitAll()`, which includes `/me`. A request with no/invalid token reaches the controller with `user == null`, and `user.toDto()` throws an NPE — caught by the generic handler above and returned as a confusing 400. This endpoint should require authentication like everything else; only `/register` and `/login` should be public.

### 🟠 1.6 No token invalidation on password change
Changing a password does not rotate/invalidate previously issued JWTs. A stolen token remains valid for the rest of its 1-hour lifetime even after the legitimate user changes their password because they suspect compromise.
**Fix (pragmatic given stateless JWT):** track a `tokenVersion`/`passwordChangedAt` claim on the user and check it in the filter, or move to short-lived access tokens + refresh tokens as already noted in your own `docs/security/authentication.md`.

### 🟡 1.7 Hardcoded weak secrets committed to source control
`application.yml` ships a literal fallback JWT secret (`change-this-to-a-very-long-random-secret-key!!`) and DB credentials (`root`/`root`) that are also the compose-file defaults. If `JWT_SECRET`/DB env vars are ever forgotten in a real deployment, the app **silently boots with a known, public secret** instead of failing fast.
**Fix:** remove defaults from `application.yml` entirely (`${JWT_SECRET}` with no fallback) so Spring fails to start if the env var is missing; never commit even a "placeholder" production secret.

### 🟡 1.8 Filter ordering doesn't match the code comment
The comment in `SecurityConfig` claims the chain is `RateLimitFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter`, but because `addFilterBefore(jwtFilter, ...)` is called *before* `addFilterBefore(rateLimitFilter, ...)`, Spring actually inserts them so **JwtAuthenticationFilter runs first**. Currently low-impact (JWT filter doesn't block anything), but it's misleading and will bite you if either filter changes behavior.
**Fix:** swap the two calls, or better, use explicit filter ordering.

### 🟡 1.9 No rate limiting beyond `/api/auth/**`
Deposit, withdraw, contribute, vote, and proposal-creation endpoints have no throttling — they can be hammered (accidental double-submits from the UI, or deliberate abuse/spam of proposals & votes).

### 🔵 1.10 JWT subject is a mutable field (phone number)
The token's `sub` claim is the user's phone number, and `UserService.updateProfile` allows changing it freely (no uniqueness/format re-validation either — see 2.4). If a user updates their phone, their existing token silently stops resolving to their account. Prefer using the immutable numeric user ID as the JWT subject.

### 🔵 1.11 No account-recovery flow
There's no "forgot password" mechanism — a locked-out user has no self-service recovery path. Worth a design discussion even if not in this bug-fix pass.

---

## 2. Backend — Business Logic

### 🔴 2.1 No optimistic/pessimistic locking on `Wallet.balance`
Wallet balance reads/updates (`read → mutate in Java → save`) have no `@Version` field or row locking. Two concurrent requests (double-click deposit, or a contribution racing a proposal payout) can produce a classic lost-update race condition — a serious correctness bug for a financial ledger.
**Fix:** add `@Version` to `Wallet` for optimistic locking (simplest), or use `@Lock(PESSIMISTIC_WRITE)` on the wallet fetch inside financial transactions for stronger guarantees under contention.

### 🟠 2.2 `deleteGroup` doesn't cascade proposals/votes — will throw a raw FK violation
`Group.proposals`/`Proposal.votes` aren't cleaned up before `groupRepo.delete(group)`. If any proposal was ever created in that group, deletion will fail with a foreign-key constraint violation, surfaced to the client as a leaked DB error (see 1.4).
**Fix:** cascade-delete votes → proposals → members → wallet → group inside the transaction, or add `ON DELETE CASCADE` at the schema level and add integration tests for this path.

### 🟡 2.3 Proposals can stay `OPEN` forever
There's no expiry/quorum timeout — if a vote never reaches a strict majority in either direction, the proposal sits open indefinitely with no way to close it. Worth adding a deadline field and a scheduled job (or lazy check-on-read) to auto-reject stale proposals.

### 🟡 2.4 `UserService.updateProfile` has no validation at all
Unlike `RegisterRequest` (which validates phone format via regex), `PUT /api/users/me` accepts a raw `Map<String,String>` and writes `phoneNumber`/`fullName` straight into the entity — no format check, no uniqueness check. A duplicate phone number will blow up as a raw `DataIntegrityViolationException` (leaked per 1.4), and a malformed phone number will silently corrupt the user's login identity (see 1.10).
**Fix:** introduce a proper `UpdateProfileRequest` DTO with the same `@Pattern`/`@NotBlank` constraints as registration, and explicitly check `existsByPhoneNumber` before applying a change.

### 🔵 2.5 Inconsistent request bodies
Auth DTOs (`RegisterRequest`, `LoginRequest`) are validated, typed classes. Group/Proposal/Wallet endpoints instead take raw `Map<String,String>`/`Map<String,BigDecimal>` with manual `.get(...)` calls and no `@Valid`. This is both a bug source (see 1.2, 2.4) and a maintainability problem.
**Fix:** create proper `CreateGroupRequest`, `CreateProposalRequest`, `DepositRequest`, etc. with bean validation, consistent with the auth package's existing pattern.

---

## 3. Infrastructure / CI-CD / Config

### 🟠 3.1 `ddl-auto: update` with no migrations, no environment profiles
There is a single `application.yml` used everywhere — no `application-dev.yml`/`application-prod.yml` split. `hibernate.ddl-auto: update` lets Hibernate auto-mutate the schema on every boot; for a financial app this is risky (silent schema drift, no audit trail of schema changes, no safe rollback).
**Fix:** introduce Flyway (or Liquibase) migrations, set `ddl-auto: validate` in all real environments, and split config into Spring profiles.

### 🟡 3.2 `show-sql: true` always on
Logs raw SQL (and potentially bound values, depending on log level) to stdout unconditionally — a PII leak risk in production logs. Should be dev-profile-only.

### 🟡 3.3 MariaDB port published to the host in both compose files
`docker-compose.yml` and `docker-compose.prod.yml` both map `3306:3306`, exposing the database directly on the host network. In production this is unnecessary attack surface — the backend reaches the DB over the internal `akiba-net` Docker network already.
**Fix:** drop the `ports:` mapping for `mariadb` in `docker-compose.prod.yml` (keep it in the dev file if you want local DB access for debugging).

### 🟡 3.4 `docker-compose.prod.yml` pulls a floating `:latest` tag
The CI pipeline builds and pushes both `:latest` and `:<sha>`, but the prod compose file only ever deploys `:latest`. This makes rollbacks and reproducible deploys hard — you can't easily pin or roll back to a known-good build from the deploy script alone.
**Fix:** template the tag (e.g., pass the SHA into the deploy step and `docker compose pull backend@sha` or set an env var consumed by the compose file).

### 🔵 3.5 No post-deploy health check / rollback in CI
`deploy.yml` pulls the new image and does `docker compose up -d` with no verification step afterward (e.g., curling `/health`), so a broken deploy isn't automatically caught.

### 🔵 3.6 No dependency/image vulnerability scanning in CI
`pom.xml` already includes `dependency-check-maven` as a plugin but the workflow never actually runs it (`mvn -B package` doesn't trigger the OWASP check goal by default). No container image scan (e.g., Trivy) either.

### 🔵 3.7 No container resource limits
Neither compose file sets CPU/memory limits, so a runaway container isn't contained.

---

## 4. Frontend

### 🔴 4.1 Stored XSS via unescaped proposal titles in `dashboard.js` / `proposals.js`
`group.js` and `groups.js` both escape user content with `escapeHtml()` before injecting into `innerHTML`. **`dashboard.js` and `proposals.js` do not** — `${p.title}` and `${p.status}` are interpolated raw. Since a proposal title is free-text user input with no backend sanitization, a title like `<img src=x onerror=alert(document.cookie)>` becomes a stored XSS payload that fires for every group member who views their dashboard or proposals list.
**Fix:** move `escapeHtml()` into `utils.js` as a shared helper and apply it consistently everywhere user-controlled strings (titles, descriptions, references, names) are rendered — including `transactions.js` and `wallet.js`'s `t.reference` field.

### 🟠 4.2 There is no way to vote "NO" in the UI
Every vote button ("Vote YES") is hardcoded to submit `{ vote: 'YES' }` in `dashboard.js`, `group.js`, and `proposals.js`, even though the backend fully supports `NO` votes and the whole point of the app is democratic governance. This is a functional bug, not just a UX gap — members literally cannot reject a proposal through the app.

### 🟠 4.3 JWT stored in `localStorage`
`akiba_token` lives in `localStorage`, readable by any script on the page — a stored-XSS bug like 4.1 becomes full account takeover, not just cosmetic damage. Your own `docs/security/authentication.md` already flags this as a known risk.
**Fix (in order of effort):** at minimum, fix all XSS holes first; longer-term, move to an httpOnly, Secure, SameSite cookie-based session or short-lived access token + refresh token pattern.

### 🟡 4.4 Dead/duplicate file `frontend/js/app.js`
Not referenced by any `.html` file — it's leftover code that redefines `getToken`, `authHeaders`, `deposit`, `createGroup`, `joinGroup`, `contributeToGroup`, `createProposal`, `voteOnProposal`, `fetchMyProposals` with **different, sometimes buggier behavior** than the real `api.js` (e.g. its `voteOnProposal` ignores the vote value entirely, its fetch helpers don't unwrap `ApiResponse` or check `success`). Harmless today only because nothing loads it, but it's a landmine for the next person who wires it into a page by habit.
**Fix:** delete it.

### 🟡 4.5 Inline `onclick="vote('${p.id}')"` handlers
Used in `group.js` and `proposals.js`. Works today, but (a) it's inline JS in HTML markup, which blocks you from ever adding a strict Content-Security-Policy without `unsafe-inline`, and (b) reassigns `window.vote` on every render, which is a code smell even if not currently harmful.
**Fix:** use `addEventListener` with `data-proposal-id` attributes and event delegation instead.

### 🔵 4.6 Serial `await` chains instead of `Promise.all`
`dashboard.js` and `group.js` fetch several independent resources one after another (`fetchDashboard()` → `fetchMyGroups()` → `fetchMyProposals()`, and in `group.js`: group → current user → invite code → wallets → stats → proposals). None of these depend on each other in most cases; running them serially adds unnecessary latency to every page load.

### 🔵 4.7 `group.js` fires a state-changing `POST /invite` on every page load
For the group creator, loading the group page automatically calls `POST /api/groups/{id}/invite` (to fetch/display the invite code) on every visit. It's idempotent today only because the backend happens to only generate a code if one doesn't already exist — but semantically a `POST` shouldn't be triggered by a page load, and this breaks the moment invite-code rotation is added.
**Fix:** expose a `GET` for reading the existing code, reserve `POST` for explicit regeneration via a button.

### 🔵 4.8 No pagination anywhere
Transactions, proposals, and groups lists all render the full result set. Fine at current scale, worth flagging before it becomes a real problem.

---

## 5. Suggested Fix Order

Given your priority on auth/security, I'd tackle it roughly in this order:

1. **🔴 Money-safety bugs first** — negative-amount validation (1.2) and wallet locking (2.1). These are the ones that can actually cost someone real money.
2. **🔴 Rate-limit bypass (1.1)** and **stored XSS (4.1)** — both are trivially exploitable today.
3. **🟠 Access control fixes** — IDOR on group/member/proposal reads (1.3), `/auth/me` auth requirement (1.5).
4. **🟠 Error handling overhaul** — exception hierarchy + generic handler (1.4), which also fixes the `deleteGroup` FK crash (2.2) and profile-update crash (2.4) as a side effect.
5. **🟡 Everything else** — secrets/config hygiene (1.7, 3.1–3.3), DTO validation cleanup (2.5), UI fixes (4.2, 4.4, 4.5).
6. Then move on to the UI/asset refresh once the above is solid.

Let me know which items you want me to start fixing — happy to go straight down the list, or jump to whichever section worries you most.