# IMPROV

**A gamified workout tracker.** Track your training, earn XP, level up, and rank up — built for people who log weight and reps, not just rep counters.

> **Status: 🚧 pre-alpha.** The current code is a demo slice — authentication and a set of static frontend screens. The progression engine (XP, quests, streaks, ranks) is specified and scheduled, not yet built. See [`docs/PHASE_PLAN.md`](docs/PHASE_PLAN.md) for the roadmap.

---

## The idea

Most gamified fitness apps game the *checkbox* — they reward you for showing up, not for what you actually did. So the game becomes decoration on a to-do list, and it stops being interesting within a month.

Most serious trackers have the opposite problem: excellent training data, no reason to open the app tomorrow.

**IMPROV is the middle.** The game layer runs on your actual training — load, progression, muscle balance, consistency — so the thing that keeps you coming back is the thing that makes you stronger.

> **The gamified part of a fitness tracker isn't the XP bar. It's the reward function.**

Read the full reasoning in [`docs/REFERENCE_ANALYSIS.md`](docs/REFERENCE_ANALYSIS.md).

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
- Web / PWA-first: no install, any device, installable, works offline

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot, Spring Data JPA, Flyway |
| Database | MySQL |
| Frontend | HTML / CSS / JavaScript *(framework decision pending)* |
| Infrastructure | Docker, Docker Compose, nginx, GitHub Actions |
| Observability | Actuator, Prometheus + Grafana, Sentry |

---

## Repository layout

```
IMPROV/
├── Backend/
│   └── ap/                     # Spring Boot application (Maven)
│       ├── src/main/java/wo/ap/
│       │   ├── ApApplication.java
│       │   ├── Controller.java      # /api/Users/register, /api/Users/login
│       │   ├── User.java            # JPA entity
│       │   ├── Repository.java
│       │   └── service.java
│       └── src/main/resources/application.properties
├── Frontend/
│   └── Login Page/             # Login, registration, homepage, exercise browser
└── docs/
    ├── PHASE_PLAN.md           # Roadmap: phases, tasks, exit criteria
    └── REFERENCE_ANALYSIS.md   # Research: flaws, category analysis, gamification theory
```

> **Note:** the frontend currently lives in a folder called `Login Page` and contains far more than login. Restructuring is on the P0 task list.

---

## Getting started

### Prerequisites

- JDK 21+ *(the build currently targets Java 26 — see the risk note in the phase plan)*
- Maven *(or use the included `mvnw` wrapper)*
- MySQL 8+
- MySQL database named `workoutapp`

### Backend

```bash
cd Backend/ap
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. The two endpoints that exist today:

| Method | Endpoint | Body |
|---|---|---|
| `POST` | `/api/Users/register` | `firstname, lastname, username, email, gender, height, weight, password` |
| `POST` | `/api/Users/login` | `username, password` |

### Frontend

The frontend is static — open `Frontend/Login Page/LoginPage.html` in a browser. It expects the backend on `localhost:8080`.

> ⚠️ **Do not deploy this as-is.** The current backend stores passwords in plaintext, returns them in API responses, and has a database credential committed to git history. All three are P0 items. See [`docs/REFERENCE_ANALYSIS.md` §4](docs/REFERENCE_ANALYSIS.md) for the full audit.

---

## Roadmap

| Phase | Focus | Outcome |
|---|---|---|
| **P0** | Foundations & contracts | Repo builds, tests and runs end-to-end; no secrets in git |
| **P1** | Core loop | XP, quests, streaks, levels and ranks working via API |
| **P2** | Screens | **MVP — live HTTPS URL with a working core loop** |
| **P3** | Real training | Weight/reps/RPE tracking and load-based XP |
| **P4** | Social & seasons | Friends, leaderboards, guild bosses, rank resets |

Full task breakdown, exit criteria and the risk register: [`docs/PHASE_PLAN.md`](docs/PHASE_PLAN.md).

---

## Design principles

1. **Gamify the training, not the checkbox.** The reward function reads training data.
2. **Server-authoritative.** The client sends events; the server decides XP.
3. **Derived, not mutated.** Levels, ranks and streaks are computed from an append-only ledger — retune and recompute freely.
4. **Rerunnable.** If it can't be reproduced with one command, it isn't done.
5. **No secrets in git. Ever.**

---

## Team

| Role | Responsibilities |
|---|---|
| **Fullstack** | API, data model, progression engine, frontend screens |
| **DevOps** | Containers, CI/CD, TLS, observability, backups, migrations, release process |

---

## Attribution & IP

IMPROV is an original project. It is inspired by the *gamified fitness* category and by RPG progression systems generally.

**No third-party art, icons, screenshots, character names or branding from any other application are used or included in this repository.** Ranks (E–S), XP, levels, quests and streaks are generic role-playing-game vocabulary.

If you contribute, do not add licensed assets or names to the repo. See [`docs/REFERENCE_ANALYSIS.md` §6](docs/REFERENCE_ANALYSIS.md).
