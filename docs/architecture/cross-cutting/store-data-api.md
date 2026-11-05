# StoreData API — Unified Offline-First Data Layer

> **Module:** `core-base/store` (synced to all consumer apps via scripts/white-label/sync-dirs.sh)
> **Package:** `template.core.base.store`
> **Store 5 Version:** 5.1.0-alpha08

---

## Overview

The StoreData API provides a **unified interface** for feature modules to consume data from repositories without knowing whether the data comes from a network API, a local Room database, or an in-memory cache. It wraps [MobileNativeFoundation/Store 5](https://github.com/MobileNativeFoundation/Store) responses into a single `StoreData<T>` type that carries data + metadata about origin, staleness, refresh status, and errors.

### Key Design Principles

1. **ViewModel doesn't care about the source** — same API for network+cache and network-only stores
2. **Cache-then-refresh UX** — show cached data instantly, update when network responds
3. **Staleness awareness** — feature modules know how old their data is
4. **Error recovery** — show stale data with an error indicator instead of a blank screen
5. **Empty state detection** — distinguish "empty result" from "no data yet"

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Feature Module (ViewModel)                                  │
│                                                              │
│  collects Flow<StoreData<T>>  or  Flow<DataState<T>>        │
│  (calls repository directly — no use case needed)            │
│  (calls domain use case — only for paging/transformation)    │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│  core/data (Repository)                                      │
│                                                              │
│  store.streamData(key)          → Flow<StoreData<T>>        │
│  store.streamDataWithErrors(…)  → Flow<StoreData<T>>        │
│  store.freshData(key)           → Flow<StoreData<T>>        │
│  store.localData(key)           → Flow<StoreData<T>>        │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│  core-base/store                                             │
│                                                              │
│  ┌─────────────────┐  ┌──────────────────┐                  │
│  │ StoreData<T>     │  │ StoreDataMapper  │                  │
│  │ DataOrigin       │  │ mapToStoreData() │                  │
│  │ toDataState()    │  │ mapToStoreData   │                  │
│  │ map()            │  │   WithErrors()   │                  │
│  └─────────────────┘  └──────────────────┘                  │
│                                                              │
│  ┌─────────────────┐  ┌──────────────────┐                  │
│  │ StoreFactory     │  │ PageKey          │                  │
│  │ createStore()    │  │ StorePageResult  │                  │
│  │ createMemory     │  │ loadPage()       │                  │
│  │   Store()        │  │                  │                  │
│  │ createMutable    │  │                  │                  │
│  │   Store()        │  │                  │                  │
│  └─────────────────┘  └──────────────────┘                  │
└──────────────────┬──────────────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼───────┐   ┌────────▼────────┐
│ core/network  │   │ core/database   │
│ (Ktor Fetcher)│   │ (Room 3 SOT)   │
└───────────────┘   └─────────────────┘
```

---

## Source Modes

The same `StoreData<T>` type works for both modes. The difference is in how the Store is created — the consumer (ViewModel) never knows.

### Mode 1: Network + Cache (offline-first)

Store created with `StoreFactory.createStore(fetcher, sourceOfTruth)`.

**Emission sequence:**
```
Initial → Loading(Fetcher) → Data(SourceOfTruth) → Data(Fetcher)
```

**StoreData mapping:**
```
Loading       → (no emission — sets refreshing flag)
Data(SOT)     → StoreData(data, origin=CACHE, isRefreshing=true, isStale=true)
Data(Fetcher) → StoreData(data, origin=NETWORK, isRefreshing=false, isStale=false)
```

**UX:** Data shows instantly from cache → subtle progress indicator → data updates silently.

### Mode 2: Network Only (no database)

Store created with `StoreFactory.createMemoryStore(fetcher)`.

**Emission sequence:**
```
Initial → Loading(Fetcher) → Data(Fetcher)
```

**StoreData mapping:**
```
Loading       → (no emission — sets refreshing flag)
Data(Fetcher) → StoreData(data, origin=NETWORK, isRefreshing=false, isStale=false)
```

**UX:** Loading spinner → data appears. No cache/staleness concept.

---

## API Reference

### `StoreData<T>`

The core data class that carries data + metadata.

```kotlin
data class StoreData<out T>(
    val data: T,               // The actual payload
    val origin: DataOrigin,    // CACHE, NETWORK, or MEMORY
    val isRefreshing: Boolean, // Network fetch in progress?
    val fetchedAt: TimeMark?,  // When last fetched from network
    val error: Throwable?,     // Non-null if refresh failed
    val isEmpty: Boolean,      // True if data is empty (empty list, etc.)
)
```

**Computed properties:**

| Property | Type | Description |
|----------|------|-------------|
| `isStale` | `Boolean` | True if data has never been fetched from network (cache-only). Always false for `origin=NETWORK`. |
| `staleDuration` | `Duration?` | Time elapsed since last network fetch. Null if never fetched. |
| `isSuccess` | `Boolean` | True if no error and not empty. |
| `isError` | `Boolean` | True if error exists and data is empty (terminal error). |

### `DataOrigin`

```kotlin
enum class DataOrigin {
    CACHE,    // From Room database (SourceOfTruth)
    NETWORK,  // From API (Fetcher)
    MEMORY,   // From Store's in-memory cache
}
```

### Mapper Functions

#### `mapToStoreData()`

Maps `Flow<StoreReadResponse<T>>` → `Flow<StoreData<T>>`. Skips errors silently.

```kotlin
fun <T : Any> Flow<StoreReadResponse<T>>.mapToStoreData(
    isEmpty: (T) -> Boolean = { false },
): Flow<StoreData<T>>
```

#### `mapToStoreDataWithErrors()`

Same as above but emits on errors too, carrying last known data.

```kotlin
fun <T : Any> Flow<StoreReadResponse<T>>.mapToStoreDataWithErrors(
    fallback: T,                          // Used if error arrives before any data
    isEmpty: (T) -> Boolean = { false },
): Flow<StoreData<T>>
```

### Store Extensions

One-liner convenience functions for repositories.

```kotlin
// Primary API — cache-first with optional refresh
store.streamData(key, refresh = true, isEmpty = { it.isEmpty() })

// Same but with error emissions
store.streamDataWithErrors(key, fallback = emptyList(), isEmpty = { it.isEmpty() })

// Force network fetch (pull-to-refresh)
store.freshData(key, isEmpty = { it.isEmpty() })

// Local only, no network
store.localData(key, isEmpty = { it.isEmpty() })
```

### Transform Functions

```kotlin
// Transform data while preserving all metadata
val mapped: StoreData<Int> = storeData.map { it.toInt() }

// Transform a Flow of StoreData
val flow: Flow<StoreData<Int>> = storeDataFlow.mapData { it.toInt() }
```

### DataState Bridge

```kotlin
// Convert to DataState for ViewModels using the existing pattern
val dataState: DataState<T> = storeData.toDataState()
```

Mapping rules:

| StoreData state | DataState |
|----------------|-----------|
| isEmpty + no error | `DataState.Loading` |
| isRefreshing + data | `DataState.Pending(data)` |
| error + no data | `DataState.Error(error, null)` |
| error + stale data | `DataState.Error(error, data)` |
| success | `DataState.Success(data)` |

---

## Paging Support

For paginated lists, use `PageKey` as the Store key and `loadPage()` in a PagingSource.

### `PageKey`

```kotlin
data class PageKey(
    val page: Int,                          // Zero-based page index
    val pageSize: Int = 20,                 // Items per page
    val query: String? = null,              // Optional search filter
) {
    val offset: Int get() = page * pageSize // For SQL LIMIT/OFFSET
    fun next(): PageKey                     // Next page
}
```

### `loadPage()`

Suspends until the first Data or Error response, then cancels the Store stream.

```kotlin
suspend fun <T : Any> Store<PageKey, List<T>>.loadPage(
    key: PageKey,
): StorePageResult<T>
```

> **Implementation note:** Store's `stream()` returns an **infinite Flow** that never completes.
> `loadPage()` uses `filterNot { Loading/NoNewData/Initial }.first()` to get the first
> meaningful response and cancel. Do NOT use `collect { return@collect }` — `return@collect`
> only returns from the lambda, it does not cancel the flow.

### PagingSource Integration (in core/domain)

```kotlin
class ClientPagingSource(
    private val store: Store<PageKey, List<Client>>,
) : PagingSource<Int, Client>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Client> {
        val page = params.key ?: 0
        val pageKey = PageKey(page, params.loadSize)
        return when (val result = store.loadPage(pageKey)) {
            is StorePageResult.Success -> LoadResult.Page(
                data = result.items,
                prevKey = result.prevKey,
                nextKey = result.nextKey,
            )
            is StorePageResult.Error -> LoadResult.Error(result.error)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Client>): Int? =
        state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) }
}
```

---

## Usage Patterns

### Pattern 1: Network + Cache (most features)

```kotlin
// ── core/data ──
class ClientRepository(
    private val store: Store<ClientKey, List<Client>>,
) : ClientRepositoryApi {
    override fun getClients(key: ClientKey): Flow<StoreData<List<Client>>> =
        store.streamDataWithErrors(key, fallback = emptyList(), isEmpty = { it.isEmpty() })
}

