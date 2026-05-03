# HTTPS Deployment with Nginx + Certbot

This project now supports `Nginx -> Spring` reverse-proxy layout in Docker Compose.

## 1) Prerequisites

- A domain (example: `api.example.com`) pointing to the server public IP
- Inbound ports `80` and `443` open in firewall / cloud security group
- Replace domain placeholders in `nginx/conf.d/default.conf` and `nginx/conf.d/default-ssl.conf.example`

## 2) Start app + nginx (HTTP)

```bash
docker compose up -d --build postgres spring nginx
```

Check:

```bash
docker compose ps
curl -I http://api.example.com
```

## 3) Issue certificate (webroot)

Run certbot one-shot container with your domain/email:

```bash
docker compose run --rm certbot certonly \
  --webroot -w /var/www/certbot \
  -d api.example.com \
  --email you@example.com \
  --agree-tos --no-eff-email
```

## 4) Enable HTTPS server block

After certificate issuance:

1. Copy `nginx/conf.d/default-ssl.conf.example` to `nginx/conf.d/default.conf` (or merge it)
2. Reload nginx

```bash
docker compose exec nginx nginx -t
docker compose restart nginx
```

## 5) Renewal

Dry-run:

```bash
docker compose run --rm certbot renew --dry-run
```

Typical cron example (host cron):

```bash
0 3 * * * cd /path/to/hablehome-server && \
  docker compose run --rm certbot renew && \
  docker compose restart nginx
```

## Spring settings recommendation

- `SESSION_COOKIE_SECURE=true`
- `SESSION_COOKIE_SAME_SITE=none` (if frontend/backend are cross-site)
- `FRONTEND_ALLOWED_ORIGINS` must use `https://...` origins

