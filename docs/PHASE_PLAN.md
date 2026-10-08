# IMPROV — Phase Plan

> **Status:** P0 DevOps lane complete · P0 fullstack lane in progress · **Team:** 2 (fullstack dev + DevOps) · **Updated:** Oct 2026
> **Companion doc:** [`REFERENCE_ANALYSIS.md`](./REFERENCE_ANALYSIS.md) — the research this plan is built on.

---

## 1. Product thesis

> **The gamified part of a fitness tracker isn't the XP bar. It's the reward function.**

If XP is a function of *taps*, you've built a to-do list in a costume — it dies at the novelty cliff around week 5. If XP is a function of *load, progression, balance and consistency*, the game and the training become the same system, and the thing that keeps users coming back is the thing that makes them stronger.

**IMPROV gamifies the training, not the checkbox.**

Most of the category does the opposite. Fitness-RPG apps (Habitica, Fitness RPG, the Solo Leveling clone swarm) have motivation but no training substance. Serious trackers (Hevy, Strong, Boostcamp) have substance but no motivation design. **IMPROV lives in the gap between those two halves.**

---

## 2. What we're building

**One-liner:** *The System, for the gym.* A web-first, no-install workout RPG that levels you up — built for people who track weight and reps, not just rep counters, and who want to compete with friends.

**Three differentiators, in priority order:**

1. **Web / PWA-first** — zero install, any device, installable, offline logging. The entire category is store-bound.
2. **Real strength tracking** — sessions, sets, weight, reps, RPE, PRs, volume per muscle group.
3. **Social** — friends, leaderboards, cooperative guild bosses.

**Stack:** Spring Boot (Java) + REST API + relational DB, served behind nginx as a single origin. Frontend framework decision is pending — see §5, D10.

---

## 3. What we are NOT building (scope guard)

Full feature parity with the reference platform is a ~6-month project for two people. These are explicitly out of scope until the MVP ships:

- ❌ Store submission (Play / App Store). Capacitor + Tauri packaging is configured and CI-built, but shipping to the stores is post-MVP
- ❌ GPS run tracking with audio cues
- ❌ Progress photo journal
- ❌ AI/LLM-generated coaching programs
- ❌ Hardware integrations (Apple Watch, Garmin, heart-rate straps)
- ❌ Full offline sync with conflict resolution — MVP is online-only, fail loudly

**Rule:** if a proposal isn't in the current phase's task table, it goes to §9 Backlog. It does not get built.

---

## 4. The reference & the gap

Full teardown in [`REFERENCE_ANALYSIS.md`](./REFERENCE_ANALYSIS.md). The condensed version:

**What the reference platform gets right (copy the pattern, not the art):** the "System" visual language, the daily reset countdown, the streak calendar + timeline, the rank-hexagon screens, offline logging. The retention skeleton is sound.

**Where it fails:**

| Flaw | Consequence |
|---|---|
| **Tracks reps, never load** — `TOTAL REPS 26.5K` is the headline stat | Progression is unmeasurable. A stronger you and a tireder you look identical. Lifters bounce off |
| **XP multiplier is economically backwards** — `x1.0 → x5.0` on a linear curve | Late levels get *faster*. The endgame accelerates into meaninglessness |
| **No endgame** — ranks are one-way, S-Rank at Lvl 200 | Two years of grinding to a state where nothing new unlocks |
| **Levels and ranks are undifferentiated** | Two progress bars for one resource; no strategic choice |
| **Streaks punish rest** | In a *fitness* app. Teaches overtraining. Actively harmful |
| **Stats are cosmetic** — `STR +10` modifies nothing | RPG stat theatre |
| **Zero social layer** | Removes the strongest retention multiplier |
| **Ad-walled free tier** | Interstitials land exactly at the mid-set logging moment |
| **No iOS build** | Half the market locked out |
| **Static quest templates** | Same 4 quests every day. Template fatigue |

**⚠️ IP constraint.** "Solo Leveling", "Sung Jin Woo", "Shadow Monarch", "Arise" are licensed IP. Ranks E–S, XP, quests and the "System" aesthetic are generic RPG vocabulary and safe. Character names, lifted UI art and third-party branding are not. **We use an original theme** — no lifted assets, no licensed names.

**The category's shared flaw — and our opening:** almost everyone gamifies *the checkbox*. The game reacts to whether you showed up, never to what you did. That's the gap.

