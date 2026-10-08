# IMPROV — Reference Analysis

> The research behind [`PHASE_PLAN.md`](./PHASE_PLAN.md).
> Covers: the reference platform's flaws, the wider category's flaws, our own codebase's flaws, and what "gamified" actually means.

---

## 1. The category-wide flaw

Almost every gamified fitness app makes the same mistake:

> **They gamify the checkbox, not the training.**

The game layer reacts to *whether you showed up*, never to *what you did*. Habitica is the clearest case — independent reviews describe it as "checking a box that says *I worked out*: the app has no idea what your workout was, whether your form was correct, or whether the programming is effective. The RPG is fun, but the fitness is entirely on you."

The reference app has the identical flaw: its atomic unit is a rep counter (`PUSH-UPS 100`) with no weight, no load, no intensity.

**Why it matters:** a reward function that ignores training data is trivially fakeable, teaches the user nothing, and can't distinguish a great session from a bad one. The game becomes decoration bolted onto a to-do list.

**Everything in §6 and §7 is downstream of fixing this.**

---

## 2. Reference teardown

**ARISE SOLO** (`arisesolo.com`, Google Play id `arisesolo.com`) — a Solo Leveling–themed habit + workout RPG. ~50K+ installs, 4.5★ from ~3,300 reviews, actively updated.

### 2.1 The core loop

```
Open app → see today's quests + reset countdown
  → do work → tap to log reps in real time
  → quest completes → XP + stat points awarded
  → level/rank progress moves → streak survives another day
  → history fills the calendar → come back tomorrow before the timer hits zero
```

The retention engine is **three timers**: the daily reset countdown (urgency), the streak (loss aversion), and the level curve (progress). Everything else is decoration on those three.

### 2.2 Mechanics

| Mechanic | Implementation |
|---|---|
| Daily reset | Live countdown on Home (`Reset In 23:09:54`) → local midnight |
| Daily quests | Fixed list (Push-ups 100, Sit-ups 100, Squats 100, Running 10), each with a checkbox, a stat reward (`STR +10`, `AGI +8`) and XP (`+50 XP`) |
| Logging | Tap-to-log with **incremental steps of 5 / 10 / 20**; progress bar fills toward target |
| Level | Simple counter (`LEVEL 9`), independent of rank |
| Rank ladder | `E (Lvl 1–9) → D (10) → C (25) → B (50) → A (100) → S (200–999)`, each with a colour and an **XP multiplier** `x1.0 → x5.0` |
| Streak | `STREAK 87`, `BEST STREAK 87`, flame badge |
| Calendar | Month grid: filled = active, outlined = today, empty = missed. Tap a day → that day's quests with XP + rep totals |
| Analytics | Overview (level/rank/XP/streak/tiles + weekly activity chart + consistency squares), Stats (total reps, weekly, monthly, avg/day, tasks completed, daily-reps chart, "Top Task This Month"), Progress |
| Timeline | Alternate streak history view (list) |
| Rank screen | Three tabs — CARDS / CORE / ORIG. **CORE** = radial system with rank hexagons, unlock levels, progress arc, `SYNC STATUS 88%` console readout. **ORIG** = vertical ladder with detail cards (XP MULTI, AUTH CODE, flavour text) |
| Extras | GPS run tracking w/ audio cues, progress photos, weight chart, custom routines, offline logging, Firebase cloud sync, dark mode |

### 2.3 Design language

- Deep navy/near-black background, **neon cyan + electric blue** accents, green for completion, gold/amber for streak
- **Glassy panels with notched/beveled corners** — the "system window" look, plus thin connector lines and corner brackets
- **Monospace uppercase** for system text (`LINK_ESTABLISHED`, `SYNC STATUS`, `XP MULTI`) vs. bold sans for content
- Hexagons for badges/ranks, animated progress arcs, subtle particle/grid backgrounds
- Red/orange monospace tags for stat rewards (`STR +10`), blue monospace for XP (`+50 XP`)

All achievable with **CSS + SVG**. No 3D, no external assets required.

### 2.4 Mechanic-by-mechanic flaws

