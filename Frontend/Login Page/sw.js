/**
 * IMPROV — service worker.
 *
 * Strategy, deliberately conservative:
 *   * App shell (HTML/CSS/JS/icons) — pre-cached, then stale-while-revalidate
 *   * API responses             — network only, never cached
 *
 * Why never cache the API: quest progress and XP are per-user, server-
 * authoritative state. A stale cached copy would show the user the wrong XP,
 * which is worse than showing nothing. Real offline logging needs a write
 * queue (IndexedDB + replay), which is scheduled in the phase plan, not here.
 */

// Bump this on every release. It is what evicts the previous cache.
const CACHE_VERSION = 'v1';
const CACHE_NAME = `improv-shell-${CACHE_VERSION}`;

const PRECACHE_URLS = [
  './',
  './LoginPage.html',
  './LoginPage.css',
  './LoginPage.js',
  './RegistrationPage.html',
  './RegistrationPage.css',
  './RegistrationPage.js',
  './workout-homepage.html',
  './workout-homepage.css',
  './workout-homepage.js',
  './exercises-detail.html',
  './exercises-detail.css',
  './exercises-detail.js',
  './config.js',
  './theme.css',
  './api.js',
  './app.js',
  './analytics.html',
  './analytics.js',
  './streak.html',
  './streak.js',
  './rank.html',
  './rank.js',
  './offline.html',
  './manifest.webmanifest',
  './favicon.ico',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/icon-maskable-192.png',
  './icons/icon-maskable-512.png',
  './icons/apple-touch-icon.png'
];

// ---------------------------------------------------------------------------
// Install — pre-cache the shell.
// ---------------------------------------------------------------------------
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) =>
        // addAll is atomic: one 404 fails the whole install. Add individually
        // so a single missing optional file can't block the service worker.
        Promise.all(
          PRECACHE_URLS.map((url) =>
            cache.add(url).catch((err) => console.warn('[sw] precache miss', url, err))
          )
        )
      )
      .then(() => self.skipWaiting())
  );
});

// ---------------------------------------------------------------------------
// Activate — drop old caches.
// ---------------------------------------------------------------------------
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) =>
        Promise.all(
          keys
            .filter((key) => key.startsWith('improv-shell-') && key !== CACHE_NAME)
            .map((key) => caches.delete(key))
        )
      )
      .then(() => self.clients.claim())
  );
});

// ---------------------------------------------------------------------------
// Fetch
// ---------------------------------------------------------------------------
self.addEventListener('fetch', (event) => {
  const { request } = event;

  // Only ever handle GET.
  if (request.method !== 'GET') return;

  const url = new URL(request.url);

  // Cross-origin (e.g. the packaged build calling the API host): leave it to
  // the network. Never cache somebody else's response.
  if (url.origin !== self.location.origin) return;

  // API — network only. Failures surface as errors the UI can report.
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(request).catch(() =>
        new Response(
          JSON.stringify({
            error: 'offline',
            message: 'No connection. Your progress was not saved — try again when you are back online.'
          }),
          { status: 503, headers: { 'Content-Type': 'application/json' } }
        )
      )
    );
    return;
  }

  // Navigations — network first, fall back to the cached shell, then the
  // offline page. This is what makes the PWA open instantly and still work
  // with no signal.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
          return response;
        })
        .catch(() =>
          caches.match(request).then((cached) => cached || caches.match('./offline.html'))
        )
    );
    return;
  }

  // Static assets — cache first, revalidate in the background.
  event.respondWith(
    caches.match(request).then((cached) => {
      const network = fetch(request)
        .then((response) => {
          if (response && response.status === 200 && response.type === 'basic') {
            const copy = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(request, copy));
          }
          return response;
        })
        .catch(() => cached);

      return cached || network;
    })
  );
});

// ---------------------------------------------------------------------------
// Messages — lets the page trigger an immediate update.
// ---------------------------------------------------------------------------
self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});
