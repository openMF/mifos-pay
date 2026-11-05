# White-label registries — the fork extension seams

This package (`cmp.navigation.registry`) is the **white-label extension surface** of the template.
It is the small set of `owner: fork` files a consumer edits to make the app theirs. The template's
navigation/DI/app-entry infrastructure only ever **reads** from these registries — it never hardcodes
feature, tab, dashboard, or startup wiring. So a template sync (`/kmp-project-template-sync`,
`scripts/white-label/sync-dirs.sh`) full-copies the infrastructure while your registrations survive
untouched.

Everything here is **commonMain**. A fork wires its app once, here, and every platform
(Android / iOS / Desktop / Web) picks it up — there is no per-platform app-class to edit. See the
[ownership contract](../../../../../../../docs/architecture/cross-cutting/customization-surface.md) for how these
files are classified `owner: fork`.

## The seams

| Registry | What a fork controls | Read by (template infra) |
|---|---|---|
| [`ForkFeatureRegistry`](ForkFeatureRegistry.kt) | Feature **routes + Koin modules** | `di/KoinModules.kt`, `authenticated/AuthenticatedNavigation.kt` |
| [`ForkBackboneRegistry`](ForkBackboneRegistry.kt) | The **home-tab body** (default: the demo dashboard) | `feature/home`'s `HomeScreen` shell (via the navbar graph) |
| [`ForkTabRegistry`](ForkTabRegistry.kt) | The **bottom-nav tabs** (backbone Home/Profile + extras) | `authenticatednavbar/AuthenticatedNavbarNavigationScreen.kt` |
| [`ForkInitializers`](ForkInitializers.kt) | **App-startup hooks** run after Koin init | `cmp-shared/.../KoinExt.kt#initKoin` (commonMain, every platform) |
| [`ForkAppShell`](ForkAppShell.kt) | The **app root**: forced theme mode, an app-wide wrapper (SDK init, `CompositionLocalProvider`s), the root content | `ComposeApp.kt` |

## How each seam works

### `ForkFeatureRegistry` — features (routes + DI)
A feature contributes two things: a Koin `Module` and a set of nav destinations.

```kotlin
object ForkFeatureRegistry {
    val featureKoinModules: List<Module> = listOf(MyFeatureModule, /* … */)
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        myFeatureGraph(navController)
    }
}
```

`di/KoinModules.kt` installs `featureKoinModules`; `AuthenticatedNavigation` invokes
`featureDestinations` into the authenticated graph. Add a feature entirely in **fork-owned** files:
1. `include(":feature:my-feature")` in [`settings.local.gradle.kts`](../../../../../../../settings.local.gradle.kts) (the settings seam) — **not** `settings.gradle.kts`.
2. Its Gradle dependency in [`feature-deps.gradle.kts`](../../../../../../../feature-deps.gradle.kts) (the build-dep seam) — **not** `cmp-navigation/build.gradle.kts` (which `apply(from = …)`s it).
3. Its module + graph in `ForkFeatureRegistry` here.

None of the template shell/build/settings files are touched — so a template sync full-copies them.

### `ForkBackboneRegistry` — the home body
The home tab is a framework-owned shell (top bar + settings action) with a fork-owned **body**. The shell
carries zero demo imports; it just renders whatever `homeBody` provides.

```kotlin
object ForkBackboneRegistry {
    val homeBody: @Composable (NavController) -> Unit = { navController ->
        MyHomeDashboard(onOpenX = { navController.navigateToX() })
    }
}
```

This is the seam that collapsed the old **12 hardcoded `navigateToX` demo lambdas** that used to thread
through five template layers (`AuthenticatedNavigation` → navbar graph → navbar screen → `homeGraph` →
`HomeScreen`). All of that home wiring now lives in this one fork-owned place; the shell forwards an opaque
`homeBody`.

### `ForkTabRegistry` — bottom-nav tabs
The navbar renders `ForkTabRegistry.tabs` generically. Home + Profile are the always-present backbone shell
tabs; a fork appends its own via `extraTabs` — no edit to a sealed class + item list + click `when` +
`NavHost`.

```kotlin
object ForkTabRegistry {
    val extraTabs: List<NavigationItem> = listOf(MyTab)
    val tabs: List<NavigationItem> = buildList { add(HomeTab); add(ProfileTab); addAll(extraTabs) }
}
```

### `ForkInitializers` — startup hooks
A fork's one-time startup work (analytics, crash reporting, remote-config, feature-flag warmup) without
editing any platform app class:

```kotlin
object ForkInitializers {
    val onAppStart: List<() -> Unit> = listOf({ MyAnalytics.init() })
}
```

`cmp-shared`'s commonMain `initKoin` calls `ForkInitializers.runAll()` once, after Koin starts — on **every**
platform. Koin is already started, so a hook may resolve dependencies.

### `ForkAppShell` — the app root

What 9 of 21 forks used to change by editing the template's `ComposeApp.kt` / `SharedApp.kt`:
`darkThemeOverride` / `dynamicColorOverride` (`null` follows the user's setting), `wrap` (runs around the
whole app, outside the theme) and `root` (default: `RootNavScreen`). Brand colours are not here — they
live in `core/designsystem/.../theme/ForkTheme.kt`.

## The `demo:begin` / `demo:end` fences and `--clean`

The template ships each seam pre-populated with its **demo** set (the Money-Toolkit showcase) as a working
default, fenced with `// demo:begin` / `// demo:end`. The customizer's `--clean` deletes the fork-owned
`**/demo/**` packages and empties those fenced blocks together, leaving each seam as an empty default
(`listOf()`, `{ }`) for a fork to fill — the backbone shells (home top bar, Home/Profile tabs, app entry
points) survive unchanged.

## Rules of thumb

- **commonMain first.** Never wire a shared seam into `cmp-android`/`cmp-ios` or a `*/src/{androidMain,
  iosMain,desktopMain}/**` app class — that runs on one platform only. If the logic is not genuinely
  platform-specific, it belongs in commonMain (this package, or `cmp-shared`'s `initKoin`).
- **Edit the registry, not the shell.** If you find yourself editing a template infra file to add a
  feature/tab/dashboard/startup step, there is a seam for it here instead.
- **Keep it `owner: fork`.** These files must stay classified `owner: fork` in
  `customization-surface.yaml`, or a sync will overwrite your registrations.

See also: [`FEATURE_AUTHORING.md`](../../../../../../../FEATURE_AUTHORING.md) (how to author a feature's
data flow end-to-end by `store_archetype`) and each `core/*/CONSUMPTION.md`.
