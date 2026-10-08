# IMPROV

**A gamified workout tracker.** Track your training, earn XP, level up, and rank up — built for people who log weight and reps, not just rep counters.

**Runs as a web app, an installable PWA, an Android app, an iOS app, and a desktop app for Windows / macOS / Linux — from one codebase.**

> **Status: 🚧 pre-alpha.** The backend is a demo slice — authentication only. The progression engine (XP, quests, streaks, ranks) is specified and scheduled, not yet built. Infrastructure, packaging and CI are in place. See [`docs/PHASE_PLAN.md`](docs/PHASE_PLAN.md).

---

## The idea

Most gamified fitness apps game the *checkbox* — they reward you for showing up, not for what you actually did. So the game becomes decoration on a to-do list, and it stops being interesting within a month.

Most serious trackers have the opposite problem: excellent training data, no reason to open the app tomorrow.

**IMPROV is the middle.** The game layer runs on your actual training — load, progression, muscle balance, consistency — so the thing that keeps you coming back is the thing that makes you stronger.

> **The gamified part of a fitness tracker isn't the XP bar. It's the reward function.**

Full reasoning in [`docs/REFERENCE_ANALYSIS.md`](docs/REFERENCE_ANALYSIS.md).

---

## Planned features

**Core loop**
- Daily quests with incremental tap-to-log (5 / 10 / 20)
- XP ledger → levels → rank ladder (E → S) with unlock milestones
- Streaks with a calendar and timeline view, plus rest-day tokens
- Daily reset countdown, weekly challenges, seasons

**Real training** *(the differentiator)*
- Workout sessions: exercises, sets, weight, reps, RPE
- **XP computed from load** — sets × reps × weight × RPE, server-authoritative
- Auto-detected personal records, surfaced as loot
- Volume analytics per muscle group, framed as a "System diagnostic"
- Estimated 1RM and strength curves

**Social** *(the gap in the category)*
- Friends and weekly leaderboards
- Cooperative guild bosses — boss HP = the guild's collective volume

**Platform**
- Web / PWA-first: no install, any device, installable, offline app shell
- Android, iOS and desktop builds from the same code

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 26, Spring Boot 4, Spring Data JPA, Flyway |
| Database | MySQL 8.4 |
| Frontend | HTML / CSS / JavaScript *(framework decision pending)* |
| Packaging | Capacitor (Android/iOS), Tauri (desktop), service worker (PWA) |
| Infrastructure | Docker, Docker Compose, nginx (reverse proxy), GitHub Actions |
| Observability | Actuator; Prometheus + Grafana and Sentry planned |

---

## Quick start

```bash
cp .env.example .env          # then edit the passwords
docker compose up --build
# -> http://localhost:8080
```

Frontend and API are served from **one origin** by nginx, so there is no CORS
anywhere in this project and no JS file hardcodes a backend URL.

For a development stack (MySQL exposed on 3306, API on 8081, SQL logging):

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Running the API without containers:

```bash
cd Backend/ap
export DB_URL='jdbc:mysql://localhost:3306/workoutapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=improv DB_PASSWORD=...
./mvnw spring-boot:run
```

Tests — no database required, they run against in-memory H2:

```bash
cd Backend/ap && ./mvnw verify
```

Full instructions, configuration reference and troubleshooting:
[`docs/DEVOPS.md`](docs/DEVOPS.md).

---

## Repository layout

```
IMPROV/
├── Backend/ap/                        # Spring Boot application (Maven)
│   ├── Dockerfile                     # multi-stage, non-root, healthcheck
│   └── src/main/
│       ├── java/wo/ap/                # ApApplication, Controller, User, ...
│       └── resources/
│           ├── application.yml        # profiles: dev / prod / test
│           └── db/migration/          # Flyway migrations
├── Frontend/
│   ├── Dockerfile                     # nginx runtime
│   ├── nginx.conf                     # static + /api proxy + rate limiting
│   ├── capacitor.config.json          # Android / iOS
│   ├── package.json                   # Capacitor scripts
│   ├── src-tauri/                     # Tauri desktop shell
│   └── Login Page/                    # the web app itself
│       ├── LoginPage.*                # login
│       ├── RegistrationPage.*         # registration
│       ├── workout-homepage.*         # home / quest board
│       ├── exercises-detail.*         # exercise browser
│       ├── config.js                  # API base resolution
│       ├── pwa.js  sw.js  manifest.webmanifest  offline.html
│       └── icons/                     # generated PWA / app icons
├── deploy/.well-known/                # App Links / Universal Links templates
├── docs/
│   ├── PHASE_PLAN.md                  # roadmap: phases, tasks, exit criteria
│   ├── REFERENCE_ANALYSIS.md          # research, category analysis, flaw audit
│   ├── DEVOPS.md                      # build, run, deploy, troubleshoot
│   ├── MOBILE_DESKTOP.md              # Android / iOS / desktop packaging
│   └── CREDITS.md                     # attribution and acknowledgements
├── docker-compose.yml
├── docker-compose.dev.yml
└── .github/workflows/                 # ci, release, deploy, android, ios, desktop
```