---

## 5. Locked decisions

Settle these before code. Each is cheap now and expensive in month two.

| # | Decision | Choice | Why |
|---|---|---|---|
| D1 | Auth | JWT: short-lived access token + refresh token in httpOnly cookie; BCrypt hashing | Server-side sessions force sticky sessions and break horizontal scaling |
| D2 | "Today" definition | Store `users.timezone`; server computes `quest_date` in the **user's** local time | Streak bugs are always timezone bugs |
| D3 | Quest generation | **Lazy, generate-on-read.** No cron | A midnight job breaks with 2 containers or 2 timezones |
| D4 | XP authority | Server-authoritative. Client sends *events*, never XP amounts | Client-side XP is trivially cheatable |
| D5 | State model | Append-only **XP ledger**; level/rank/streak **derived** and cached, fully recomputable | Lets us retune the curve post-launch without corrupting history |
| D6 | Curve & rewards | Config **tables**, not code constants | We will rebalance. Ship the ability to do it without a deploy |
| D7 | Reward snapshots | Copy `target`, `xp_reward`, `stat_reward` onto the user's quest row at generation | Templates change; history must not rewrite itself |
| D8 | Schema | **Flyway** from commit #1. `ddl-auto=validate` | Two devs, one DB. Hibernate must not guess |
| D9 | API shape | REST under `/api`, RFC 7807 errors, DTOs, 401 not 500 | Current code returns 500 for a bad password and leaks the password field |
| D10 | Frontend | **Decision pending** — vanilla multi-page or React + Vite | At 10 screens with derived state, vanilla gets painful by screen 5. Decide in P0 |
| D11 | Offline | MVP online-only, fail loudly. Offline queue deferred | Full sync is a project by itself |
| D12 | Secrets | Env vars only, Spring profiles. **Rotate the committed MySQL password** | It's in git history right now |

**Cross-lane interfaces (DevOps enforces):** logs to stdout · `/actuator/health` · stateless JWT · injected `Clock` · Flyway migrations · env-var secrets.

---

## 6. Mechanics spec

### 6.1 XP & levels

Config-driven curve. Starting point:

```
xpToNext(L) = 100 + 25 × (L − 1)     →  L1→2 = 100,  L5→6 = 200,  L9→10 = 300
```

| Milestone | Cumulative XP | ~Days at 275 XP/day |
|---|---|---|
| Level 10 (D-Rank) | 1,800 | ~7 |
| Level 25 (C-Rank) | 9,300 | ~34 |
| Level 50 (B-Rank) | 34,300 | ~4 months |
| Level 100 (A-Rank) | 131,175 | ~15 months |
| Level 200 (S-Rank) | 512,425 | ~5 years ← **tune down** |

> **Action:** flatten the tail (e.g. cap S at level 150) via the config table. Target daily XP budget of **200–300** for an engaged user.

### 6.2 Ranks

| Rank | Levels | Colour | XP multiplier |
|---|---|---|---|
| E | 1–9 | grey | ×1.0 |
| D | 10–24 | green | ×1.1 |
| C | 25–49 | blue | ×1.2 |
| B | 50–99 | purple | ×1.35 |
| A | 100–199 | gold | ×1.5 |
| S | 200+ | red | ×1.75 |

> **Do not copy the reference's ×5.0.** On a linear curve it makes late levels *faster* — backwards. Keep the ladder visually prominent; keep the multiplier modest and asymptotic, or apply it to session XP only.

### 6.3 Stats

Three, not more:

- **STR** — weighted volume (sets × reps × load)
- **AGI** — cardio / runs / timed work
- **VIT** — consistency (full quest boards, streaks)

Each quest template carries a `stat_code` + `stat_amount`, snapshotted onto the user's quest row.

### 6.4 Streaks

- **Active day = ≥1 daily quest completed.** Forgiving — a bad day shouldn't nuke a 40-day streak.
- **Rest-day tokens** (P3): earn 1/week, auto-spend to protect a streak on a rest day. *Rest is training.*
- **Best streak never decreases.**
- **Timezone fixed at signup, editable in settings.** Changing it does **not** rewrite history.
- Streak is always **derived** from `day_summaries`, never mutated directly.

### 6.5 Quests

