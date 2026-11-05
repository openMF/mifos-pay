# The demo showcase

The template ships a working app — loan tracking, bill reminders, interest rates, calculators,
country macro indicators. Those features are not filler and not a sample app bolted on the side.
**Each one is the canonical proof of a `core-base/store` archetype**, and together they are how the
template demonstrates that its own contracts hold end to end.

They are also **designed to be deleted**. A fork keeps them, brands them, or strips them with one
command — and the strip has to leave a building, coherent app behind. That requirement is what
shapes everything below.

## Why a fork should care

Two decisions depend on understanding this layer:

1. **Removing it.** `scripts/remove-demo.sh --apply` (also reachable as
   `scripts/white-label/fork-init.sh --clean`) must remove every trace. If the convention is broken
   anywhere, the strip leaves a module that does not configure, or deletes something the fork owns.
2. **Learning from it.** Before writing a new feature, the demo for the matching archetype is the
   reference implementation. `STORE_ARCHETYPES.yaml` maps archetype → showcase, and a health check
   fails the build if an archetype loses its last one — so the examples cannot silently rot.

## The three conventions

The strip is **marker-driven, not name-driven**. Nothing infers "this looks like a demo"; every
removable thing is declared. There are exactly three ways a thing is declared removable:

### 1. Demo domain code lives under a `demo/` package

`**/demo/**` packages are deleted wholesale — deleting a directory is unambiguous, whereas deleting
"files that look demo-ish" is a guess.

Measured on the template today, these packages live in the **backbone** features —
`feature/{home,profile,settings}` — which survive the strip while their demo content does not. That
is the case the convention exists for: a module that is half template and half demo cannot be
deleted or kept wholesale, so the demo half is quarantined into a subpackage.

The `core/**` and `core-base/**` modules carry **almost no demo content**: only `core/network` has
any, and it is fenced rather than packaged (convention 2). A fork stripping the demo should expect
`core/*` to be essentially untouched — each module guide states its own exposure under
**Demo showcase exposure**.

The inverse matters just as much — the **fork seams stay outside `demo/`** so the strip leaves them
standing. `core/{data,database,network}/**/di/Fork*Module.kt` are empty on the template and are
where a fork registers its own bindings; their demo counterparts (`**/demo/di/Demo*Module.kt`) go.

### 2. Demo entries in shared files are fenced

Where demo and framework content share a file, the demo half is wrapped:

```kotlin
// demo:begin
include(":feature:loans")
// demo:end
```

Nine files carry fences today:

| file | what is fenced |
|---|---|
| `settings.gradle.kts` | the demo feature `include()`s |
| `feature-deps.gradle.kts` | their dependency wiring |
| `core/network/build.gradle.kts` · `.../di/ForkNetworkModule.kt` | demo endpoint plumbing |
| `cmp-navigation/.../ForkShowcaseRegistry.kt` · `ForkBackboneRegistry.kt` | demo destinations |
| `feature/home/.../di/HomeModule.kt` | the demo dashboard's bindings |
| `sync/.../DataSyncWorker.kt` | demo sync jobs |
| `app-profile/migration-ledger.yaml` | demo schema history |

A fence that is opened and never closed, or demo code outside any fence, is the failure mode this
convention exists to prevent — and it is invisible until someone strips. `fork-init --verify`
(FV-6) checks both halves: no demo feature modules **and** no surviving `demo:begin` fences. A tree
that passes the module check and fails the fence check is a strip that stopped halfway.

### 3. Demo feature modules are whole directories

`feature/<demo>` modules are deleted outright. Their `include()` is fenced (convention 2) so the
Gradle graph stays consistent after removal.

## What the strip also does

Beyond removal, `remove-demo.sh` **resets** two things, because deleting alone would leave the app
broken rather than clean:

- **The database schema** is reset to a fresh-fork baseline. Demo entities leaving would otherwise
  strand migrations pointing at tables that no longer exist.
- **The app shell** — the demo-coupled home dashboard and nav — is swapped for a minimal
  placeholder, so the fork has a screen to start from instead of a hole.

## Ownership

`customization-surface.yaml` gives the demo layer its own ownership class. The rules are ordered and
first-match-wins, and the ordering is load-bearing: the `home/profile/settings` **backbone** is
declared **before** the demo rule, so those three survive the strip while every other
`feature/**` module is treated as demo/fork and follows its module. Demo modules are deliberately
**not** in `SYNC_DIRS` — a template sync never pushes them back into a fork that removed them.

## The archetype contract

Eight archetypes, each with at least one live showcase:

`NETWORK_WITH_CACHE` · `NETWORK_ONLY` · `CACHE_ONLY` · `OFFLINE_LOCAL_ONLY` · `MEMORY_ONLY` ·
`PERIODIC` · `LOAD_ONCE` · `MUTABLE`

`core/store/STORE_ARCHETYPES.yaml` is the machine-readable source of truth;
`scripts/product-health/checks/store-archetype-coverage.sh` fails the build if an archetype loses
its last showcase, or if a showcase stops calling its declared factory.

Two archetypes are **non-cache-first by definition** and must not be "fixed": `MEMORY_ONLY` has no
SourceOfTruth, so its cache dies with the process; `NETWORK_ONLY` is network-first with cache only
as a failure fallback. This is not hypothetical — it happened, the two guarding tests were
`assertTrue(true, …)` and passed, and the coverage gate exists because of it.

## See also

- [`store-architecture.md`](store-architecture.md) — the archetypes end to end
- [`customization-surface.md`](customization-surface.md) — ownership rules and their ordering
- [`../modules/core/store.md`](../tree/core/store.md) — `@StoreProvider` / `@CacheKey`, and why
  deleting a demo package takes its bindings with it
