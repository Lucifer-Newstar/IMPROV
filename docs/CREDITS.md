# Credits & Acknowledgements

IMPROV is an original project. It did not happen in a vacuum — this page
records what we studied, what we borrowed *ideas* from, and what we depend on.

> **No third-party code, art, icons, screenshots, character names or branding
> are included in this repository.** Everything visual here was made for this
> project. See [§4](#4-what-we-did-not-take) for the specifics.

---

## 1. Primary product reference

### ARISE SOLO — *Level Up System*

| | |
|---|---|
| Website | <https://arisesolo.com/> |
| Google Play | <https://play.google.com/store/apps/details?id=arisesolo.com> |
| Developer | Ranjeet Soren (*Arise Solo*) |
| Analysis | [`REFERENCE_ANALYSIS.md` §2](./REFERENCE_ANALYSIS.md) |

**Why it's credited:** ARISE SOLO is the closest existing product to what
IMPROV is trying to be, and it is the single biggest influence on this plan. We
studied its mechanics in detail — the daily reset countdown, incremental
tap-to-log, the E→S rank ladder, the streak calendar, the rank-hexagon screens —
and several of those patterns are deliberate design choices on our side too.

**What we took:** *patterns and lessons only.*

- The general shape of the daily quest loop
- The idea of a rank ladder with unlock thresholds as a long-horizon goal
- Presenting the streak as a calendar, not just a number
- A "System console" visual treatment for stats and progression

**What we deliberately did differently:** ARISE SOLO gamifies the *checkbox* —
its core unit is a rep counter. IMPROV gamifies the *training* — XP is derived
from sets, weight, reps and RPE. That distinction is the entire product thesis,
and it exists *because* we analysed this app. Credit where it's due: it showed
us the shape of the problem.

**Independent analysis consulted:** AppBrain's listing for ARISE SOLO
(<https://www.appbrain.com/app/arise-solo-level-up-system/arisesolo.com>) was
used for download figures, ratings and its own "cons" assessment, which
informed our positioning around social features.

---

## 2. Category reviewed

These were reviewed to understand the competitive landscape. Each one informed
the comparison in [`REFERENCE_ANALYSIS.md` §3](./REFERENCE_ANALYSIS.md), and we
are grateful to all of them for demonstrating what works and what doesn't.

### Gamified fitness

| Project | Link | What we learned from it |
|---|---|---|
| **Habitica** | <https://habitica.com/> | The clearest example of gamifying the checkbox rather than the training. The negative case that defined our thesis |
| **Zombies, Run!** | <https://www.zombiesrungame.com/> | Narrative as a motivation engine. Proof that theme can carry an app — and that novelty fades without mechanical depth |
| **Fitness RPG** (Shikudo) | <https://shikudo.com/> | Step-count gamification done at scale; showed us the ceiling of pedometer-driven design |
| **WalkScape** | <https://walkscape.app/> | Deep MMO-style progression from walking. A benchmark for how much game depth is achievable |
| **BITLETICS** | <https://bitletics.com/> | External rewards (raffles, gift cards) as motivation. Informed our reasoning about overjustification |
| **Freeletics** | <https://www.freeletics.com/> | AI programming with light gamification — evidence that superficial game layers don't drive behaviour |
| **Peloton** | <https://www.onepeloton.com/> | Social leaderboards done well, and the hardware-gating trade-off |
| **Nike Run Club** | <https://www.nike.com/nrc-app> | Badges and challenges at consumer scale |
| **Ring Fit Adventure** (Nintendo) | — | A genuinely great game built *on* exercise. Proof the premise works when the game and the activity are one system |

### Serious trackers

| Project | Link | What we learned from it |
|---|---|---|
| **Strong** | <https://www.strong.app/> | Clean, fast set/rep/weight logging. The interaction bar we have to clear |
| **Hevy** | <https://www.hevyapp.com/> | Excellent logging plus a social layer — the closest thing to our target feature set without the game |
| **Boostcamp** | <https://www.boostcamp.app/> | Program-driven training; showed us how much users value structure |
| **Fitbod** | <https://fitbod.me/> | Adaptive programming |
| **Jefit** | <https://www.jefit.com/> | Exercise library depth |

### Platform-scale competition

| Project | Link | What we learned from it |
|---|---|---|
| **Strava** | <https://www.strava.com/> | Segments and leaderboards are the most powerful — and most dangerous — social mechanic in fitness. Its documented problems with fabricated data directly shaped our decision to make XP server-authoritative and to prioritise *cooperative* guild mechanics over competitive leaderboards |

---

## 3. Research sources

The evidence base behind the design argument in
[`REFERENCE_ANALYSIS.md` §5](./REFERENCE_ANALYSIS.md).

### Peer-reviewed

- **Evaluating the Effectiveness of Gamification on Physical Activity:
  Systematic Review and Meta-analysis of Randomized Controlled Trials** —
  *Journal of Medical Internet Research*, 2022.
  <https://pmc.ncbi.nlm.nih.gov/articles/PMC8767479/>
  → Source of the *g = 0.42* (12-week) and *g = 0.15* (follow-up) effect sizes
  that set our retention expectations.

- **Gamification suffers from the novelty effect but benefits from the
  familiarization effect: Findings from a longitudinal study** —
  *International Journal of Educational Technology in Higher Education*, 2022.
  <https://link.springer.com/article/10.1186/s41239-021-00314-6>
  → Source of the "novelty effect begins around week 4" finding, which is why
  the plan treats surviving week 4 as an explicit design constraint.

- **Koivisto, J. & Hamari, J.** — *The rise of motivational information
  systems: a review of gamification research*, 2019; and related work on
  declining perceived enjoyment in gamified services (Fitocracy).

### Journalism & industry

- **The Guardian** — *Kudos, leaderboards, QOMs: how fitness app Strava became
  a religion*, 2020.
  <https://www.theguardian.com/news/2020/jan/14/kudos-leaderboards-qoms-how-fitness-app-strava-became-a-religion>
  → Risk-taking behaviour, cheating, and the antisocial effects of segment
  chasing.

- **TechCrunch** — *Strava taps AI to weed out leaderboard cheats*, 2024.
  <https://techcrunch.com/2024/05/16/strava-taps-ai-to-weed-out-leaderboard-cheats-unveils-family-plan-dark-mode-and-more/>

- **Cybernews** — coverage of Strava's removal of 1.6M vehicle and 2.3M
  e-bike activities from segment leaderboards, 2026.
  <https://cybernews.com/tech/strava-cleanup-segment-leaderboards/>

- **AppBrain** — install, rating and feature-gap data for ARISE SOLO.
  <https://www.appbrain.com/app/arise-solo-level-up-system/arisesolo.com>

---

## 4. What we did NOT take

To be explicit, because this matters:

- ❌ **No code** was copied from any other application. IMPROV's source is
  original.
- ❌ **No art, icons, screenshots, logos or UI assets** from any other
  application. The IMPROV icon (`Frontend/Login Page/icons/`) was generated
  for this project, procedurally, from our own palette.
- ❌ **No licensed character names or IP.** "Solo Leveling", "Sung Jin Woo",
  "Shadow Monarch" and "Arise" are the property of their respective rights
  holders (Chugong / Kakao / Crunchyroll). IMPROV does not use them.
- ✅ **Generic RPG vocabulary only.** Ranks (E–S), XP, levels, quests and
  streaks are genre conventions, not anyone's property. We use those freely,
  as every app in this category does.
- ❌ **No screenshots of other apps are committed to this repository.** Where
  the analysis references specific screens, it describes them in prose.

**If you contribute:** do not add licensed assets, character names, logos or
scraped content. See [`REFERENCE_ANALYSIS.md` §6](./REFERENCE_ANALYSIS.md).

---

## 5. Open source dependencies

Built entirely on other people's excellent work.

### Backend

| Project | Licence | Use |
|---|---|---|
| [Spring Boot](https://spring.io/projects/spring-boot) | Apache-2.0 | Application framework |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | Apache-2.0 | Persistence |
| [Spring Security](https://spring.io/projects/spring-security) | Apache-2.0 | *(planned — auth)* |
| [Flyway](https://flywaydb.org/) | Apache-2.0 | Versioned schema migrations |
| [Hibernate ORM](https://hibernate.org/orm/) | LGPL-2.1 | JPA implementation |
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | Apache-2.0 | Connection pool |
| [Lombok](https://projectlombok.org/) | MIT | Boilerplate reduction |
| [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) | GPL-2.0 w/ FOSS exception | JDBC driver |
| [JUnit 5](https://junit.org/junit5/) | EPL-2.0 | Testing |
| [H2](https://h2database.com/) | MPL-2.0 / EPL-1.0 | In-memory test database |

### Frontend & packaging

| Project | Licence | Use |
|---|---|---|
| [Capacitor](https://capacitorjs.com/) (Ionic) | MIT | Android / iOS packaging |
| [Tauri](https://tauri.app/) | MIT / Apache-2.0 | Desktop packaging |
| [Rust](https://www.rust-lang.org/) | MIT / Apache-2.0 | Tauri runtime |

### Infrastructure

| Project | Licence | Use |
|---|---|---|
| [Docker](https://www.docker.com/) | Apache-2.0 | Containerisation |
| [nginx](https://nginx.org/) | BSD-2-Clause | Static serving + reverse proxy |
| [MySQL](https://www.mysql.com/) | GPL-2.0 | Database |
| [Eclipse Temurin](https://adoptium.net/) | GPL-2.0 w/ Classpath Exception | JDK runtime |
| [Maven](https://maven.apache.org/) | Apache-2.0 | Build tool |
| [GitHub Actions](https://github.com/features/actions) | — | CI/CD |

### Planned (scheduled in the phase plan)

[Grafana](https://grafana.com/), [Prometheus](https://prometheus.io/),
[Caddy](https://caddyserver.com/), [Uptime Kuma](https://github.com/louislam/uptime-kuma),
[Sentry](https://sentry.io/), [k6](https://k6.io/), [Redis](https://redis.io/).

---

## 6. How to request a correction

If you maintain one of the projects above and believe something here
misrepresents your work, or if you want a link removed, open an issue and we
will act on it. The critique in this repository is aimed at *design patterns*,
not at the people who built them — and every project listed here shipped
something real, which is more than most ideas ever do.
