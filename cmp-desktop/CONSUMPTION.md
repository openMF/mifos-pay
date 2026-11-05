# Consuming `cmp-desktop` in a fork

`cmp-desktop` is the JVM app-shell — a single `main()` (`src/jvmMain/kotlin/Main.kt`) that opens a
Compose Desktop `Window` and renders `cmp-shared`'s `SharedApp`. There is no per-screen desktop
code; everything renders through the shared Compose UI.

## What's here

- **`main()`** — reads the window title from the `app.name` JVM system property (set via
  `jvmArgs("-Dapp.name=$windowTitle")` in `build.gradle.kts`), sets
  `apple.awt.application.name` (controls the macOS menu-bar process name), calls `initKoin()`,
  and opens a `Window` hosting `SharedApp`. Locale changes are applied via `Locale.setDefault` +
  a `key(localeVersion)` forced recomposition (Desktop has no activity-recreate equivalent).

## What a fork touches

- **App identity / packaging** — `appName`, `packageNameSpace` (`appId`), and `appVersion` in
  `build.gradle.kts` all resolve from the version catalog (`app-profile/` → `syncForkConfig`).
  Vendor/copyright/description/macOS category read from `gradle/fork.properties` via the local
  `forkProp(key, default)` helper (`org.name`, `org.copyright`, `app.description`,
  `mac.app.category`) — set these in `app-profile/`, not in `build.gradle.kts`.
- **Icons** — `icons/ic_launcher.{icns,ico,png}` per platform, populated by
  `app-profile/icons/` + `syncForkConfig`.
- **macOS signing/notarization** — `MAC_SIGNING_IDENTITY` / `MAC_KEYCHAIN_PATH` /
  `MAC_PROVISIONING_PROFILE_PATH` / `NOTARIZATION_*` env vars, supplied by the Fastlane `mac` lane
  — not hardcoded here.
- **App behavior/UI** — add features via the `cmp-navigation` registries, not by editing `main.kt`.

## What to leave to sync

The `compose.desktop { application { ... } }` block (target formats, macOS/Windows/Linux
packaging, proguard config) is template infrastructure shared by every fork; a template sync
full-copies it. Only the `forkProp(...)`-sourced values change per fork.

See [`README.md`](README.md) for the module graph, and `cmp-navigation`'s
[`CONSUMPTION.md`](../cmp-navigation/CONSUMPTION.md) for how to add features.

Symbols: main
