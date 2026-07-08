# 📘 API Reference

## Base URL

**Production**

```text
https://akiba.unitybridge.dev
```

**Local Development**

```text
http://localhost:8080
```

---

# 📦 Standard Response Format

All API endpoints return the following response envelope:

```json
{
  "success": true,
  "message": "optional message",
  "data": {
    ...
  }
}
```

If an operation fails, the response will be:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

# 🔐 Authentication

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **POST** | `/api/auth/register` | Register a new user *(phone, password, fullName, idNumber)* | ❌ No |
| **POST** | `/api/auth/login` | Login using phone number or ID number. Returns JWT. | ❌ No |
| **GET** | `/api/auth/me` | Retrieve the currently authenticated user's profile. | ✅ Yes |

---

# 💰 Wallet

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **GET** | `/api/wallets/me` | Get all wallets *(personal + group wallets)* | ✅ Yes |
| **POST** | `/api/wallets/me/personal/deposit` | Deposit into personal wallet *(internal)* | ✅ Yes |
| **POST** | `/api/wallets/me/personal/withdraw` | Withdraw from personal wallet | ✅ Yes |
| **POST** | `/api/wallets/groups/{groupId}/contribute` | Contribute funds to a group wallet | ✅ Yes |

---

# 👥 Groups

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **POST** | `/api/groups` | Create a new group *(name, description, rules)* | ✅ Yes |
| **POST** | `/api/groups/{groupId}/join` | Join a group using an ID or invite code | ✅ Yes |
| **GET** | `/api/groups/my` | List all groups the current user belongs to | ✅ Yes |
| **GET** | `/api/groups/{groupId}` | Retrieve group details | ✅ Member |
| **PUT** | `/api/groups/{groupId}` | Update group *(name, description)* | 👑 Creator only |
| **DELETE** | `/api/groups/{groupId}` | Delete a group *(wallet must be empty)* | 👑 Creator only |
| **POST** | `/api/groups/{groupId}/invite` | Generate an invite code or invite link | 👑 Creator only |
| **GET** | `/api/groups/{groupId}/stats` | Get group statistics *(total savings, member count)* | ✅ Member |
| **GET** | `/api/groups/{groupId}/members` | List all group members | ✅ Member |

---

# 🗳️ Proposals

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **POST** | `/api/groups/{groupId}/proposals` | Create a withdrawal proposal | ✅ Member |
| **POST** | `/api/proposals/{proposalId}/vote` | Vote **YES** or **NO** on a proposal | ✅ Member |
| **GET** | `/api/proposals/my` | List proposals across the user's groups | ✅ Yes |
| **GET** | `/api/proposals/{proposalId}` | Retrieve proposal details | ✅ Yes |
| **PUT** | `/api/proposals/{proposalId}` | Edit an open proposal | 👑 Creator only |
| **DELETE** | `/api/proposals/{proposalId}` | Delete an open proposal | 👑 Creator only |
| **GET** | `/api/groups/{groupId}/proposals` | List proposals for a specific group | ✅ Member |

---

# 💳 Transactions

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **GET** | `/api/transactions/me?type=&groupId=&start=&end=` | Retrieve filtered transaction history | ✅ Yes |

---

# 👤 User Profile

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **PUT** | `/api/users/me` | Update profile *(fullName, phoneNumber)* | ✅ Yes |
| **PUT** | `/api/users/me/password` | Change password *(currentPassword, newPassword)* | ✅ Yes |

---

# 📊 Dashboard

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **GET** | `/api/dashboard` | Retrieve aggregated dashboard data *(balances, groups, pending votes)* | ✅ Yes |

---

# ❤️ Health Check

| Method | Endpoint | Description | Authentication |
|---------|----------|-------------|----------------|
| **GET** | `/health` | Returns the application health status (`{"status":"UP"}`) | ❌ No |