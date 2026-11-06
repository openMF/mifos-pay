# `core-base/store`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_STORE.md`
> **Measured:** 112 Kotlin files, 51 test files

**Defines annotations:** `@CacheKey`, `@ExperimentalScreenDataStreamTestingApi`, `@StoreProvider`

**Consumes contracts:** `@ExperimentalScreenDataStreamTestingApi`

## Principal types

`BatchResult`, `BatchSubmitHandler`, `BatchSubmitMode`, `BlockReason`, `CacheKey`, `CombinedState`, `CommandSpec`, `ConflictEntry`, `ConflictInbox`, `ConflictReport`, `ConflictResolution`, `ConflictStrategy`, `DataOrigin`, `DecisionEngine`  …and 58 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/store sha=626b01f91a22231152882d91a1ffd8ae92b66071 -->
## API reference

_Generated from `core-base/store` at tree `626b01f91a22` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/annotation/StoreProvider.kt`

```kotlin
annotation class StoreProvider(
```
Marks a Store5 provider function.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:55</code></summary>

```kotlin
 * is deliberately not part of this store's contract.
 */
@StoreProvider(id = "userData")
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
```

</details>

```kotlin
annotation class CacheKey(
```
A stream cache key for the annotated store — the string that keys per-stream freshness tracking.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:56</code></summary>

```kotlin
 */
@StoreProvider(id = "userData")
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/CombinedState.kt`

```kotlin
data class CombinedState<R, W>(
```
Snapshot pairing the read-side `ScreenState` with the write-side `SubmitState` — the single value a screen needs to render every read-write cycle (form preload, submission progress, post-submit outcome, offline outbox indicator).

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/ScreenWithMutationStream.kt:65</code></summary>

```kotlin
     * on each subscription.
     */
    val state: StateFlow<CombinedState<R, W>>

    /**
     * Trigger a network refresh of the read pipeline. No-op if read-policy is
     * [kpt.core.base.store.screen.FetchPolicy.CACHE_ONLY]. Mirrors
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/ScreenWithMutationStream.kt`

```kotlin
interface ScreenWithMutationStream<R, W>
```
Unified read + write + sync seam for screens that both display data and submit mutations against the same domain object (edit forms, settings panels, in-place record updates).

<details><summary>Example</summary>

```kotlin
private val stream = StoreFactory.createScreenWithMutation(
    store = loanStore,
    key = loanId,
    networkMonitor = networkMonitor,
    fetchedAtRepository = fetchedAtRepo,
    cacheKey = "loans:detail:$loanId",
    submitHandler = viewModelScope.submitHandler<LoanForm>(),
    submitBlock = { form -> loanRepository.update(loanId, form); form },
    scope = viewModelScope,
)
val state = stream.state

fun onSave(form: LoanForm) = stream.submit(form)
fun onRetry() = stream.refresh()
val combined by viewModel.state.collectAsStateWithLifecycle()
ScreenContent(state = combined.read) { data ->
    LoanForm(initial = data, onSubmit = viewModel::onSave)
    SubmitProgressOverlay(visible = combined.mutation.isSubmitting)
    OutboxBadge(pending = combined.outboxPending, syncing = combined.isSyncing)
}
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/internal/ScreenWithMutationStreamImpl.kt:58</code></summary>

```kotlin
    scope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : ScreenWithMutationStream<R, W> {

    override val state: StateFlow<CombinedState<R, W>> = combine(
        readStream,
        submitHandler.state,
```

</details>

- `val state: StateFlow<CombinedState<R, W>>` — Combined read + write + outbox state. Backed by a hot `StateFlow` so the screen can `collectAsStateWithLifecycle()` without rebuilding the pipeline on each subscription.
- `fun refresh()` — Trigger a network refresh of the read pipeline. No-op if read-policy is `kpt.core.base.store.screen.FetchPolicy.CACHE_ONLY`. Mirrors `kpt.core.base.store.screen.ScreenDataStream.refresh`.
- `fun submit(payload: W)` — Submit `payload` through the wired write pipeline.
- `fun cancelPendingSubmit()` — Cancel an in-flight submission and return `CombinedState.mutation` to `kpt.core.base.store.submit.SubmitState.Idle`. Called by screens after the user dismisses an error dialog or navigates away.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt`

```kotlin
val StoreModule = module
```
Koin module providing base Store infrastructure. Consumer apps should include this module and add their own store bindings in their `core/data` DI module using `StoreFactory` to create store instances.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/di/KptStoreModule.kt:63</code></summary>

```kotlin
 * ```
 */
val appStoreModule: Module = module {
    // Framework write-SoT: the base-store module provides the single write door (MutationGateway)
    // + its Room-backed ConflictInbox. Every repo migrated onto `gateway.*` resolves `get()` here,
    // so a fork wiring `appStoreModule` gets the gateway for free (needs ConflictDao from
    // DatabaseModule + NetworkMonitor on the graph — both present in KoinModules.allModules).
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/error/ErrorCategory.kt`

```kotlin
sealed interface ErrorCategory
```
High-level classification of a `Throwable` for state-aware UI rendering. Pure logic — no platform calls, no side effects. Lives in `core-base/store` (state layer) so both UI and non-UI consumers can branch on the same categorization.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptErrorMapper.kt:49</code></summary>

```kotlin
 */
fun errorCategoryToken(error: Throwable): String = when (val cat = categorize(error)) {
    ErrorCategory.Network -> "network"
    ErrorCategory.Timeout.Connect -> "timeout_connect"
    ErrorCategory.Timeout.Read -> "timeout_read"
    ErrorCategory.Auth -> "auth"
    ErrorCategory.RateLimit -> "rate_limit"
```

</details>

```kotlin
fun categorize(error: Throwable): ErrorCategory
```
Classifies `error` into an `ErrorCategory` by inspecting its class name, message, and cause chain.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/error/OfflineException.kt`

```kotlin
class OfflineException(message: String = "Device is offline") : RuntimeException(message)
```
Synthetic signal that the device was offline at the moment a Store load was attempted. Used by `PagingScreenStream` (and any other framework loader) to fail fast without burning network retry budget when there is clearly no point trying.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt:176</code></summary>

```kotlin
            // the no-network treatment and the pull-to-refresh spinner can hide.
            if (forceRefresh && !isOnlineNow()) {
                error.value = OfflineException()
                isInitialLoading.value = false
                return@launch
            }
            val pageKey = PageKey.first(pageSize = pageSize, query = query)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessBand.kt`

```kotlin
enum class FreshnessBand
```
Pure time-relative staleness band for cached data — independent of network state. Computed by `FreshnessBands.bandFor` from `(now, lastSyncedAt, ttl, lastError)` alone.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessBands.kt:40</code></summary>

```kotlin
        ttl: Duration,
        lastError: Throwable?,
    ): FreshnessBand = when {
        lastError != null -> FreshnessBand.VeryStale
        lastSyncedAt == null -> FreshnessBand.Initial
        else -> {
            val age = now - lastSyncedAt
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessBands.kt`

```kotlin
object FreshnessBands
```
Pure computation of `FreshnessBand` from time + last-error inputs only. Decision table (first match wins): 1. `lastSyncedAt == null && lastError == null` → `FreshnessBand.Initial` 2.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DecisionEngine.kt:186</code></summary>

```kotlin
        val lastSyncedAt = storeData.fetchedAtInstant
        val lastError = storeData.error
        val band = FreshnessBands.bandFor(
            now = now,
            lastSyncedAt = lastSyncedAt,
            ttl = ttl,
            lastError = lastError,
```

</details>

- `fun bandFor(` — Derives the freshness band from a last-synced instant and a TTL. Pure, so the banding rule is unit-testable without standing up a Store.
- `val age = now - lastSyncedAt`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/freshness/FreshnessSignal.kt`

```kotlin
data class FreshnessSignal(
```
Pure-staleness signal carried by `kpt.core.base.store.screen.ScreenDataStream.freshness` alongside `state`. Decouples cache age from network connectivity.

<details><summary>Used in the template — <code>feature/pocket/src/commonMain/kotlin/kpt/feature/pocket/screens/ManagePocketScreen.kt:167</code></summary>

```kotlin
internal fun ManagePocketContent(
    linkedUiState: ScreenState<List<ManagePocketAccount>>,
    linkedFreshness: FreshnessSignal,
    onAction: (ManagePocketAction) -> Unit,
) {
    MifosScaffold(
        backPress = { onAction(ManagePocketAction.NavigateBack) },
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/ConflictStrategy.kt`

```kotlin
sealed interface ConflictStrategy<O>
```
Policy for reconciling a server-side value with a local-side value when both have diverged — the classic "I edited offline, the server also changed" conflict that surfaces during outbox sync, optimistic-UI rollback, and eventually-consistent reads. Wired into `StoreFactory.createMutableStore` as an optional parameter so forks can pick a default. The strategy itself is plain Kotlin — Store5's `org.mobilenativefoundation.store.store5.Updater` interface does not directly consume a `ConflictStrategy`; instead, the recommended pattern is for the fork's `Updater` block to call `conflictStrategy.resolve(server, client)` and write back the resolved value. The `StoreFactory` parameter is therefore exposed for *future* automated wiring + as a discovery hook (and the forks' Updater code documents which strategy is in use). Four shipping strategies cover the dominant cases — see the per-class KDoc. Forks needing bespoke merge logic should implement their own `ConflictStrategy<O>` directly.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/infra/ConflictStrategyTest.kt:30</code></summary>

```kotlin
    @Test
    fun `ServerWins returns server value`() {
        val strategy = ConflictStrategy.ServerWins<Record>()
        val server = Record(id = "S", updatedAt = 100L)
        val client = Record(id = "C", updatedAt = 200L)

        assertEquals(server, strategy.resolve(server, client))
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DecisionEngine.kt`

```kotlin
object DecisionEngine
```
Pure function combining StoreData metadata + NetworkStatus into ScreenState. No side effects, no coroutines — exhaustively unit testable. Uses full `NetworkStatus` (not just Boolean) to detect captive portals.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt:282</code></summary>

```kotlin
            isEmpty = itemList.isEmpty(),
        )
        DecisionEngine.decide(storeData, status)
    }

    return PagingScreenStream(
        state = screenState,
```

</details>

- `fun <T> decide(` — Maps `storeData` + `networkStatus` to the correct `ScreenState`.
- `val noData = storeData.isEmpty`
- `val error = storeData.error`
- `val isOnline = networkStatus is NetworkStatus.Available`
- `val isCaptivePortal = networkStatus is NetworkStatus.CaptivePortal`
- `val emptyCacheBehindError =`
- `val fetchedAt = storeData.fetchedAtInstant`
- `val networkStateNoLongerConsumedHere = isOnline || isCaptivePortal || (error != null)`
- `fun <T> decideFreshness(` — Pure sibling function: maps `storeData` + `ttl` to a `FreshnessSignal`. Decoupled from `decide`; runs in parallel and outputs the per-card freshness signal consumed by `FreshnessIndicator`.
- `val now = Clock.System.now()`
- `val lastSyncedAt = storeData.fetchedAtInstant`
- `val lastError = storeData.error`
- `val band = FreshnessBands.bandFor(`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DefaultValidator.kt`

```kotlin
class DefaultValidator<Output : Any>(
```
A TTL-based `Validator` that marks cached data as stale after a given duration. Tracks when data was last fetched using `TimeSource.Monotonic` and considers it invalid once the `ttl` has elapsed. Call `markFresh` when fresh data arrives.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/infra/DefaultValidatorTest.kt:23</code></summary>

```kotlin
    @Test
    fun isValid_beforeFirstFetch_returnsTrue() = runTest {
        val validator = DefaultValidator<String>(ttl = 5.minutes)
        assertTrue(validator.isValid("data"))
    }

    @Test
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DraftInventory.kt`

```kotlin
interface DraftInventory
```
Framework-shared, **cross-form** view over every draft the app is holding. `SubmitOutbox` is generic in a single payload type `P` (one instance per form).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsViewModel.kt:35</code></summary>

```kotlin
 */
class SyncAndDraftsViewModel(
    private val draftInventory: DraftInventory,
) : BaseViewModel<SyncAndDraftsUiState, Nothing, SyncAndDraftsAction>(SyncAndDraftsUiState.Loading) {

    init {
        draftInventory.observeAll()
```

</details>

- `fun observeAll(): Flow<List<DraftRecord>>` — Live cross-form feed of every non-terminal draft (PENDING / RETRYING / FAILED), newest-first by last-update. Terminal SUBMITTED rows are excluded — they are not actionable and are pruned by `StoreCacheManager.pruneExpiredDrafts`.
- `suspend fun discard(id: Long)` — Permanently discards a single draft by `id` (the per-row Discard). Irreversible — the caller confirms intent in the UI.
- `suspend fun retry(id: Long)` — Re-queues a draft by `id` for sync: transitions it back to PENDING and clears its error, so the per-form `kpt.core.base.store.submit.OfflineSubmitSyncer` re-attempts it on the next online transition.
- `suspend fun pruneExpired()` — Manual counterpart to the app-start `StoreCacheManager.pruneExpiredDrafts` — deletes SUBMITTED/FAILED rows older than `StoreCacheManager.DEFAULT_DRAFT_TTL_MS`. PENDING drafts are never pruned.

```kotlin
data class DraftRecord(
```
One draft row as seen by the cross-form `DraftInventory` — untyped (no deserialized payload), because the Sync & Drafts screen renders every form's drafts side-by-side and cannot know each `P`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:36</code></summary>

```kotlin
            uiState = SyncAndDraftsUiState.Success(
                drafts = listOf(
                    DraftRecord(3, "bill", "electricity", SubmitOutboxStatus.PENDING, 0, 0, null),
                    DraftRecord(4, "loan", "home-loan", SubmitOutboxStatus.PENDING, 0, 0, null),
                ),
                syncing = listOf(
                    DraftRecord(2, "alert", "btc-alert", SubmitOutboxStatus.RETRYING, 0, 0, null),
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/FetchedAtRepository.kt`

```kotlin
interface FetchedAtRepository
```
Persists "when was this Store's data last successfully fetched from network", keyed by `storeKey`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:59</code></summary>

```kotlin
    private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
    // transfer-detail Store5 vertical (GOAL D13) — makes the transaction-detail
    // drill-down offline-first. Nullable-default so existing unit tests without the
    // store harness continue to compile; the legacy getAccountTransfer(...) —
    // asScreenStateFlow shim over the raw Ktorfit flow — is unaffected.
```

</details>

- `suspend fun read(storeKey: String): Instant?` — Returns the last persisted timestamp for `storeKey`, or null if none.
- `suspend fun write(storeKey: String, instant: Instant)` — Persists `instant` as the latest fetch time for `storeKey`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreCacheManager.kt`

```kotlin
interface StoreCacheManager
```
Manages Store cache lifecycle. - Call `clearAll` on logout to prevent stale data leaking across user sessions. - Call `pruneExpiredDrafts` on app start to clean up old SUBMITTED/FAILED draft rows.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserLogoutManagerImpl.kt:34</code></summary>

```kotlin
class UserLogoutManagerImpl(
    private val repository: UserPreferencesRepository,
    private val storeCacheManager: StoreCacheManager,
    dispatcherManager: DispatcherManager,
) : UserLogoutManager {

    private val scope = CoroutineScope(dispatcherManager.unconfined)
```

</details>

- `suspend fun clearAll()` — Clears all store caches (in-memory + database). Call on logout.
- `suspend fun pruneExpiredDrafts(maxAgeMs: Long = DEFAULT_DRAFT_TTL_MS)` — Removes SUBMITTED and FAILED draft rows older than `maxAgeMs` milliseconds. PENDING drafts are never removed — users can still resume them offline. Default retention is 30 days. Call once on app start.
- `const val DEFAULT_DRAFT_TTL_MS: Long = 30L * 24 * 60 * 60 * 1000` — 30 days in milliseconds.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreFactory.kt`

```kotlin
object StoreFactory
```
Factory for creating `Store` and `MutableStore` instances with sensible defaults.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt:58</code></summary>

```kotlin
@CacheKey(name = "KEY", key = "userData")
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
            // Writes go through UserDataRepository's typed setters (see KDoc above); this
            // writer exists only to satisfy SourceOfTruth's shape and is never invoked,
```

</details>

- `fun <Key : Any, Input : Any, Output : Any> createStore(` — Creates a read-only `Store` where the fetcher output type matches the source of truth input type (no conversion needed).
- `fun <Key : Any, Output : Any> createMemoryStore(` — Creates a read-only `Store` backed only by a `Fetcher` (no local persistence). Data is cached in-memory only. Useful for transient data that doesn't need to survive process death.
- `fun <Key : Any, Output : Any> createOfflineStore(` — Creates a read-only `Store` backed only by a `SourceOfTruth` (no network fetcher).
- `fun <Key : Any, Output : Any> createOfflineMutableStore(` — Creates a `MutableStore` for a LOCAL-ONLY entity — every mutation flows through `store.write` / `store.clear` (→ the `sourceOfTruth` writer/delete, i.e. Room), but there is NO network.
- `val identity = Converter.Builder<Output, Output, Output>()`
- `val noopUpdater = Updater.by<Key, Output, Output>(`
- `fun <Key : Any, Network : Any, Local : Any, Output : Any> createMutableStore(` — Creates a `MutableStore` that supports reads, writes, and offline sync. Uses a `Converter` to transform between network, local, and output types.
- `fun <Key : Any, R : Any, W : Any> createScreenWithMutation(` — Creates a `ScreenWithMutationStream` — a fused read + write + sync seam for screens that both display data and submit mutations against the same domain object (edit forms, settings panels, in-place record updates).
- `val readStream = store.asScreenStream(`

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/StoreRegistry.kt`

```kotlin
abstract class StoreRegistry
```
Base registry for Store DI qualifiers (Koin).

<details><summary>Example</summary>

```kotlin
object AppStoreRegistry : StoreRegistry() {
    val ExchangeRates = store("exchangeRates")
    val CoinMarkets = store("coinMarkets")
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/DraftInventoryImpl.kt`

```kotlin
class DraftInventoryImpl(
```
Room-backed `DraftInventory`. A thin, framework-owned read/action facade over `DraftDao` that exposes the cross-form draft feed the Sync & Drafts screen renders.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt:75</code></summary>

```kotlin
    // Cross-form drafts inventory — the live feed + actions behind the template-level
    // Settings -> "Sync & Drafts" screen. Framework infra, so it belongs with the framework.
    single<DraftInventory> { DraftInventoryImpl(draftDao = get()) }
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomBookkeeper.kt`

```kotlin
class RoomBookkeeper<Key : Any>(
```
Room-backed `Bookkeeper` that persists sync-failure tracking across process restarts. Use this with `org.mobilenativefoundation.store.store5.MutableStore` to enable reliable offline-first writes that retry on connectivity restore.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/infra/impl/RoomBookkeeperTest.kt:45</code></summary>

```kotlin

    private fun bookkeeper(dao: FakeBookkeeperDao = FakeBookkeeperDao()) =
        RoomBookkeeper<Key>(dao) { "todo:${it.id}" }

    @Test
    fun unknownKeyHasNoFailedSync() = runTest {
        assertNull(bookkeeper().getLastFailedSync(Key(1)))
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomFetchedAtRepository.kt`

```kotlin
class RoomFetchedAtRepository(
```
Production `FetchedAtRepository` backed by Room (`framework_fetched_at` table). Wired into Koin in `RepositoryModule`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt:54</code></summary>

```kotlin
    // core-base/store, and the only dependency arrives through `get()`. ScreenStreamContext right
    // above already consumes it, so the binding now sits beside its consumer instead of a layer up.
    single<FetchedAtRepository> { RoomFetchedAtRepository(get()) }

    single { ScreenStreamContext(networkMonitor = get(), fetchedAtRepository = get()) }

    // Moved DOWN from core/store's appStoreModule. Both the TYPE and the IMPL are core-base
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomSubmitOutbox.kt`

```kotlin
class RoomSubmitOutbox<P>(
```
Room-backed `SubmitOutbox` that persists form payloads across process death. Serializes payloads to JSON via `kotlinx.serialization` and stores them in the `framework_submit_drafts` table in `kpt.core.database.AppDatabase`.

<details><summary>Example</summary>

```kotlin
single<SubmitOutbox<LoanApplicationPayload>> {
    RoomSubmitOutbox(get<AppDatabase>().draftDao, LoanApplicationPayload.serializer())
}
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/RoomSubmitOutboxReactiveInvalidationTest.kt:37</code></summary>

```kotlin

    private val dao = FakeDraftDao()
    private val outbox = RoomSubmitOutbox(dao, String.serializer())

    @Test
    fun observePendingReEmitsAcrossSaveThenSubmit() = runTest {
        outbox.observePending(FORM).test {
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/StoreCacheManagerImpl.kt`

```kotlin
class StoreCacheManagerImpl(
```
Registration-based cache manager. Feature DI modules call `register` for each Store that should be cleared on logout; foundation infrastructure stays free of feature knowledge.

<details><summary>Example</summary>

```kotlin
(get<StoreCacheManager>() as StoreCacheManagerImpl)
    .register(get(AppStoreRegistry.YourStore))
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt:67</code></summary>

```kotlin
    // appStoreModule, so the same definitions reach the same container either way.
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt`

```kotlin
class DefaultMutationGateway(
```
The default `MutationGateway` — composes the existing Store5 write machinery.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:27</code></summary>

```kotlin
 */
fun testMutationGateway(isOnline: Boolean = true): MutationGateway =
    DefaultMutationGateway(isOnline = { isOnline }, conflictInbox = NoopConflictInbox)

private object NoopConflictInbox : ConflictInbox {
    override suspend fun record(
        entity: String,
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/MutationGateway.kt`

```kotlin
interface MutationGateway
```
The single WRITE door for every mutation in the app.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:26</code></summary>

```kotlin
 * re-emit after a gateway-routed write. No fake gateway: the repos are tested against the real seam.
 */
fun testMutationGateway(isOnline: Boolean = true): MutationGateway =
    DefaultMutationGateway(isOnline = { isOnline }, conflictInbox = NoopConflictInbox)

private object NoopConflictInbox : ConflictInbox {
    override suspend fun record(
```

</details>

- `suspend fun <K : Any, V : Any> upsert(` — Upsert `value` under `key` through `store`. - `MutationPolicy.Optimistic`: the local write lands first (UI updates instantly); the network sync is queued via the store's `Updater` + `Bookkeeper` and retried on reconnect.
- `suspend fun <K : Any, V : Any> delete(` — Delete the entity for `key` through `store`, syncing the delete to the network via `deleteEndpoint`.
- `suspend fun <P : Any, R : Any> command(` — Run an RPC / command / form mutation described by `spec` (approve, submit, pay, …) — an operation that is not a plain keyed value write. Composes the durable submit outbox for offline retry.
- `suspend fun localMutation(table: String, mutate: suspend () -> Unit): MutationResult<Unit>` — Local-only mutation for a CACHE_ONLY feature (no `MutableStore` / `Updater`): runs `mutate` as a write-through to Room inside a plain DAO write, so the table's reactive readers (Store streams AND Dao the Room `Flow` queries) re-emit.

```kotlin
sealed interface MutationPolicy
```
How a mutation reaches the network relative to the local write. Two policies only — "optimistic" already means queue-and-retry; there is no separate queue-only mode.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt:43</code></summary>

```kotlin
        key: K,
        value: V,
        policy: MutationPolicy,
    ): MutationResult<V> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
```

</details>

```kotlin
sealed interface MutationResult<out T>
```
The exhaustive outcome of a mutation. The caller (ViewModel) must handle every arm, so an offline write or a conflict can never be silently swallowed.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt:44</code></summary>

```kotlin
        value: V,
        policy: MutationPolicy,
    ): MutationResult<V> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
```

</details>

```kotlin
enum class BlockReason
```
Why an `MutationPolicy.OnlineRequired` mutation was `MutationResult.Blocked`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt:46</code></summary>

```kotlin
    ): MutationResult<V> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
            store.write(StoreWriteRequest.of<K, V, Any>(key = key, value = value))
            // Optimistic offline write is persisted locally + queued in the Bookkeeper (synced=false);
```

</details>

```kotlin
class CommandSpec<P : Any, R : Any>(
```
Describes a command / RPC mutation for `MutationGateway.command`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt:96</code></summary>

```kotlin

    override suspend fun <P : Any, R : Any> command(
        spec: CommandSpec<P, R>,
        policy: MutationPolicy,
    ): MutationResult<R> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/conflict/ConflictInbox.kt`

```kotlin
interface ConflictInbox
```
Durable inbox of write conflicts surfaced to the user in Settings.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:29</code></summary>

```kotlin
    DefaultMutationGateway(isOnline = { isOnline }, conflictInbox = NoopConflictInbox)

private object NoopConflictInbox : ConflictInbox {
    override suspend fun record(
        entity: String,
        key: String,
        localPayloadJson: String,
```

</details>

- `suspend fun record(` — Record a conflict; returns the new conflict id.
- `fun observePending(): Flow<List<ConflictEntry>>` — Observe the pending (unresolved) conflicts, newest first — drives the Settings badge + list.
- `suspend fun resolve(conflictId: String, resolution: ConflictResolution)` — Resolve a recorded conflict; the entry is cleared once resolution is applied by the caller.

```kotlin
enum class ConflictResolution
```
How the user chose to settle a `ConflictEntry`.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:40</code></summary>

```kotlin
    override fun observePending(): Flow<List<ConflictEntry>> = flowOf(emptyList())

    override suspend fun resolve(conflictId: String, resolution: ConflictResolution) = Unit
}
```

</details>

```kotlin
data class ConflictReport(
```
A conflict the caller detected between its local payload and the server result — returned by a command's `conflictOf` so the gateway can record it.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/MutationGateway.kt:169</code></summary>

```kotlin
    val resultKeyOf: ((R) -> Any)? = null,
    /** Detect a conflict between the local payload and the server result; non-null → recorded + Conflicted. */
    val conflictOf: ((P, R) -> ConflictReport?)? = null,
)
```

</details>

```kotlin
data class ConflictEntry(
```
A single recorded write conflict awaiting user resolution.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/TestMutationGateway.kt:38</code></summary>

```kotlin
    ): String = "noop"

    override fun observePending(): Flow<List<ConflictEntry>> = flowOf(emptyList())

    override suspend fun resolve(conflictId: String, resolution: ConflictResolution) = Unit
}
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/conflict/impl/RoomConflictInbox.kt`

```kotlin
class RoomConflictInbox(
```
Room-backed `ConflictInbox` — persists write conflicts in the `framework_write_conflicts` table so they survive process death and are surfaced in Settings.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt:40</code></summary>

```kotlin
    // Room-backed conflict inbox surfaced in Settings.
    single<ConflictInbox> {
        RoomConflictInbox(dao = get(), now = { Clock.System.now().toEpochMilliseconds() })
    }
    // The single write door — composes MutableStore.write + the conflict inbox + connectivity.
    single<MutationGateway> {
        DefaultMutationGateway(
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/delete/DeleteSync.kt`

```kotlin
class DeleteSync<K : Any>(
```
The network-DELETE-with-sync primitive Store5 lacks (its `Updater` is write-only).

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/mutation/DefaultMutationGateway.kt:81</code></summary>

```kotlin
                // DeleteSync primitive (behaviour unit-tested in DeleteSyncTest); clearing the row is the
                // only op that can throw here.
                val deleteSync = DeleteSync(
                    clearLocal = { store.clear(it) },
                    deleteEndpoint = deleteEndpoint,
                    bookkeeper = bookkeeper,
                    isOnline = isOnline,
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt`

```kotlin
class PagingScreenStream<T : Any> internal constructor(
```
Paginated variant of `kpt.core.base.store.screen.ScreenDataStream`. Manages page loading, appending, error surfacing, and unified state for infinite lists.

<details><summary>Example</summary>

```kotlin
val pagingStream = clientStore.asPagingScreenStream(
    networkMonitor = networkMonitor,
    scope = viewModelScope,
    pageSize = 20,
)
val uiState = pagingStream.state  // Flow<ScreenState<List<Client>>>
fun loadMore() = pagingStream.loadNextPage()
```

</details>

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptPullToRefreshState.kt:85</code></summary>

```kotlin
@Composable
fun rememberKptPullToRefreshState(
    pagingStream: PagingScreenStream<*>,
    isEnabled: Boolean = true,
): KptPullToRefreshState {
    val state by pagingStream.state.collectAsState(ScreenState.Loading)
    val isRefreshing = (state as? ScreenState.Content<*>)?.freshnessSignal?.isRefreshing == true
```

</details>

```kotlin
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
```
Creates a `PagingScreenStream` with network-fused state via cmp-network-monitor.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:406</code></summary>

```kotlin
                )
                PersonItem(
                    name = "Grocery Store",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Restaurant",
```

</details>

```kotlin
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
```
`asPagingScreenStream` overload taking a bundled `ScreenStreamContext` instead of the two infra deps — so a paginated repository reads `store.asPagingScreenStream(screen, cacheKey, scope, …)` with `screen` its one injected `ScreenStreamContext`. Delegates to the primary overload.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:406</code></summary>

```kotlin
                )
                PersonItem(
                    name = "Grocery Store",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Restaurant",
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/StorePagingSource.kt`

```kotlin
data class PageKey(
```
Key for paginated Store requests. Use this as the Store key type when the data source supports pagination. The Store will cache each page independently.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt:68</code></summary>

```kotlin
    val loadMoreError: StateFlow<Throwable?>,
    private val scope: CoroutineScope,
    private val store: Store<PageKey, List<T>>,
    private val pageSize: Int,
    private val query: String?,
    private val items: MutableStateFlow<List<T>>,
    private val hasMoreMutable: MutableStateFlow<Boolean>,
```

</details>

```kotlin
suspend fun <Value : Any> Store<PageKey, List<Value>>.loadPage(
```
Triggers a Store refresh for a specific page and suspends until data arrives. Use this inside a PagingSource.load() implementation to let Store handle caching while Paging handles the pagination UX.

<details><summary>Example</summary>

```kotlin
class ClientPagingSource(
    private val store: Store<PageKey, List<Client>>,
) : PagingSource<Int, Client>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Client> {
        val page = params.key ?: 0
        val pageKey = PageKey(page = page, pageSize = params.loadSize)
        return store.loadPage(pageKey)
    }
}
```

</details>

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:406</code></summary>

```kotlin
                )
                PersonItem(
                    name = "Grocery Store",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Restaurant",
```

</details>

```kotlin
sealed class StorePageResult<out T>
```
Result of a paginated Store load, matching PagingSource.LoadResult shape.

<details><summary>Example</summary>

```kotlin
when (result) {
    is StorePageResult.Success -> LoadResult.Page(result.items, result.prevKey, result.nextKey)
    is StorePageResult.Error -> LoadResult.Error(result.error)
}
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/paging/PagingScreenStream.kt:95</code></summary>

```kotlin
            // Pull-to-refresh uses the dedicated refresh() path.
            when (val result = store.loadPage(pageKey, refresh = false)) {
                is StorePageResult.Success -> {
                    items.update { it + result.items }
                    currentPage.value = nextPage
                    hasMoreMutable.value = result.nextKey != null
                    // Only update on actual network success — cache hits keep the previous
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushEvent.kt`

```kotlin
sealed interface PushEvent<K, V>
```
Server-originated push notification that mutates a `org.mobilenativefoundation.store.store5.MutableStore`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushStoreAdapter.kt:64</code></summary>

```kotlin
class PushStoreAdapter<K : Any, V : Any>(
    private val store: MutableStore<K, V>,
    private val events: Flow<PushEvent<K, V>>,
    private val mergeStrategy: PushMergeStrategy<V> = PushMergeStrategy.Replace,
    private val scope: CoroutineScope,
    private val readCurrent: (suspend (K) -> V?)? = null,
) {
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushMergeStrategy.kt`

```kotlin
sealed interface PushMergeStrategy<out V>
```
How a `PushEvent.Update` should be combined with the existing local value held by the `org.mobilenativefoundation.store.store5.MutableStore`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushStoreAdapter.kt:65</code></summary>

```kotlin
    private val store: MutableStore<K, V>,
    private val events: Flow<PushEvent<K, V>>,
    private val mergeStrategy: PushMergeStrategy<V> = PushMergeStrategy.Replace,
    private val scope: CoroutineScope,
    private val readCurrent: (suspend (K) -> V?)? = null,
) {
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/push/PushStoreAdapter.kt`

```kotlin
class PushStoreAdapter<K : Any, V : Any>(
```
Bridges a transport-side `Flow` of `PushEvent`s into a Store5 `MutableStore`, applying the configured `PushMergeStrategy` per `Update` event.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/push/PushStoreAdapterTest.kt:46</code></summary>

```kotlin
    fun `Insert event dispatches store write with the incoming value`() = runTest {
        val store = FakeMutableStore<String, String>()
        val adapter = PushStoreAdapter(
            store = store,
            events = flowOf<PushEvent<String, String>>(PushEvent.Insert("k1", "v1")),
            scope = TestScope(testScheduler),
        )
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/FetchPolicy.kt`

```kotlin
sealed interface FetchPolicy
```
Controls whether a screen stream reads from cache, hits the network, or both. Pass to `ScreenDataStream.asScreenStream`, `LoadOnceStream.asLoadOnceStream`, or `PagingScreenStream` to override that entry point's default.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:151</code></summary>

```kotlin
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.SELF_ACCOUNTS,
        )
    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/LoadOnceStream.kt`

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asLoadOnceStream(
```
Load-once variant of `asScreenStream` for edit/mutation screens. Transitions: `Loading → Content(initialData)` — then stops. Further Store updates are ignored so in-progress user edits are never overwritten by background refreshes.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenDataStream.kt`

```kotlin
class ScreenDataStream<T> internal constructor(
```
One screen's read surface: a cold `ScreenState` flow plus the refresh/retry controls that drive it.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:81</code></summary>

```kotlin

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>> {
        return screenDataStreamForTesting(
            state = if (shouldReturnError) {
                flowOf(StoreScreenState.Error(Throwable("Network error")))
            } else {
```

</details>

```kotlin
annotation class ExperimentalScreenDataStreamTestingApi
```
Opt-in marker for the testing-only `ScreenDataStream` constructors.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:80</code></summary>

```kotlin
    }

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>> {
        return screenDataStreamForTesting(
            state = if (shouldReturnError) {
                flowOf(StoreScreenState.Error(Throwable("Network error")))
```

</details>

```kotlin
fun <T> screenDataStreamForTesting(
```
Construct a `ScreenDataStream` from an arbitrary `state` flow and a caller-owned `refreshTrigger`.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Creates a `ScreenDataStream` from this Store, fusing network state via cmp-network-monitor.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Overload accepting a Flow<Key> for dynamic keys (e.g., selected client from DataStore). Re-streams from Store when key changes. Resets lastContent on key change.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenState.kt`

```kotlin
sealed interface ScreenState<out T>
```
Unified UI state produced by `ScreenDataStream`. Replaces per-ViewModel ScreenUiState + isFromCache + isRefreshing + networkStatus.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:75</code></summary>

```kotlin
            } else {
                flowOf(
                    ScreenState.Error(IllegalStateException("Client ID not available")) as ScreenState<kpt.core.model.account.AccountContent>,
                )
            }
        }
        .mapLatest { screenState ->
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenStateExtensions.kt`

```kotlin
fun <T, R> Flow<ScreenState<T>>.mapContent(
```
Transforms only `ScreenState.Content` data, passing through all other states unchanged.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <T, S, R> Flow<ScreenState<T>>.combineContent(
```
Combines ScreenState with a local state flow for reactive filter/sort/preferences.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <T> Flow<ScreenState<T>>.emptyIfContent(
```
Converts Content to Empty when business-level predicate says data is empty. Applied AFTER DecisionEngine (which only handles structural empty from Store).

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
val <T> ScreenState<T>.dataOrNull: T?
```
Extracts data from Content state or null for other states.

```kotlin
val <T> ScreenState<T>.hasContent: Boolean
```
True if state has displayable content.

```kotlin
fun <T> Flow<ScreenState<T>>.mapError(
```
Maps Error throwable to a user-facing type.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <T, R> SubmitHandler<R>.submitWhenContent(
```
Execute `block` only when `screenState` has loaded content, passing its data. No-op when state is Loading / Error / NoNetwork / Empty — safe to call at any time. Guards against premature submits before data arrives on edit screens.

```kotlin
fun <T, R> ScreenState<T>.canInteract(submitState: SubmitState<R>): Boolean =
```
True when the screen has content AND no submission is in-flight.

<details><summary>Example</summary>

```kotlin
Button(enabled = screenState.canInteract(submitState)) { … }
```

</details>

```kotlin
fun <T> T.asLocalScreenState(): ScreenState<T> =
```
Wraps any value in a `ScreenState.Content` with `DataFreshness.FRESH`. Use for utility or settings screens that hold local state and don't use Store5.

<details><summary>Example</summary>

```kotlin
val screenState = stateFlow
    .map { it.asLocalScreenState() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)
```

</details>

```kotlin
fun <T> Flow<T>.asLocalScreenStream(): Flow<ScreenState<T>> =
```
Turns any `Flow`<T> into a `Flow`<`ScreenState`<T>> for use with `ScreenContent`.

```kotlin
fun <A, B, R> combineScreenStates(
```
Combines two independent `ScreenState` flows into one, merging their `ScreenState.Content` data via `transform` when both sources have loaded.

<details><summary>Example</summary>

```kotlin
val screenState: Flow<ScreenState<DashboardUiState>> = combineScreenStates(
    a = accountStore.asScreenStream(Unit),
    b = transactionStore.asScreenStream(Unit),
) { account, transactions ->
    DashboardUiState(account = account, transactions = transactions)
}
```

</details>

```kotlin
fun <A, B, C, R> combineScreenStates(
```
Combines three independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun <A, B, C, D, R> combineScreenStates(
```
Combines four independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun <A, B, C, D, E, R> combineScreenStates(
```
Combines five independent `ScreenState` flows. See `combineScreenStates` for priority rules.

```kotlin
fun combineScreenStates(
```
Combines N independent `ScreenState` flows (vararg form). The resulting `ScreenState.Content` holds a `List<Any?>` of the per-source `Content.data` values in input order.

<details><summary>Example</summary>

```kotlin
val combined: Flow<ScreenState<List<Any?>>> = combineScreenStates(
    accountStream, transactionStream, ratesStream, billsStream, loansStream, alertsStream,
)
```

</details>

```kotlin
fun combineScreenStates(
```
Combines N independent `ScreenState` flows (list form). Mirror of the vararg overload — convenient when callers already have a `List<Flow<ScreenState<*>>>` (e.g. a dynamic collection built per-screen).

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/ScreenStreamContext.kt`

```kotlin
class ScreenStreamContext(
```
Bundles the app-infra dependencies that `asScreenStream` needs — the `NetworkMonitor` and the `FetchedAtRepository` — so a repository injects ONE screen-stream context instead of threading two framework singletons through every read method. Homed here in `core-base/store` next to `asScreenStream` and DI-provided once (a single). Note: with Koin constructor injection, one injected param is the floor — a repository can't reach zero without a `KoinComponent` service-locator (an anti-pattern). This collapses the two scattered infra params into one clearly-named context owned by `core-base/store`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/di/StoreModule.kt:56</code></summary>

```kotlin
    single<FetchedAtRepository> { RoomFetchedAtRepository(get()) }

    single { ScreenStreamContext(networkMonitor = get(), fetchedAtRepository = get()) }

    // Moved DOWN from core/store's appStoreModule. Both the TYPE and the IMPL are core-base
    // (`kpt.core.base.store.infra`), and both dependencies arrive through `get()` — so this binding
    // never needed to sit in the fork-facing layer. It sat there only because that is where it was
```

</details>

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
`asScreenStream` overload taking a bundled `ScreenStreamContext` instead of the two infra deps — so a repository reads `store.asScreenStream(key, screen, cacheKey, scope, …)` with `screen` its one injected `ScreenStreamContext`.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
```
Dynamic-key `asScreenStream` overload taking a bundled `ScreenStreamContext` — for repositories whose key changes over time (`keyFlow`) with a per-key `cacheKeyFor`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreData.kt`

```kotlin
data class StoreData<out T>(
```
Wraps data with metadata about its origin, freshness, and refresh state.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DecisionEngine.kt:49</code></summary>

```kotlin
     */
    fun <T> decide(
        storeData: StoreData<T>,
        networkStatus: NetworkStatus,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
    ): ScreenState<T> {
        val noData = storeData.isEmpty
```

</details>

```kotlin
enum class DataOrigin
```
Indicates where a `StoreData` emission originated.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/DecisionEngine.kt:68</code></summary>

```kotlin
        // returned, is real content and keeps the has-data branch.
        val emptyCacheBehindError =
            error != null && storeData.origin != DataOrigin.NETWORK && storeData.data.isEmptyCollection()
        if (noData || emptyCacheBehindError) return decideNoData(fetchPolicy, error, isOnline, isCaptivePortal)

        // === Has data branch ===
        // Phase A of data-freshness-redesign epic (2026-06-17): network state no longer
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreDataExtensions.kt`

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamData(
```
Streams data from a `Store` with full `StoreData` metadata. This is the primary API for repositories.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamDataWithErrors(
```
Like `streamData` but also emits on errors with last known data preserved.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.freshData(
```
Forces a fresh network fetch, ignoring any cached data. Useful for pull-to-refresh or explicit "reload" actions.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.refreshFresh(
```
Sibling fresh-fetch path for Store5 S5-5 — the counterpart to `FetchPolicy.CACHE_FIRST_SWR`'s cache-first read.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.localData(
```
Reads only from local cache/database, no network. Useful for offline-only screens or pre-fetched data.

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.streamDataNoFallback(
```
Like `streamDataWithErrors` but without requiring a fallback value. Emits StoreData with isEmpty=true when error arrives before any data. Used by ScreenDataStream where DecisionEngine handles the no-data case.

```kotlin
fun <T, R> StoreData<T>.map(transform: (T) -> R): StoreData<R>
```
Maps `StoreData` content while preserving all metadata.

```kotlin
fun <T, R> Flow<StoreData<T>>.mapData(transform: (T) -> R): Flow<StoreData<R>>
```
Maps a Flow of `StoreData` content while preserving all metadata.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <Key : Any, Output : Any> Store<Key, Output>.skipMemoryData(
```
Bypasses in-memory cache, reads from SourceOfTruth + optional network refresh. Useful when you know in-memory state may be stale but disk is authoritative.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreDataMapper.kt`

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToStoreData(
```
Maps a `StoreReadResponse` flow into `Flow<StoreData<Output>>`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToStoreDataWithErrors(
```
Like `mapToStoreData` but also emits on errors, carrying the last known data.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/screen/StoreResponseMapper.kt`

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToResult(): Flow<Result<Output>>
```
Maps a `StoreReadResponse` flow to `Flow<Result<Output>>`. - `StoreReadResponse.Data` → `Result.success` - `StoreReadResponse.Error` → `Result.failure` - `StoreReadResponse.Loading` and `StoreReadResponse.NoNewData` are filtered out.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
fun <Output : Any> Flow<StoreReadResponse<Output>>.mapToData(): Flow<Output>
```
Maps a `StoreReadResponse` flow to `Flow<Output>`, emitting only data values. Loading, error, and no-data responses are silently filtered out. Use `mapToResult` when error handling is needed.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

```kotlin
suspend fun <Output : Any> StoreReadResponse<Output>.requireData(): Output
```
Returns the first `StoreReadResponse.Data` value or throws on error.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitHandler.kt`

```kotlin
class BatchSubmitHandler<P>(
```
Orchestrator for submitting a list of payloads via a single per-payload `submitBlock`. Modes: - `BatchSubmitMode.AllOrNothing` — abort on first failure; remaining payloads are not attempted.

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/submit/BatchSubmitHandlerTest.kt:32</code></summary>

```kotlin
    @Test
    fun `AllOrNothing all payloads succeed produces full success result`() = runTest {
        val handler = BatchSubmitHandler<Int>(BatchSubmitMode.AllOrNothing)
        val attempted = mutableListOf<Int>()

        val result = handler.submitBatch(listOf(1, 2, 3)) { _, payload, _ ->
            attempted += payload
```

</details>

```kotlin
data class BatchResult(
```
Aggregate outcome of `BatchSubmitHandler.submitBatch`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitMode.kt`

```kotlin
sealed interface BatchSubmitMode
```
Semantics for `BatchSubmitHandler.submitBatch` when one or more payloads fail. - `AllOrNothing` — fail-fast: the first failure aborts the remaining submissions and the resulting `BatchResult` records only the payloads attempted so far.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitHandler.kt:40</code></summary>

```kotlin
 */
class BatchSubmitHandler<P>(
    private val mode: BatchSubmitMode = BatchSubmitMode.AllOrNothing,
) {

    /**
     * Submit each payload in [payloads] via [submitBlock], applying the configured [mode].
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/DraftResumeState.kt`

```kotlin
sealed interface DraftResumeState<out P>
```
The UI state emitted by a draft-resume stream. Screens collect this to show a "Resume previous submission?" banner when a PENDING draft exists, or to silently prefill a form field set from a saved draft.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/MutationUiState.kt:34</code></summary>

```kotlin
    val submit: SubmitState<R> = SubmitState.Idle,
    /** Whether an unsent draft was found for this form, so the screen can offer to resume it. */
    val resume: DraftResumeState<T> = DraftResumeState.None,
) {
    /** True when data is loaded and no submission is in-flight. Use to enable the submit button. */
    val canInteract: Boolean
        get() = screen is ScreenState.Content && submit !is SubmitState.Submitting
```

</details>

```kotlin
fun <P> SubmitOutbox<P>.resumeStateFor(formKey: String): Flow<DraftResumeState<P>> =
```
Converts a `SubmitOutbox.observePending` flow into a typed `DraftResumeState` flow.

<details><summary>Example</summary>

```kotlin
val draftState: StateFlow<DraftResumeState<LoanPayload>> =
    outbox.resumeStateFor("loan_application")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DraftResumeState.None)
```

</details>

```kotlin
fun <P> SubmitOutbox<P>.resumeStateFor(formKey: String, uniqueKey: String?): Flow<DraftResumeState<P>> =
```
Multi-pending-aware overload. When `uniqueKey` is non-null, observes the PENDING draft for that exact `(formKey, uniqueKey)` pair (one of N concurrent drafts); when null, behaves exactly like the singleton `resumeStateFor` above.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/DraftSubmitHandler.kt`

```kotlin
class DraftSubmitHandler<P, R>(
```
Out-of-box form submission handler with user-prompted draft save and restore/discard support. **Two save modes — controlled by `autoSaveDraft`:** `autoSaveDraft = false` (default) — **user-prompted save:** 1.

<details><summary>Example</summary>

```kotlin
// User-prompted (default):
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox  = roomSubmitOutbox,
    formKey = "loan_application",
)
// Auto-save:
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox         = roomSubmitOutbox,
    formKey        = "loan_application",
    autoSaveDraft  = true,
)
val submitState = draftHandler.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubmitState.Idle)

fun onSubmit(payload: LoanPayload) = draftHandler.submit(payload) { repository.submitLoan(it) }
fun onRetry()              = draftHandler.retry()
fun onSaveDraft()          = draftHandler.saveDraft()          // user-prompted mode only
fun onDismissDraftPrompt() = draftHandler.discardDraft()       // user-prompted mode only
```

</details>

```kotlin
fun <P, R> CoroutineScope.draftSubmitHandler(
```
Creates a `DraftSubmitHandler` bound to this `CoroutineScope` (typically `viewModelScope`).

<details><summary>Example</summary>

```kotlin
// User-prompted save (default):
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox  = roomSubmitOutbox,
    formKey = "client_registration",
)
// Silent auto-save on network failure:
private val draftHandler = viewModelScope.draftSubmitHandler<LoanPayload, Unit>(
    outbox        = roomSubmitOutbox,
    formKey       = "client_registration",
    autoSaveDraft = true,
)
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/IdempotencyKey.kt`

```kotlin
object IdempotencyKey
```
Idempotency-key generator for client-side de-duplication of retried submissions. The contract: a caller generates one key per logical user intent (e.g.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/BatchSubmitHandler.kt:59</code></summary>

```kotlin
    suspend fun submitBatch(
        payloads: List<P>,
        idempotencyKey: String = IdempotencyKey.generate(),
        submitBlock: suspend (index: Int, payload: P, key: String) -> Unit,
    ): BatchResult = when (mode) {
        BatchSubmitMode.AllOrNothing -> submitAllOrNothing(payloads, idempotencyKey, submitBlock)
        BatchSubmitMode.Independent -> submitIndependent(payloads, idempotencyKey, submitBlock)
```

</details>

- `fun generate(): String = Uuid.random().toString()` — Generate a fresh, cryptographically-random idempotency key.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/MutationUiState.kt`

```kotlin
data class MutationUiState<out T, out R>(
```
Combined UI state for edit/mutation screens that load existing data before allowing changes.

<details><summary>Example</summary>

```kotlin
data class EditClientState(
    val mutation: MutationUiState<ClientDetail, Unit> = MutationUiState(),
)
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/submit/MutationUiStateTest.kt:25</code></summary>

```kotlin
    @Test
    fun defaults_screenIsLoading_submitIsIdle() {
        val state = MutationUiState<String, Unit>()
        assertEquals(ScreenState.Loading, state.screen)
        assertEquals(SubmitState.Idle, state.submit)
    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OfflineSubmitSyncer.kt`

```kotlin
class OfflineSubmitSyncer<P, R>(
```
Retries all PENDING outbox entries whenever the device regains connectivity. Wire this once in the app's DI scope (e.g.

<details><summary>Example</summary>

```kotlin
val syncer = coroutineScope.offlineSubmitSyncer(
    outbox             = roomSubmitOutbox,
    networkStatusFlow  = networkMonitor.networkStatus,
    submitBlock        = { payload -> repository.submitLoanApplication(payload) },
    retryOnStatus      = RetryOnNetworkStatus.OnlineOnly,   // default
)
syncer.start()
```

</details>

```kotlin
fun <P, R> CoroutineScope.offlineSubmitSyncer(
```
Creates an `OfflineSubmitSyncer` bound to this `CoroutineScope`.

<details><summary>Example</summary>

```kotlin
val syncer = viewModelScope.offlineSubmitSyncer(
    outbox             = roomSubmitOutbox,
    networkStatusFlow  = networkMonitor.networkStatus,
    submitBlock        = { payload -> api.submit(payload) },
)
syncer.start()
```

</details>

```kotlin
typealias OfflineSubmitSyncerCrashSeverity = CrashSeverity
```
Re-exported for callers that want to set retry-classification severity in their own code. Currently informational only — `OfflineSubmitSyncer` records exceptions at the default severity (no `CrashSeverity` argument needed).

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OptimisticSubmit.kt`

```kotlin
suspend fun <P, R> SubmitHandler<R>.optimisticSubmit(
```
Optimistic-UI submission orchestration: mutate the local store immediately, then perform the network call, and roll back if the network call fails.

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<Item>()

fun onLike(item: Item) = viewModelScope.launch {
    submit.optimisticSubmit(
        payload  = item,
        apply    = { p -> localStore.update(p.copy(liked = true)) },
        network  = { p -> api.like(p.id) },
        rollback = { p, _ -> localStore.update(p.copy(liked = false)) },
    )
}
```

</details>

```kotlin
suspend fun <Key : Any, Value : Any, P, R> MutableStore<Key, Value>.optimisticSubmit(
```
`MutableStore` variant of `optimisticSubmit` — identical semantics, just a different receiver for IDE discoverability when the caller is holding a `MutableStore` rather than a `SubmitHandler`.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/RetryOnNetworkStatus.kt`

```kotlin
sealed interface RetryOnNetworkStatus
```
Policy controlling when `OfflineSubmitSyncer` retries pending outbox entries. Default (`OnlineOnly`) preserves the pre-2026-05-27 behavior — retry only when fully online. Forks whose API is reachable behind a captive portal (e.g.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OfflineSubmitSyncer.kt:64</code></summary>

```kotlin
    private val networkStatusFlow: Flow<NetworkStatus>,
    private val submitBlock: suspend (P) -> R,
    private val retryOnStatus: RetryOnNetworkStatus = RetryOnNetworkStatus.OnlineOnly,
    private val crashReporter: CrashReporter? = null,
    /**
     * Per-attempt retry-backoff policy — APPLIED (v14+). Each entry carries a persisted
     * `attemptCount`; [retryAll] waits [RetryPolicy.delayFor] before re-submitting and stops
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/RetryPolicy.kt`

```kotlin
data class RetryPolicy(
```
Exponential-backoff-with-jitter retry policy for outbox-style retries. Pure data — no scheduler, no side effects. Consumers compute the next delay via `delayFor` and apply it themselves (e.g. `delay(policy.delayFor(attempt))`).

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/OfflineSubmitSyncer.kt:74</code></summary>

```kotlin
     * 1s/2s/4s backoff, ±25% jitter).
     */
    private val retryPolicy: RetryPolicy = RetryPolicy(),
) {

    /**
     * Begin watching [networkStatusFlow]. The status is mapped through [retryOnStatus] to
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitHandler.kt`

```kotlin
class SubmitHandler<R> internal constructor(
```
Reusable one-shot executor for a single form/action submission lifecycle.

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<ClientId>()
val submitState    = submit.state
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubmitState.Idle)

fun onSave(form: ClientForm) = submit.submit { repository.createClient(form) }
fun onRetry()               = submit.retry()
fun onDismiss()             = submit.reset()
Box(Modifier.fillMaxSize()) {
    FormContent(enabled = !submitState.isSubmitting, onSubmit = { viewModel.onSave(it) })
    SubmitProgressOverlay(visible = submitState.isSubmitting)
    SubmitResultHandler(
        state = submitState,
        onSubmitted = { onNavigateBack() },
        onFailed = { error, category -> viewModel.onDismiss(); showError(error, category) },
    )
}
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/combine/internal/ScreenWithMutationStreamImpl.kt:52</code></summary>

```kotlin
internal class ScreenWithMutationStreamImpl<R, W>(
    readStream: Flow<ScreenState<R>>,
    private val submitHandler: SubmitHandler<W>,
    private val submitBlock: suspend (W) -> W,
    pendingCountFlow: Flow<Int>,
    syncingFlow: Flow<Boolean>,
    scope: CoroutineScope,
```

</details>

```kotlin
fun <R> CoroutineScope.submitHandler(): SubmitHandler<R> = SubmitHandler(this)
```
Creates a `SubmitHandler` bound to this `CoroutineScope` (typically `viewModelScope`).

<details><summary>Example</summary>

```kotlin
private val submit = viewModelScope.submitHandler<Unit>()
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitMessages.kt`

```kotlin
data class SubmitMessages(
```
Per-mutation copy overrides. Pass to `SubmitHandler.submit` to surface action-specific messages instead of the generic "Saving…" / "Saved" / "Failed" defaults wired at the theme level.

<details><summary>Example</summary>

```kotlin
submitHandler.submit(
    messages = SubmitMessages(
        submitting = "Creating loan…",
        submitted = "Loan created",
        failed = "Couldn't create loan",
    ),
) { repository.createLoan(form) }
```

</details>

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitHandler.kt:69</code></summary>

```kotlin
     * Reset to null when [reset] is called.
     */
    private var currentMessages: SubmitMessages? = null

    /**
     * Execute [block] as a one-shot submission.
     *
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitOutbox.kt`

```kotlin
interface SubmitOutbox<P>
```
Durable outbox for form payloads that failed to reach the server. Persist a payload on network failure → the user can resume later from any session.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomSubmitOutbox.kt:47</code></summary>

```kotlin
    private val serializer: KSerializer<P>,
    private val ownsFormKey: (String) -> Boolean = { true },
) : SubmitOutbox<P> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun save(formKey: String, payload: P): Long = run {
```

</details>

- `suspend fun save(formKey: String, payload: P): Long` — Save `payload` as the singleton PENDING draft under `formKey`. If a PENDING draft already exists for `formKey` with `uniqueKey IS NULL`, this method updates it in-place and returns its existing id (idempotent upsert — no duplicate rows).
- `suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long` — Multi-pending companion to `save`. Saves `payload` as a PENDING draft scoped on `(formKey, uniqueKey)`. N concurrent drafts can coexist under one `formKey` as long as each carries a distinct `uniqueKey` (e.g.
- `suspend fun getPending(formKey: String): SubmitOutboxEntry<P>?` — Retrieve the current singleton PENDING entry for `formKey`, or `null` if none.
- `suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>?` — Retrieve the PENDING entry for a specific `(formKey, uniqueKey)` pair, or `null` if none.
- `fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?>` — Hot flow that emits the current singleton PENDING entry whenever it changes.
- `fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<SubmitOutboxEntry<P>?>` — Streaming variant of `getPendingByUniqueKey`.
- `fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>>` — Observes ALL non-terminal drafts (PENDING / RETRYING / FAILED) for `formKey`, ordered newest-first. Used by feature UIs that need to display "N drafts pending sync" across multiple uniqueKeys (e.g. Portfolio: "3 holdings pending").
- `suspend fun getAllPending(): List<SubmitOutboxEntry<P>>` — All entries currently in PENDING status (excludes RETRYING) — used by `OfflineSubmitSyncer`.
- `fun observePendingIds(): Flow<Set<Long>> = emptyFlow()` — Ids of this outbox's PENDING entries, re-emitted on change — lets the syncer send a draft the moment it becomes pending while online (a Retry tapped in Sync & Drafts), instead of only on the next reconnect.
- `suspend fun markRetrying(id: Long)` — Claim this entry for retry — transitions PENDING → RETRYING; prevents concurrent UI double-submit.
- `suspend fun markSubmitted(id: Long)` — Transition the entry identified by `id` to SUBMITTED.
- `suspend fun markFailed(id: Long, error: String?)` — Transition the entry identified by `id` to FAILED with an optional `error` message.
- `suspend fun requeue(id: Long, error: String?) = markFailed(id, error)` — Put the entry back to PENDING after a failed attempt that is still within the retry cap, so the syncer tries it again. Its `attemptCount` (bumped by `markRetrying`) is kept.
- `suspend fun deleteByFormKey(formKey: String)` — Delete all drafts for `formKey` (e.g. on screen close or after successful submit).
  _…more members; read the file._

```kotlin
data class SubmitOutboxEntry<out P>(
```
A single outbox record as seen by the framework.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/infra/impl/RoomSubmitOutbox.kt:90</code></summary>

```kotlin
        }

    override suspend fun getPending(formKey: String): SubmitOutboxEntry<P>? =
        dao.getPendingByFormKey(formKey)?.toEntry()

    override suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>? =
        dao.getPendingByUniqueKey(formKey, uniqueKey)?.toEntry()
```

</details>

```kotlin
enum class SubmitOutboxStatus
```
Lifecycle states for a `SubmitOutboxEntry`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:36</code></summary>

```kotlin
            uiState = SyncAndDraftsUiState.Success(
                drafts = listOf(
                    DraftRecord(3, "bill", "electricity", SubmitOutboxStatus.PENDING, 0, 0, null),
                    DraftRecord(4, "loan", "home-loan", SubmitOutboxStatus.PENDING, 0, 0, null),
                ),
                syncing = listOf(
                    DraftRecord(2, "alert", "btc-alert", SubmitOutboxStatus.RETRYING, 0, 0, null),
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitState.kt`

```kotlin
sealed interface SubmitState<out R>
```
State machine for a single form/action submission lifecycle.

<details><summary>Example</summary>

```kotlin
  Idle  ──submit()──▶  Submitting  ──success──▶  Submitted<R>
                                   ──failure──▶  Failed
  Submitted / Failed  ──reset()──▶  Idle
  Failed              ──retry()──▶  Submitting  (re-runs last block)
```

</details>

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:130</code></summary>

```kotlin
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AccountState.DialogState.Loading)
                        }
                    }
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/SubmitStateExtensions.kt`

```kotlin
val <R> SubmitState<R>.isSubmitting: Boolean
```
True while the API call is in-flight. Use to disable the submit button.

```kotlin
val <R> SubmitState<R>.isIdle: Boolean
```
True when in the initial or post-reset state.

```kotlin
val <R> SubmitState<R>.isSubmitted: Boolean
```
True when the last submission completed successfully.

```kotlin
val <R> SubmitState<R>.isFailed: Boolean
```
True when the last submission failed. `SubmitHandler.retry` is available.

```kotlin
val <R> SubmitState<R>.resultOrNull: R?
```
Returns the result payload for `SubmitState.Submitted`, or null for all other states.

```kotlin
val <R> SubmitState<R>.errorOrNull: Throwable?
```
Returns the error for `SubmitState.Failed`, or null for all other states.

```kotlin
val <R> SubmitState<R>.categoryOrNull: ErrorCategory?
```
Returns the `ErrorCategory` for `SubmitState.Failed`, or null for all other states.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraft.kt`

```kotlin
data class FormDraft(
```
A form submission that did not reach the server, kept so it can be RESUMED in its form or REPLAYED by an `OfflineSubmitSyncer` — one row type for every form, in `framework_submit_drafts`.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftRepository.kt:44</code></summary>

```kotlin
     * taps Retry in Sync & Drafts.
     */
    suspend fun save(slot: String, draft: FormDraft, failure: String? = null)

    /** Drop the slot's draft — on a successful submit, or Discard. */
    suspend fun delete(form: String, slot: String)
}
```

</details>

```kotlin
fun formDraftSlot(ownerId: Long?, kind: String? = null): String = ownerId?.let { "${kind ?: "owner"}:$it" } ?: "new"
```
The outbox slot: one draft per form per owner — `kind` keeps owner 7 of two kinds apart.

```kotlin
object FormDraftJson
```
The one JSON configuration drafts are written and read with.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftRepository.kt:97</code></summary>

```kotlin
                formKey = draft.form,
                uniqueKey = slot,
                payloadJson = FormDraftJson.encode(FormDraft.serializer(), draft),
                status = if (failure == null) SubmitOutboxStatus.PENDING.name else SubmitOutboxStatus.FAILED.name,
                createdAtMs = now,
                updatedAtMs = now,
                errorMessage = failure,
```

</details>

- `val json: Json = Json` — Ignores unknown keys, so a draft saved by an older build still decodes.
- `fun <T> encode(serializer: KSerializer<T>, value: T): String = json.encodeToString(serializer, value)` — `value` as a draft JSON string.
- `fun <T> decode(serializer: KSerializer<T>, value: String): T = json.decodeFromString(serializer, value)` — A draft JSON string back to `T`. Throws when it no longer matches `serializer`.
- `fun <T> FormDraft.payloadAs(serializer: KSerializer<T>): T = decode(serializer, payload)` — A replay decodes the payload with the form's own serializer.

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftRepository.kt`

```kotlin
interface FormDraftRepository
```
Drafts of a set of forms — one slot per form per owner (`formDraftSlot`). A draft is written when a submit does not land, and is either REPLAYED (offline ones, on reconnect) or RESUMED in its form.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftSlot.kt:33</code></summary>

```kotlin
 */
class FormDraftSlot<S, P>(
    private val drafts: FormDraftRepository,
    private val scope: CoroutineScope,
    /** The form's key — also the outbox `formKey`. */
    val form: String,
    private val ownerId: Long?,
```

</details>

- `fun observe(form: String, slot: String): Flow<SavedFormDraft?>` — The draft in `slot` of `form` — pending, retrying or failed — or null.
- `fun observeAll(): Flow<List<SavedFormDraft>>` — Every draft of these forms, newest first — what Sync & Drafts lists.
- `suspend fun save(slot: String, draft: FormDraft, failure: String? = null)` — Keep `draft` in its slot, replacing whatever was there. `failure` null = the submit never reached the server (offline): PENDING, and the syncer sends it on reconnect.
- `suspend fun delete(form: String, slot: String)` — Drop the slot's draft — on a successful submit, or Discard.

```kotlin
data class SavedFormDraft(
```
A stored `FormDraft` with its outbox bookkeeping — what a form offers and Sync & Drafts lists.

<details><summary>Used in the template — <code>core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftSlot.kt:48</code></summary>

```kotlin

    /** The draft to offer on this form, or null. */
    val offered: StateFlow<SavedFormDraft?> =
        combine(drafts.observe(form, slot), handled) { draft, done -> draft.takeUnless { done } }
            .stateIn(scope, SharingStarted.Eagerly, null)

    /** The offered draft's snapshot, and the banner goes. Null when there is nothing to resume. */
```

</details>

```kotlin
class RoomFormDraftRepository(
```
Over the DAO rather than `RoomSubmitOutbox`: a form must see its FAILED draft too (the outbox's observers are PENDING-only), and a save must replace the slot whatever its status (the outbox upserts only a PENDING row, so saving over a FAILED one would leave two).

<details><summary>Used in the template — <code>core-base/store/src/commonTest/kotlin/kpt/core/base/store/submit/draft/RoomFormDraftRepositoryTest.kt:27</code></summary>

```kotlin

    private val dao = FakeDraftDao()
    private val drafts = RoomFormDraftRepository(dao, listOf("create.group"))
    private val slot = formDraftSlot(null)

    private fun draft(name: String) = FormDraft(
        form = "create.group",
```

</details>

### `core-base/store/src/commonMain/kotlin/kpt/core/base/store/submit/draft/FormDraftSlot.kt`

```kotlin
class FormDraftSlot<S, P>(
```
One form's draft slot — what a ViewModel holds so drafts cost it a few lines. `S` is the form's own snapshot type (what `resume` gives back), `P` the payload its submit sends.

```kotlin
val MutationResult<*>.isOffline: Boolean
```
True when the write never reached the server — the case a draft is kept for automatically.

```kotlin
fun <T> MutationResult<T>.offlineWhenUnsent(): MutationResult<T> =
```
A network failure as the `Blocked(OFFLINE)` it really is, so a form's existing offline handling (keep the draft, say "no connection") covers both shapes of `isOffline`. Anything else unchanged.

```kotlin
fun MutationResult<*>.failureReason(reasonOf: (Throwable) -> String? = { null }): String = when (this)
```
Why a write did not land, for a FAILED draft. `reasonOf` extracts the server's own message from a failure's cause (apps parse their backend's error body); the cause's message is the fallback.

---

_71 type(s), 126 function(s)/property(ies); 182 carry KDoc at source; 22 authored example(s); 78 live call site(s)._
<!-- api-docs:end -->
