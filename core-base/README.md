# core-base

Framework-shared base modules — the template-neutral foundation every fork
inherits unchanged. Upgrades flow cleanly across template versions.

The submodules under `core-base/*` are:

| Module | Purpose |
|---|---|
| `analytics` | `AnalyticsHelper` interface + `NoOpAnalyticsHelper` / `StubAnalyticsHelper` / `FirebaseAnalyticsHelper` implementations + per-platform Koin DI wiring. (The higher-level `KptAnalyticsTracker` lives one layer up at `core/analytics/` — see "awaiting promotion" below.) |
| `common` | Pure-Kotlin utilities (no Compose / no DI) |
| `database` | Room base helpers and KMP database wiring |
| `datastore` | Multiplatform DataStore preferences |
| `designsystem` | Theme, motion, shared design primitives, shimmer |
| `network` | Ktor client base, interceptors, error types |
| `platform` | Platform-specific glue (expect/actual) |
| `security` | Crypto + secure-storage primitives |
| `store` | Offline-first Store contract (the framework's beating heart) |
| `ui` | Screen scaffolding, motion modifiers, submit flow, transition system |

**Do NOT add fork-specific or brand-specific symbols here.** Push that pressure
to `core/*` (the fork-extensible layer) — see the root `CLAUDE.md` for the
seam.

---

## Naming convention — `Kpt*`

The template uses **`Kpt*`** (Kotlin Project Template) as the brand-neutral
prefix for framework-shared symbols. Every symbol inside `core-base/*` — and
the template-shared parts of `core/*` — follows this convention:

> **The lists below are representative examples, not the complete surface.** The
> `Kpt*` surface is actively growing — at the time of writing, `core-base/` defines
> **45** Kpt* declarations and `core/` adds **10** more awaiting promotion. To
> enumerate the full set yourself, run:
>
> ```bash
> git grep -hE '^(object|class|interface|fun) Kpt[A-Z]' -- core-base/ core/ \
>   | grep -v '.deprecated.kt' | sort -u
> ```
>
> Keep this README in sync with reality when adding new `Kpt*` symbols — bump the
> count, and if a new symbol exemplifies a new category (e.g. layout primitives,
> theme builders), extend the buckets below rather than burying it.

### Symbols in `core-base/` (representative examples)

- **Theme (composables + builder DSL):** `KptTheme`
  (`core-base/designsystem/KptTheme.kt`), `KptMaterialTheme`
  (`core-base/designsystem/KptMaterialTheme.kt`),
  `KptThemeBuilder` / `KptColorSchemeBuilder` / `KptTypographyBuilder` /
  `KptShapesBuilder` / `KptSpacingBuilder` / `KptElevationBuilder`
  (`core-base/designsystem/theme/KptColorSchemeImpl.kt`) plus the
  `object KptTheme` accessor at the same location for `KptTheme.colorScheme` /
  `.typography` / `.shapes` / `.spacing` / `.elevation` reads.
- **Theme contracts (interfaces):** `KptComponent`, `KptThemeProvider`,
  `KptColorScheme`, `KptTypography`, `KptShapes`, `KptSpacing`, `KptElevation`
  (`core-base/designsystem/core/KptComponent.kt`).
- **Theme extensions + defaults:** `KptThemeExtensions.kt` (Material3 bridge
  fns + `paddingValues` / `cardElevation` helpers), `KptSpacingDefaults`,
  `KptElevationDefaults`, `KptAnimationSpecs`.
- **Layout primitives:** `KptStack`, `KptGrid`, `KptMasonryGrid`,
  `KptFlowRow`, `KptFlowColumn`, `KptSplitPane`, `KptSidebarLayout`,
  `KptResponsiveLayout` (`core-base/designsystem/layout/`).
- **Components:** `KptButton` / `KptOutlinedButton` / `KptTextButton`,
  `KptToastHost`, `KptShimmerLoadingBox`, `KptShimmerListItem`,
  `KptSlideTransition` (`core-base/designsystem/component/`).
- **App bars:** `KptTopAppBar` (+ size variants `KptSmallTopAppBar` /
  `KptCenterAlignedTopAppBar` / `KptMediumTopAppBar` / `KptLargeTopAppBar`),
  `KptSearchAppBar`, `KptProfileAppBar`, `KptSettingsAppBar`,
  `KptTopAppBarConfiguration` + `KptTopAppBarBuilder` DSL
  (`core-base/designsystem/component/KptTopAppBar.kt` +
  `core-base/designsystem/core/KptTopAppBarConfiguration.kt`).
- **Motion primitives:** `KptSharedAxis`, `KptFadeThrough`,
  `Modifier.kptListItemEnter`, `Modifier.kptRefreshingPulse`
  (`core-base/ui/motion/`).
- **Transitions:** `TransitionProviders.Kpt.*`, `RootTransitionProviders.Kpt.*`
  (`core-base/ui/util/Transition.kt`).

### Symbols currently at `core/*` awaiting promotion

These follow the same `Kpt*` convention but live one layer up while a
dependency on a fork-customizable type is unwound. They WILL move into
`core-base/*` in a future phase.

- **Theme (fork-branded wrapper):** `KptTheme`
  (`core/designsystem/src/commonMain/kotlin/org/mifos/core/designsystem/theme/KptTheme.kt`)
  — wraps the framework-shared `KptTheme` from `core-base/designsystem/` with
  the fork's Material color scheme + screen-state defaults. Two `KptTheme`s
  exist by design: the inner one (`core-base/designsystem/KptTheme.kt`) is the
  brand-neutral primitive; the outer one (`core/designsystem/theme/KptTheme.kt`)
  is the fork's entry point that wires `appScreenStateDefaults()` and the
  Material color scheme.
- **Analytics:** `KptAnalyticsTracker` (`core/analytics/KptAnalyticsTracker.kt`)
  — the higher-level tracker that consumes `core-base/analytics`'s
  `AnalyticsHelper` interface. Promotion deferred while the event-naming
  contract stabilizes.
- **Scaffold:** `KptScaffold` (`core/ui/scaffold/`) — pull-to-refresh +
  freshness-banner-aware screen scaffold.
- **Nav chrome:** `KptBottomBar`, `KptNavigationRail` (`core/ui/bottombar/`)
  — promotion deferred pending nav-graph contract.
- **Charts:** `KptSparkline`, `KptAreaChart`, `KptDonutChart`, `KptBarChart`,
  `KptCandlestick` (`core/designsystem/chart/`) — promotion deferred while
  `ChartTokens` / `MaterialTheme.finance` is unwound from fork theming.

### Symbols that intentionally keep domain names

Domain-specific types are NOT framework-neutral and intentionally do NOT take
the `Kpt*` prefix. Examples: `Loan`, `LoanRepository`, `BillReminder`,
`BillReminderScheduler`, `MacroIndicator`, `FredApiConfig`. They live under
`core/model/`, `core/data/`, `core/database/`, and fork apps freely extend
them.

---

## Neutral `Kpt*` naming (brand-prefixed aliases removed)

Every framework-shared symbol uses the brand-neutral `Kpt*` prefix. Earlier
brand-prefixed aliases (and their deprecated typealias shims) have been fully
removed — there is no brand-prefixed name left to migrate off. New code uses
`Kpt*` exclusively.

If you are porting an older fork that still references a brand-prefixed symbol,
map it to its `Kpt*` equivalent by hand (the rename was 1:1).

---

## Day-1 fork checklist

The minimum five steps to turn a fresh clone of this template into a branded,
running app. For the long-form walkthrough see
[`templates/FORK_QUICKSTART.md`](../docs/architecture/tree/app-profile/fork-quickstart.md).

1. **Rebrand identifiers** — edit the five `APP_*` keys in `gradle.properties`
   (`APP_ID_BASE`, `APP_NAME`, `APP_VERSION_BASE`, `APP_BUNDLE_DISPLAY_NAME`,
   `APP_BRAND_PREFIX`). See the **Fork branding** section of the root
   `CLAUDE.md` (Phase 10) for the consumer-file rename surface.

2. **Override the 4 customization-point Koin bindings** in your fork's app
   module (each ships a working stub today — swap the binding, no callers
   change):
   - `AuthProvider` — `NoOpAuthProvider` → your auth impl (Firebase Auth,
     OAuth, biometric, etc.).
   - `CrashReporter` — `ConsoleCrashReporter` → Firebase Crashlytics, Sentry,
     Bugsnag, or similar.
   - `AnalyticsHelper` — `NoOpAnalyticsHelper` → `FirebaseAnalyticsHelper`,
     Amplitude, Mixpanel, etc.
   - **Network config bindings** — `FredApiConfig`, `FrankfurterApiConfig`,
     `WorldBankApiConfig`: override the `baseUrl` (and `apiKey` for FRED) for
     your backend / proxy.

3. **Generate Android keystores** — `./keystore-manager.sh generate` creates
   the ORIGINAL + UPLOAD keystores under `keystores/` (gitignored).

4. **Populate `.env.local`** — `cp .env.local.example .env.local` and add
   `FRED_API_KEY=...` (free key from https://fred.stlouisfed.org/docs/api/api_key.html).

5. **Smoke-test** — `./gradlew :cmp-android:installDemoDebug` should install
   the demo build on a connected device / emulator.

That's the floor. Everything else (icons, splash, push, deep links, store
listings) layers on top — see the [Fork Quickstart](../docs/architecture/tree/app-profile/fork-quickstart.md)
for the expanded walkthrough.
