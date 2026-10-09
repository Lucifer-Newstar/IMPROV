/**
 * IMPROV — runtime configuration.
 *
 * The web app is same-origin: nginx proxies /api to the backend, so the
 * browser never needs an absolute backend URL. That is what keeps CORS out of
 * this project entirely.
 *
 * Packaged builds (Capacitor for Android/iOS, Tauri for desktop) are served
 * from a fake origin (capacitor://localhost, tauri://localhost) and therefore
 * DO need an absolute URL. That is the only case this file exists for.
 *
 * CI enforces that no other .js file contains a hardcoded backend origin.
 */
(function () {
  'use strict';

  // ---------------------------------------------------------------------
  // CHANGE ME before building the Android/iOS/desktop packages.
  // The web build ignores this value completely.
  // ---------------------------------------------------------------------
  var PRODUCTION_API_ORIGIN = 'https://improv.example.com';

  var NATIVE_ORIGINS = [
    'capacitor://localhost',   // Capacitor iOS
    'https://localhost',       // Capacitor Android (androidScheme: "https")
    'http://localhost',        // Capacitor Android (cleartext scheme, dev only)
    'tauri://localhost',       // Tauri (macOS / Linux)
    'https://tauri.localhost'  // Tauri (Windows)
  ];

  var origin = window.location.origin;
  var isNative = NATIVE_ORIGINS.indexOf(origin) !== -1;
  var isPlaceholder = PRODUCTION_API_ORIGIN.indexOf('example.com') !== -1;

  if (isNative && isPlaceholder) {
    console.warn(
      '[IMPROV] Running in a packaged shell but PRODUCTION_API_ORIGIN is still ' +
      'the placeholder in config.js. API calls will fail. Set it to your real ' +
      'deployed origin before building.'
    );
  }

  window.IMPROV = {
    /** True when running inside Capacitor or Tauri. */
    isNative: isNative,

    /** 'https://host/api' in packaged builds, '/api' on the web. */
    apiBase: isNative ? PRODUCTION_API_ORIGIN + '/api' : '/api',

    /** Endpoints, in one place so a path change is a one-line change. */
    endpoints: {
      login: '/Users/login',
      register: '/Users/register',
      refresh: '/auth/refresh',
      logout: '/auth/logout',
      me: '/auth/me',
      questsToday: '/quests/today',
      questLog: '/quests/{id}/log',
      questComplete: '/quests/{id}/complete',
      progression: '/progression',
      ranks: '/ranks',
      analyticsOverview: '/analytics/overview',
      analyticsStreak: '/analytics/streak',
      analyticsCalendar: '/analytics/calendar'
    },

    /** Fills {placeholders} in an endpoint template:
     *  IMPROV.url(IMPROV.endpoints.questLog, { id: 7 }) -> '/quests/7/log' */
    url: function (template, params) {
      return template.replace(/\{(\w+)\}/g, function (_, key) {
        return encodeURIComponent(params[key]);
      });
    },

    version: '0.1.0'
  };
})();