> **Note:** the frontend still lives in a folder called `Login Page` and
> contains far more than login. Restructuring is a P0 task.

---

## Packaging

| Target | Tool | Command |
|---|---|---|
| Web | nginx | `docker compose up` |
| **PWA** | manifest + service worker | Already wired — deploy and install from the browser |
| Android | Capacitor | `cd Frontend && npm install && npx cap add android && npx cap sync` |
| iOS | Capacitor | Same, then open in Xcode *(needs a Mac)* |
| Desktop | Tauri | `cd Frontend/src-tauri && cargo tauri build` |

Before building any native package, set `PRODUCTION_API_ORIGIN` in
`Frontend/Login Page/config.js` to your deployed origin. Details and
prerequisites: [`docs/MOBILE_DESKTOP.md`](docs/MOBILE_DESKTOP.md).

---

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PR / push to `main` | Backend build + test, frontend static checks, full compose smoke test |
| `release.yml` | tag `v*` | Builds and pushes images to GHCR with cache, SBOM, provenance |
| `deploy.yml` | manual | SSH deploy, gated on the API healthcheck |
| `android.yml` | tag / manual | Debug APK + release AAB artifacts |
| `ios.yml` | manual | Unsigned simulator build *(needs a macOS runner)* |
| `desktop.yml` | tag / manual | Tauri installers for Windows, macOS, Linux |

The `ci` job that matters most is the compose smoke test: it boots the whole
stack, waits for the healthcheck, asserts Flyway applied the baseline, and
asserts nginx serves the shell and proxies `/api`. That's the difference
between "it compiles" and "it runs".

---

## Roadmap

| Phase | Focus | Outcome |
|---|---|---|
| **P0** | Foundations & contracts | Repo builds, tests and runs end-to-end; no secrets in git |
| **P1** | Core loop | XP, quests, streaks, levels and ranks working via API |
| **P2** | Screens | **MVP — live HTTPS URL with a working core loop** |
| **P3** | Real training | Weight/reps/RPE tracking and load-based XP |
| **P4** | Social & seasons | Friends, leaderboards, guild bosses, rank resets |

Full task breakdown, exit criteria and the risk register:
[`docs/PHASE_PLAN.md`](docs/PHASE_PLAN.md).

---

## Design principles

1. **Gamify the training, not the checkbox.** The reward function reads training data.
2. **Server-authoritative.** The client sends events; the server decides XP.
3. **Derived, not mutated.** Levels, ranks and streaks are computed from an append-only ledger — retune and recompute freely.
4. **One origin.** nginx proxies `/api`, so there is no CORS and no environment-specific URL.
5. **Rerunnable.** If it can't be reproduced with one command, it isn't done.
6. **No secrets in git. Ever.**

---

## ⚠️ Security notice

The current backend **is not safe to deploy as-is**:

- Passwords are stored in **plaintext** and returned in API responses
- A database password was committed to git history and must be considered burned
- Authentication failures return HTTP 500 instead of 401

All three are P0 items. See
[`docs/REFERENCE_ANALYSIS.md` §4](docs/REFERENCE_ANALYSIS.md) for the full audit.

---

## Team

| Role | Responsibilities |
|---|---|
| **Fullstack** | API, data model, progression engine, frontend screens |
| **DevOps** | Containers, CI/CD, TLS, observability, backups, migrations, releases, packaging |

---

## Credits & IP

IMPROV is an original project. Frameworks, tools and influences are credited in
[`docs/CREDITS.md`](docs/CREDITS.md) — including the products we studied, the
research behind our design decisions, and every open-source dependency.

**No third-party art, icons, screenshots, character names or branding from any
other application are included in this repository.** Ranks (E–S), XP, levels,
quests and streaks are generic role-playing-game vocabulary. Licensed
properties are not used. If you contribute, do not add licensed assets or names
to the repo.

---

## Licence

Not yet chosen. Until one is added, all rights are reserved.
