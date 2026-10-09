/**
 * IMPROV — shared chrome for authenticated pages.
 *
 * Wires the logout button that every app page's top bar carries. Loaded after
 * api.js on Home, Exercises, Analytics, Streak and Rank.
 */
(function () {
  'use strict';

  var button = document.getElementById('logout-btn');
  if (!button) {
    return;
  }
  button.addEventListener('click', async function () {
    try {
      await window.improvApi.post(window.IMPROV.endpoints.logout);
    } catch (err) {
      // Even if the call fails, the session is not worth keeping — the
      // server clears the cookies it can, and the login page starts fresh.
    }
    window.location.href = 'LoginPage.html';
  });
})();
