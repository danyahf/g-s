# 🔐 Authentication Flow

## Overview
The authentication system uses **JWT** and **refresh token** to manage user sessions securely.  
Users authenticate via `/auth/login`, refresh their tokens via `/auth/refresh`, and log out via `/auth/logout`.  
Expired or unused tokens are automatically cleaned up.

---

## 1. Login — `POST /auth/login`
When a user provides valid credentials:

- The server issues a **pair of tokens**:
    - **Access Token (JWT)** — short-lived, used for API authentication.
    - **Refresh Token** — long-lived, used to obtain new tokens after JWT expiry.
- The frontend stores both tokens securely (e.g., JWT in memory/localStorage, refresh token in a secure store).

### Example Response
```json
{
  "accessToken": "jwt-token-value",
  "refreshToken": "refresh-token-value"
}
```

---

## 2. Using the Access Token
Each request to a protected endpoint must include the JWT in the `Authorization` header:

```http
Authorization: Bearer <jwt>
```

Authentication is implemented in a **stateless** manner — the server does not query the database on each request. It simply verifies the validity and signature of the provided JWT (**JwtAuthenticationFilter**)
If the JWT expires, the client must refresh it using the refresh token.

---

## 3. Refreshing Tokens — `POST /auth/refresh`
- The client sends the **refresh token** in the request body.
- If valid, the server returns a **new pair of tokens** (JWT + refresh token).
- The **previous refresh token is invalidated** immediately.
- Refresh tokens are stored in the database (`refresh_tokens` table).

### Example Request
```json
{
  "refreshToken": "old-refresh-token"
}
```

### Example Response
```json
{
  "accessToken": "new-jwt-token",
  "refreshToken": "new-refresh-token"
}
```

---

## 4. Logout — `POST /auth/logout`
- All refresh tokens associated with the user are **deleted from the database**.
- The frontend must **remove the JWT** from localStorage or memory.
- This effectively terminates all active sessions for the user.

---

## 5. Token Cleanup
Inactive or expired refresh tokens remain in the database until removed.  
A scheduled maintenance task periodically runs:

```java
purgeExpiredTokens();
```

This method deletes all expired tokens, cleaning up **dead sessions**.
