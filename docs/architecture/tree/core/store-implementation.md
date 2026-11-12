# Store Implementation Guide

> How the offline-first data pipeline works and how to extend it in your fork.

---

## Architecture Overview

```
Fetcher (network) ──► Store5 ──► SourceOfTruth (Room)
                          │
                          ▼
                    StoreData<T>  ◄──  asScreenStream()
                          │
                    DecisionEngine.decide()
                          │
                    ScreenState<T>  ──►  Screen Composable
```

Every screen-data flow goes through three layers:

1. **Store5** — in-memory + Room cache; handles network/cache coordination
2. **`asScreenStream()`** — converts `StoreResponse` flow to `Flow<ScreenState<T>>`
3. **`DecisionEngine`** — pure function mapping `StoreData + NetworkStatus → ScreenState`

---

## Screen State Types

`ScreenState<T>` has six variants:

| Variant | When |
|---|---|
| `Loading` | No cached data yet, waiting for first fetch |
| `Content(data, freshness, fetchedAt)` | Data available (FRESH / STALE / UPDATING) |
| `NoNetwork` | Offline and no cached data; `isCaptivePortal` flag for portal detection |
| `Error` | Non-network error with no cached data |
| `Empty` | Fetch succeeded, server returned nothing |
| `Unauthenticated` | Auth error — prompt re-login |

`DataFreshness` inside `Content`:
- `FRESH` — successfully fetched from network
- `STALE` — cached data shown while offline or after a fetch error
- `UPDATING` — background refresh in progress (show spinner overlay)

---

## Read-Only Streams

### Single-entity / list screen
```kotlin
// ViewModel
val uiState: StateFlow<ScreenState<List<Loan>>> = store
    .asScreenStream(StoreRequest.cached(key, refresh = true))
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

// Screen
ScreenContent(uiState) { loans -> LoanList(loans) }
```

### Paginated screen
```kotlin
val pager = Pager(PagingConfig(pageSize = 20)) {
    StorePagingSource(store, keyFactory = { page -> LoanPageKey(page) })
}
val pagingFlow = pager.flow.cachedIn(viewModelScope)

// Screen
PagingScreenContent(pagingFlow) { loan -> LoanItem(loan) }
```

### Load-once (reference data, user profile)
```kotlin
val uiState = loadOnceStream(
    fetch = { apiService.getProfile() },
    cache = { profileDao.observe() },
).stateIn(...)
```

---

## Fetch Policy

`FetchPolicy` controls whether the Store hits network on each request:

| Policy | Behaviour |
|---|---|
| `CACHE_ONLY` | Never fetch; emit stale data or `NoNetwork` |
| `NETWORK_ONLY` | Always fetch; ignore cache |
| `NETWORK_WITH_CACHE` | Emit cached immediately, then refresh (default) |

Pass via `StoreRequest`:
```kotlin
store.asScreenStream(StoreRequest.cached(key, refresh = policy == NETWORK_WITH_CACHE))
```

### NETWORK_WITH_CACHE

Use `FetchPolicy.NETWORK_WITH_CACHE` for any domain with a Room SourceOfTruth + Fetcher.
Internal routing (from `StoreDataExtensions.kt`):
- Fresh cache (within TTL, `validator.isValid == true`) → emit cache immediately + background refresh
- Stale or empty cache → try network first; on failure fall back to cache

### createOfflineStore — OFFLINE_LOCAL_ONLY

`StoreFactory.createOfflineStore(sourceOfTruth)` wraps `StoreBuilder.from(sourceOfTruth)` with no Fetcher.
Use when the app is the single source of truth (alerts, loans, bill reminders).

### SpotRateLookupStore — NETWORK_ONLY + Room write side-effect

`createStore(fetcher=frankfurterApi, sourceOfTruth=exchangeRatesDao)` with `FetchPolicy.NETWORK_ONLY`
at the stream callsite. Read path always skips cache (always-fresh). Write path persists to Room
as a side-effect — so `NETWORK_WITH_CACHE` consumers (currency list) pick up the fresh rate
without their own network call.

---

## Mutation / Input Submission

> Screen-archetype name in `ui.yaml` (when used with `/kmp-feature` codegen): `type: input`.
> The "form" terminology here refers to the *API parameter names* (e.g. `formKey`,
> `observePendingByFormKey`) and shape of user input — the screen archetype itself is
> called `input` (covers form / wizard / quick-action / confirm / gesture).

> **Unified write view-model.** Screens extend the single `BaseMutationViewModel<T, R>`
> parameterized by **`MutationMode`** — `MutationMode.InSession` (single-shot submit) or
> `MutationMode.Draft` (offline draft, 3-case resume). An earlier design split this into two base
> VMs; the mode now expresses the difference. The `SubmitHandler` / `DraftSubmitHandler` shown below
> are the lower-level handlers the base VM composes.

> **Read shape is picked by `store_archetype`.** The generator routes on
> `feature_profile.store_archetype` (8 archetypes) to select the `core/store` factory — see
> [STORE_DATA_API.md](../../cross-cutting/store-data-api.md) and `FEATURE_AUTHORING.md`.


### Basic mutation (no draft persistence)
```kotlin
class CreateLoanHandler(
    private val api: LoanApi,
    private val loanStore: Store<LoanKey, Loan>,
) : SubmitHandler<CreateLoanRequest, Loan> {

    override suspend fun submit(payload: CreateLoanRequest): Result<Loan> =
        runCatching { api.createLoan(payload) }
            .onSuccess { loanStore.clear() }  // invalidate cache
}
```

