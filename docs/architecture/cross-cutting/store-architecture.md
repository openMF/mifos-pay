# Store Architecture — End to End

> **Scope.** How data moves through this app: which store type to build, how a read reaches a screen,
> and how a write reaches the server. This is the in-repo entry point for `core-base/store` (framework,
> 101 files) and `core/store` (fork seam, 28 files).
>
> Read-path internals (`StoreData<T>`, `DataOrigin`, mappers, paging, the submit outbox) are documented
> in [`STORE_DATA_API.md`](store-data-api.md). Screen-state rendering is in
> [`../../claude/store-implementation.md`](../tree/core/store-implementation.md). This document covers the
> parts those two do not: the **store type catalogue** and the **write path**.

---

## 1. Module ownership

| Module | Owner | Contains |
|---|---|---|
| `core-base/store` | **template** — do not edit in a fork | `StoreFactory`, `DecisionEngine`, `MutationGateway`, `ConflictInbox`, `DeleteSync`, `asScreenStream`, `infra/impl` Room defaults |
| `core/store` | **fork seam** — edit freely | `provide<Name>Store(...)` + `@StoreProvider`/`@CacheKey`, `KptErrorMapper`, `KptScreenStateDefaults`, `StoreModule` DI |

A fork never calls `StoreFactory` directly from a feature. It authors a `provide<Name>Store(...)` in
`core/store`, annotates it `@StoreProvider`, and `core/data` consumes the store. Fork
pressure goes to `core/store`; a genuine framework fix goes upstream.

---

## 2. Store type catalogue

Three vocabularies describe the same choice. **This is the only place they are mapped.**

| `store_archetype` (spec / codegen key) | `StoreFactory` factory | Fetcher | Source of truth | Writes |
|---|---|:--:|:--:|:--:|
| `NETWORK_WITH_CACHE` | `createStore` | ✅ | Room | ✗ |
| `NETWORK_ONLY` | `createStore` + `FetchPolicy.NETWORK_ONLY` | ✅ | Room | ✗ |
| `CACHE_ONLY` | `createStore` + `FetchPolicy.CACHE_ONLY` | ✅ | Room | ✗ |
| `PERIODIC` | `createStore` + `FetchPolicy.PERIODIC(interval)` | ✅ | Room | ✗ |
| `MEMORY_ONLY` | `createMemoryStore` | ✅ | in-memory | ✗ |
| `OFFLINE_LOCAL_ONLY` | `createOfflineStore` | ✗ | Room | ✗ |
| `MUTABLE` | `createMutableStore` | ✅ | Room | ✅ network + local |
| `LOAD_ONCE` | any read store + `asLoadOnceStream` | ✅ | Room | ✗ |
| *(no archetype)* | `createOfflineMutableStore` | ✗ | Room | ✅ local only |

`kmp-store-gen` reads `feature_profile.store_archetype` and emits the matching factory + `FetchPolicy`.
The decision matrix for *choosing* an archetype lives in
[`FEATURE_AUTHORING.md`](../../../FEATURE_AUTHORING.md).

**Two gaps worth knowing:**

- `createOfflineMutableStore` has **no archetype** — it is not reachable from codegen and must be wired
  by hand. It is the local-only write door, designed to pair with `createOfflineStore` reading the same
  table.
- `createScreenWithMutation` is **not a store type**. It is a screen-level combinator that fuses an
  existing read `Store` with a `SubmitHandler`. Most screens compose the two separately instead.

### FetchPolicy defaults differ per entry point

There is no single global default:

| Entry point | Default |
|---|---|
| `asScreenStream` (all 4 overloads) | `CACHE_FIRST_SWR` |
| `asLoadOnceStream` | `NETWORK_WITH_CACHE` |
| `PagingScreenStream` / `asPagingScreenStream` | `NETWORK_WITH_CACHE` |
| `StoreFactory.createScreenWithMutation` | `NETWORK_WITH_CACHE` |