// ── feature/ (ViewModel calls repo directly) ──
class ClientListViewModel(
    private val repository: ClientRepositoryApi,
) : ViewModel() {

    val state: StateFlow<DataState<List<Client>>> = repository.getClients(ClientKey(0))
        .mapData { clients -> clients.sortedBy { it.name } }
        .map { it.toDataState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState.Loading)
}
```

### Pattern 2: Network Only (transient data)

```kotlin
// ── core/data ──
class ExchangeRateRepository(
    private val store: Store<CurrencyKey, ExchangeRate>,  // createMemoryStore(fetcher)
) : ExchangeRateRepositoryApi {
    override fun getRate(key: CurrencyKey): Flow<StoreData<ExchangeRate>> =
        store.streamDataWithErrors(key, fallback = ExchangeRate.EMPTY)
}

// ── feature/ (IDENTICAL ViewModel code — doesn't know it's network-only) ──
class ExchangeViewModel(
    private val repository: ExchangeRateRepositoryApi,
) : ViewModel() {

    val state = repository.getRate(CurrencyKey("USD"))
        .map { it.toDataState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState.Loading)
}
```

### Pattern 3: Paging (large lists via core/domain)

```kotlin
// ── core/domain (transformation needed — use case) ──
class GetClientsPaginatedUseCase(
    private val store: Store<PageKey, List<Client>>,
) {
    operator fun invoke(query: String? = null): Flow<PagingData<Client>> =
        Pager(
            config = PagingConfig(pageSize = PageKey.DEFAULT_PAGE_SIZE),
            pagingSourceFactory = { ClientPagingSource(store) },
        ).flow
}