| # | Severity | Flaw | Evidence | Consequence |
|---|---|---|---|---|
| 1 | 🔴 | **Tracks reps, never load** | Quests are `100 push-ups / 100 sit-ups`; headline stat is `TOTAL REPS 26.5K` | Progression unmeasurable. Serious lifters bounce off |
| 2 | 🔴 | **XP multiplier is economically backwards** | `x1.0 → x5.0` across E→S on an apparently linear curve | Late levels get *faster*. Endgame accelerates into meaninglessness |
| 3 | 🔴 | **No endgame** | Ranks are one-way; S-Rank = Lvl 200–999 | ~2 years of grinding to a state where nothing new unlocks |
| 4 | 🟠 | **Ranks and levels undifferentiated** | Both derived from the same XP, shown side by side | Two progress bars for one resource; no strategic choice |
| 5 | 🟠 | **Streak punishes rest** | 87-day streak, no rest-day mechanic | Harmful in a *fitness* app — teaches overtraining |
| 6 | 🟠 | **Stats are cosmetically meaningless** | `STR +10 / AGI +8 / VIT +8` modify nothing visible | RPG stat theatre |
| 7 | 🟠 | **Zero social layer** | AppBrain's own cons list: *"Limited social features (friends or team challenges) not highlighted"* | Removes the strongest retention multiplier. Solo grind |
| 8 | 🟠 | **Ad-walled free tier** | Review: *"every time I click 1 button I get a 10–20 sec ad"* | Punishes the moment of engagement — mid-set logging |
| 9 | 🟡 | **No iOS build** | Site links `APP_STORE_URL` as a literal placeholder | Half the market locked out |
| 10 | 🟡 | **Static quest templates** | Same 4 quests daily | Template fatigue; no personalization or periodization |
| 11 | 🟡 | **Duplicate/abandoned listings** | Two Play listings; the older one stopped updating Nov 2025 | Fragmented userbase and reviews |
| 12 | 🟡 | **No anti-cheat** | Nothing stops a user tapping 5,000 reps | Leaderboards (if added) inherit Strava's problem |

**What it gets right (copy the pattern):** the visual language, the daily reset countdown, the streak calendar + timeline, the rank-hexagon screens, offline logging. The *retention skeleton* is sound. The *reward function* is hollow.

---

## 3. The wider category

| Platform | Model | Where it fails |
|---|---|---|
| **Habitica** | RPG over all habits | No workout tracking at all — no programming, no exercise guidance, no fitness progression. Scores the checkbox |
| **Zombies, Run!** | Audio-story running | Running only; reviews note the novelty fades. Zero strength training |
| **Fitness RPG** (Shikudo) | Step-RPG world exploration | Pedometer in a costume — no strength, no programming |
| **WalkScape / Walkr** | Steps → MMO / spaceship fuel | Volume of steps, zero training quality |
| **Fitocracy** | The original gamified fitness | **Dead.** Research found "perceived enjoyment and usefulness of the gamification decline with use" — the novelty-effect cautionary tale |
| **Strava** | Segments, KOMs, kudos | Opposite failure: hyper-real data, but **"digital doping"** — 1.6M vehicle + 2.3M e-bike activities removed from leaderboards, 293K athletes re-ranked. Also linked to risk-taking (a rider died chasing a KOM) and documented stress with gender-specific negative social effects |
| **Peloton** | Leaderboards + community | Social gamification works, but expensive and hardware-gated |
| **Freeletics** | AI programs + light gamification | Gamification stays superficial — surfaces in UI, doesn't drive behaviour |
| **BITLETICS** | Activity → raffle tickets | External rewards only; overjustification risk |
| **Ring Fit Adventure** | Full RPG built on exercise | Not a tracker; console + hardware; no external progression continuity |
| **Nike Run Club / Adidas Running** | Badges + challenges | Running only; badges are decorative |
| **Solo Leveling clone swarm** | Same formula, worse execution | Same flaws, crowded, low quality |
| **Hevy / Strong / Boostcamp / Fitbod / Jefit** | Serious trackers | Real data and programming — **but almost no game layer.** Boring. *This is the gap* |

### 3.1 Two structural insights

1. **The category splits into two halves that never meet.** Fitness-RPG apps have the motivation but no training substance. Serious trackers have the substance but no motivation design. **IMPROV lives in the gap.**
2. **The game layer works, then fades.** A meta-analysis of 16 RCTs (2,407 participants) found gamified interventions produce a real effect on physical activity (**Hedges g = 0.42** at ~12 weeks), shrinking to **g = 0.15** at ~14-week follow-up. Longitudinal work shows the *novelty effect* begins around **week 4**, lasts 2–6 weeks, then partially recovers (*familiarization effect*) — and recovery is only reliable if the underlying activity became genuinely rewarding. Separately, **enjoyment is the strongest predictor of long-term adherence**, and ~63% of new gym members quit before month three.

**Translation:** there's roughly a **4-week novelty window**. If the game is paint on a checkbox, users churn at week 5–6 — like Fitocracy. If the game is *made of* the training, familiarization takes over and the loop survives.

---

## 4. Our codebase: flaws found in the audit

Audited from source, October 2026, against the original code. Separate from
the design flaws above. **The Status column shows where each one stands now** —
the functional bugs and the critical response-contract issues are fixed; the
structural work (BCrypt, JWT, DTOs, validation) is P0/P1.

### 4.1 Critical

