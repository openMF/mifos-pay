# Consuming `cmp-navigation` in a fork

`cmp-navigation` is template-owned infrastructure (DI aggregation + root nav graph) — a fork does
**not** edit `ComposeApp.kt`, `AuthenticatedNavigation.kt`, or `KoinModules.kt` directly. Instead it
extends the app through the four **registry** seams in
[`registry/`](src/commonMain/kotlin/cmp/navigation/registry/README.md), which the template infra
only ever reads from:

| Seam | Add | Read by |
|---|---|---|
| `ForkFeatureRegistry` | your feature's Koin module + nav destinations | `KoinModules`, `AuthenticatedNavigation` |
| `ForkBackboneRegistry` | the home-tab body (default: demo dashboard) | the navbar graph |
| `ForkTabRegistry` | extra bottom-nav tabs | `AuthenticatedNavbarNavigationScreen` |
| `ForkInitializers` | app-startup hooks (analytics, crash reporting, …) | `cmp-shared`'s `initKoin` |

## Adding a feature

1. `include(":feature:my-feature")` in `settings.local.gradle.kts` (fork-owned settings seam).
2. Its Gradle dependency in `feature-deps.gradle.kts` (fork-owned build-dep seam — **not**
   `cmp-navigation/build.gradle.kts`, which `apply(from = …)`s it).
3. Its module + nav graph entries in `ForkFeatureRegistry.featureKoinModules` /
   `featureDestinations`.

None of that touches this module's own source, so a template sync (`/kmp-project-template-sync`)
full-copies `cmp-navigation` while your registrations survive untouched.

## What to leave alone

`ComposeApp`, `AppViewModel`, `RootNavScreen`/`RootNavViewModel`, `AuthenticatedNavigation`, and
`KoinModules` are template shell files — the backbone (Home/Profile tabs, splash, settings) is
wired once here for every platform. If you find yourself editing one of these to add a
feature/tab/dashboard/startup step, there is a registry seam for it instead (see the table above).

Full mechanics + code samples: [`registry/README.md`](src/commonMain/kotlin/cmp/navigation/registry/README.md).

Symbols: ComposeApp, AppViewModel, RootNavScreen, RootNavViewModel, AuthenticatedNavigation, KoinModules, ForkFeatureRegistry, ForkBackboneRegistry, ForkTabRegistry, ForkInitializers, ForkShowcaseRegistry
