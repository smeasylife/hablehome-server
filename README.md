# Shopping Mall Backend

Spring Boot REST API for the Hable/Haein shopping mall.

## Run with Docker Compose

```bash
docker compose up --build
```

The API is exposed at `http://localhost:8080`.

## Local Defaults

- Database: PostgreSQL in Docker Compose
- Seed user: `user@example.com`
- Auth-required APIs accept `X-Member-Id`; omit it to use the seed member with id `1`.
- Signup verification codes are stored in memory and logged by the application.

## Main Endpoints

- `POST /auth/kakao/login`
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