| Flaw | File | Detail | Status |
|---|---|---|---|
| **Plaintext passwords, returned to client** | `service.java`, `Controller.java` | `findByUsernameAndPassword(...)` compares raw strings; `/login` and `/register` both serialise the full `User` entity — **including the password field** | 🟡 Leak **fixed** (`@JsonProperty(WRITE_ONLY)`); plaintext *storage* remains — P0/P1 (BCrypt) |
| **Auth failure returns HTTP 500** | `service.java` | `orElseThrow(() -> new RuntimeException(...))` — a wrong password is not a server error | ✅ Fixed — now 401 (`ResponseStatusException`) |
| **Credential committed to git** | `application.properties` | `spring.datasource.password=sphy2323`, with `root` as the DB user | 🟡 Removed from the tree; still in git history — rotate, treat as burned |
| **No unique constraint on username/email** | `User.java` | Duplicate accounts possible | 🟡 Username unique in the V1 Flyway migration; email deliberately not yet (optional field) |
| **`mvn test` cannot pass in CI** | `ApApplicationTests.java` | Bare `@SpringBootTest` + JPA needs a live MySQL at `127.0.0.1:3306`. No test DB, no H2, no Testcontainers | ✅ Fixed — tests run on in-memory H2 |

### 4.2 Functional bugs

| Flaw | Detail | Status |
|---|---|---|
| **Login redirects to `workout-homepage.html.html`** | `LoginPage.js` — double extension; broken navigation right after login | ✅ Fixed |
| **Deep link silently ignored** | `workout-homepage.js` navigates to `exercises-detail.html?muscle=chest`, but `exercises-detail.js` never reads the query string. User lands on "Select a Muscle Group" — the click appears to do nothing | ✅ Fixed — `?muscle=` is honoured on load |
| **Confirm-password never validated** | Present in `RegistrationPage.html`, absent from `RegistrationPage.js` | ✅ Fixed |
| **`gender` throws on null** | `document.querySelector("input[name='gender']:checked").value` — throws if no radio chosen; registration dies silently | ✅ Fixed — absent gender is tolerated |
| **Height/weight type mismatch** | Inputs allow decimals (`step="0.1"`), backend binds to `Long`. `68.5` → deserialisation failure → generic "Registration failed" | ✅ Fixed — truncated client-side |
| **No error handling on login** | No `try/catch` around the `fetch`. Backend down = button does nothing, silently | ✅ Fixed — rejections surface an alert |
| **No auth state anywhere** | Nothing stored on login; homepage shows a hardcoded "Guest Athlete". The homepage is publicly reachable | ⏳ P1/P2 — needs the JWT work; the homepage also stops overwriting the profile name with the workout category |

### 4.3 Quality / hygiene

| Flaw | Detail | Status |
|---|---|---|
| `innerHTML` with unescaped data | `createExerciseCard()` interpolates `exercise.*` directly — XSS vector once data comes from an API | ⚠️ Data is local-only today; escape when API data lands (P3) |
| Fake loading state | `displayExercises()` runs `setTimeout(..., 500)` to simulate an API that doesn't exist | ✅ Removed — renders immediately |
| Duplicated CSS | The same `:root` token block copy-pasted across 4 stylesheets | ⏳ P2 design-system pass |
| Debris in the repo | `New Text Document.txt` ("testgit"), empty `readme.md`, a `.webp` loose in a source folder, a commit titled `...` | ✅ `New Text Document.txt`, empty `readme.md` and `Backend/.project` removed (the `.webp` avatar is referenced — kept) |
| `.project` committed | While `Backend/ap/.gitignore` explicitly excludes `.project` | ✅ Removed |
| No root `.gitignore`, no root README | — | ✅ Added |
| CORS `origins = "*"` | Symptom of no reverse proxy | ✅ Removed — single origin via nginx |
| `show-sql=true` left on | In the only config file present | ✅ Moved to the `dev` profile |
| Zero validation annotations | No `@Valid`, no `@NotBlank`, no length checks | ⏳ P1 |
| Java 26 / Spring Boot 4.1.0 | Unverified base-image and runner support — a CI risk before a runtime risk | ✅ Verified — base images exist on Docker Hub |

---

## 5. What "gamified" actually means

The question worth answering precisely, because getting it wrong is what produces Habitica and the Solo Leveling clone swarm.

### 5.1 Definition

> **A gamified fitness tracker converts training you can't feel yet into a score you can see today, then builds loops around that score.**

### 5.2 Layer 1 — The proxy

Fitness outcomes are slow, invisible and noisy: strength, body composition, health. You cannot feel a 2% strength gain on a Tuesday. Gamification replaces the delayed outcome with an **immediate, visible proxy** — XP, volume, a number that moves *today*.

**The psychological job: manufacture short feedback loops for a long-horizon process.**

### 5.3 Layer 2 — The loops

