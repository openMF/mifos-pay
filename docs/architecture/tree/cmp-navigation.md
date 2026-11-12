# `cmp-navigation/`

> **Kind:** Kotlin Multiplatform module  
> **Measured:** 80 tracked files — 53× `.kt`, 23× `.xml`, 3× `.md`, 1× `.kts`

## Shape

| Path | Files |
|---|---:|
| `cmp-navigation/src/` | 77 |
| (files at the root) | 3 |

## Modules

| | |
|---|---|
| `cmp-navigation:src` | 53 Kotlin files |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-navigation/CONSUMPTION.md` | Consuming `cmp-navigation` in a fork |
| `cmp-navigation/README.md` | cmp-navigation |

<!-- tree-scaffold:end -->
## Significance

The app shell: the root nav graph, the bottom bar, and the `AppViewModel` that owns theme, locale and
screen-capture policy for the whole process. It sits above every feature because those decisions are
process-wide — a theme change has to reach the root `MaterialTheme`, a locale change has to reach the
root `LayoutDirection`, and no single screen owns either.

**`RootNavViewModel` makes one decision in one place:** splash → onboarding → auth → lock → app. Two
screens re-deriving that order is how an app ends up showing onboarding to a signed-in user.

**The registries are fork seams.** `ForkBackboneRegistry`, `ForkFeatureRegistry` and `ForkTabRegistry` are where a
fork contributes its own body, modules, destinations and tabs. The feature halves are DERIVED by
`build-logic`'s generators; the `Fork*Module` seams stay listed by hand because they are core-layer
seams, not features, and nothing under `feature/` declares them.

**Features meet only here.** A feature module never depends on another feature module (nor on
`core-base/`; it reaches `core-base` through the `core/` module that re-exports it). A feature whose
screens open only each other declares a `@FeatureDestination` graph in its `navigation` package and the
aggregate registers it. A screen that opens another feature's screen — or shows another feature's widget
— takes that link as a callback or a composable slot, and `ForkFeatureRegistry.featureDestinations`
wires it to the other feature's `navigateTo…` (the demo `loansGraph(navController, onAmortizationClick)`
and `cryptoGraph(navController, coinAction)` are the pattern).

**`ForkAppShell` owns the root.** `ComposeApp` renders `ForkAppShell.root` inside the theme; a fork with
its own root (its own login, lock and tab shell) renders it there instead of editing `ComposeApp`. The
platform locale switch reaches that root through `LocalAppLocaleHandler`, provided around it — so a fork
language picker can apply a locale the template's `LanguageConfig` does not list. A fork that keeps the
template's root but needs signed-out screens (sign-in, server setup) installs them through
`ForkAppShell.rootDestinations`, which `RootNavScreen` adds beside splash and onboarding — outside the
signed-in graph that `@FeatureDestination` registers onto.
