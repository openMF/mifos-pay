# `core-base/data`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_DATA.md`
> **Measured:** 9 Kotlin files, 0 test files

**Defines annotations:** `@DataProvider`, `@FromQualifier`, `@FromStore`, `@RepositoryBinding`

## Principal types

`DataProvider`, `FromQualifier`, `FromStore`, `NetworkChange`, `NetworkMonitorContract`, `NetworkMonitorImpl`, `RepositoryBinding`, `SyncManager`, `Syncable`, `Synchronizer`, `TimeZoneMonitor`, `TimeZoneMonitorImpl`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/data sha=e6a4950697ff0dc578f5cffeb8d5c39da62a73ec -->
## API reference

_Generated from `core-base/data` at tree `e6a4950697ff` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/annotation/RepositoryBinding.kt`

```kotlin
annotation class RepositoryBinding(val binds: KClass<*>)
```
Marks a repository implementation so its Koin binding is DERIVED, not hand-written.

<details><summary>Example</summary>

```kotlin
@RepositoryBinding(binds = LoanRepository::class)
internal class LoanRepositoryImpl(
    @FromStore("loans") private val loansStore: Store<Unit, List<Loan>>,
    @FromStore("loansMutable") private val loansWriteStore: MutableStore<String, Loan>,
    private val loanDao: LoanDao,
) : LoanRepository
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:53</code></summary>

```kotlin
// TODO use self api for account operations later
/** Default [AccountRepository] backed by the network data sources. */
@RepositoryBinding(binds = AccountRepository::class)
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
```

</details>

