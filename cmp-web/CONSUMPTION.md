# Consuming `cmp-web` in a fork

`cmp-web` is the browser app-shell — two nearly-identical entry points, one per Kotlin/JS target,
that both call `initKoin()` and render `cmp-shared`'s `SharedApp` into the page.

## What's here

- **`src/jsMain/kotlin/Application.kt`** — the classic Kotlin/JS entry (`js(IR)` target, output
  `cmp-web.js`). Calls `initKoin()`, restores a `localStorage`-persisted `app_language`, then
  `onWasmReady { ComposeViewport(document.body!!) { ... } }` rendering `SharedApp`. Locale changes
  persist to `localStorage` and reload the page.
- **`src/wasmJsMain/kotlin/Main.kt`** — the `wasmJs` target (output `cmp-wasm.js`). Same shape,
  plus `configureWebResources { resourcePathMapping { "./$path" } }` for Compose resource loading
  under the wasm target.
- **`webpack.config.d/dev-server-headers.js`** — sets COOP/COEP headers on the local dev server so
  `crossOriginIsolated` is `true`, enabling `WebWorkerSQLiteDriver` + OPFS-backed Room storage
  locally.
- **`src/jsWasmMain/kotlin/WebNetworkMonitor.kt`** — installs the network monitor with the browser's
  own online signal before `initKoin()`. The default HTTP probe (google `generate_204`) is a
  CORS-blocked request from a page, so it would report offline on every network.
- **`src/{js,wasmJs}Main/resources/_worker.js`** — the Cloudflare Pages worker (the recommended web
  host). It serves the site with COOP/COEP, so the page is cross-origin isolated and the app's
  OPFS-backed database persists, and it runs the site's own CORS relay at `/cors/?<https-url>`
  (`app-profile/app.yaml#network.web_cors_proxy: /cors`). The relay forwards GET/HEAD to allowlisted
  hosts only: the `PROXY_ALLOWED_HOSTS` Pages environment variable (comma-separated) or the worker's
  default list. Keep it in sync with `proxied_host` entries.
- **`src/{js,wasmJs}Main/resources/_headers`** — the same COOP/COEP headers for Netlify, which reads
  `_headers`. GitHub Pages can neither send headers nor run the relay: there the database is
  in-memory (one page session) and `proxied_host` calls fail, so screens show their error state.

## Hosting

Deploy with the release workflow's `web_host: cloudflare-pages` (needs the `CLOUDFLARE_API_TOKEN`
and `CLOUDFLARE_ACCOUNT_ID` repository secrets; the Pages project defaults to the repository name).
To deploy by hand: `./gradlew :cmp-web:jsBrowserDistribution`, then
`wrangler pages deploy cmp-web/build/dist/js/productionExecutable --project-name=<project> --branch=main`.
`wrangler pages dev <same dir>` serves it locally with the worker, exactly as Cloudflare runs it.

## What a fork touches

- **SEO / link-preview metadata** — `index.html` (`jsMain`/`wasmJsMain/resources/`) is
  token-substituted at build time (`jsProcessResources`/`wasmJsProcessResources` in
  `build.gradle.kts`) from `gradle/fork.properties` — `APP_DISPLAY_NAME`, `APP_DESCRIPTION`
  (`store.android.short.description` / `store.subtitle`), `APP_KEYWORDS`, `APP_COPYRIGHT`,
  `APP_AUTHOR`, `APP_URL`, `OG_IMAGE`. Set these via `app-profile/` (store listing + org fields),
  not by hand-editing `index.html`. The substitution is scoped to `index.html` — it is a text
  filter, and running it over the binaries in the same task (`skiko.wasm`, icons) corrupts them.
- **App behavior/UI** — add features via the `cmp-navigation` registries, not by editing
  `Application.kt`/`Main.kt`.

## What to leave to sync

Both entry points and the webpack dev-server config are template infrastructure — identical
across forks except for the token-substituted metadata above.

See [`README.md`](README.md) for the module graph, and `cmp-navigation`'s
[`CONSUMPTION.md`](../cmp-navigation/CONSUMPTION.md) for how to add features.

Symbols: main (jsMain), main (wasmJsMain)