One score isn't enough; it saturates. Four loops, each catching a different churn window:

| Loop | Period | Catches users who quit… | Mechanics |
|---|---|---|---|
| **Session** | minutes | mid-workout | quests, progress bars, tap-to-log |
| **Day** | 24h | …when they skip one day | daily reset countdown, streak, daily quests |
| **Week** | 7d | …when motivation dips | weekly challenges, leaderboards, guild bosses |
| **Macro** | months | …when they plateau | levels, ranks, seasons, PRs |

### 5.4 Layer 3 — The stakes

Progress alone doesn't create return behaviour. **Loss does.** Streak break, rank demotion, leaderboard slip, letting guildmates down, an unclaimed daily reset. Loss aversion is the engine under the streak — which is exactly why misusing it (punishing rest, in a *fitness* app) is so destructive.

### 5.5 The six mechanic families

| Mechanic | What it produces | Psychology | Fails when | Verdict |
|---|---|---|---|---|
| **Quantification / XP** | Visible daily progress | Feedback loop, goal gradient | The number is arbitrary or fakeable | **Load-bearing** |
| **Levels / Ranks** | Long horizon, status, identity | Goal-gradient, endowed progress, status | Curve too slow, or gains accelerate | **Load-bearing** |
| **Streaks + daily reset** | Return pressure | Loss aversion, commitment | Too punishing → one miss = quit | **Load-bearing** |
| **Quests / challenges** | Session structure, variety | Variable reward, open loops (Zeigarnik) | Templates never change | **Load-bearing** |
| **Achievements / badges** | Collection, novelty spike | Completionism | Nobody looks after week 2 | **Decorative** |
| **Leaderboards / social** | Comparison, accountability | Social proof, relatedness | Cheating, stress, demotivates the middle | **Powerful & dangerous** |

Two of six are decoration. **The four that work all change what the user does next**, not decorate what they already did.

### 5.6 What the evidence supports

- Gamification works: **g = 0.42** at 12 weeks across 16 RCTs — a real, moderate effect.
- It decays: **g = 0.15** at ~14-week follow-up.
- Novelty effect begins around **week 4**, lasts 2–6 weeks, then partially recovers *if* the underlying activity became rewarding on its own.
- **Enjoyment is the strongest predictor of long-term adherence**; ~63% of new gym members quit before month three.

**The design constraint is not "add XP". It is: survive week 4 without the novelty.** The only mechanism that does that is making the training itself legible and rewarding.

### 5.7 The distinction that matters

| | **Gamifying the checkbox** | **Gamifying the training** |
|---|---|---|
| Reward function reads | whether you showed up | what your body actually did |
| Examples | Habitica, most Solo Leveling clones | *(nobody, yet — the opportunity)* |
| Can be faked | Yes, instantly | No — the score *is* the work |
| Teaches the user | Nothing | Load, progression, balance, rest |
| Reaction to a bad session | Same reward as a good one | Different reward — correctly |
| Lifetime | Dies at the novelty cliff (±week 5) | Survives it: progression is intrinsically rewarding |
| What it fundamentally is | A to-do list in a costume | A game made of training |

### 5.8 Summary

> **The gamified part of a fitness tracker isn't the XP bar. It's the reward function.**
>
> If XP is a function of *taps*, you've built a to-do list with a progress bar — and it dies at week 5, exactly like Fitocracy did.
> If XP is a function of *load, progression, balance and consistency*, the game and the training are the same system — and what keeps users coming back is what makes them stronger.

---

## 6. IP note

"Solo Leveling", "Sung Jin Woo", "Shadow Monarch" and "Arise" are licensed IP (Chugong / Kakao / Crunchyroll).

**Safe:** ranks E–S, XP, levels, quests, streaks, and the general "System" aesthetic — generic RPG vocabulary.
**Not safe:** character names, lifted UI art or icons, and third-party branding.

**IMPROV uses an original theme.** No lifted assets, no licensed names, and no screenshots or art from the reference app committed to this repo.

Full attribution for everything studied and depended on:
[`CREDITS.md`](./CREDITS.md).

---

## Sources

- ARISE SOLO — official site and Google Play listing (feature descriptions, review excerpts, screenshots)
- AppBrain — download/rating statistics and the "cons" analysis for the reference app
- *Evaluating the Effectiveness of Gamification on Physical Activity: Systematic Review and Meta-analysis of RCTs* — PMC8767479
- *Gamification suffers from the novelty effect but benefits from the familiarization effect* — Springer, 2022
- Research on Fitocracy use and declining perceived enjoyment of gamification
- Strava leaderboard integrity reporting (vehicle/e-bike activity removal, ML anti-cheat)
- Category reviews and comparison guides for Habitica, Zombies Run!, WalkScape, FitCraft, BITLETICS, Freeletics, Peloton