```kotlin
annotation class FromStore(val id: String)
```
Resolves this parameter from the store registry rather than by bare type.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:59</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.SelfAccounts) private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.TransferDetail) private val transferDetailStore: Store<TransferDetailKey, TransferDetail>?,
```

</details>

```kotlin
annotation class DataProvider(
```
Marks a factory function whose Koin `single` is DERIVED from its signature.

<details><summary>Example</summary>

```kotlin
@DataProvider(qualifier = "outbox.loan")
fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
    RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillDataProviders.kt:27</code></summary>

```kotlin
 * consumers yet; they wait for Fineract's bill-management endpoints.
 */
@DataProvider(qualifier = "kpt.core.data.repository.BillerRepository")
fun provideNetworkBillerRepository(
    apiManager: FineractApiManager,
    @FromQualifier("IO") ioDispatcher: CoroutineDispatcher,
): BillerRepository = BillerRepositoryImpl(apiManager, ioDispatcher)
```

</details>

```kotlin
annotation class FromQualifier(val name: String)
```
Resolves this parameter from a NAMED qualifier rather than by bare type. The counterpart to `FromStore` for bindings the data layer itself qualifies — chiefly a `SubmitOutbox<T>`, whose erased type is shared by every outbox in the graph.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:57</code></summary>

```kotlin
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.SelfAccounts) private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
```

</details>

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/NetworkMonitor.kt`

```kotlin
typealias NetworkMonitor = io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
```
Backward-compatible typealias — existing consumers keep their import. Delegates to cmp-network-monitor's full-featured NetworkMonitor interface.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/fork/di/AndroidPlatformDependentDataModule.kt:25</code></summary>

```kotlin
    private val scope: CoroutineScope,
) : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context, dispatcher)
    }

    override val timeZoneMonitor: TimeZoneMonitor by lazy {
```

</details>

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/NetworkMonitorContract.kt`

```kotlin
object NetworkMonitorContract
```
Framework contract for `NetworkMonitor` implementations. The bundled `cmp-network-monitor` (v3.3.1+, from MobileByteLabs KmpToolkit) satisfies this contract; forks substituting their own implementation MUST also satisfy it.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/NetworkMonitorContractTest.kt:47</code></summary>

```kotlin
        assertTrue(
            monitor.isOnline is StateFlow<*>,
            "NetworkMonitor.isOnline MUST be StateFlow per NetworkMonitorContract invariant 1",
        )
    }

    // TODO: re-enable when cmp-network-monitor provides a JVM-friendly default
```

</details>

- `const val MIN_DEBOUNCE_MS: Long = 100L` — Minimum sane reconnect-debounce window (anything lower thrashes on flaps).
- `const val DEFAULT_DEBOUNCE_MS: Long = 300L` — Default reconnect-debounce window — sensible balance for most apps.
- `const val MAX_DEBOUNCE_MS: Long = 5_000L` — Upper bound — beyond this, the user perceives the app as unresponsive to network changes.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/SyncManager.kt`

```kotlin
interface SyncManager
```
Observer surface for the in-flight sync state. NiA-port. Single binary signal — `isSyncing` — collected by composables that want to surface "Refreshing…" indicators globally (typically the top app bar, the home dashboard hero, etc.).

- `val isSyncing: Flow<Boolean>` — Whether a sync is in flight — drives the refreshing affordance without each screen tracking it.
- `fun requestSync()` — Requests a sync. Advisory: the manager may coalesce it with one already running.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/Synchronizer.kt`

```kotlin
interface Synchronizer
```
Synchronization contract — ports Now in Android's `core/data/SyncUtilities.kt`.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/SnapshotSyncTest.kt:28</code></summary>

```kotlin

    /** Minimal in-memory [Synchronizer] — no DataStore, no persistence. */
    private class InMemorySynchronizer : Synchronizer {
        var stored = ChangeListVersions()
            private set

        override suspend fun getChangeListVersions(): ChangeListVersions = stored
```

</details>

- `suspend fun getChangeListVersions(): ChangeListVersions` — The per-feature last-synced versions, so a sync resumes rather than re-reading everything.
- `suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)` — Updates the stored versions after a successful sync.
- `suspend fun Syncable.sync(): Boolean = this.syncWith(this@Synchronizer)` — Convenience: call `someSyncable.sync()` to run `Syncable.syncWith` against this.

```kotlin
interface NetworkChange
```
Identified network record. Used by `changeListSync` to partition deletes from updates and to advance the per-feature version pointer.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/SynchronizerExtensionsTest.kt:44</code></summary>

```kotlin
        override val changeListVersion: Int,
        override val isDelete: Boolean,
    ) : NetworkChange

    @Test
    fun changeListSync_partitions_deletes_and_updates_and_advances_the_version() = runTest {
        val sync = InMemorySynchronizer()
```

</details>

- `val id: String` — Identity of the changed record.
- `val changeListVersion: Int` — Version this change was published at — the cursor a later sync resumes from.
- `val isDelete: Boolean` — Whether the change is a deletion, which must be applied rather than fetched.

```kotlin
interface Syncable
```
Adopter contract. A `Syncable` knows how to bring its slice of local state up to date with the network. At v1 the contract is intentionally minimal (no payload arg) — the worker enqueues all-pinned-keys per adopter at fixed defaults.

- `suspend fun syncWith(synchronizer: Synchronizer): Boolean` — Syncs this entity using `synchronizer`. Returns false if the sync could not complete, so the caller leaves the stored version untouched and retries.

```kotlin
suspend fun <T : NetworkChange> Synchronizer.changeListSync(
```
Delta-API algorithm. NiA-port. Used when the server returns `[{id, version, isDelete}, ...]` from `?since=N` semantics — partitions deletes/updates, fans out body fetch, bumps the version pointer.

```kotlin
suspend fun Synchronizer.snapshotSync(name: String, fetcher: suspend () -> Unit): Boolean = coroutineScope
```
Snapshot-API algorithm. Used by both canonical adopters (`CurrencyRepository` over Frankfurter; `MacroIndicatorsRepository` over World Bank).

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/TimeZoneMonitor.kt`

```kotlin
interface TimeZoneMonitor
```
Utility for reporting current timezone the device has set. It always emits at least once with default setting and then for each TZ change.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/PlatformDependentDataModule.android.kt:27</code></summary>

```kotlin
    includes(CommonModule)

    singleOf(::TimeZoneMonitorImpl) bind TimeZoneMonitor::class
}
```

</details>

- `val currentTimeZone: Flow<TimeZone>` — The device time zone, re-emitting when it changes — so a date rendered while travelling updates rather than silently going wrong.

### `core-base/data/src/commonMain/kotlin/kpt/core/base/data/infra/impl/NetworkMonitorImpl.kt`

```kotlin
class NetworkMonitorImpl : NetworkMonitor by NetworkMonitorProvider.install()
```
Singleton NetworkMonitor backed by cmp-network-monitor. Auto-initializes on first access via NetworkMonitorProvider.

---

_12 type(s), 15 function(s)/property(ies); 27 carry KDoc at source; 2 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->