`CACHE_FIRST_SWR` being the `asScreenStream` default is what makes an offline screen with an empty
store render `ScreenState.Empty` rather than a blocking `ScreenState.NoNetwork` — the `.onStart`
NoNetwork pre-emit is deliberately skipped for that policy. Per-variant semantics are in the
`FetchPolicy` KDoc and [`STORE_DATA_API.md`](store-data-api.md#fetchpolicy).

---

## 3. Read path

```
Screen ──collects── ViewModel ──ScreenDataStream── core/data repository
                                                        │ store.asScreenStream(key, screen, cacheKey, scope)
                                                        ▼
                                              core/store  provide<Name>Store()
                                                        │ StoreFactory.create*
                                                        ▼
                                          Store5 ── Fetcher (network) + SourceOfTruth (Room)
```

Rules that hold at every step:

- The entity→domain map lives **inside** `SourceOfTruth.reader`, so the store emits the **domain** model.
- `core/data` calls `asScreenStream`, never the feature layer.
- A feature module sees only `core/data` + `core/domain` — never `core/store` or `core/network`.
- `cacheKey` comes from `AppCacheKeys` (`core/store`), never an inline string literal. (The demo
  showcase's own keys live in the fork-owned `DemoCacheKeys` under `core/store/**/demo/`, so
  `remove-demo.sh` strips them without touching the template seam.) Static keys are
  constants (`AppCacheKeys.MY_THINGS`); per-key streams use typed builders (`AppCacheKeys.myThing(id)`) so the
  format lives in one place and cannot drift or collide.

### What actually hits the network, and when

A common wrong assumption is *"every store serves Room first, then refreshes from the network in the
background."* That is true of exactly **two** policies. The dispatch is `streamDataForPolicy`:

| Policy | Store5 request | Serves cache first | Network on subscribe |
|---|---|:--:|---|
| `CACHE_FIRST_SWR` *(the `asScreenStream` default)* | `cached(key, refresh = false)` | ✅ | **no** — revalidation is band-gated (below) |
| `NETWORK_WITH_CACHE` | `cached(key, refresh = true)` | ✅ | ✅ every subscription |
| `PERIODIC(interval)` | same as `NETWORK_WITH_CACHE`, plus a ticker | ✅ | ✅ every subscription + on cadence |
| `NETWORK_ONLY` | `fresh(key, fallBackToSourceOfTruth = true)` | ✗ network first | ✅ (cache is only a fallback) |
| `CACHE_ONLY` | `localOnly(key)` | ✅ | ✗ never |

So "cache-first **and always** refresh behind it" is `NETWORK_WITH_CACHE` — which is *not* what most
screens get, because `asScreenStream` defaults to `CACHE_FIRST_SWR`.

### The SWR band gate

Under `CACHE_FIRST_SWR` the read path uses `refresh = false`, so **subscribing performs no network call
at all**. Revalidation is a separate side-fetch guarded by three conditions:

```kotlin
val isStale  = band == FreshnessBand.Stale || band == FreshnessBand.VeryStale
val isOnline = networkStatusFlow.value is NetworkStatus.Available
if (isOnline && isStale && !wasStale) { /* launch fresh(...) side-fetch */ }
```

- **`isStale`** — a `Fresh`/`Initial` band means no fetch. Reopening a screen inside the TTL makes zero
  network calls.
- **`!wasStale`** — edge-triggered: fires once on the transition, not on every emission.
- **`isOnline`** — never fires a doomed fetch while offline; the reconnect trigger re-runs the gate.

The swap-in is **indirect**: the side-fetch writes the SourceOfTruth, and because `cached(refresh = false)`
keeps the subscription open on the SoT, that write re-fans-out through the *original* subscription as a
fresh emission — no re-subscription. Pinned by `SourceOfTruthReEmissionSwrTest.sourceOfTruthReEmissionRefreshesSwrStream`.

### Not every store has Room, or a network leg

"Room first, network behind it" also presumes both halves exist. They often don't:

| Store type | SourceOfTruth | Fetcher | Consequence |
|---|---|---|---|
| `createStore` | Room | ✅ | the full cache-then-network shape |
| `createMemoryStore` | **none** (in-memory) | ✅ | no SoT, so the SWR swap-in is a **no-op** — a memory cache does not re-emit on write, and the cache dies with the process. This is the **MEMORY_ONLY archetype showcase** (`macroIndicator`), deliberately non-cache-first — see `core/store/STORE_ARCHETYPES.yaml` |
| `createOfflineStore` | Room | **none** | **no network leg at all** — nothing to revalidate |
| `createOfflineMutableStore` | Room | **none** | local-only reads and writes |

A `SourceOfTruth` is also not *required* to be Room — it is whatever `provide<Name>Store` passes. Room is
the convention, not a guarantee.

### Explicit refresh is a different path from SWR

The band gate deliberately will **not** refetch on demand. Pull-to-refresh and post-mutation invalidate
are separate, explicit `refresh = true` paths:

| Call | Layer | Debounced | Forces a fetch |
|---|---|:--:|:--:|
| `ScreenDataStream.refresh()` | screen | ✅ `userRefreshDebounceMs` | ✗ policy-driven |
| `ScreenDataStream.refresh(forceFresh = true)` | screen | ✅ | ✅ |
| `ScreenDataStream.retry()` | screen | ✅ | ✅ — a retry after an error must reach the network |
| `ScreenDataStream.refreshFresh()` | screen | ✗ caller debounces | ✅ — undebounced, for post-mutation invalidate |
| `Store<K,O>.refreshFresh(key)` | repository | ✗ | ✅ |

**Cache-first is the default, not a universal invariant.** `CACHE_FIRST_SWR` (the `asScreenStream`
default), `NETWORK_WITH_CACHE`, `PERIODIC` and `CACHE_ONLY` all serve cache first. Two archetypes
deliberately do **not**, and are declared as such in `core/store/STORE_ARCHETYPES.yaml#cache_first`:
`NETWORK_ONLY` (routes to `fresh(fallBackToSourceOfTruth = true)` — network first, cache only as a
failure fallback) and `MEMORY_ONLY` (no SourceOfTruth, so nothing survives process death).

Before flagging a non-cache-first read as a defect, **check that registry** — it distinguishes a real
gap from an archetype demo. For a genuinely new read path, prefer `CACHE_FIRST_SWR` plus the staleness
banner, which shows the old value *and* its age.

**Use `forceFresh = true` for anything user-initiated** (pull-to-refresh, retry button). Under the
`CACHE_FIRST_SWR` default a plain `refresh()` only re-serves cache while revalidation stays band-gated,
so on a `Fresh` band the user's pull-to-refresh does **nothing**.

**How the force is threaded.** Every refresh — policy-driven or forced — emits on the single
`refreshTrigger`. Intent rides alongside it in a `ForceFreshLatch`: set immediately before the emit,
read-and-reset by `storeFlow`'s `flatMapLatest`, and passed as
`streamDataForPolicy(..., forceFresh = …)`, which short-circuits to `StoreReadRequest.fresh(...)` ahead
of the policy `when`. `forceFreshTrigger` exists only as an optional observation channel for tests.

**Why a latch and not a second flow.** Merging a `forceFreshTrigger` into the read pipeline adds an
async subscription hop before the underlying `SharedFlow`s are collected — and `refreshTrigger` has
`replay = 0`, so a reconnect `tryEmit` landing inside that widened window is **silently dropped** and
the screen never refreshes on reconnect. The latch leaves the collector's subscription topology exactly
as it was before force-fresh existed. Keeping `refreshTrigger` a `MutableSharedFlow<Unit>` also means
the opt-in `screenDataStreamForTesting(...)` factory keeps its original signature.

**`CACHE_ONLY` is exempt.** An offline-only screen must never reach the network, so `streamDataForPolicy`
ignores `forceFresh` for that policy. The guard lives there, not at the call site, so no consumer can
violate the policy by passing the flag.

> **History.** `refreshTrigger` was originally a `MutableSharedFlow<Unit>` carrying no intent, so
> `refreshFresh()` merely re-subscribed to `cached(refresh = false)` and issued **no request at all**
> under the default policy. The covering test asserted only "at least one more emission", which a
> re-emission of cached data satisfies — which is why it passed against a broken implementation. It now
> asserts the **fetcher ran**; see the regression guard in
> `CacheFirstSwrTest.refreshFreshBypassesBandGate`, plus `refreshForceFreshDrivesFetcherUnderSwr` and
> `cacheOnlyIgnoresForceFresh`.

### `ScreenStreamContext`

`asScreenStream` needs a `NetworkMonitor` and a `FetchedAtRepository`. `ScreenStreamContext` bundles
both so a repository injects **one** dependency instead of threading two framework singletons through
every read method. It is DI-provided once as a `single`:

```kotlin
class LoanRepositoryImpl(
    private val store: Store<LoanId, Loan>,
    private val screen: ScreenStreamContext,
) {
    fun loan(id: LoanId, scope: CoroutineScope) =
        store.asScreenStream(id, screen, AppCacheKeys.Loans.item(id.value), scope)
}
```

---

## 4. Write path — store as the single write SoT

**A repository never calls a DAO write directly.** Every upsert, delete, and command routes through
`MutationGateway`, which reaches the network and write-throughs to Room, so every reader — a Store
stream *and* a DAO reactive query on the same table — stays coherent.

The gateway **composes** Store5's existing write machinery (`MutableStore.write` + `Updater` +
`Bookkeeper`) plus the command outbox. It does not replace them. Room remains the durable source of
truth; the gateway is the single write door on top of it.

