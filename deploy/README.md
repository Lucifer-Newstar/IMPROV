# Deploy helpers

Files that must be served from the **site root** at exact paths, rather than
shipped inside the web container. Copy them onto the host / into the CDN, and
replace the placeholders.

| File | Served at | Purpose |
|---|---|---|
| `.well-known/assetlinks.json` | `https://<host>/.well-known/assetlinks.json` | Android App Links |
| `.well-known/apple-app-site-association` | `https://<host>/.well-known/apple-app-site-association` | iOS Universal Links |

Both files must be:

- served with `Content-Type: application/json`
- reachable **without a redirect**
- served over HTTPS (no self-signed certs)

If you skip App/Universal Links, the app still works — deep links just open in
the browser instead of the installed app. See `docs/MOBILE_DESKTOP.md`.
