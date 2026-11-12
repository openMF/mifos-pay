# `core-base/store` — DEVELOPMENT

Internal-contributor guide to the Store5 factory primitives module (91 Kotlin files,
`commonMain`/`commonTest` only — pure Kotlin, no platform actuals). For how a **fork**
consumes this module (branding hooks, `config/KptScreenStateDefaults`, `@StoreProvider`-declared stores), see
[`../../core/store/CONSUMPTION.md`](../../core/store/CONSUMPTION.md) — this doc does not
repeat that content. Architecture reference: [`docs/architecture/cross-cutting/store-data-api.md`](../../docs/architecture/cross-cutting/store-data-api.md);
worked examples: [`docs/claude/store-implementation.md`](../../docs/architecture/tree/core/store-implementation.md).

## Purpose

`core-base/store` is the framework-shared, **do-not-fork** layer beneath every project's
`core/store`. It wraps Store5 (`org.mobilenativefoundation.store:store5`) into:

- **`StoreFactory`** (`infra/StoreFactory.kt`) — `createStore` (network+cache), `createMemoryStore`
  (fetcher-only, in-memory), `createOfflineStore` (SourceOfTruth-only, no fetcher — the
  `OFFLINE_LOCAL_ONLY` archetype), `createMutableStore` (read+write+`Bookkeeper` offline sync),
  and `createScreenWithMutation` (fused read+write seam, see `combine/`).
- **Screen state plumbing** (`screen/`) — `ScreenDataStream`, `ScreenState`, `FetchPolicy`,
  `DecisionEngine` (pure `(StoreData, NetworkStatus, FetchPolicy) -> ScreenState` mapping),
  `LoadOnceStream`, `StoreDataMapper`/`StoreResponseMapper`.
- **Write-side infra** (`submit/`) — `SubmitHandler`, `DraftSubmitHandler`, `SubmitOutbox`,
  `OfflineSubmitSyncer`, `RetryPolicy`/`RetryOnNetworkStatus`, `BatchSubmitHandler`, `IdempotencyKey`.
- **Cache/TTL/invalidation** (`infra/`) — `StoreCacheManager`/`StoreCacheManagerImpl`,
  `DefaultValidator` (TTL), `DecisionEngine`, `StoreRegistry` (Koin qualifier base),
  `RoomBookkeeper`/`RoomFetchedAtRepository`/`RoomSubmitOutbox` (Room-backed `infra/impl/`).
- **Paging** (`paging/`) — `StorePagingSource`, `PagingScreenStream`.
- **Freshness** (`freshness/`) — `FreshnessBands`/`FreshnessBand`/`FreshnessSignal` (staleness
  banner thresholds).

## Build & test the module itself

Pure `commonMain`/`commonTest` — no Android/iOS/Desktop source sets, so the KMP `test` aggregate
task runs everything:

```bash
./gradlew :core-base:store:test                       # full commonTest suite
./gradlew :core-base:store:test --tests "*DecisionEngineTest"
./gradlew :core-base:store:check                       # + detekt/spotless for this module
```

Tests live under `src/commonTest/kotlin/kpt/core/base/store/**`, mirroring the `commonMain`
package layout 1:1 (e.g. `infra/DecisionEngineTest.kt`, `submit/OfflineSubmitSyncerTest.kt`,
`paging/StorePagingSourceTest.kt`). Shared fakes (`FakeNetworkMonitor`, `FakeFetchedAtRepository`,
`FakeScreenDataStream`, `FakeSubmitOutbox`) live alongside their production counterparts, not in
a separate `fixtures` source set — reuse them instead of hand-rolling new doubles. Uses
`kotlin.test` + `kotlinx-coroutines-test` + `turbine` (declared in `commonTest.dependencies` in
`build.gradle.kts`) — Turbine's `.test { }` is the idiomatic way to assert on a `Flow<ScreenState<T>>`
emission sequence (see `ScreenDataStreamIntegrationTest.kt`, `CacheFirstSwrTest.kt`).

## Internal architecture & key contracts

- **Read path**: `Store<Key, Output>.asScreenStream(key, networkMonitor, fetchedAtRepository,
  cacheKey, scope, fetchPolicy)` (`screen/ScreenDataStream.kt`) combines the Store5 `stream()`
  Flow with `NetworkMonitor.status` and runs both through `DecisionEngine.decide(storeData,
  networkStatus, fetchPolicy)` — **all state-transition logic funnels through `DecisionEngine`**,
  a pure function deciding Loading/NoNetwork/Empty/Error/Content+freshness. New state-transition
  rules belong there, never scattered across call sites.