### The three operations

| Operation | Use for | Notes |
|---|---|---|
| `upsert(store, key, value, policy)` | a plain keyed value write | cannot roll back or record a conflict synchronously — its network leg is async |
| `delete(store, key, deleteEndpoint, bookkeeper, policy)` | removing an entity | Store5 has no network-DELETE path, so you supply the endpoint |
| `command(spec, policy)` | RPC / form / action mutations (approve, pay, submit) | the only path that can roll back and record conflicts |

If a mutation **must** await the server and roll back or record a conflict, route it through
`command` — not `upsert`.

### Policies

| `MutationPolicy` | Behaviour |
|---|---|
| `Optimistic` *(default)* | Room-first: apply locally, queue the network sync, retry on reconnect, roll back on permanent reject |
| `OnlineRequired` | Network-first: await the server, ingest the real server record; offline → `Blocked` |

"Optimistic" already means queue-and-retry — there is deliberately no separate queue-only mode.

### Results — exhaustive, nothing fails silently

```kotlin
when (val result = gateway.upsert(store, key, value)) {
    is MutationResult.Applied    -> if (result.synced) showSaved() else showQueuedOffline()
    is MutationResult.Blocked    -> showBlocked(result.reason)   // OFFLINE | UNAUTHENTICATED | PRECONDITION_FAILED
    is MutationResult.Conflicted -> navigateToConflict(result.conflictId)
    is MutationResult.Failed     -> showError(result.cause, rolledBack = result.rolledBack)
}
```