// ── feature/ (ViewModel calls use case) ──
class ClientListViewModel(
    private val getClientsPaginated: GetClientsPaginatedUseCase,
) : ViewModel() {
    val pagingData = getClientsPaginated(query = null).cachedIn(viewModelScope)
}
```

### Pattern 4: Using StoreData directly (rich UX)

```kotlin
// ── feature/ (use StoreData fields directly for rich UI) ──
class ClientListViewModel(
    private val repository: ClientRepositoryApi,
) : ViewModel() {

    data class UiState(
        val clients: List<Client> = emptyList(),
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val dataOrigin: DataOrigin? = null,
        val isStale: Boolean = false,
        val staleDuration: Duration? = null,
        val error: String? = null,
        val isEmpty: Boolean = false,
    )

    val state: StateFlow<UiState> = repository.getClients(ClientKey(0))
        .map { storeData ->
            UiState(
                clients = storeData.data,
                isLoading = false,
                isRefreshing = storeData.isRefreshing,
                dataOrigin = storeData.origin,
                isStale = storeData.isStale,
                staleDuration = storeData.staleDuration,
                error = storeData.error?.message,
                isEmpty = storeData.isEmpty,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}

// ── Compose UI ──
@Composable
fun ClientListScreen(state: UiState) {
    if (state.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (state.isStale) InfoBanner("Data may be outdated")
    state.staleDuration?.let { if (it > 30.minutes) InfoBanner("Updated ${it.inWholeMinutes}m ago") }
    state.error?.let { Snackbar("Couldn't refresh: $it") }
    if (state.isEmpty && !state.isLoading) EmptyState("No clients found")
    LazyColumn { items(state.clients) { ClientItem(it) } }
}
```

---

## When to Use Domain Layer

| Scenario | Layer | Why |
|----------|-------|-----|
| Simple data fetch | `core/data` → ViewModel | No transformation needed |
| Sort/filter data | `core/data` → ViewModel (use `mapData`) | Simple transformation via extension |
| Combine multiple repos | `core/domain` use case | Multiple data sources need merging |
| Paging | `core/domain` use case | PagingSource is a transformation layer |
| Complex business logic | `core/domain` use case | Validation, calculations, etc. |

---

## Module Structure

```
core-base/store/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/template/core/base/store/
│   │   ├── — READ PATH ——————————————————————————————
│   │   ├── ScreenState.kt            # Loading/Content/Empty/NoNetwork/Error
│   │   ├── ScreenDataStream.kt       # Single-item stream (Store.asScreenStream)
│   │   ├── ScreenStateExtensions.kt  # mapContent, combineContent, emptyIfContent
│   │   ├── PagingScreenStream.kt     # Paginated stream (Store.asPagingScreenStream)
│   │   ├── StoreData.kt              # StoreData<T> + DataOrigin + toDataState()
│   │   ├── StoreDataMapper.kt        # mapToStoreData + mapToStoreDataWithErrors
│   │   ├── StoreDataExtensions.kt    # streamData, freshData, localData, map
│   │   ├── StorePagingSource.kt      # PageKey + loadPage + StorePageResult
│   │   ├── DecisionEngine.kt         # StoreData + NetworkStatus → ScreenState
│   │   ├── — WRITE PATH —————————————————————————————
│   │   ├── SubmitState.kt            # Idle/Submitting/Submitted<R>/Failed
│   │   ├── SubmitHandler.kt          # One-shot executor (submitHandler() factory)
│   │   ├── SubmitStateExtensions.kt  # isSubmitting, resultOrNull, errorOrNull, …
│   │   ├── — INFRASTRUCTURE ————————————————————————
│   │   ├── StoreFactory.kt           # Store/MutableStore creation
│   │   ├── StoreResponseMapper.kt    # mapToResult/mapToData
│   │   ├── DefaultValidator.kt       # TTL-based cache validation
│   │   ├── ErrorCategory.kt          # Network/Auth/Server/Generic classification
│   │   └── di/
│   │       └── StoreModule.kt        # Koin module
│   └── commonTest/kotlin/template/core/base/store/
│       ├── StoreDataMapperTest.kt        # 20+ read-path tests
│       ├── SubmitHandlerTest.kt          # 12 write-path tests (T1–T12)
│       └── SubmitStateExtensionsTest.kt  # exhaustive property tests
```

---

## UX Scenario Matrix

| Scenario | Source Mode | StoreData State | UI Behavior |
|----------|-----------|-----------------|-------------|
| First load (no cache) | N+C | Loading → Data(Network) | Spinner → data |
| Cached data + refresh | N+C | Data(Cache, refreshing) → Data(Network) | Data instantly + progress → updated |
| Cache hit, no changes | N+C | Data(Cache) → NoNewData | Data shown, no indicator |
| Network fails, has cache | N+C | Data(Cache) → Error(stale data) | Stale data + "Couldn't refresh" snackbar |
| Network fails, no cache | N+C | Error(isEmpty=true) | Error screen + retry |
| Network success | N-only | Loading → Data(Network) | Spinner → data |
| Network fails | N-only | Loading → Error(isEmpty=true) | Error screen + retry |
| Empty result | Both | Data(isEmpty=true) | Empty state UI |
| TTL expired | N+C | Data(Cache, isStale) + refreshing | "Outdated" badge + auto-refresh |
| Pull-to-refresh | Both | freshData() → Data(Network) | Refresh indicator → data |
| Paginated list | N+C | PagingData via domain use case | Lazy list + page loading |

---


---

## Form / Submit

### State machine

```
Idle  ──submit()──▶  Submitting  ──success──▶  Submitted<R>
                                 ──failure──▶  Failed(error, category)
Submitted / Failed  ──reset()──▶  Idle
Failed              ──retry()──▶  Submitting
```

### UX Scenario Matrix

| Scenario | SubmitState | UI |
|----------|-------------|-----|
| Form idle | `Idle` | Button enabled |
| User taps Submit | `Submitting` | Button disabled + scrim overlay |
| API succeeds | `Submitted(result)` | Navigate / toast |
| API fails (network) | `Failed(Network)` | No-network bottom sheet |
| API fails (auth) | `Failed(Auth)` | Navigate to login |
| API fails (server) | `Failed(Server)` | Error dialog + retry button |
| User taps Retry | `Submitting` again | Overlay reappears |
| User dismisses | `Idle` (via reset) | Dialog hidden |

### ViewModel

```kotlin
private val submit = viewModelScope.submitHandler<ClientId>()
val submitState    = submit.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubmitState.Idle)

fun onSave(form: ClientForm) = submit.submit { repository.createClient(form) }
fun onRetry()               = submit.retry()
fun onDismiss()             = submit.reset()
```

### Screen

```kotlin
Box(Modifier.fillMaxSize()) {
    FormContent(
        enabled = !submitState.isSubmitting,
        onSubmit = { viewModel.onSave(it) },
    )
    SubmitProgressOverlay(state = submitState)  // core-base/ui
    SubmitResultHandler(                         // core-base/ui
        state = submitState,
        onSubmitted = { clientId -> onNavigateToDetail(clientId) },
        onFailed = { error, category ->
            viewModel.onDismiss()
            when (category) {
                ErrorCategory.Network -> showNoNetworkSheet()
                ErrorCategory.Auth    -> onNavigateToLogin()
                else                  -> showErrorDialog(error.message)
            }
        },
    )
}
```

### BaseMutationViewModel + MutationMode (the unified write idiom)

For most write screens, extend the single base view-model
`BaseMutationViewModel<T, R>` (`core-base/ui/.../viewmodel/BaseMutationViewModel.kt`) rather than
wiring `submitHandler` by hand. It is parameterized by **`MutationMode`**:

- **`MutationMode.InSession`** — single-shot submit, no persistence.
- **`MutationMode.Draft`** — offline-resilient draft with 3-case resume (fresh / resume-in-progress /
  resume-after-crash); the payload survives restarts and is retried on reconnect.

There is exactly one base mutation VM (an earlier design split it into two — the mode now expresses
the difference). The **Sync & Drafts** surface lists in-flight drafts from both modes.

### Store archetypes (generator routing key)

The read/write shape is selected by **`feature_profile.store_archetype`** — the 8 archetypes are
`NETWORK_WITH_CACHE | MUTABLE | OFFLINE_LOCAL_ONLY | NETWORK_ONLY | CACHE_ONLY | PERIODIC |
MEMORY_ONLY | LOAD_ONCE`. `kmp-store-gen` reads `store_archetype` and emits the matching
`StoreFactory.create*` factory + `FetchPolicy`. Full decision matrix + module chain:
[`FEATURE_AUTHORING.md`](../../../FEATURE_AUTHORING.md).

## Store 5 API Notes

### StoreReadResponseOrigin (verified from bytecode)

`StoreReadResponseOrigin` is a **standalone top-level sealed class** — NOT nested inside `StoreReadResponse`.

```kotlin
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

// Variants:
StoreReadResponseOrigin.Fetcher()       // data class with optional name: String?
StoreReadResponseOrigin.SourceOfTruth   // object
StoreReadResponseOrigin.Cache           // object
StoreReadResponseOrigin.Initial         // object
```

### Store.stream() is Infinite

`Store.stream()` returns a **never-completing Flow**. It continues emitting updates as long as the collector is active. Use `.first()` when you need a single value (like in `loadPage()`).

### StoreReadRequest Variants

```kotlin
StoreReadRequest.cached(key, refresh = true)              // Cache-first, optionally refresh
StoreReadRequest.fresh(key, fallBackToSourceOfTruth = true)  // Force network
StoreReadRequest.localOnly(key)                           // Cache/SOT only, no network
StoreReadRequest.skipMemory(key, refresh = true)          // Bypass in-memory cache
```

---

## Dependencies

```kotlin
// core-base/store/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.store5)                          // Store 5
            api(libs.store5.cache)                    // Cache 5
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core-base:common"))  // DataState bridge
        }
    }
}
```

Consumer apps that need paging add `androidx.paging` to their own `libs.versions.toml` — it is NOT a dependency of `core-base/store`.

---

## FetchPolicy

`FetchPolicy` controls whether a screen stream reads from cache, hits the network, or both.
Pass it to `asScreenStream`, `asLoadOnceStream`, or `PagingScreenStream` to override the default.

```kotlin
enum class FetchPolicy {
    NETWORK_WITH_CACHE,  // default — show cache instantly, refresh in background
    NETWORK_ONLY,        // skip cache, always fetch fresh (e.g. payment confirmation)
    CACHE_ONLY,          // never hit network (offline view, pre-fetched data)
}
```

**Choosing a policy:**

| Scenario | Policy |
|---|---|
| Normal screen — fast load + background refresh | `NETWORK_WITH_CACHE` (default) |
| Stale data is harmful (payment status, balance) | `NETWORK_ONLY` |
| Explicit offline screen or no network available | `CACHE_ONLY` |

**Usage:**

```kotlin
// Always-fresh payment confirmation screen
val stream = store.asScreenStream(
    key = paymentId,
    networkMonitor = networkMonitor,
    fetchedAtRepository = fetchedAtRepository,
    cacheKey = "payment:$paymentId",
    scope = viewModelScope,
    fetchPolicy = FetchPolicy.NETWORK_ONLY,
)

