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
| Schema management | ✅ Flyway baseline, `ddl-auto=validate` |
| CI | ✅ Build + test, frontend static checks, compose smoke test |
| CD | ✅ Image publishing to GHCR on tag; SSH deploy workflow (needs secrets) |
| Desktop packaging | ✅ Tauri config + cross-platform release workflow |
| Mobile packaging | ✅ Capacitor config + Android/iOS workflows |
| Observability | ⏳ Scheduled — P2 (Grafana, uptime, alerting) |
| Backups | ⏳ Scheduled — P1 |

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

## 7. Backups

**Not implemented yet. Scheduled for P1.** When you build it:

```bash
# Nightly dump to object storage
docker compose exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  | gzip | aws s3 cp - "s3://improv-backups/db/$(date -u +%F).sql.gz"

# Retention
aws s3 ls s3://improv-backups/db/ | ... # delete > 30 days
```

**The part people skip:** a scheduled **restore test**. Pull yesterday's dump
into a scratch container and assert `SELECT COUNT(*) FROM users` returns a
sane number. An untested backup is a rumour.

Also back up: the production `.env`, and any signing keystores (see
[`MOBILE_DESKTOP.md`](./MOBILE_DESKTOP.md) — losing a keystore means you can
never update that Play Store listing again).

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

**P0 / P1 remaining**

- [ ] Rotate the leaked MySQL password on any real database (it is out of the
      tree but still in git history — treat `sphy2323` as burned)
- [ ] BCrypt + JWT auth, DTOs, `@Valid` (fullstack lane — see
      [`PHASE_PLAN.md`](./PHASE_PLAN.md) P0)
- [ ] Add Testcontainers + an injected `Clock` bean; test quest-day boundaries across ≥3 timezones
- [ ] Branch protection on `main` requiring CI to pass (a GitHub repo setting)
- [ ] Backups + restore test
- [ ] Sentry (backend + frontend)

**P2+**

- [ ] Grafana dashboards — instrument DAU, quests/day, XP/day, streak histogram, level-up funnel
- [ ] Uptime Kuma, alerts to Discord/Telegram
- [ ] Core Web Vitals budget in CI
- [ ] Ephemeral preview environment per PR *(the highest-visibility item on this list)*