`MutationResult` is a sealed interface precisely so a ViewModel cannot swallow an offline write or a
conflict by omission.

### `CommandSpec` — the command contract

```kotlin
CommandSpec(
    payload     = approveRequest,           // sent to endpoint
    endpoint    = { api.approve(it) },      // suspend network call → R
    localApply  = { applyLocally(it) },     // Optimistic only; omit for OnlineRequired
    rollback    = { undoLocal() },          // undoes localApply on permanent reject
    resultKeyOf = { it.serverId },          // temp-key → server-key remap
    conflictOf  = { local, server -> … },   // non-null → recorded + Conflicted
)
```

The caller owns serialization in `conflictOf`, which keeps the gateway free of any JSON dependency.

### Conflicts — `ConflictInbox`

When an optimistic mutation syncs and the server record diverges from the recorded local payload:

1. **Server-wins is applied to the store immediately** — the UI never shows a stale local value.
2. The user's version is **preserved** as a `ConflictEntry` (with `localPayloadJson` and `formRoute`).
3. `observePending()` drives the Settings badge and conflict list, newest first.
4. The user opens the entry, the originating form re-opens **pre-filled from the local payload**, and
   they resolve with `ACCEPT_SERVER` (discard local) or `RETRY_LOCAL` (re-submit their version).

