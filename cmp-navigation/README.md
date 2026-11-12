# cmp-navigation

The app-shell's DI aggregator + root navigation graph. Every platform entry point
(`cmp-android`, `cmp-desktop`, `cmp-web`, `cmp-ios`) reaches the app through this module via
`cmp-shared`'s `SharedApp` → `ComposeApp` — there is no per-platform nav graph.

## Key types

- **`ComposeApp`** (`ComposeApp.kt`) — the root `@Composable`. Wraps `RootNavScreen` in `KptTheme`
  and forwards `AppViewModel` state (dark theme, dynamic color, screen-capture) to the platform
  callbacks passed down from `SharedApp`.
- **`AppViewModel`** / `AppState` / `AppEvent` / `AppAction` — app-root MVI: observes
  `UserDataRepository` (theme, dynamic color, screen-capture, language) and emits
  `AppEvent.Recreate` / `UpdateAppTheme` / `UpdateAppLocale` for the platform shell to act on.
- **`RootNavScreen`** / `RootNavViewModel` (`rootnav/`) — splash → authenticated graph gate.
- **`AuthenticatedNavigation`** (`authenticated/`) — the merge-owned graph shell; wires the bottom-nav
  (`authenticatedNavbarGraph`), then invokes the three registry seams below. Carries zero direct
  feature imports.
- **`KoinModules`** (`di/KoinModules.kt`) — `allModules`, the full app DI graph (Security, Data,
  Database, Firebase, Datastore, feature modules incl. `ForkFeatureRegistry.featureKoinModules`, App,
  Sync). Installed once by `cmp-shared`'s `initKoin()`.
- **Registries** (`registry/`) — `ForkFeatureRegistry`, `ForkBackboneRegistry`, `ForkTabRegistry`,
  `ForkInitializers`, `ForkShowcaseRegistry` — the fork's white-label extension seams. See
  [`registry/README.md`](src/commonMain/kotlin/cmp/navigation/registry/README.md) for the full
  contract.
- **`KptRootScaffold`** / `RememberKptNavController` (`ui/`) — the app-root Scaffold + `NavController`
  factory shared across platforms.

## How it fits

`cmp-shared`'s `SharedApp` composable calls `ComposeApp` directly; `KoinExt.kt#initKoin` installs
`KoinModules.allModules`. cmp-navigation depends on every `core/*` module (data, database, network,
model, common, datastore, firebase, platform) plus `core-base/security` (the DI aggregator's one
sanctioned `core-base` exception) and the always-present backbone features (`feature/home`,
`feature/profile`, `feature/settings`). Fork features attach via `feature-deps.gradle.kts` (applied
at the bottom of `build.gradle.kts`) + the `ForkFeatureRegistry` seam — never as a direct dependency
here.

See also: [`FEATURE_AUTHORING.md`](../FEATURE_AUTHORING.md),
[`docs/architecture/cross-cutting/customization-surface.md`](../docs/architecture/cross-cutting/customization-surface.md).
