# `core-base/`

> **Kind:** Kotlin Multiplatform module group  
> **Measured:** 507 tracked files — 425× `.kt`, 28× `.md`, 22× `.xml`, 13× `.kts`, 6× `.gitignore`

## Shape

| Path | Files |
|---|---:|
| `core-base/store/` | 116 |
| `core-base/ui/` | 110 |
| `core-base/security/` | 47 |
| `core-base/designsystem/` | 45 |
| `core-base/network/` | 37 |
| `core-base/database/` | 36 |
| `core-base/platform/` | 30 |
| `core-base/crypto/` | 23 |
| `core-base/datastore/` | 22 |
| `core-base/common/` | 16 |
| `core-base/data/` | 12 |
| `core-base/observability/` | 8 |
| `core-base/firebase/` | 4 |
| (files at the root) | 1 |

## Modules

| | |
|---|---|
| `core-base:common` | 10 Kotlin files — [guide](core-base/common.md) |
| `core-base:crypto` | 22 Kotlin files — [guide](core-base/crypto.md) |
| `core-base:data` | 9 Kotlin files — [guide](core-base/data.md) |
| `core-base:database` | 27 Kotlin files — [guide](core-base/database.md) |
| `core-base:datastore` | 19 Kotlin files — [guide](core-base/datastore.md) |
| `core-base:designsystem` | 41 Kotlin files — [guide](core-base/designsystem.md) |
| `core-base:firebase` | 1 Kotlin files — [guide](core-base/firebase.md) |
| `core-base:network` | 32 Kotlin files — [guide](core-base/network.md) |
| `core-base:observability` | 4 Kotlin files — [guide](core-base/observability.md) |
| `core-base:platform` | 25 Kotlin files — [guide](core-base/platform.md) |
| `core-base:security` | 43 Kotlin files — [guide](core-base/security.md) |
| `core-base:store` | 112 Kotlin files — [guide](core-base/store.md) |
| `core-base:ui` | 80 Kotlin files — [guide](core-base/ui.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `core-base/README.md` | core-base |
| `core-base/common/CONSUMPTION.md` | Consuming `core-base/common` in a fork |
| `core-base/common/README.md` | :core:common module |
| `core-base/data/CONSUMPTION.md` | Consuming `core-base/data` in a fork |
| `core-base/data/README.md` |  |
| `core-base/database/CONSUMPTION.md` | Consuming `core-base/database` in a fork |
| `core-base/database/README.md` | Core Base Database Module |
| `core-base/datastore/CONSUMPTION.md` | Consuming `core-base/datastore` in a fork |
| `core-base/datastore/README.md` |  |
| `core-base/designsystem/CONSUMPTION.md` | Consuming `core-base/designsystem` in a fork |
| `core-base/designsystem/README.md` | KPT Design System |
| `core-base/firebase/CONSUMPTION.md` | Consuming `core-base/firebase` in a fork |
| `core-base/firebase/README.md` |  |
| `core-base/network/CONSUMPTION.md` | Consuming `core-base/network` |
| `core-base/network/README.md` | Ktorfit Networking Module Setup |
| `core-base/observability/CONSUMPTION.md` | Consuming `core-base/observability` |
| `core-base/observability/README.md` | `core-base/observability` |
| `core-base/platform/CONSUMPTION.md` | Consuming `core-base/platform` |
| `core-base/platform/README.md` | Platform Module Documentation |
| `core-base/security/CONSUMPTION.md` | Consuming `core-base/security` |
| `core-base/security/README.md` | `core-base/security` |
| `core-base/store/CONSUMPTION.md` | Consuming `core-base/store` (from `core/store`) |
| `core-base/store/DEVELOPMENT.md` | `core-base/store` — DEVELOPMENT |
| `core-base/store/README.md` | `core-base/store` — Framework-Shared State Infrastructure |
| `core-base/ui/CONSUMPTION.md` | Consuming `core-base/ui` |
| `core-base/ui/MOTION.md` | Motion — core-base/ui |
| `core-base/ui/README.md` | Documentation: core-base/ui Module |

## Guides

_Authored in depth, living in this area's own directory._

- [core-base/analytics](core-base/analytics.md)
- [NetworkMonitor contract — operator guide](core-base/network-monitor-contract.md)
- [RetryPolicy + outbox retry-backoff (Phase 05 deferred work)](core-base/retry-policy.md)

## Deeper reading

_This page is the map; these are the depth. Both are authored, and linking them is what
stops two descriptions of one thing from drifting apart._

- [Store architecture](../../architecture/cross-cutting/store-architecture.md)
- [Store + data API reference](../../architecture/cross-cutting/store-data-api.md)
- [Source-set hierarchy](../../architecture/cross-cutting/source-set-hierarchy.md)

<!-- tree-scaffold:end -->
## Significance

Framework-shared code, **read-only to a fork**. This is the half of the toolkit that upgrades cleanly
across template versions, and it holds the machinery a screen should never re-implement:
`core-base/store` decides every state transition (loading / offline / captive-portal / empty / error /
content, plus freshness), `core-base/ui` owns the scaffolds and the paging footer, `core-base/crypto`
and `core-base/security` hold the platform crypto seams.

**The rule that keeps it upgradable:** do not modify it. Fork pressure goes to `core/`, which is why
`core/store` exists as a thin branding seam over `core-base/store`. A change made here in one project
is drift — every other project keeps the defect and the next sync fights the edit. When a real bug is
found, the fix flows upstream as a PR rather than landing locally.

**Read-only to generators, but still taught.** `/implement`'s codegen never writes here, yet every
generator is trained on these APIs — being read-only is precisely why its documentation has to be
right: a generator that misreads `asScreenStream` produces a screen that compiles and mishandles
offline.

### Per-module detail

Each module has its own guide with a generated API reference:
[`docs/architecture/modules/core-base/`](../modules/core-base/).
