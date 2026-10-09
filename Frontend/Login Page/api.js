/**
 * IMPROV — authenticated API client.
 *
 * Auth is cookie-based (httpOnly JWT), so a request only needs
 * credentials: 'same-origin'. This wrapper adds the three things every page
 * would otherwise repeat: uniform error handling (the API answers RFC 7807
 * problem documents), one automatic token refresh when the access token has
 * expired, and a redirect to the login page when the session is really gone.
 */
(function () {
  'use strict';

  var refreshAttempted = false;

  function isLoginPage() {
    return /LoginPage\.html$/.test(location.pathname)
      || /index\.html$/.test(location.pathname)
      || /\/$/.test(location.pathname);
  }

  function problemDetail(res) {
    return res.json().then(function (p) {
      if (!p) {
        return 'Request failed (' + res.status + ')';
      }
      var message = p.detail || 'Request failed (' + res.status + ')';
      if (Array.isArray(p.errors) && p.errors.length) {
        message += ' — ' + p.errors.map(function (e) {
          return e.field + ': ' + e.message;
        }).join('; ');
      }
      return message;
    }).catch(function () {
      return 'Request failed (' + res.status + ')';
    });
  }

  async function request(method, path, body) {
    var options = { method: method, credentials: 'same-origin' };
    if (body !== undefined) {
      options.headers = { 'Content-Type': 'application/json' };
      options.body = JSON.stringify(body);
    }

    var res;
    try {
      res = await fetch(window.IMPROV.apiBase + path, options);
    } catch (err) {
      throw new Error('Could not reach the server. Check your connection and try again.');
    }

    if (res.status === 401) {
      if (isLoginPage()) {
        // The login/register endpoint itself rejected the credentials.
        throw new Error('Wrong username or password.');
      }
      if (!refreshAttempted) {
        // The access token expired — rotate the pair once and retry.
        refreshAttempted = true;
        var refreshRes = await fetch(
          window.IMPROV.apiBase + window.IMPROV.endpoints.refresh,
          { method: 'POST', credentials: 'same-origin' }
        ).catch(function () { return null; });
        if (refreshRes && refreshRes.ok) {
          return request(method, path, body);
        }
      }
      location.href = 'LoginPage.html';
      throw new Error('Your session has expired. Please log in again.');
    }

    if (!res.ok) {
      throw new Error(await problemDetail(res));
    }
    if (res.status === 204) {
      return null;
    }
    return res.json();
  }

  window.improvApi = {
    get: function (path) { return request('GET', path); },
    post: function (path, body) { return request('POST', path, body); }
  };
})();
