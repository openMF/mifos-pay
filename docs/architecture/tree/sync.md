# `sync/`

> **Kind:** Kotlin Multiplatform module  
> **Measured:** 16 tracked files — 13× `.kt`, 2× `.md`, 1× `.kts`

## Shape

| Path | Files |
|---|---:|
| `sync/src/` | 13 |
| (files at the root) | 3 |

## Modules

| | |
|---|---|
| `sync:src` | 13 Kotlin files |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `sync/DEVELOPMENT.md` | `sync/` — DEVELOPMENT |
| `sync/README.md` | sync/ |

<!-- tree-scaffold:end -->
## Significance

Background work, wrapped so the app does not care which platform scheduler runs it. `WorkScheduler` is
the seam; `WorkMode` (foreground vs background) collapses to a single mode off Android, where the
distinction does not exist.

**`WorkStatus` is deliberately smaller than the platform's state machine** — it carries only the
statuses a caller can act on. Exposing the full `WorkInfo.State` would leak scheduler detail into
feature code and make the surface impossible to implement identically on every target.
