# DevOps Guide

How to build, run, test and deploy IMPROV.

**Companion docs:** [`PHASE_PLAN.md`](./PHASE_PLAN.md) (what's planned) ·
[`MOBILE_DESKTOP.md`](./MOBILE_DESKTOP.md) (native packaging)

---

## 1. What exists now

| Area | Status |
|---|---|
| Containerisation | ✅ Dockerfiles for api + web, compose stack with healthchecks |
| Reverse proxy | ✅ nginx serves static + proxies `/api`, single origin, rate limiting |
| Config & secrets | ✅ Profiles, env vars, `.env.example`, `.gitignore`, no secrets in git |
| Schema management | ✅ Flyway: users baseline + progression schema, seeded config (curve, ranks, templates) |
| Auth | ✅ JWT access + refresh in httpOnly `SameSite=Strict` cookies, BCrypt passwords |
| CI | ✅ Build + 19 unit tests, frontend static checks, compose smoke test, e2e core loop |
| CD | ✅ Image publishing to GHCR on tag; SSH deploy workflow (needs secrets) |
| Desktop packaging | ✅ Tauri config + cross-platform release workflow |
| Mobile packaging | ✅ Capacitor config + Android/iOS workflows |
| Observability | ✅ Prometheus metrics + Grafana/Uptime Kuma compose (dashboards to build) |
| Backups | ✅ `deploy/backup.sh` + CI-proven restore test |

---

## 2. Run it locally

### Prerequisites

- Docker Engine 24+ with the Compose v2 plugin, **or**
- JDK 26 + Maven (use `./mvnw`) + MySQL 8.4 for running the API bare

### The one-command path

```bash
cp .env.example .env          # then edit the passwords
docker compose up --build
```

| URL | What |
|---|---|
| <http://localhost:8080> | The app (nginx) |
| <http://localhost:8080/api/> | Backend, proxied |
| <http://localhost:8080/actuator/health> | *(proxied only if you add the route — see §4)* |

### Development override

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Adds: MySQL published on `localhost:3306` (for a GUI client) and the API
published on `localhost:8081` (to hit endpoints without going through nginx).
Runs the `dev` Spring profile with SQL logging.

### Running the API bare (no containers)

```bash
cd Backend/ap
export DB_URL='jdbc:mysql://localhost:3306/workoutapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=improv
export DB_PASSWORD=...
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

### Tests

```bash
cd Backend/ap && ./mvnw verify
```

Tests run against in-memory H2 — **no database required.** This is a change
from before: the only existing test needed a live MySQL on `127.0.0.1:3306`,
which meant CI could never pass.

Migration correctness is *not* covered by H2 (the DDL is MySQL-specific). The
CI `integration` job proves those against a real MySQL.

---

## 3. Configuration

### Why `application.properties` is gone

It was replaced by `application.yml` plus profile files:

| File | Purpose |
|---|---|
| `application.yml` | Shared config, defaults to the `dev` profile |
| `application-dev.yml` | SQL logging, expanded actuator endpoints |
| `application-prod.yml` | Strict validation, stdout logging, minimal actuator |
| `src/test/resources/application-test.properties` | H2, Flyway off |

The old file had `spring.datasource.password=sphy2323` committed in plaintext.
**That password is in git history and must be treated as burned.** Rotate it —
and rotate anything else that ever used it.

### Environment variables

Spring's relaxed binding maps `DB_URL` → `spring.datasource.url`, so the app
needs no code awareness of its environment.

| Variable | Required | Default | Notes |
|---|---|---|---|
| `DB_URL` | prod | `jdbc:mysql://localhost:3306/workoutapp` | Full JDBC URL |
| `DB_USERNAME` | prod | `improv` | |
| `DB_PASSWORD` | prod | *(none)* | **No default in prod — fails loudly if unset** |
| `DB_ROOT_PASSWORD` | yes | *(none)* | MySQL root, used by the db container |
| `DB_NAME` | no | `workoutapp` | |
| `DB_POOL_SIZE` | no | `20` in prod | Hikari `maximumPoolSize` |
| `SPRING_PROFILES_ACTIVE` | no | `dev` | `prod` in containers |
| `HTTP_PORT` | no | `8080` | Host port for the web container |
| `API_IMAGE` | no | `improv-api:local` | Image tag for the api service — `deploy.yml` overrides it with the GHCR image |
| `WEB_IMAGE` | no | `improv-web:local` | Image tag for the web service — `deploy.yml` overrides it with the GHCR image |
| `SERVER_PORT` | no | `8080` | API container port |
| `JAVA_OPTS` | no | see `.env.example` | JVM flags |
| `TZ` | no | `UTC` | Keep UTC; user timezones live in the DB |
| `JWT_SECRET` | yes | *(none)* | HS256 signing secret for the auth cookies — `openssl rand -base64 48`; no default in any profile, compose refuses to start without it |
| `AUTH_COOKIE_SECURE` | no | `true` | Secure flag on the auth cookies; `false` only for plain-http testing |
| `GRAFANA_ADMIN_PASSWORD` | monitoring | *(none)* | Grafana admin password (required with the monitoring stack) |
| `GRAFANA_PORT` | no | `3000` | Host port for Grafana |
| `RETENTION_DAYS` | no | `30` | Local backup retention (`deploy/backup.sh`) |
| `S3_BUCKET` | no | *(unset)* | If set (and aws CLI present), backups copy to `s3://$S3_BUCKET/db/` |

### Authentication

Login sets two httpOnly, `SameSite=Strict` cookies: `improv_access` (JWT, 15
min) and `improv_refresh` (JWT, 7 days). The browser attaches them
automatically; JavaScript never sees a token. Because the cookies are
SameSite=Strict, cross-site requests never carry them — that is why CSRF is
disabled in the security config (the cookie policy *is* the CSRF protection).

When the access token expires, the frontend calls `POST /api/auth/refresh`
once and retries the original request; if that fails too, it redirects to the
login page. Refresh tokens cannot be revoked before they expire — the accepted
trade-off of stateless JWT, and the reason the access token is short.

Passwords are BCrypt-hashed. `JWT_SECRET` is required **everywhere** — there
is no default in any profile; `docker-compose.yml` enforces it with
`${JWT_SECRET:?}` and the app itself fails fast on a missing or too-short
secret.

---

## 4. The reverse proxy

`Frontend/nginx.conf` is the single most important file in the deploy story.

**It collapses two problems into one solution:**

1. **CORS disappears.** Frontend and API share an origin, so there is no
   cross-origin request anywhere. The old `@CrossOrigin(origins = "*")` on the
   controller can be deleted — it is a development crutch, not a design.
2. **No environment-specific URLs.** JS calls `/api/...` and nothing else. CI
   fails the build if a hardcoded `localhost:PORT` reappears in any `.js` file.

Other things it does:

- **Rate limiting** — `limit_req_zone` on `~ ^/api/quests/[^/]+/log$`, the
  endpoint that awards XP and is therefore the abuse target.
- **Caching** — HTML and `sw.js` are `no-cache, must-revalidate` (so a deploy
  is visible immediately and installed clients don't get stuck on an old
  build); images/CSS/JS get a short TTL.
  *The TTL is short because filenames are not content-hashed yet. Once the
  frontend build emits hashed names, raise it to `immutable` for a year.*
- **Security headers** — `X-Content-Type-Options`, `X-Frame-Options`,
  `Referrer-Policy`.
- **Health** — `/healthz` returns 200 for the container healthcheck.

**Deliberately absent:** a `Content-Security-Policy`. The current markup uses
inline `onclick=` handlers, which any useful CSP blocks. Adding one is a task
once the markup is cleaned up.

### Exposing actuator through nginx

Not routed by default, on purpose — a public `/actuator/env` would leak
configuration. If you need it for monitoring, add a path-restricted location:

```nginx
location /actuator/health {
    proxy_pass http://improv_api;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    # allow 10.0.0.0/8;  deny all;
}
```

---

## 5. CI/CD

### `ci.yml` — pull requests and pushes to `main`

| Job | What it does |
|---|---|
| `backend` | `mvn verify` on JDK 26, uploads Surefire reports |
| `frontend` | Asserts the app shell exists, validates the PWA manifest, **fails on any hardcoded backend origin**, `node --check` on every JS file |
| `integration` | Builds and boots the full compose stack, waits for the API healthcheck, asserts Flyway applied the baseline, asserts nginx serves the shell and proxies `/api` |

The `integration` job is the one that matters. It's the difference between "it
compiles" and "it runs".

The proxy check asserts a bad login answers **401** — the auth error contract
fixed in P0. It fails on anything else, including 502/504, which would mean
nginx genuinely can't reach the API.

### `release.yml` — tags

```bash
git tag v0.1.0
git push origin v0.1.0
```

Builds and pushes to GHCR with layer caching, provenance and SBOM:

```
ghcr.io/<owner>/improv-api:0.1.0
ghcr.io/<owner>/improv-web:0.1.0
```

### `deploy.yml` — manual

Requires these repository secrets (**Settings → Secrets and variables → Actions**):

| Secret | Example |
|---|---|
| `DEPLOY_HOST` | `improv.example.com` |
| `DEPLOY_USER` | `deploy` |
| `DEPLOY_SSH_KEY` | private ed25519 key |
| `DEPLOY_PATH` | `/opt/improv` |
| `DEPLOY_ENV` | full production `.env` contents |

The workflow pulls the images on the host, restarts compose, and **gates on the
healthcheck** — if the API comes up unhealthy it fails the deploy rather than
leaving a broken release live. It is `workflow_dispatch`-only for now: get
comfortable before letting it fire on every merge.

---

## 6. Production deployment

Target: a small VPS (Hetzner / DigitalOcean, ~$5–6/mo).

### Layout

```
/opt/improv/            # repo checkout — docker-compose.yml + .env
/etc/caddy/Caddyfile    # TLS termination, reverse-proxies to :8080
```

Caddy gets automatic certificates, which is why it's preferred over manual
certbot:

```
improv.example.com {
    encode gzip
    reverse_proxy localhost:8080
}
```

### First deploy

1. Provision the VPS; create a non-root `deploy` user with docker access
2. Add `deploy`'s public key to GitHub's deploy secrets
3. Clone the repo to `/opt/improv`, write `.env` (chmod 600), **not** in git
4. Point DNS at the host; let Caddy issue the cert
5. Actions → **Deploy** → run with the tag you want

### A note on the database

MySQL runs as the `db` compose service with data in a named volume. That's fine
for one host. It is *not* a backup strategy — see §7.

---

## 7. Backups & monitoring

### Backups

`deploy/backup.sh` dumps the `db` service (single transaction, routines and
triggers), gzips it into `./backups`, prunes anything older than
`RETENTION_DAYS` (default 30) and — if the aws CLI is present and `S3_BUCKET`
is set — copies it off-site.

```bash
# on the host, via cron:
0 3 * * *  cd /opt/improv && ./deploy/backup.sh >> /var/log/improv-backup.log 2>&1
```

**The part people skip:** a scheduled **restore test** — and it is not
skipped here. `.github/workflows/backup.yml` runs nightly, restores the dump
into a scratch MySQL container and asserts the data survived. An untested
backup is a rumour.

Restore by hand:

```bash
gunzip < backups/improv-YYYYMMDD-HHMMSS.sql.gz \
  | docker compose exec -T db mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"
```

Also back up: the production `.env`, and any signing keystores (see
[`MOBILE_DESKTOP.md`](./MOBILE_DESKTOP.md) — losing a keystore means you can
never update that Play Store listing again).

### Monitoring

`docker-compose.monitoring.yml` adds two services to the stack:

| Service | URL | What |
|---|---|---|
| Grafana | <http://localhost:3000> | Dashboards over Prometheus |
| Uptime Kuma | <http://localhost:3001> | Uptime monitoring + alerts |

```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

Grafana ships with the Prometheus datasource pre-provisioned
(`deploy/monitoring/grafana/provisioning/`) pointing at the API's
`/actuator/prometheus` on the internal network. The API also emits custom
counters — `improv.users.registered`, `improv.quests.completed`,
`improv.xp.awarded` — which are the product metrics to chart first
(quests/day, XP/day, registrations). Building those dashboards in the Grafana
UI is the remaining task.

`/actuator/prometheus` is **internal only**: nginx never routes `/actuator`,
and the api container publishes no ports in the base compose. Do not expose it.

---

## 8. Conventions (enforced, not suggested)

| Convention | Why | Enforced by |
|---|---|---|
| **Flyway owns the schema** | Hibernate guessing is untenable with 2 devs | `ddl-auto=validate` |
| **Never edit a migration that has shipped** | Someone's already applied it | Review |
| **Secrets in env vars only** | `sphy2323` is why this rule exists | `.gitignore` + review |
| **No hardcoded API origins in JS** | Breaks every deploy but localhost | CI `frontend` job |
| **Log to stdout** | Containers shouldn't own log rotation | `application-prod.yml` |
| **`/actuator/health` is the only health signal** | One contract for compose, LB and the deploy gate | compose + CI + deploy |
| **Graceful shutdown** | In-flight requests finish during a rolling restart | `server.shutdown: graceful` |
| **Containers never run as root** | Standard hardening | Dockerfiles (`USER improv`) |

---

## 9. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `DB_PASSWORD is required` on `compose up` | No `.env` | `cp .env.example .env` and fill it in |
| `api` container restarts in a loop | DB not ready, or Flyway failed | `docker compose logs api` |
| `FlywayException: Found non-empty schema` | Pre-existing dev DB without a history table | Already handled by `baseline-on-migrate: true`; if it persists, check `flyway_schema_history` |
| `Schema-validation: missing table 'users'` | Migration didn't run | Confirm `spring.flyway.enabled` and that the file is under `db/migration/` |
| `502 Bad Gateway` from nginx | API not up / not healthy | `docker compose ps` — the `web` service waits on `api` health |
| Login button does nothing | Backend unreachable (a fixed bug — it used to fail silently) | Check the browser console; a fetch rejection now surfaces an alert |
| CI `frontend` job fails on "hardcoded backend origin" | A `.js` file has `http://localhost:…` | Use `window.IMPROV.apiBase` |
| `mvn verify` fails: "release version 26 not supported" | JDK mismatch locally | CI pins JDK 26; match it, or lower `<java.version>` in `pom.xml` |

---

## 10. Next steps

**Remaining**

- [ ] Rotate the leaked MySQL password on any real database (it is out of the
      tree but still in git history — treat `sphy2323` as burned)
- [ ] Generate a strong `JWT_SECRET` for the production `.env` (compose now
      refuses to start without one — there is no default anywhere)
- [ ] Branch protection on `main` requiring CI to pass (a GitHub repo setting)
- [ ] Sentry (backend + frontend)
- [ ] Grafana dashboards — the datasource is provisioned; chart
      `improv.quests.completed`, `improv.xp.awarded`, `improv.users.registered`
- [ ] Alerting on backup failure and disk >80% (needs a host agent)
- [ ] Core Web Vitals budget in CI (needs a browser runner)
- [ ] Ephemeral preview environment per PR *(the highest-visibility item on this list)*
