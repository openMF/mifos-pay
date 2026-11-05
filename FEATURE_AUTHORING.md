# Authoring a feature end-to-end (keyed by `store_archetype`)

This is the in-template consumption contract: how to build a feature by composing the `core/**`
modules, from remote/local source all the way to the Compose screen. Pick the **row** that matches
your data behaviour (the `store_archetype`), then follow that module chain — each stage links the
module's own `CONSUMPTION.md`.

> **Registering** the feature (its routes, DI, tab, home body, startup hooks) is a separate, one-file
> step per surface — see the white-label extension seams in
> [`cmp-navigation/.../registry/README.md`](cmp-navigation/src/commonMain/kotlin/cmp/navigation/registry/README.md).
> This document is about a feature's *data flow*; that one is about *wiring it into the app shell*.

> **Source of truth.** The full archetype decision matrix + framework-API taxonomy is
> `training-layer/instructions/stream-first/latest/CORE_STORE.md` (consumed by `/kmp-implement`).
> This document is the always-present, syncable in-repo summary of *what to call*. `/kmp-implement`
> drift-detects this doc against the module source + corpus and updates it (draft PR) on change.

## The universal read path

Every read screen is the SAME shape; the archetype only changes which **store factory** and which
**source(s)** you wire:

```
DTO (core/network) ──▶ core/network (Ktorfit API + config)
                          │
                          ▼
core/database (Room @Entity/@Dao, the SourceOfTruth)  ──▶ core/store (StoreFactory.create* → Store<Key, Domain>)
                                                              │  emits core/model domain types
                                                              ▼
core/data (Repository → store.asScreenStream → ScreenDataStream<T>)
                                                              │  .state : Flow<ScreenState<T>>
                                                              ▼
ViewModel (BaseViewModel, exposes StateFlow<ScreenState<UiState>>)
                                                              │
                                                              ▼
Compose screen: ScreenContent<T>(state) { data -> ... } inside KptScaffold (core/ui + core-base/ui)
```

Writes add `core/domain` (pure calc on inputs) and, for offline/remote writes, a `SubmitHandler` /
`MutationScreenContent` (`core-base/store` + `core-base/ui`).

## Archetype → module chain

| `store_archetype` | store factory (`core/store`) | recipe (module chain) |
|---|---|---|
| **NETWORK_WITH_CACHE** | `StoreFactory.createStore(fetcher, sourceOfTruth)` | DTO → [core/network](core/network/CONSUMPTION.md) (Ktorfit `Fetcher`) → [core/database](core/database/CONSUMPTION.md) (Room SoT) → [core/store](core/store/CONSUMPTION.md) `createStore` → [core/data](core/data/CONSUMPTION.md) `store.asScreenStream` → ViewModel(`.state`) → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **MUTABLE** | `StoreFactory.createMutableStore(fetcher, sourceOfTruth, updater, bookkeeper)` | DTO → [core/network](core/network/CONSUMPTION.md) (writable API + `Updater`) → [core/database](core/database/CONSUMPTION.md) (SoT + `BookkeeperDao`) → [core/store](core/store/CONSUMPTION.md) `createMutableStore` → [core/data](core/data/CONSUMPTION.md) (`store.write` + read stream) → `SubmitHandler` ([core/domain](core/domain/CONSUMPTION.md) for input calc) → ViewModel → `MutationScreenContent` |
| **OFFLINE_LOCAL_ONLY** | `StoreFactory.createOfflineStore(sourceOfTruth)` | [core/model](core/model/CONSUMPTION.md) → [core/database](core/database/CONSUMPTION.md) (Room `@Dao`, no fetcher) → [core/store](core/store/CONSUMPTION.md) `createOfflineStore` → [core/data](core/data/CONSUMPTION.md) (`asScreenStream` + `dao.upsert`) → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **NETWORK_ONLY** | `StoreFactory.createStore(...)` + `FetchPolicy` (skip SoT) | DTO → [core/network](core/network/CONSUMPTION.md) (`Fetcher`) → [core/store](core/store/CONSUMPTION.md) `createStore` (no persisted SoT / fetch-always policy) → [core/data](core/data/CONSUMPTION.md) `asScreenStream` → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **CACHE_ONLY** | `StoreFactory.createStore(...)` reading SoT only | [core/database](core/database/CONSUMPTION.md) (Room SoT) → [core/store](core/store/CONSUMPTION.md) `createStore` (`FetchPolicy` cache-only, no network) → [core/data](core/data/CONSUMPTION.md) `asScreenStream` → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **PERIODIC** | `StoreFactory.createStore(...)` + `@StoreProvider(ttl = …)` | DTO → [core/network](core/network/CONSUMPTION.md) (`Fetcher`) → [core/database](core/database/CONSUMPTION.md) (SoT + `FetchedAtDao` for TTL) → [core/store](core/store/CONSUMPTION.md) `createStore` (registry TTL drives refresh) → [core/data](core/data/CONSUMPTION.md) `asScreenStream` → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **MEMORY_ONLY** | `StoreFactory.createMemoryStore(fetcher)` | DTO → [core/network](core/network/CONSUMPTION.md) (`Fetcher`) → [core/store](core/store/CONSUMPTION.md) `createMemoryStore` (in-memory, no SoT) → [core/data](core/data/CONSUMPTION.md) `asScreenStream` → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |
| **LOAD_ONCE** | `StoreFactory.createStore(...)` + `asLoadOnceStream` | [core/model](core/model/CONSUMPTION.md) / DTO → [core/network](core/network/CONSUMPTION.md) or [core/domain](core/domain/CONSUMPTION.md) → [core/store](core/store/CONSUMPTION.md) `createStore` → [core/data](core/data/CONSUMPTION.md) `store.asLoadOnceStream(...)` (fetch once, no live refresh) → ViewModel → `ScreenContent` ([core/ui](core/ui/CONSUMPTION.md)) |

## Pure-compute features (no store)

A calculator/tool feature with no persisted or remote data skips the store chain entirely:

```
User input → ViewModel → core/domain (EmiCalculator / AffordabilityCalculator / CalculateEmiUseCase)
           → fold result into UiState → ScreenContent (core/ui)
```

See [core/domain/CONSUMPTION.md](core/domain/CONSUMPTION.md) (canonical: feature/emi-calculator, feature/calculators).

## Wiring checklist (every archetype)

1. Domain type in [core/model](core/model/CONSUMPTION.md); DTO in [core/network](core/network/CONSUMPTION.md); `@Entity`/`@Dao` in [core/database](core/database/CONSUMPTION.md) when it persists.
2. `provide<Name>Store` annotated `@StoreProvider(id = …, logout = …)` — the qualifier, TTL, cache keys,
   Koin binding and logout registration are all GENERATED from it; there is no wiring step ([core/store](core/store/CONSUMPTION.md)).
3. Repository interface + `Impl` + `RepositoryModule` binding, exposing a `ScreenDataStream<T>` ([core/data](core/data/CONSUMPTION.md)).
4. `BaseViewModel` exposing `StateFlow<ScreenState<UiState>>` from `stream.state`.
5. Screen renders via `ScreenContent` inside `KptScaffold`, nav entries as `NavigationItem` ([core/ui](core/ui/CONSUMPTION.md)).
6. Preferences (settings/session flags) go through [core/datastore](core/datastore/CONSUMPTION.md), NOT the store/database chain.
