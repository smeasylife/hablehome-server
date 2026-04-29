# Shopping Mall Backend

Spring Boot REST API for the Hable/Haein shopping mall.

## Run with Docker Compose

```bash
docker compose up --build
```

The API is exposed at `http://localhost:8080`.

## Local Defaults

- Database: PostgreSQL in Docker Compose
- Seed user: `user@example.com` / `password`
- Authentication: Spring Security session login with `JSESSIONID`
- Admin seed account: set `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, and optionally `APP_ADMIN_NICKNAME`
- Signup verification codes are stored in memory and logged by the application.
- State-changing requests require a CSRF token from `GET /auth/csrf`.

## Authorization

| Role | Access |
|------|--------|
| Anonymous | item reads, signup, email login, Kakao login stub, CSRF token |
| `ROLE_USER` | cart, likes, reviews, questions, session lookup, logout |
| `ROLE_ADMIN` | user access plus item writes, coupons, Q&A answers, review comments |

## Main Endpoints

- `POST /auth/kakao/login`
- `GET /auth/csrf`
- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/logout`
- `POST /signup`
- `POST /signup/send-code?email=...`
- `POST /signup/verify-code`
- `GET /items?page=0`
- `GET /items/{itemId}`
- `POST /items`
- `PUT /items/{itemId}`
- `POST /{itemId}/cart`
- `POST /{itemId}/like`
- `POST /{itemId}/review`
- `POST /{reviewId}/comment`
- `POST /question`
- `POST /answer/{questionId}?answer=...`
- `POST /coupon`