- Generated from `quest_templates` filtered by level/rank
- Types: `REPS` | `DURATION` | `SETS` | `WORKOUT` | `CUSTOM`
- Logging: incremental taps (5 / 10 / 20), plus an exact-entry path
- `UNIQUE(user_id, quest_date, template_id)`
- **Anti-abuse:** plausibility bounds (reps ≤ 500, weight ≤ 500 kg), rate limit on log endpoints, daily XP soft cap (~400 → 50% thereafter)

### 6.6 Derived views

Everything the UI shows is a `GROUP BY`, never a maintained counter:

| View | Source |
|---|---|
| Calendar / heatmap | `day_summaries` |
| Weekly activity chart | `day_summaries.xp_earned` by date |
| Rep analytics | quest logs / `set_entries` by range |
| Top task this month | `GROUP BY template ORDER BY reps DESC` |
| Level & rank | `SUM(xp_delta)` over the ledger |

---

## 7. Phase overview

| Phase | Name | Duration | Outcome | Owner split | Status |
|---|---|---|---|---|---|
| **P0** | Foundations & contracts | ~1 week | Repo can build, test and run end-to-end | Both | 🟡 DevOps ✅ · fullstack 🟡 |
| **P1** | Core loop | ~3 weeks | XP, quests, streaks, levels, ranks working via API | Fullstack-heavy | ⬜ not started |
| **P2** | Screens → **MVP SHIP** | ~3 weeks | **Live HTTPS URL with a working core loop** | Both | ⬜ not started |
| **P3** | Real training | ~2 weeks | Weight/reps/RPE tracking — the differentiator | Fullstack-heavy | ⬜ not started |
| **P4** | Social & seasons | ~2 weeks | Friends, leaderboards, guild bosses, seasons | Both | ⬜ not started |

Durations assume **~10 hrs/week each**. **P2 is the MVP. Everything after is upside.**

---

## 8. Phases in detail

### P0 — Foundations & contracts · ~1 week

**Objective:** the repo builds, tests and runs end-to-end with one command, with no secrets in git.

**Fullstack dev**

- [ ] Rotate the committed MySQL password on any real database (it is out of
      the tree but still in git history — treat it as burned)
- [ ] BCrypt password hashing; JWT access + refresh
- [x] DTOs so `password` never serialises in any response — done early via
      `@JsonProperty(WRITE_ONLY)` on `User.password`; full DTOs land with P1
- [x] Fix auth error contract: 401 for bad credentials, not 500 from
      `RuntimeException` (plus 409 for a duplicate username)
- [ ] `@Valid` + validation annotations; unique constraints on username & email
      (username is unique in V1; email is deliberately not yet — see the migration)
- [x] Fix functional bugs: `workout-homepage.html.html` double extension; `gender`
      null-crash; `confirm-password` never validated; height/weight `Long` vs
      decimal mismatch; unhandled fetch rejections
- [x] Fix `ApApplicationTests` — it previously could not pass without a live
      MySQL (now runs on in-memory H2)
- [ ] **Decide D10 (frontend framework)** and commit to it

**DevOps**

- [x] `application-dev.yml` / `application-prod.yml`; every value via env var (Spring relaxed binding)
- [x] `.env` + `.env.example`, root `.gitignore`
- [x] Remove `localhost:8080` from all three frontend JS files
- [x] **nginx**: serve static + proxy `/api` → backend on one origin → CORS deleted
- [x] Backend **Dockerfile** — multi-stage Maven → JRE, layered jar, non-root, `HEALTHCHECK`
- [x] Frontend **Dockerfile** — build stage → nginx runtime
- [x] **`docker-compose.yml`** — mysql + api + web, healthchecks + `depends_on: service_healthy`
- [x] **Flyway** baseline migration, `ddl-auto=validate`
- [ ] `Clock` bean + Testcontainers integration test proving day-boundary logic across ≥3 timezones
- [x] GitHub Actions: PR → build + test (CI green is proven by the compose smoke
      test). Branch protection on `main` is a repo setting — enable it in
      GitHub once the first PR lands

**Exit criteria**

1. 🟡 `docker compose up` brings the existing app up end-to-end — built and
   statically verified; the first real boot happens in CI (the `integration` job)
2. 🟡 CI green on a PR — workflows are in place; the first green run needs a push
3. ✅ No secrets in the working tree
4. ⬜ Timezone test passes — waiting on the `Clock` bean + Testcontainers task

---

### P1 — Core loop · ~3 weeks

