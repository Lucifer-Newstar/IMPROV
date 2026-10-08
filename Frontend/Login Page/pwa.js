/**
 * IMPROV — PWA wiring.
 *
 * Included by every page. Registers the service worker, handles the install
 * prompt, and keeps the service worker up to date when a new build is served.
 */
(function () {
  'use strict';

  // Service workers require a secure context (https, or localhost in dev).
  // Inside Capacitor/Tauri the assets are local and the SW is unnecessary.
  var supported = 'serviceWorker' in navigator
    && (window.isSecureContext || location.hostname === 'localhost');

  if (supported && !(window.IMPROV && window.IMPROV.isNative)) {
    window.addEventListener('load', function () {
      navigator.serviceWorker.register('sw.js', { scope: './' })
        .then(function (registration) {
          // If a new worker is waiting, activate it and reload once so the
          // user is not stuck on a stale shell.
          registration.addEventListener('updatefound', function () {
            var installing = registration.installing;
            if (!installing) return;
            installing.addEventListener('statechange', function () {
              if (installing.state === 'installed' && navigator.serviceWorker.controller) {
                installing.postMessage('SKIP_WAITING');
              }
            });
          });
        })
        .catch(function (err) {
          console.warn('[IMPROV] service worker registration failed', err);
        });

      var reloading = false;
      navigator.serviceWorker.addEventListener('controllerchange', function () {
        if (reloading) return;
        reloading = true;
        window.location.reload();
      });
    });
  }

  // -------------------------------------------------------------------------
  // Install prompt
  // -------------------------------------------------------------------------
  var deferredPrompt = null;

  window.addEventListener('beforeinstallprompt', function (event) {
    // Keep the browser's banner for the native app-store route and use our
    // own button instead.
    event.preventDefault();
    deferredPrompt = event;
    document.dispatchEvent(new CustomEvent('improv:installable'));
  });

  window.addEventListener('appinstalled', function () {
    deferredPrompt = null;
    document.dispatchEvent(new CustomEvent('improv:installed'));
  });

  /** Returns true if a prompt was shown. */
  window.improvPromptInstall = function () {
    if (!deferredPrompt) return false;
    deferredPrompt.prompt();
    deferredPrompt.userChoice.then(function (choice) {
      document.dispatchEvent(
        new CustomEvent('improv:install-choice', { detail: choice.outcome })
      );
      deferredPrompt = null;
    });
    return true;
  };

  window.improvCanInstall = function () {
    return Boolean(deferredPrompt);
  };

  // Report connection state changes so screens can warn before a failed write.
  window.addEventListener('offline', function () {
    document.dispatchEvent(new CustomEvent('improv:offline'));
  });
  window.addEventListener('online', function () {
    document.dispatchEvent(new CustomEvent('improv:online'));
  });
})();
