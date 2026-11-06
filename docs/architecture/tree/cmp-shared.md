# `cmp-shared/`

> **Kind:** Kotlin Multiplatform module  
> **Measured:** 8 tracked files — 5× `.kt`, 2× `.md`, 1× `.kts`

## Shape

| Path | Files |
|---|---:|
| `cmp-shared/src/` | 5 |
| (files at the root) | 3 |

## Modules

| | |
|---|---|
| `cmp-shared:src` | 5 Kotlin files |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-shared/CONSUMPTION.md` | Consuming `cmp-shared` in a fork |
| `cmp-shared/README.md` |  |

<!-- tree-scaffold:end -->
## Significance

The one place every platform entry point meets: `SharedApp()` is the root composable each target
renders, and `initKoin()` the graph each one starts. Five entry points, one app.

It is deliberately thin. Anything that looks like it belongs here — theme, locale, nav — lives in
`cmp-navigation` instead, because those are decisions the app shell owns and a per-platform wrapper
must not be able to diverge on.