**Objective:** the entire progression engine works and is testable via API.

**Fullstack dev**

- [ ] Migrations: `user_stats`, `ranks`, `level_curve`, `quest_templates`, `user_quests`, `xp_ledger`, `day_summaries`
- [ ] Seed config rows: level curve, rank ladder, starter quest templates
- [ ] `XP ledger` — append-only writes, idempotency keys
- [ ] Level + rank derivation from the ledger; `user_stats` cache
- [ ] Streak derivation (current / best) from `day_summaries`
- [ ] Quest generation on first read of a new `quest_date`
- [ ] `GET /api/quests/today`, `POST /api/quests/{id}/log`, `/complete`
- [ ] Plausibility bounds + daily XP soft cap
- [ ] `GET /api/progression`, `GET /api/ranks`
- [ ] `day_summaries` upsert on quest completion

**DevOps**

- [x] CD: on tag `v*` → build + push images to GHCR with layer cache (`release.yml`)
- [x] Deploy to VPS over SSH (`compose pull && up -d`), rolling restart
      (`deploy.yml`, gated on the healthcheck; needs the DEPLOY_* secrets)
- [ ] Domain + **TLS** (Caddy automatic certs, or nginx + certbot) — documented
      in `docs/DEVOPS.md` §6, not automated yet
- [x] Actuator `/actuator/health` wired into compose healthcheck **and** the deploy gate
- [ ] Nightly `mysqldump` → object storage + **a scheduled restore test**
- [ ] Sentry (frontend + backend)
- [x] Static asset caching: `Cache-Control` on HTML/`sw.js`/assets in nginx
      (short TTL until filenames are content-hashed — P2/P3)

**Exit criteria**

1. Logged quests move XP, level, rank and streak correctly — verified by an end-to-end test
2. TZ boundary tests pass for three timezones
3. Deployed live over HTTPS with a real domain
4. Backup restores successfully into a scratch DB

---

### P2 — Screens → **MVP SHIP** · ~3 weeks

**Objective:** a stranger can sign up, log a day, and see their progress. **This is the demo.**

**Fullstack dev**

- [ ] **S1 Auth** — rework login/register to the new API
- [ ] **S2 Home / System** — hunter card, level, rank badge, streak, reset countdown, daily quests
- [ ] **S3 Quest logging sheet** — incremental tap-to-log (5 / 10 / 20) + exact entry
- [ ] **S4 Analytics** — Overview / Progress / Stats tabs, weekly activity chart, consistency squares, rep analytics
- [ ] **S5 Streak Details** — Calendar + Timeline views
- [ ] **S6 Rank screen** — hexagon ladder, unlock levels, progress arc
- [ ] Design system pass: the "System" look as **shared CSS tokens**, not copy-pasted blocks

**DevOps**

- [ ] Grafana dashboards — instrument **product metrics**: DAU, quests/day, XP/day, streak histogram, level-up funnel
- [ ] Uptime Kuma (off-box) → alerts to Discord/Telegram
- [x] Rate limiting at nginx (`limit_req`) on `/api/quests/*/log` — done early
- [ ] Core Web Vitals budget in CI
- [ ] Alerting on backup failure and disk >80%

**Exit criteria**

1. A new user can register, log a full day of quests, and see XP + streak + rank update
2. Analytics reflect real logged data
3. Deployed, monitored, backed up, alerting
4. **🎯 MVP COMPLETE — shareable URL**

---

### P3 — Real training · ~2 weeks

**Objective:** the differentiator. XP now comes from load, not taps.

**Fullstack dev**

- [ ] Migrations: `exercises` (seed the existing 45), `workout_sessions`, `set_entries`, `personal_records`
- [ ] Move the hardcoded exercise DB into the DB + `GET /api/exercises`
- [ ] **S7 Workout logger** — sessions, sets, weight, reps, RPE; rest timer
- [ ] **XP computed from load** — `f(sets × reps × weight × RPE)`, server-side
- [ ] PR detection + `GET /api/stats/records`
- [ ] Volume analytics per muscle group → **"System diagnostic"** balance panel
- [ ] Estimated 1RM / strength curve per lift
- [ ] Rest-day tokens + deload-week quests

**DevOps**

- [ ] k6/vegeta load test on `/api/quests/today` (hottest path)
- [ ] Query/index review with `EXPLAIN` on the analytics endpoints
- [ ] Redis for hot reads **only if MySQL actually hurts** — measure first

