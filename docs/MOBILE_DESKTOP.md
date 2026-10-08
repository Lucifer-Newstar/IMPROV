# Mobile & Desktop Packaging

**Short answer: yes — the same codebase ships as a web app, a PWA, an Android
app, an iOS app, and a desktop app for Windows/macOS/Linux.**

**Companion docs:** [`DEVOPS.md`](./DEVOPS.md) · [`PHASE_PLAN.md`](./PHASE_PLAN.md)

---

## 1. Why this is possible

The frontend is plain HTML/CSS/JS with no build step and no server-side
rendering. That makes it directly consumable by every packaging tool. Nothing
about it assumes a browser.

| Target | Tool | Effort | Status |
|---|---|---|---|
| **Web** | nginx (already running) | — | ✅ working |
| **PWA** (install to home screen) | manifest + service worker | Low | ✅ wired |
| **Android** | Capacitor | Low | ✅ configured, CI builds it |
| **iOS** | Capacitor | Low | ✅ configured, needs a Mac/Apple account to ship |
| **Windows / macOS / Linux** | Tauri | Medium | ✅ configured, CI builds it |

**Installable PWA is not a consolation prize.** On Android and desktop,
Chrome/Edge will install the PWA to the home screen or app menu from the site
itself — no store, no download prompt, no review. For most users that *is* the
app. Capacitor and Tauri exist for the cases where it isn't enough (see §6).

---

## 2. What was added

```
Frontend/
├── Login Page/
│   ├── manifest.webmanifest         # PWA identity, icons, shortcuts
│   ├── sw.js                        # service worker
│   ├── pwa.js                       # SW registration + install prompt
│   ├── config.js                    # API base resolution (web vs packaged)
│   ├── offline.html                 # offline fallback page
│   ├── favicon.ico
│   └── icons/                       # generated icons (192/512/maskable/apple)
├── capacitor.config.json            # Android + iOS
├── package.json                     # Capacitor scripts
└── src-tauri/                       # desktop
    ├── tauri.conf.json
    ├── Cargo.toml
    ├── build.rs
    └── src/main.rs
```

Plus three workflows: `.github/workflows/android.yml`,
`ios.yml`, `desktop.yml`.

**The key design decision** is in `config.js`. On the web the frontend calls
`/api` (same origin, nginx proxies it). Packaged builds are served from a fake
origin — `capacitor://localhost`, `tauri://localhost` — so they need an absolute
URL. `config.js` resolves the correct base at runtime:

```js
window.IMPROV.apiBase   // '/api' on web, 'https://your-host/api' when packaged
```

**Before building any native package, change `PRODUCTION_API_ORIGIN` in
`config.js` to your real deployed origin.** It ships as a placeholder and the
code warns in the console if you forget.

---

## 3. PWA — what works today

- **Installable** on Android (Chrome), desktop Chrome/Edge, and iOS Safari
  (*Share → Add to Home Screen*)
- **Offline shell** — the app opens with no network and shows an offline page
  rather than a browser error
- **App shortcuts** — long-press the icon for *Today's Quests* / *Exercise Library*
- **Standalone display** — no browser chrome
- **Theme colour** matching the app's dark red palette

### What the service worker deliberately does *not* do

`sw.js` uses **network-only for `/api/`**. Quest progress and XP are
server-authoritative state; serving a stale cached copy would show the user the
wrong XP, which is worse than showing nothing.

Real offline logging needs a write queue — IndexedDB plus replay-on-reconnect —
because a fitness app is used in gyms and basements with no signal. That's
scheduled in the phase plan (D11), not hand-waved here.

**When you deploy a new build, bump `CACHE_VERSION` in `sw.js`.** That's what
evicts the old cache.

---

## 4. Android

```bash
cd Frontend
npm install
npx cap add android        # once — generates Frontend/android/
npx cap sync android       # copy web assets in
npx cap open android       # opens Android Studio
```

Or from CI: push a `v*` tag, or run the **Android build** workflow manually.
It produces a debug APK and a release AAB as artifacts.

### Prerequisites