- **Write path**: `SubmitHandler<W>` / `DraftSubmitHandler<W>` (`submit/`) wrap a suspend submit
  block with `MutationUiState` (Idle/Submitting/Success/Failed) and, for the Draft variant, a
  `DraftResumeState` (fresh / resume-in-progress / resume-after-crash) persisted via `SubmitOutbox`
  (Room-backed via `RoomSubmitOutbox`). `OfflineSubmitSyncer` drains the outbox on reconnect using
  `RetryPolicy`/`RetryOnNetworkStatus`.
- **Bookkeeper**: `MutableStore` failure tracking is Store5's `Bookkeeper`; `RoomBookkeeper`
  (`infra/impl/`) is the shipped Room-backed implementation, keyed by a caller-supplied
  `(Key) -> String` serializer.
- **TTL/validity**: `DefaultValidator.withTtl<T>()` — **the fetcher MUST call
  `validator.markFresh()` after a successful network response**, or the TTL clock never starts and
  cached data is treated as permanently valid (documented on `StoreFactory.createStore`).
- **Cache lifecycle**: `StoreCacheManager.clearAll()` (logout) and `pruneExpiredDrafts(maxAgeMs =
  30 days default)` (app start) are the two DI-wired lifecycle hooks — `StoreCacheManagerImpl`
  clears Store5's in-memory cache plus the Room `Bookkeeper`/`Draft`/`FetchedAt` rows via
  `core-base/database` DAOs.
- **`DraftInventory`** (`infra/DraftInventory.kt`) is the cross-form, untyped view over every
  non-terminal draft (PENDING/RETRYING/FAILED) backing the Sync & Drafts screen — feature code
  never queries `DraftDao` directly.
- **`ScreenWithMutationStream`** (`combine/`) is the fused read+write seam
  (`StoreFactory.createScreenWithMutation`) for edit-in-place screens — one
  `StateFlow<CombinedState<R, W>>` instead of separate read/write flows.

## How to extend/modify safely

- **New `StoreFactory` variant**: follow the existing four — accept only Store5 primitive types
  (`Fetcher`/`SourceOfTruth`/`Converter`/`Updater`/`Bookkeeper`/`Validator`/`MemoryPolicy`), return
  a bare `Store`/`MutableStore`, document any wiring gotcha in KDoc (see the `markFresh()` note).
  Never bake in a feature-specific type.
- **Invalidation/TTL**: extend `DecisionEngine` (pure — no coroutines/side effects, stay
  exhaustively unit-testable) or `freshness/FreshnessBands`, not inline in `ScreenDataStream`.
- **Bookkeeper/outbox**: `RoomBookkeeper`/`RoomSubmitOutbox` are the only Room touch points — a new
  persisted infra type needs a matching `infra/impl/Room*` + `Fake*` test double, and must be added
  to `StoreCacheManagerImpl.clearAll()` or it leaks stale data across logout.
- **Framework-shared — never fork-brand it.** Zero Compose, zero `KptScreenStateDefaults`, zero
  fork qualifiers. Fork/project pressure (named `Store` qualifiers, branded error mapping,
  app-specific `FetchPolicy` defaults) goes into the project's own `core/store` (see
  `CONSUMPTION.md`), which depends on this module — never the reverse.
- `conflictStrategy` on `createMutableStore` is **informational only** — Store5's `Updater` does
  not auto-consume it; real conflict resolution belongs inside the caller's `Updater` block.
- Changes to the read-state contract (`ScreenState`, `DecisionEngine`, `FetchPolicy`) are
  training-corpus-consumed (`CORE_BASE_STORE.md` in the framework repo's training-layer) — reflect
  behavior changes upstream via `/kmp-project-template-retrain`, don't let the generator drift.

## Gotchas

- `createOfflineStore` still needs a real `SourceOfTruth` (it wraps `Fetcher.ofFlow { emptyFlow() }`
  under the hood) — it is not memory-only; use `createMemoryStore` for that.
- `DecisionEngine.decide` treats `NetworkStatus.CaptivePortal` as offline for content decisions but
  surfaces a distinct UI flag — don't collapse it to a plain boolean upstream.
- `CACHE_ONLY` never emits `DataFreshness.UPDATING`, only `STALE` — screens check `freshness`
  inside their `content` lambda rather than expecting a spinner.
- `PERIODIC.foregroundOnly` is parsed but not yet enforced (needs `LocalAppForegroundFlow` from
  `core-base/ui`) — don't assume it gates anything today.