// Offline-capable cached list
val stream = store.asScreenStream(
    key = "clients",
    networkMonitor = networkMonitor,
    fetchedAtRepository = fetchedAtRepository,
    cacheKey = "clients",
    scope = viewModelScope,
    fetchPolicy = FetchPolicy.CACHE_ONLY,
)
```

---

## Offline Submit Outbox

The write-side complement to offline-first reads. When a form submission fails due to a
network error, the payload is saved locally so the user can resume later.

### Architecture

```
ViewModel
  └─ DraftSubmitHandler<P, R>
       ├─ on network error → SubmitOutbox.save(formKey, payload)
       ├─ on retry success → SubmitOutbox.markSubmitted(id)
       └─ on retry failure → SubmitOutbox.markFailed(id, error)

SubmitOutbox (interface, core-base/store)
  └─ RoomSubmitOutbox<P> (impl, core/data)
       └─ DraftDao → AppDatabase.framework_submit_drafts

DraftResumeStream
  └─ observePending(formKey) → Flow<DraftResumeState<P>>
       ├─ DraftResumeState.None        — form starts fresh
       └─ DraftResumeState.HasDraft<P> — show "Resume?" banner

OfflineSubmitSyncer
  └─ watches isOnlineFlow → retries all PENDING entries on reconnect