### Draft-preserving mutation (offline-resilient)

Use `DraftSubmitHandler` when you want to persist form payloads across app restarts:

```kotlin
class CreateLoanDraftHandler(
    private val api: LoanApi,
    private val draftDao: DraftDao,
    private val loanStore: Store<LoanKey, Loan>,
) : DraftSubmitHandler<CreateLoanRequest, Loan>(
    formKey = "create_loan",
    draftDao = draftDao,
) {
    override suspend fun doSubmit(payload: CreateLoanRequest): Result<Loan> =
        runCatching { api.createLoan(payload) }
            .onSuccess { loanStore.clear() }
}
```

The framework will:
- Save the draft as PENDING on first call
- Transition it SUBMITTED on success / FAILED on network error
- Re-surface PENDING drafts on the next app launch (via `observePendingByFormKey`)

### Mutation Screen wiring
```kotlin
MutationScreenContent(
    uiState = viewModel.submitState,
    onSubmit = viewModel::submit,
) { formState ->
    CreateLoanForm(formState)
}
```

---

## Cache Lifecycle

### On logout — clear all caches
```kotlin
// In your AuthRepository or logout use-case
storeCacheManager.clearAll()
```

`clearAll()` wipes:
- All registered Store5 in-memory caches and SourceOfTruth (Room) rows
- Bookkeeper sync-failure records
- All draft rows in `framework_submit_drafts`

### On app start — prune expired drafts
```kotlin
// In your app initializer or startup WorkManager task
storeCacheManager.pruneExpiredDrafts()  // default 30-day TTL
```

Only SUBMITTED and FAILED rows are pruned. PENDING drafts are never removed — the
user may still want to resume an unsent form.

Custom TTL:
```kotlin
storeCacheManager.pruneExpiredDrafts(maxAgeMs = 7L * 24 * 60 * 60 * 1000)  // 7 days
```

### Registering your Store for logout clearing
```kotlin
// In your feature's Koin DI module
single<StoreCacheManager> { get<StoreCacheManagerImpl>() }

// Register each store that should be cleared on logout
// generated from @StoreProvider(logout = true) — no hand-registration needed
```

---

## Combining Multiple Streams

```kotlin
// Two independent stores → single combined ScreenState
val uiState: Flow<ScreenState<DashboardData>> = combineScreenStates(
    loansFlow,
    savingsFlow,
) { loans, savings -> DashboardData(loans, savings) }
```

Priority when combining: `NoNetwork > Loading > Error > Content`.

---

## Room invalidation on web — no bridge needed

> **TL;DR for feature authors:** write with the DAO (`dao.upsert(...)`) and read with the DAO's
> Room `Flow` (`dao.observeXxx()`). Room's `InvalidationTracker` re-emits to live collectors after
> a write on every target, web included. There is nothing to wrap.

### History (removed 2026-09-17)

A `RoomChangeBus` / `daoFlow { }` / `notifyingWrite { }` bridge used to sit in
`core-base/database/invalidation/`, added against Room 3 **alpha05** on the belief that
`InvalidationTracker` did not fan out to live collectors on the single-threaded wasmJs event loop.

On **Room 3.1.0-alpha01** that no longer reproduces. Measured in ChromeHeadless 153 on **both js
and wasmJs** (`core/database/src/{js,wasmJs}Test/.../WebInvalidationProbeTest.kt`):

| scenario | result |
|---|---|
| single write | ✅ re-emits |
| two writes in rapid succession | ✅ re-emits (signals coalesce into one emission) |
| 20-write burst while the event loop is contended | ✅ terminal state observed |

So the bridge was removed and every call site unwrapped. The probe tests stay as the regression
guard: if a future Room release regresses here, they fail and the bridge case must be reopened.

### The measurement trap, if you ever re-test this

Do **not** measure web database behaviour with turbine's `awaitItem()` inside `runTest`. `runTest`
runs on virtual time; waiting there for a value that can only arrive from a real web-worker round
trip spins the single JS thread, the browser stops answering Karma's pings, and the run dies with
`Disconnected (0 times) reconnect failed before timeout of 2000ms (ping timeout)`. That looks
exactly like "Room lost the invalidation" and is not — it is the harness blocking the event loop
the driver needs. It produced one wrong conclusion already. Use real waits on
`Dispatchers.Default` and a plain `launch` + list, as the probe test does.

## Customisation Seam — `core/store/`

**DO NOT** modify `core-base/store` — it upgrades cleanly across template versions.

Everything consumer-forks need lives in `core/store/`:

| File | Purpose |
|---|---|
| `KptScreenStateDefaults.kt` | Brand visuals, copy, Lottie animations, telemetry hooks |
| `KptErrorMapper.kt` | Domain-error → user-message mapping (extends `categorize()`) |
| `@StoreProvider` on the provider | Store id, TTL, logout purge — qualifier is GENERATED |
| `@CacheKey` on the provider | Stream cache keys — the `<Store>Keys` object is GENERATED |

`KptTheme` provides `LocalScreenStateDefaults` automatically — zero per-screen wiring.

---

## DefaultValidator wiring note

If you pass a `DefaultValidator` to `StoreFactory.createStore()`, you **must** call
`validator.markFresh()` inside the Fetcher block after every successful network fetch.
Failing to do so causes every response to be treated as stale, re-triggering network
fetches on every collection.

```kotlin
StoreFactory.createStore(
    fetcher = Fetcher.of { key ->
        val result = api.fetch(key)
        validator.markFresh(key)   // <-- required
        result
    },
    validator = validator,
    ...
)
```