- Node 20+
- JDK 21 (Capacitor's Gradle project targets 21, not 26)
- Android Studio + SDK, for local builds
- A keystore, for a signed release

### Creating a keystore (do this once, then guard it with your life)

```bash
keytool -genkey -v -keystore release.keystore \
  -alias improv -keyalg RSA -keysize 2048 -validity 10000
```

Base64 it for GitHub Actions, and add the secrets named in
`android.yml`:

```bash
base64 -w0 release.keystore    # -> ANDROID_KEYSTORE_BASE64
```

> **If you lose this keystore you can never update that Play Store listing
> again.** Back it up somewhere that isn't the repo. This is a real, permanent
> footgun — Google will not reset it for you.

### Network config for the packaged build

Android blocks cleartext HTTP by default, and `localhost` inside the app is the
app's own WebView — not your machine. Two consequences:

1. Your API must be served over **HTTPS**.
2. If you're testing against a local HTTP backend, add a network security config.
   Create `Frontend/android/app/src/main/res/xml/network_security_config.xml`:

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
     <domain-config cleartextTrafficPermitted="true">
       <domain includeSubdomains="true">10.0.2.2</domain> <!-- host loopback from the emulator -->
     </domain-config>
   </network-security-config>
   ```

   and reference it from `AndroidManifest.xml`:
   `android:networkSecurityConfig="@xml/network_security_config"`

   **Do not** ship `cleartextTrafficPermitted="true"` for a real domain.

> `Frontend/android/` is gitignored — the native project is generated, and
> regenerating it is cheap. If you end up doing heavy native customisation,
> narrow those ignore rules and commit the platform folder instead.

### App Links (optional)

To make `https://your-host/...` links open in the app rather than the browser,
host a Digital Asset Links file at
`https://your-host/.well-known/assetlinks.json` containing your app's package
name (`com.improv.app`) and the SHA-256 fingerprint of your **signing**
certificate:

```bash
keytool -list -v -keystore release.keystore -alias improv | grep SHA256
```

---

## 5. iOS

Same flow, but **it requires a Mac with Xcode.** There is no way around this.

```bash
cd Frontend
npm install
npx cap add ios
npx cap sync ios
npx cap open ios        # opens Xcode
```

The `ios.yml` workflow builds an **unsigned simulator app** on a macOS runner —
enough to verify the project compiles. Producing a real `.ipa` additionally
needs:

- An Apple Developer account (**$99/year**)
- A distribution certificate + provisioning profile
- `xcodebuild -exportArchive` with an `ExportOptions.plist`
- App Store review (or TestFlight for beta distribution)

None of that can be faked in CI, which is why it isn't wired up. Budget for it
deliberately — the reference app we analysed still doesn't have an iOS build,
and its website links a placeholder `APP_STORE_URL`.

> **Cost note:** macOS runners bill at roughly 10× Linux on private repos. If
> your repo is public they're free. The workflow only runs on manual dispatch
> for this reason.

### Universal Links (optional)

Host `.well-known/apple-app-site-association` (no file extension, served as
`application/json`) with your Team ID, bundle ID and allowed paths.

---

## 6. Desktop (Tauri)

Tauri wraps the same web assets in a native window using the OS webview.
Compared to Electron: **~3–10 MB installers instead of ~100 MB**, and a much
smaller memory footprint.

```bash
# one-time
cargo install tauri-cli --version "^2"

# dev, live-reloading
cd Frontend/src-tauri && cargo tauri dev

# installers for the current platform
cargo tauri build
```

CI does this for all three platforms on a `v*` tag — `.deb` and AppImage on
Linux, `.msi` and NSIS `.exe` on Windows, `.dmg` and `.app` on macOS.

### Prerequisites

- Rust (stable) + Cargo
- **Linux:** `libwebkit2gtk-4.1-dev`, `libxdo-dev`, `libssl-dev`,
  `libayatana-appindicator3-dev`, `librsvg2-dev`, `patchelf`
- **Windows:** WebView2 (preinstalled on Win10+), MSVC build tools
- **macOS:** Xcode command-line tools

The workflows install the Linux set for you.

### Notes

- `tauri.conf.json` points `frontendDist` at `../Login Page`, so it packages
  the same files the web build serves — no separate build step.
- Icons reuse the PWA icons in `Login Page/icons/`. Tauri normally wants a
  multi-resolution `.ico`/`.icns`; generating those (`cargo tauri icon`) is a
  task for when you actually ship a desktop release.
- Desktop builds store data in the webview's local storage, which is per-app.
  Fine for now; revisit when auth lands.

---

## 7. What's still missing before shipping anywhere

| Gap | Impact | Where it's tracked |
|---|---|---|
| **`PRODUCTION_API_ORIGIN` is a placeholder** | Native builds can't reach the API | `config.js` — change before building |
| **Tests can't run in a simulator without a real API** | No E2E mobile coverage | P3+ |
| **No offline write queue** | Logging fails with no signal — bad for gyms | D11, P3 |
| **Tokens in `localStorage`** | Not secure on a shared device | Auth work, P0/P1 |
| **No push notifications** | Lost retention lever (streak reminders) | Backlog |
| **No app icons for iOS/desktop `.ico`/`.icns`** | Store submission needs them | `cargo tauri icon`, later |
| **Store listings, screenshots, privacy policy** | Required for Play/App Store | Before submission |
| **Health data permissions** | Not needed now; required if you ever read HR/steps | If scoped |

---

## 8. Recommended order

1. **Ship the PWA.** It's done — deploy it and see if install-to-home-screen is
   enough for your users. It often is.
2. **Android**, because it's free and the CI already works.
3. **Desktop**, if there's demand — the effort is low and Tauri is genuinely good.
4. **iOS last**, because it costs $99/yr and needs a Mac. Do it when there's a
   reason to.