```

### DraftSubmitHandler — out-of-box drop-in

`DraftSubmitHandler` wraps `SubmitHandler` and auto-saves on network failure.
Use it exactly like `SubmitHandler` — the draft logic is invisible to the caller.

```kotlin
// ViewModel
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, LoanId>(
    outbox  = get(), // injected RoomSubmitOutbox<LoanPayload>
    formKey = "loan_application",
)
val submitState = draftHandler.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubmitState.Idle)

fun onSubmit(payload: LoanPayload) = draftHandler.submit(payload) { repo.submitLoan(it) }
fun onRetry()                      = draftHandler.retry()
fun onDismiss()                    = draftHandler.reset()
```

### DraftResumeStream — resume banner

```kotlin
// ViewModel — expose resume state to the screen
val draftState: StateFlow<DraftResumeState<LoanPayload>> =
    outbox.resumeStateFor("loan_application")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftResumeState.None)

// Screen
when (val draft = uiState.draftState) {
    is DraftResumeState.HasDraft -> ResumeBanner(
        message = "You have an unsaved submission from ${draft.entry.createdAtMs.toRelativeTime()}",
        onResume = { viewModel.onResumeDraft(draft.entry.payload) },
        onDiscard = { viewModel.onDiscardDraft() },
    )
    DraftResumeState.None -> { /* form starts fresh */ }
}
```

### OfflineSubmitSyncer — reconnect retry

Wire once in a long-lived scope (e.g. `AppViewModel`) to retry PENDING drafts automatically:

```kotlin
val syncer = viewModelScope.offlineSubmitSyncer(
    outbox       = loanOutbox,
    isOnlineFlow = networkMonitor.isOnline,
    submitBlock  = { payload -> loanRepository.submitLoan(payload) },
)
syncer.start()
```

### DI wiring (Koin)

```kotlin
// In your feature's DI module
single<SubmitOutbox<LoanPayload>> {
    RoomSubmitOutbox(
        dao        = get<AppDatabase>().draftDao,
        serializer = LoanPayload.serializer(),
    )
}
```

`AppDatabase.draftDao` is bound automatically by `DataModule` (`core/data/di/RepositoryModule.kt`).
`StoreCacheManager.clearAll()` calls `draftDao.deleteAll()` on logout — preventing user A's
drafts from surfacing on user B's session.

### Database migration

`AppDatabase` bumped to **VERSION 5** with `AutoMigration(from = 4, to = 5)`.
The migration adds the `framework_submit_drafts` table automatically — no manual SQL needed.

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK AUTOINCREMENT` | Surrogate key |
| `formKey` | `TEXT` | Consumer form identifier |
| `payloadJson` | `TEXT` | Serialized form payload |
| `status` | `TEXT` | `PENDING` / `SUBMITTED` / `FAILED` |
| `createdAtMs` | `INTEGER` | Epoch millis, creation time |
| `updatedAtMs` | `INTEGER` | Epoch millis, last status change |
| `errorMessage` | `TEXT?` | Last failure reason (nullable) |
