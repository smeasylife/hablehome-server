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
- Signup verification codes are stored in memory. Mail delivery is disabled by default and the code is logged for local development.
- State-changing requests require a CSRF token from `GET /auth/csrf`.

## Profiles and Environment

The default profile is `local`. Use `SPRING_PROFILES_ACTIVE=prod` in production.

- `local`: uses PostgreSQL local defaults, `ddl-auto=update`, localhost CORS origins, and non-secure session cookies.
- `prod`: requires datasource and CORS environment variables, defaults `ddl-auto=validate`, enables SMTP mail, and uses `Secure` + `SameSite=None` session cookies.

Copy the root `.env.example` to your local environment manager and replace every `change-me` value before running outside local development. Production secrets should be supplied by the hosting platform, not committed.

## Signup Mail

Set these environment variables to send real signup verification emails:

```bash
APP_MAIL_ENABLED=true
APP_MAIL_FROM=sender@example.com
APP_MAIL_FROM_NAME=HABLE
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=sender@example.com
SPRING_MAIL_PASSWORD=app-password
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS_ENABLE=true
```

## Production Session Cookies

For cross-site frontend/backend deployments, keep:

```bash
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=none
```

If the API and frontend share a parent domain and you need a cookie domain, set Spring Boot's standard variable directly:

```bash
SERVER_SERVLET_SESSION_COOKIE_DOMAIN=.example.com
```

## Kakao Login

Set these environment variables and register `http://localhost:5173/login` as a Kakao Login redirect URI in Kakao Developers:

```bash
KAKAO_REST_API_KEY=your-rest-api-key
KAKAO_CLIENT_SECRET=your-client-secret-if-enabled
```

The frontend also needs:

```bash
VITE_KAKAO_REST_API_KEY=your-rest-api-key
```

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
