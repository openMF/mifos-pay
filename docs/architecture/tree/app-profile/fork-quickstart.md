# Fork Quickstart — Day-1 Customization Checklist

This is the long-form walkthrough for turning a fresh clone of
`kmp-project-template` into a branded, running app. The condensed five-step
version lives in [`core-base/README.md`](../../README.md#day-1-fork-checklist) —
this file expands each step with rationale, gotchas, and pointers into the rest
of the codebase.

> **Estimated time:** 5 minutes for steps 1–2 (one command does it), plus
> whatever your auth / analytics / crash-reporter integrations take.

---

## Step 1 — Set fork identity (one command)

All fork identity lives in **six lines** at the top of `gradle/libs.versions.toml`.
You never touch Android source files, iOS source files, Fastlane config, or
`settings.gradle.kts` — a single Gradle task bridges the catalog to every
platform automatically.

### Option A — One-shot via `scripts/white-label/fork-init.sh` (recommended)

```bash
bash scripts/white-label/fork-init.sh com.myapp.example MyAwesomeApp "My Awesome App" ABCDE12345
#                  <package_id>       <project_name> [display_name]  [ios_team_id]
```

That's it. The script:
1. Rewrites the 5 identity lines in `gradle/libs.versions.toml`
2. Runs `./gradlew syncForkConfig` to propagate changes to all platform config files
3. **Removes the demo showcase by default** — deletes the Money-Toolkit demo (10 feature
   modules + all demo data/domain packages) and resets the database schema, leaving a
   clean, branded, compiling framework shell for you to build on. This is the whole point
   of forking: you start from *your* app, not the template's demo.

> **Want to keep the demo** (to study the framework's reference features first)? Append
> `--keep-demo`:
> ```bash
> bash scripts/white-label/fork-init.sh com.myapp.example MyAwesomeApp "My Awesome App" ABCDE12345 --keep-demo
> ```
> You can remove it later at any time with `bash scripts/white-label/fork-init.sh --clean --apply` (see
> [Remove the demo showcase](#optional--remove-the-demo-showcase-customizersh---clean)).

### Option B — Edit `libs.versions.toml` directly

Open `gradle/libs.versions.toml` and change only the six lines in the fork
identity block (near the top of the `[versions]` section):

```toml
# ── Fork Identity — edit ONLY these 5 lines, then run ./gradlew syncForkConfig ──
appId            = "com.myapp.example"   # Android applicationId + iOS bundle ID
appDisplayName   = "My Awesome App"      # Android app_name, iOS CFBundleDisplayName
desktopAppName   = "MyAwesomeAppDesktop" # JVM package name, dock name
projectName      = "my-awesome-app"      # rootProject.name, Fastlane PROJECT_NAME
iosTeamId        = "ABCDE12345"          # Apple Developer Team ID
```

> Module Android namespaces (`kpt.*`) are a fixed framework label — **not** a fork setting. You do
> NOT rename them; leave them as-is (see the table below).

Then propagate to all platform config files:

```bash
./gradlew syncForkConfig
```

### What `syncForkConfig` writes

| File | What changes |
|------|-------------|
| `cmp-ios/Configuration/Config.xcconfig` | `APP_BUNDLE_ID`, `APP_NAME`, `TEAM_ID` |
| `local.properties` | `fork.app.id`, `fork.app.display.name`, `fork.project.name` |
| `gradle.properties` | `fork.project.name` (→ `rootProject.name`) |

`Config.xcconfig` is committed to your repo and excluded from upstream
`sync-dirs` syncs so your identity survives template updates.

### How identity flows to each platform

| Platform | Identity source |
|---------|----------------|
| Android `applicationId` | `appId` via `cmp-android/build.gradle.kts` |
| Android `app_name` | `appDisplayName` via `resValue` in `build.gradle.kts` |
| All 38 library module namespaces | `org.convention.BASE_MODULE_NAMESPACE` (`kpt`, framework-owned) + module path — derived by convention plugin, not a fork setting |
| iOS bundle ID | `Config.xcconfig` → `APP_BUNDLE_ID` → `project.pbxproj` |
| iOS display name | `Config.xcconfig` → `APP_NAME` → Info.plist `CFBundleDisplayName` |
| iOS team | `Config.xcconfig` → `TEAM_ID` → Xcode signing settings |
| Desktop app name / package | `desktopAppName` via `cmp-desktop/build.gradle.kts` |
| Fastlane `package_name` / `app_identifier` | `local.properties` → `deployment/<platform>/<target>/lane.rb` AppFile configs |
| Gradle project name | `gradle.properties` → `settings.gradle.kts` |

**Kotlin source files never change.** `package kpt.*` in `.kt` files is
the framework's internal code organization — it is completely separate from
the Android `applicationId` / iOS bundle ID the stores use to identify your
app. You do not rename source packages.

### Updating identity later

```bash
# Edit libs.versions.toml, then:
./gradlew syncForkConfig
```

Or re-run `scripts/white-label/fork-init.sh` with the new values.

## Step 2 — Override the 4 customization-point Koin bindings

The template ships working stubs for every fork-customizable dependency so the
demo build runs out of the box. Each binding lives behind a stable interface;
your fork swaps the implementation in its app Koin module and no callers
change.

### 2a. `AuthProvider`

- **Default:** `NoOpAuthProvider` (returns "not authenticated" forever — the
  demo app has no login).
- **Swap for:** Firebase Auth, OAuth, biometric-only, your own SSO.
- **Where:** consumer Koin module (typically `cmp-shared/`'s DI). Bind your
  impl as `single<AuthProvider> { ... }`.

### 2b. `CrashReporter`

- **Default:** `ConsoleCrashReporter` (prints to logcat / stderr).
- **Swap for:** `FirebaseCrashlyticsCrashReporter`, Sentry, Bugsnag.
- **Where:** consumer Koin module. Bind as `single<CrashReporter> { ... }`.
  The interface lives in `core-base/observability/`.

### 2c. `AnalyticsHelper`

- **Default:** `NoOpAnalyticsHelper` (drops events).
- **Swap for:** `FirebaseAnalyticsHelper` (already shipped — opt-in by
  changing the Koin binding), Amplitude, Mixpanel, PostHog.
- **Where:** consumer Koin module. The higher-level `KptAnalyticsTracker`
  (in `core/analytics/`) consumes whichever `AnalyticsHelper` you bind — no
  call-site changes.

### 2d. Network config bindings

- **`FredApiConfig`** — the FRED API key for B7 Interest Rates. Swap
  `apiKey` for your own key (free signup at
  https://fred.stlouisfed.org/docs/api/api_key.html) and optionally swap
  `baseUrl` to point at a proxy.
- **`FrankfurterApiConfig`** — the Frankfurter exchange-rates base URL.
  Override `baseUrl` to point at a self-hosted Frankfurter or your own FX
  service.
- **`WorldBankApiConfig`** — the World Bank Open Data base URL. No auth
  required; override `baseUrl` only if proxying.

All three configs are plain data classes — bind one of each in your fork's
Koin module with the values you want.

## Step 3 — Generate Android keystores

```bash
scripts/white-label/keystore.sh generate
```

Creates `keystores/upload_keystore.keystore` (and the UPLOAD variant for Play
Console). The `keystores/` directory is `.gitignore`'d — values never reach
the public history. Use `scripts/white-label/keystore.sh encode-secrets` later to push
encoded keystores into GitHub Actions secrets for CI builds.

iOS keystore equivalents (Fastlane Match, `.p8` keys) — see
`scripts/ios/setup_ios_complete.sh` and the [Secrets Management Guide](../secrets/management.md).

## Step 4 — Populate secrets

> The legacy `.env.local.example` pattern was removed at `template_version:
> "2.6.0"` (fastlane-modernization epic). See
> [`deployment/BOOTSTRAP.md`](../../../../deployment/BOOTSTRAP.md) for the full Path A
> (manual) vs Path B (vault) walkthrough — this section is the short version.

### Path A (OSS fork — manual mode, default)

```bash
# 1. Fill in non-secret identity/metadata
cp -n gradle/fork.properties.template gradle/fork.properties
# Edit gradle/fork.properties and set at minimum:
#   apple.team.id, org.email, org.first.name, org.last.name, etc.

# 2. Drop secret values as per-value files under secrets/live/<platform>/
#    (see secrets/sample/ for the exact paths and placeholders)
#    e.g. secrets/live/apple/appstore/key_id, secrets/live/apple/match/.match_password
```

`secrets/live/` is gitignored; `gradle/fork.properties.template` is committed and shows which
keys to fill in. `secrets/sample/` is committed and carries placeholder files with magic
markers (`# CLAUDE-PLACEHOLDER` for text, `CLAUDE-PLHLD-v1\0` for binary) so it's
unambiguous which tree is safe to commit.

`build-secrets` resolves paths from `secrets/LAYOUT.yaml` (live-wins-else-sample) in the
order `ENV → secrets/live/<platform>/file → fork.properties → default`,
so there is no `source shared_keys.env` step.

### Path B (vault mode — framework maintainers)

```bash
/secrets request mifos_x_fred_api_key   # opens vault PR for the new alias
# (admin merges the PR)
/secrets pull                            # materializes to local.properties + secrets/live/<platform>/
```

Leave the key unset and the B7 Interest Rates screen renders an explicit
"FRED key not configured" empty state rather than crashing.

If your fork uses additional third-party APIs, add them via the same path:
- **Path A:** add the secret as a file under `secrets/live/<platform>/` and the non-secret
  metadata to `gradle/fork.properties`.
- **Path B:** run `/secrets request <alias>` to open a vault PR.

In both cases, wire the value through Koin (or BuildKonfig) the same way
`FredApiConfig` does.

## Step 5 — Smoke-test the demo build

```bash
./gradlew :cmp-android:installDemoDebug
```

Should install the demo Android variant on a connected device or emulator
and launch successfully. If the install fails:

1. Check Android Studio's `Build` panel — most failures are missing keystores
   (re-run Step 3) or stale Gradle caches (`./gradlew clean`).
2. Confirm `FRED_API_KEY` is set if the B7 screen is your smoke-test surface.
3. See [Troubleshooting Guide](../../../troubleshooting.md) for the
   common failure modes.

---

## Optional — Remove the demo showcase (`scripts/white-label/fork-init.sh --clean`)

The template ships a full Money-Toolkit demo (loans, bills, interest rates,
calculators, crypto, …) so a fresh clone runs a real app out of the box. When
you're ready to build your own product on the framework, remove the **entire**
demo in one command:

```bash
bash scripts/white-label/fork-init.sh --clean            # preview (dry-run) — prints exactly what will change
bash scripts/white-label/fork-init.sh --clean --apply    # perform the removal
```

This leaves a **minimal, compiling shell**: the app scaffold, navigation, DI,
the Room database, and the `home` / `profile` / `settings` tabs (home becomes an
empty `Scaffold` with a title bar + settings action for you to fill). Everything
demo is gone — 10 feature modules, all demo data/domain packages, and the demo
database schema (auto-reset to `VERSION = 1`, so a fresh fork starts clean).

> `--clean` never touches module namespaces or your fork identity (Step 1) — it
> removes demo **features** only.

### The convention (so *your* features stay removable + syncable)

The separation runs on two mechanical conventions. Follow them and your own
demo/sample code stays cleanly removable, while framework improvements keep
flowing in via [`sync-dirs`](../scripts/white-label/template-sync.md) without ever clobbering your demo:

- **Demo domain code lives under a `demo/` package segment** —
  `kpt.core.<module>.demo.<domain>` (e.g. `kpt.core.data.demo.banking`). The
  `**/demo/**` glob is what `--clean` deletes and what `sync-dirs` excludes.
  Framework code **never** references a `demo/` package outside a demo-marked block.
- **Demo edits in shared/central files are wrapped in `// demo:begin … // demo:end`
  markers** — in the `@Database`, the Koin modules, the store registry,
  `settings.gradle.kts`, and the home/nav shell. `--clean` strips these blocks and
  leaves the surrounding framework code valid and compiling.
- **Demo feature modules are `include(...)`d inside the `settings.gradle.kts`
  demo-marked block** — `--clean` deletes both the module directories and their includes.

A CI gate — `scripts/verify-demo-convention.sh`, wired into the **Quality Gate**
workflow — fails the build on any drift from these rules, so the `--clean` removal
stays safe as the codebase evolves. Run it locally any time:

```bash
bash scripts/verify-demo-convention.sh
```

---

## Beyond Day 1

Once Steps 1–5 are done you have a renamed, signed, running demo build. The
following customizations are typically Day-2+ work — pick what your fork
needs:

- **Icons + splash** — `cmp-android/src/main/res/mipmap-*/` and
  `cmp-ios/iosApp/Assets.xcassets/AppIcon.appiconset/`.
- **Push notifications** — wire FCM (Android) and APNs (iOS); register a
  notification handler in the consumer Koin module.
- **Deep links** — declare intent filters / universal-link entitlements in
  `AndroidManifest.xml` and `Info.plist`.
- **Store listings** — Play Console + App Store Connect metadata, screenshots,
  release notes.
- **Replace shipped features** — every demo feature is a working
  showcase of one framework pattern; remove what you don't want from
  `feature/` and `settings.gradle.kts`. See the
  [toolkit feature showcase table](../../../../CLAUDE.md#toolkit-feature-showcase) for
  which pattern each feature demonstrates.
- **Brand visuals (Store-driven states)** — extend `KptScreenStateDefaults`
  in `core/store/` to swap loading / error / empty visuals across every
  screen at once. See [`core/store/README.md`](../../README.md).
- **Domain model** — extend `core/model/banking/` (loans, bills) and
  `core/data/banking/` with your own entities; `feature/loans` + `feature/bills`
  read straight from the repository contracts.

---

## Pointers

- **`CLAUDE.md` (root)** — the central hub. Read this first if you're new.
- **`core-base/README.md`** — framework-shared layer overview; the `Kpt*`
  symbol catalogue.
- **`core/store/README.md`** — Store contract + screen-archetype taxonomy.
- **`docs/claude/onboarding.md`** — full onboarding walkthrough.
- **`docs/claude/deployment-playbook.md`** — pushing builds to Firebase,
  TestFlight, Play Store, App Store.

---

## G10 — Migrating an existing fork to the identity-block system

If your fork was created from the template **before** the `libs.versions.toml`
identity block was introduced, follow these steps once to adopt the new system.

### What changed

| Before (legacy) | After (identity block) |
|----------------|----------------------|
| Identity split across `gradle.properties`, `cmp-android/build.gradle.kts`, `cmp-ios/Configuration/Config.xcconfig`, `local.properties`, `settings.gradle.kts` | All identity in 5 lines of `gradle/libs.versions.toml` |
| `Config.xcconfig` used `BUNDLE_ID` / `APP_NAME` keys (no `TEAM_ID`) | `Config.xcconfig` uses `APP_BUNDLE_ID` / `APP_NAME` / `TEAM_ID` |
| Module `build.gradle.kts` files each declared an explicit `namespace` | `namespace` derived by convention plugin from `org.convention.BASE_MODULE_NAMESPACE` (`kpt`, framework-owned) + module path |
| `scripts/white-label/fork-init.sh` renamed Kotlin source packages | `scripts/white-label/fork-init.sh` only edits `libs.versions.toml` + calls `syncForkConfig` |

### Migration steps

1. **Pull the updated template** (via sync-dirs PR or manual merge of
   `build-logic/`, `cmp-android/build.gradle.kts`, `cmp-desktop/build.gradle.kts`,
   root `build.gradle.kts`, `settings.gradle.kts`, `scripts/white-label/fork-init.sh`).

2. **Add the identity block to your `gradle/libs.versions.toml`** — insert
   these six lines near the top of the `[versions]` section, replacing your
   fork's current values:

   ```toml
   appId            = "com.myapp.example"
   appDisplayName   = "My Awesome App"
   desktopAppName   = "MyAwesomeAppDesktop"
   projectName      = "my-awesome-app"
   iosTeamId        = "ABCDE12345"
   ```

3. **Remove old identity properties from `gradle.properties`** — delete
   `APP_ID_BASE`, `APP_NAME`, `APP_BUNDLE_DISPLAY_NAME`, `APP_BRAND_PREFIX`,
   `APP_VERSION_BASE` if they exist. (Version config lives elsewhere.)

4. **Run `./gradlew syncForkConfig`** — regenerates `Config.xcconfig`,
   `local.properties`, and `gradle.properties` from the catalog.

5. **Verify `cmp-ios/Configuration/Config.xcconfig`** — it should now contain
   `APP_BUNDLE_ID`, `APP_NAME`, and `TEAM_ID` with your fork's values.
   In Xcode, confirm the bundle ID resolves correctly
   (`$(APP_BUNDLE_ID)` in Build Settings → Product Bundle Identifier).

6. **Remove explicit `namespace` from library module `build.gradle.kts` files**
   — if your fork has `namespace = "com.myapp.feature.loans"` etc. in each
   module, remove them. The convention plugin in the updated `build-logic`
   derives every namespace from the framework-owned `BASE_MODULE_NAMESPACE` (`kpt`) + module path.
   Run `./gradlew :cmp-android:assembleDebug` to confirm no namespace conflicts.

7. **Commit** — the only files that should now carry your fork's package
   identity are `gradle/libs.versions.toml`, `cmp-ios/Configuration/Config.xcconfig`,
   `local.properties`, and `gradle.properties`.

After migration, your fork will receive all future `build-logic/` and
`cmp-*/build.gradle.kts` improvements via `sync-dirs` without overwriting
your package identity.
