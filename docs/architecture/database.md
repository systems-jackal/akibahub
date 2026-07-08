# 🗄️ Database Schema

## Overview

**Database Engine:** MariaDB 10.11

**Database Name:** `akibahub`

The database schema is automatically managed by **Hibernate** using:

```properties
spring.jpa.hibernate.ddl-auto=update
```

---

# 📋 Tables

| Table | Description |
|--------|-------------|
| `users` | Registered users *(phone, id_number, password_hash, full_name)* |
| `wallets` | Personal and group wallets *(user_id, group_id, type, balance)* |
| `transactions` | All financial movements *(wallet_id, amount, type, reference, timestamp)* |
| `groups_table` | Savings groups *(name, description, rules, invite_code, created_by)* |
| `group_members` | Membership junction *(group_id, user_id, joined_at)* |
| `proposals` | Withdrawal proposals *(group_id, title, description, amount, status, created_by)* |
| `votes` | Votes on proposals *(proposal_id, user_id, vote)* |
| `audit_log` | Immutable event log *(event_type, payload, created_at)* |

---

# 1️⃣ users

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `phone_number` | VARCHAR(15) | UNIQUE, NOT NULL |
| `id_number` | VARCHAR(8) | UNIQUE, NOT NULL |
| `password_hash` | VARCHAR(255) | NOT NULL |
| `full_name` | VARCHAR(255) | NOT NULL |
| `created_at` | DATETIME(6) | NOT NULL |

---

# 2️⃣ wallets

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → `users.id` *(nullable)* |
| `group_id` | BIGINT | FK → `groups_table.id` *(nullable)* |
| `type` | ENUM('PERSONAL','GROUP') | NOT NULL |
| `balance` | DECIMAL(19,4) | DEFAULT 0 |

---

# 3️⃣ transactions

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `wallet_id` | BIGINT | FK → `wallets.id`, NOT NULL |
| `amount` | DECIMAL(19,4) | NOT NULL |
| `type` | ENUM('DEPOSIT','WITHDRAWAL') | NOT NULL |
| `reference` | VARCHAR(255) | |
| `timestamp` | DATETIME(6) | NOT NULL |

---

# 4️⃣ groups_table

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `name` | VARCHAR(255) | NOT NULL |
| `description` | VARCHAR(255) | |
| `rules` | VARCHAR(500) | |
| `invite_code` | VARCHAR(6) | UNIQUE |
| `created_by` | BIGINT | FK → `users.id`, NOT NULL |
| `created_at` | DATETIME(6) | NOT NULL |

---

# 5️⃣ group_members

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `group_id` | BIGINT | FK → `groups_table.id`, NOT NULL |
| `user_id` | BIGINT | FK → `users.id`, NOT NULL |
| `joined_at` | DATETIME(6) | NOT NULL |
| **Constraint** | | UNIQUE(`group_id`, `user_id`) |

---

# 6️⃣ proposals

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `group_id` | BIGINT | FK → `groups_table.id`, NOT NULL |
| `title` | VARCHAR(255) | NOT NULL |
| `description` | VARCHAR(255) | |
| `amount` | DECIMAL(19,4) | NOT NULL |
| `status` | ENUM('OPEN','APPROVED','REJECTED') | NOT NULL |
| `created_by` | BIGINT | FK → `users.id`, NOT NULL |
| `created_at` | DATETIME(6) | NOT NULL |

---

# 7️⃣ votes

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `proposal_id` | BIGINT | FK → `proposals.id`, NOT NULL |
| `user_id` | BIGINT | FK → `users.id`, NOT NULL |
| `vote` | ENUM('YES','NO') | NOT NULL |
| **Constraint** | | UNIQUE(`proposal_id`, `user_id`) |

---

# 8️⃣ audit_log

| Column | Type | Constraints |
|---------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `event_type` | VARCHAR(255) | NOT NULL |
| `payload` | TEXT | |
| `created_at` | DATETIME(6) | NOT NULL |