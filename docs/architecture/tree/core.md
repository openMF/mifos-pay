# `core/`

> **Kind:** Kotlin Multiplatform module group  
> **Measured:** 873 tracked files — 714× `.kt`, 34× `.md`, 34× `.xml`, 24× `.kts`, 17× `.json`

## Shape

| Path | Files |
|---|---:|
| `core/model/` | 163 |
| `core/data/` | 140 |
| `core/network/` | 97 |
| `core/designsystem/` | 96 |
| `core/ui/` | 87 |
| `core/database/` | 84 |
| `core/store/` | 68 |
| `core/common/` | 60 |
| `core/datastore/` | 41 |
| `core/firebase/` | 17 |
| `core/domain/` | 12 |
| `core/platform/` | 8 |

## Modules

| | |
|---|---|
| `core:common` | 51 Kotlin files — [guide](core/common.md) |
| `core:data` | 125 Kotlin files — [guide](core/data.md) |
| `core:database` | 70 Kotlin files — [guide](core/database.md) |
| `core:datastore` | 32 Kotlin files — [guide](core/datastore.md) |
| `core:designsystem` | 77 Kotlin files — [guide](core/designsystem.md) |
| `core:domain` | 2 Kotlin files — [guide](core/domain.md) |
| `core:firebase` | 9 Kotlin files — [guide](core/firebase.md) |
| `core:model` | 154 Kotlin files — [guide](core/model.md) |
| `core:network` | 80 Kotlin files — [guide](core/network.md) |
| `core:platform` | 2 Kotlin files — [guide](core/platform.md) |
| `core:store` | 42 Kotlin files — [guide](core/store.md) |
| `core:ui` | 70 Kotlin files — [guide](core/ui.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `core/common/CONSUMPTION.md` | Consuming `core/common` in a feature |
| `core/common/README.md` | :core:common module |
| `core/data/CONSUMPTION.md` | Consuming `core/data` in a feature |
| `core/data/README.md` |  |
| `core/database/CONSUMPTION.md` | Consuming `core/database` in a feature |
| `core/datastore/CONSUMPTION.md` | Consuming `core/datastore` in a feature |
| `core/datastore/README.md` |  |
| `core/designsystem/CONSUMPTION.md` | Consuming `core/designsystem` in a feature |
| `core/designsystem/README.md` |  |
| `core/domain/CONSUMPTION.md` | Consuming `core/domain` in a feature |
| `core/domain/README.md` |  |
| `core/firebase/CONSUMPTION.md` | Consuming `core/firebase` in a feature |
| `core/firebase/README.md` | :core:firebase module |
| `core/model/CONSUMPTION.md` | Consuming `core/model` in a feature |
| `core/model/README.md` |  |
| `core/network/CONSUMPTION.md` | Consuming `core/network` in a feature |
| `core/network/README.md` |  |
| `core/platform/CONSUMPTION.md` | Consuming `core/platform` in a feature |
| `core/platform/README.md` |  |
| `core/store/CONSUMPTION.md` | Consuming `core/store` in a feature |
| `core/store/README.md` | `core/store` — Consumer Customization Seam |
| `core/ui/CONSUMPTION.md` | Consuming `core/ui` in a feature |
| `core/ui/README.md` |  |

## Guides

_Authored in depth, living in this area's own directory._

- [core/analytics](core/analytics.md)
- [Store Implementation Guide](core/store-implementation.md)

## Deeper reading

_This page is the map; these are the depth. Both are authored, and linking them is what
stops two descriptions of one thing from drifting apart._

- [Customization surface](../../architecture/cross-cutting/customization-surface.md)
- [Style guide](../../architecture/cross-cutting/style-guide.md)

<!-- tree-scaffold:end -->
## Significance

The fork's own data and design layer, and the seam every feature reads through. It is the answer to
"where does my project's code go" — `core/model` for domain types, `core/network` for API clients,
`core/database` for Room entities and DAOs, `core/store` for the Store5 wiring, `core/data` for the
repositories a feature consumes, `core/designsystem` for brand tokens.

**The dependency order is the architecture.** `network` and `database` and `datastore` sit below
`store`; `store` is consumed by `data`; `data` is what a feature injects. A repository that reads a
DAO directly while its writes go through the store is the split-read defect product-health's Store5
checks exist to catch — the layers are not stylistic.

**What a fork touches:** all of it. `core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt` brands every empty and
error state in one place, `KptErrorMapper` maps domain errors to copy, and a `@StoreProvider`
annotation is the whole registration — the KSP processors in `tools/` generate the registry, ids,
cache keys, Koin binding and logout purge, so there is no registry to edit and no DI module to
remember.

### Per-module detail

Each module has its own guide with a generated API reference:
[`docs/architecture/modules/core/`](../modules/core/).