**Exit criteria**

1. A weighted session produces XP proportional to real volume
2. A PR is detected and surfaced automatically
3. Muscle imbalance is visible as a diagnostic
4. Hot-path p95 latency within budget under load

---

### P4 — Social & seasons · ~2 weeks

**Objective:** the gap the reference platform doesn't fill.

**Fullstack dev**

- [ ] Migrations: `friendships`, `guilds`, `guild_members`, `seasons`
- [ ] `GET /api/leaderboards?scope=global|friends&period=week`
- [ ] Friend requests / accept
- [ ] **Cooperative guild boss** — boss HP = guild's collective volume for the week
- [ ] **Seasons with rank soft-reset** — fixes the reference's endgame hole
- [ ] Shareable PR card (organic growth loop)

**DevOps**

- [ ] **Ephemeral preview environment per PR** — the highest-visibility DevOps flex on a project this size
- [ ] Redis for leaderboards (if needed)
- [ ] Blue/green or canary deploy

**Exit criteria**

1. Two users can befriend each other and appear on a shared weekly leaderboard
2. A guild boss takes damage from member volume
3. A season can be closed and ranks soft-reset without data loss
4. A PR gets a preview URL

---

## 9. Backlog (post-MVP)

**Exclusive feature candidates, cheapest-first:**

- Streak-freeze animation, level-up full-screen moment, rank-promotion ceremony
- Sound design
- **"No-gamification" mode** — for lifters who just want to log
- **The System as an actual voice** — LLM-generated daily framing and taunts reacting to your last session
- Anti-cheat as a *stated* feature — plausibility bounds, rate limits, optional witness/video flag on PRs
- Achievements gallery
- Body metrics + weight chart; progress photos
- Custom routine builder
- Offline queue + service-worker replay (D11)
- Native app (only if PWA proves insufficient)
- ML anti-cheat (only if leaderboards get big enough to need it)

---

## 10. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| **Licensed IP in the theme** | Takedown; can't publish | Original theme; generic RPG vocabulary only; no lifted art |
| **Scope = 6 months of parity** | Never ships | Hard MVP cut at P2; parity explicitly not a goal |
| **Timezone / streak bugs** | Silent data corruption | Injected `Clock` + Testcontainers TZ tests **before** the streak code (§8 P0) |
| **XP curve mistuned** | Dead product or broken economy | Config tables + ledger-derived state → recompute freely |
| **Novelty cliff (~week 4)** | Users churn at week 5–6 | Gamify the *training*, not the checkbox; macro loop (levels/ranks/seasons) must carry past week 6 |
| **Cheating via the API** | Leaderboards meaningless | Server-authoritative XP, plausibility bounds, rate limits |
| **Java 26 / Spring Boot 4.1.0** | CI or runtime pulls fail | ✅ Closed — `maven:3.9-eclipse-temurin-26` and `eclipse-temurin:26-jre-noble` verified to exist on Docker Hub |
| **Committed credential** | Account/database compromise | Removed from the tree; still in git history — rotate the real password, treat `sphy2323` as burned |
| **Two devs, one schema, no migrations** | Merge hell, lost data | Flyway from commit #1 (D8) |
| **Framework indecision** | Mid-project rewrite | Decide D10 in P0, not P2 |

---

## 11. Open questions

1. **What is this for?** Portfolio piece, college project, or something strangers should use? Changes almost everything downstream.
2. **Hosting:** cheap VPS (Hetzner/DO, ~$5–6/mo — most educational, full control) vs. PaaS (Railway/Render/Fly — fastest to a URL, hides the interesting parts) vs. free tier?
3. **Theme:** original "System" theme, or visibly Solo Leveling–adjacent accepting IP risk?
4. **Web only, or mobile later?** If mobile is on the roadmap, JWT + own API is correct (D1) and Firebase/Supabase would be a strategic mistake now.
5. **Hours/week** for each of you — durations here assume ~10.
6. **Frontend framework** (D10) — vanilla vs React/Vite.
7. **The second repo** — same stack? If so, share the CI workflow and compose pattern instead of building it twice.

---

## 12. Immediate next step

**P0 DevOps is done.** The next task is the P0 fullstack lane: BCrypt + JWT
auth, DTOs with validation, and the D10 frontend-framework decision.
Everything from P1 onward sits on top of that.
