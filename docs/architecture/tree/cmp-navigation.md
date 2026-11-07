# `cmp-navigation/`

> **Kind:** Kotlin Multiplatform module  
> **Measured:** 79 tracked files — 52× `.kt`, 23× `.xml`, 3× `.md`, 1× `.kts`

## Shape

| Path | Files |
|---|---:|
| `cmp-navigation/src/` | 76 |
| (files at the root) | 3 |

## Modules

| | |
|---|---|
| `cmp-navigation:src` | 52 Kotlin files |

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