Backed by `RoomConflictInbox` over `ConflictDao` / `ConflictEntity` in `core-base/database`.

### Deletes — `DeleteSync`

Store5's `Updater` is write-only, so there is no network-DELETE path. `DeleteSync` supplies it:

- **Optimistic** — clear the local row immediately (it disappears from every read at once), then sync
  the network DELETE. Offline or on failure, the key is tombstoned in the `Bookkeeper` and retried on
  reconnect by the feature's `SyncOrchestrator` — the same drain that retries failed writes.
- **OnlineRequired** — await the network first, clear local only on success. Offline → `Blocked`,
  never tombstoned.

Every dependency is a lambda or interface, so it is testable without a fake Store5 store.

---

## 5. End-to-end trace — an optimistic edit while offline

```
1. Screen           user taps Save
2. ViewModel        gateway.command(spec, Optimistic)
3. MutationGateway  spec.localApply() → Room row updated
4. Room             DAO reactive query + Store stream both re-emit → UI shows the new value instantly
5. MutationGateway  offline → network leg queued; Bookkeeper records the failed sync
6. ViewModel        MutationResult.Applied(synced = false) → "saved, will sync"
   ── device reconnects ──
7. SyncOrchestrator drains the Bookkeeper, replays spec.endpoint()
8a. success         server result ingested under resultKeyOf; Bookkeeper entry cleared
8b. divergence      spec.conflictOf() non-null → server-wins applied, ConflictEntry recorded
                    → Settings badge appears → user resolves ACCEPT_SERVER | RETRY_LOCAL
8c. permanent fail  spec.rollback() → local write undone → MutationResult.Failed(rolledBack = true)
```

---

## 6. Lifecycle

- **Logout** — `StoreCacheManager.clearAll()`. Register every new store in the logout-clear list, or
  the next user sees the previous user's cached rows.
- **App start** — `pruneExpiredDrafts()`. 30-day default TTL for `SUBMITTED`/`FAILED` draft rows;
  `PENDING` drafts are never pruned.
- **TTLs** — declared as `@StoreProvider(ttl = "5m")` on the provider.

---

## 7. Adding a store — checklist

1. Pick the archetype (§2) → it determines the factory and `FetchPolicy`.
2. Author `provide<Name>Store(...)` in `core/store`, returning `Store<Key, DomainModel>`. Map
   entity→domain inside `SourceOfTruth.reader`.
3. Annotate the provider: `@StoreProvider(id = …)` plus a `@CacheKey` per stream. Both are generated.
4. Bind it in `StoreModule` and add it to the logout-clear list.
5. Consume it from a `core/data` repository via `asScreenStream` — never from a feature module.
6. If it is writable, route mutations through `MutationGateway` (§4) — never a DAO write.

---

## See also

- [`STORE_DATA_API.md`](store-data-api.md) — `StoreData<T>`, `DataOrigin`, mappers, paging, submit outbox
- [`../../claude/store-implementation.md`](../tree/core/store-implementation.md) — screen states, Room invalidation bridge
- [`FEATURE_AUTHORING.md`](../../../FEATURE_AUTHORING.md) — archetype decision matrix
- `core/store/CONSUMPTION.md` · `core-base/store/CONSUMPTION.md` — per-module call sequences
