# Mifos Pay

Mifos Pay is a mobile wallet on Apache Fineract's self-service API, built on the
[kmp-project-template](https://github.com/openMF/kmp-project-template) for Android, iOS, desktop and web.

> **The architecture source of truth is [`docs/architecture/`](docs/architecture/ARCHITECTURE.md)** —
> module guides, cross-cutting concerns and patterns. This file only routes there and records what is
> specific to this fork. When the two disagree, the guide wins.

## How this fork sits on the template

- **Ownership decides where a change goes.** `scripts/customization-surface.sh resolve <path>` says
  whether a file is the template's, the fork's, or merged. Template-owned files are fixed in the
  template and synced here, never edited in place. `Kpt*` names are template code, `Fork*` are ours.
- **Packages:** wallet code lives under `kpt.core.*` and `kpt.feature.*`. Where the template already
  declares the same name in a package, the wallet's version sits in a `.fork` subpackage and is imported
  explicitly.
- **App root:** `cmp-navigation/.../registry/app/` holds the wallet shell (`ForkAppRoot`: login, passcode,
  re-auth lock, 401 dialog, the four-tab scaffold), rendered through the template's `ForkAppShell.root`.
  Its Koin modules register via `ForkFeatureRegistry`; module dependencies go in `feature-deps.gradle.kts`.
- **Preferences:** `ForkPreferencesRepository` extends the template's `UserPreferencesRepository` with
  the wallet's user, client, token, accounts and server instances.
- **Network:** the Fineract self-service stack is the `main` access point (`kpt.core.network.main`).
  On the web, calls go through the same-origin `/cors` relay (`cmp-web/.../_worker.js`), because the
  Fineract servers reject browser-origin requests.
- **Product definition** (screens, tester flows, run state) lives in the workspace project's
  `idea-layer`, not in this repository.

## Everyday commands

```bash
./gradlew spotlessApply                      # format
./gradlew check spotlessCheck detekt dependencyGuard
./gradlew :cmp-android:installProdDebug      # Android (prod flavor)
./gradlew :cmp-web:jsBrowserDistribution     # web build (deployable with its _worker.js)
./gradlew desktopTest                        # unit tests on the JVM
bash scripts/docs/doc-audit.sh               # documentation audit (CI "Docs are current")
bash scripts/product-health/product-health.sh --all
```

## More

- [Onboarding](docs/onboarding.md) · [Troubleshooting](docs/troubleshooting.md)
- [GitHub Actions](.github/CLAUDE.md) · [Scripts](scripts/CLAUDE.md)
