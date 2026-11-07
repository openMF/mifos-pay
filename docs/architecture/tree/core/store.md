# `core/store`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_STORE.md`
> **Measured:** 40 Kotlin files, 2 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@StoreProvider` | `store-ksp` | config/AppStoreRegistry, config/AppStoreIds, di/GeneratedStoreBindings |
| `@CacheKey` | `store-ksp` | config/AppCacheKeys |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AccountDetailKey`, `BeneficiaryKey`, `ClientDetailKey`, `ErrorMessageOverrides`, `ForkErrorMapper`, `ForkScreenStateDefaults`, `InvoiceKey`, `LinkableAccountKey`, `NotificationKey`, `OfficesKey`, `PocketKey`, `RecentPayeeKey`, `SavedCardKey`, `ScreenStateOverrides`  …and 6 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/store sha=23104aab718f0375e57b482b0ae0691a7b3acf12 -->
## API reference

_Generated from `core/store` at tree `23104aab718f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ForkErrorMapper.kt`

```kotlin
object ForkErrorMapper : ErrorMessageOverrides
```
THE FORK'S domain-error copy. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object ForkErrorMapper : ErrorMessageOverrides {
    override fun message(error: Throwable): String? = when (error) {
        is InsufficientFundsException -> "Not enough balance for this transfer."
        is CardDeclinedException -> "That card was declined. Try another payment method."
        else -> null
    }
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptErrorMapper.kt:101</code></summary>

```kotlin
    val clientCopy = stringResource(Res.string.error_category_client)
    val genericCopy = stringResource(Res.string.error_category_generic)
    val projectCopy = ForkErrorMapper::message
    return remember(
        networkCopy,
        connectTimeoutCopy,
        readTimeoutCopy,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/config/ForkScreenStateDefaults.kt`

```kotlin
object ForkScreenStateDefaults : ScreenStateOverrides
```
THE FORK'S branding for the shared empty / error / no-network / loading visuals. Neutral on the template — this is yours to fill. Implements the TEMPLATE-owned `ScreenStateOverrides`.

<details><summary>Example</summary>

```kotlin
object ForkScreenStateDefaults : ScreenStateOverrides {
    override fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults.copy(
        empty = defaults.empty.copy(visual = ScreenStateVisual.Lottie(spec = MyBrandAnimations.empty)),
        error = defaults.error.copy(onShown = { e -> AppTelemetry.recordError("screen_state_error", e) }),
    )
}
```

</details>

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt:102</code></summary>

```kotlin
                retryText = nonetRetry,
            ),
        ).let(ForkScreenStateDefaults::customize)
    }
}

/**
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/config/KptErrorMapper.kt`

```kotlin
fun errorCategoryToken(error: Throwable): String = when (val cat = categorize(error))
```
Application-level error → user-facing message mapper. ONE source of user-facing copy: `rememberAppErrorMessageFor`, which resolves every `ErrorCategory` through `stringResource(...)`.

```kotlin
interface ErrorMessageOverrides
```
The fork's error-copy extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ForkErrorMapper` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ForkErrorMapper.kt:50</code></summary>

```kotlin
 * stop refetching. Declare the key on its store instead.
 */
object ForkErrorMapper : ErrorMessageOverrides
```

</details>

- `fun message(error: Throwable): String? = null` — Fork copy for `error`, or null to fall through to the framework's categorised message.

```kotlin
fun rememberAppErrorMessageFor(): (Throwable) -> String
```
Composable factory that resolves per-category copy via `stringResource(...)`, returning a pure-Kotlin `(Throwable) -> String` lambda safe to pass through to `kpt.core.base.ui.screen.ScreenStateError.messageFor` (which is invoked from inside composition where these strings are already memoised). Reuses `categorize` to bucket the error, then picks the localized string.

### `core/store/src/commonMain/kotlin/kpt/core/store/config/KptScreenStateDefaults.kt`

```kotlin
fun appScreenStateDefaults(): ScreenStateDefaults
```
TEMPLATE-OWNED framework defaults. Brand your app in `ForkScreenStateDefaults` instead — do NOT edit this file. It used to say "!!

```kotlin
interface ScreenStateOverrides
```
The fork's screen-state branding extension point, declared HERE so it is TEMPLATE-owned and full-copied by every sync. `ForkScreenStateDefaults` implements it and is the only fork-owned half.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/config/ForkScreenStateDefaults.kt:39</code></summary>

```kotlin
 * that introduces it.
 */
object ForkScreenStateDefaults : ScreenStateOverrides
```

</details>

- `fun customize(defaults: ScreenStateDefaults): ScreenStateDefaults = defaults` — Return `defaults` with the fork's branding applied, or `defaults` unchanged.

### `core/store/src/commonMain/kotlin/kpt/core/store/di/ForkStoreModule.kt`

```kotlin
val ForkStoreModule = module
```
THE FORK'S store-layer DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
@StoreProvider(id = "myThing", ttl = "5m")
@CacheKey(name = "LIST", key = "myThing")
fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
val ForkStoreModule = module {
    single<DraftInventory> { MyForkDraftInventory(draftDao = get(), audit = get()) }
}
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/di/KptStoreModule.kt`

```kotlin
val appStoreModule: Module = module
```
App-level Store wiring — FRAMEWORK ONLY.

<details><summary>Example</summary>

```kotlin
@StoreProvider(id = "myThing", ttl = "5m")
@CacheKey(name = "LIST", key = "myThing")
fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
startKoin { modules(appStoreModule, /* … */) }
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt`

```kotlin
fun interface UserDataSource
```
The read PORT for the user-preferences store. The preferences themselves live in `core/datastore`, which `core/store` does not depend on (and must not — `core/datastore` sits beside it in the layer order, not below).

```kotlin
fun provideUserDataStore(source: UserDataSource): Store<Unit, UserData> =
```
OFFLINE_LOCAL_ONLY Store5 store over user preferences (`feature_profile.combo_id: local_only_prefs`).

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/WalletStoreTtl.kt`

```kotlin
object WalletStoreTtl
```
Freshness windows for mifos-pay's wallet stores.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:149</code></summary>

```kotlin
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.SELF_ACCOUNTS,
        )
    }

    // transfer-detail Store5 vertical — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
```

</details>

- `val HISTORY: Duration = 2.minutes` — `history` LEDGER TTL — 2 minutes. Financial-read cadence: transactions settle in seconds server-side, so a 2-minute band feels responsive without hammering the API on rapid tab-switches.
- `val ACCOUNT_DETAIL: Duration = 2.minutes` — `accountDetail` SINGLE-ROW-PER-KEY TTL — 2 minutes.
- `val BENEFICIARY: Duration = 15.minutes` — `beneficiary` LEDGER TTL — 15 minutes. Beneficiary lists change on explicit user CRUD, not automatic server events, so a longer window suppresses redundant refresh traffic on rapid transfer-flow navigation.
- `val SAVED_CARDS: Duration = 15.minutes` — `savedCards` LEDGER TTL — 15 minutes. Same reasoning as `BENEFICIARY` — saved-card lists change on explicit user CRUD, not server-side events, so a longer band is appropriate.
- `val INVOICE: Duration = 5.minutes` — `invoice` LEDGER TTL — 5 minutes. Invoices sit between transaction cadence (fast — HISTORY 2m) and CRUD-only lists (slow — BENEFICIARY 15m).
- `val NOTIFICATION: Duration = 5.minutes` — `notification` LEDGER TTL — 5 minutes. Notifications trickle in from server events at unpredictable cadences; a moderate revalidation band balances read freshness against endpoint load on a fast-tap notification-bell open.
- `val CLIENT_DETAIL: Duration = 15.minutes` — `clientDetail` SINGLE-ROW-PER-KEY TTL — 15 minutes.
- `val POCKET: Duration = 5.minutes` — `pocket` LEDGER TTL — 5 minutes. Pocket balances shift on any downstream loan/savings/share balance change (deposit, withdrawal, market-price recalculation for shares).
- `val STANDING_INSTRUCTION: Duration = 10.minutes` — `standingInstruction` LEDGER TTL — 10 minutes.
- `val OFFICES: Duration = 60.minutes` — `offices` LEDGER TTL — 60 minutes. Fineract office data is effectively static organizational reference data (the office tree evolves at admin-review cadence, not user-session cadence).
- `val SELF_ACCOUNTS: Duration = 2.minutes` — `selfAccounts` SINGLE-ROW-PER-KEY-family LEDGER TTL — 2 minutes.
- `val LINKABLE_ACCOUNTS: Duration = 2.minutes` — `linkableAccounts` LEDGER TTL — 2 minutes.
- `val TRANSFER_DETAIL: Duration = 2.minutes` — `transferDetail` SINGLE-ROW-PER-KEY TTL — 2 minutes.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/account/AccountDetailKey.kt`

```kotlin
data class AccountDetailKey(val accountId: Long)
```
Store5 key for the SINGLE-ROW-PER-KEY `accountDetail` store — one savings-account detail record per key.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavingsAccountRepositoryImpl.kt:52</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.AccountDetail) private val accountDetailStore: Store<AccountDetailKey, SavingAccountDetail>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : SavingsAccountRepository {
    override suspend fun getSavingsAccounts(
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/account/AccountDetailStore.kt`

```kotlin
fun provideAccountDetailStore(
```
Build the SINGLE-ROW-PER-KEY read `Store` for savings-account details (GOAL D13 — a variation of the `history` archetype). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/autopay/AutoPayBillStore.kt`

```kotlin
fun provideAutoPayBillStore(dao: BillDao): Store<Unit, List<BillEntity>> = StoreFactory.createOfflineStore(
```
Build the OFFLINE_LOCAL_ONLY `Store` for user-authored bills (GOAL D12 — the `autopay-bills` archetype). ## Shape `createOfflineStore` — backed exclusively by `BillDao`.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/beneficiary/BeneficiaryKey.kt`

```kotlin
data class BeneficiaryKey(val clientId: Long)
```
Store5 key for the LEDGER `beneficiary` store — one page of beneficiaries per authenticated client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SelfServiceRepositoryImpl.kt:75</code></summary>

```kotlin
    private val fetchedAtRepository: FetchedAtRepository?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Beneficiary) private val beneficiaryStore: Store<BeneficiaryKey, List<Beneficiary>>?,
) : SelfServiceRepository {
    override suspend fun loginSelf(payload: AuthenticationPayload): User {
        return withContext(dispatcher) {
            apiManager.authenticationApi.authenticate(payload)
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/beneficiary/BeneficiaryStore.kt`

```kotlin
fun provideBeneficiaryStore(
```
Build the LEDGER read `Store` for beneficiary lists (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/biller/BillerStore.kt`

```kotlin
fun provideAutoPayBillerStore(dao: BillerDao): Store<Unit, List<BillerEntity>> = StoreFactory.createOfflineStore(
```
Build the OFFLINE_LOCAL_ONLY `Store` for user-authored billers (GOAL D12 — mirrors the batch-4 pilot's `AutoPayBillStore`). ## Shape `createOfflineStore` — backed exclusively by `BillerDao`.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/client/ClientDetailKey.kt`

```kotlin
data class ClientDetailKey(val clientId: Long)
```
Store5 key for the SINGLE-ROW-PER-KEY `clientDetail` store — one client-info record per key.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ClientRepositoryImpl.kt:53</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.ClientDetail) private val clientDetailStore: Store<ClientDetailKey, Client>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : ClientRepository {
    override suspend fun getClients(): Flow<ScreenState<Page<Client>>> {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/client/ClientDetailStore.kt`

```kotlin
fun provideClientDetailStore(
```
Build the SINGLE-ROW-PER-KEY read `Store` for client-info (profile) records (GOAL D13 — a variation of the `history` archetype for a single-record cache). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/history/HistoryStore.kt`

```kotlin
fun provideHistoryStore(
```
Build the LEDGER read `Store` for savings-account transaction history (GOAL D13 — the `history` archetype). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/history/TransactionKey.kt`

```kotlin
data class TransactionKey(val accountId: Long)
```
Store5 key for the LEDGER `history` store — one page of transactions per savings account.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SelfServiceRepositoryImpl.kt:71</code></summary>

```kotlin
    @FromQualifier("IO") private val dispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.History) private val historyStore: Store<TransactionKey, List<Transaction>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Beneficiary) private val beneficiaryStore: Store<BeneficiaryKey, List<Beneficiary>>?,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/invoice/InvoiceKey.kt`

```kotlin
data class InvoiceKey(val clientId: Long)
```
Store5 key for the LEDGER `invoices` store — one page of invoices per client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InvoiceRepositoryImpl.kt:44</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Invoice) private val invoiceStore: Store<InvoiceKey, List<Invoice>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : InvoiceRepository {
    override fun getInvoice(clientId: Long, invoiceId: Long): Flow<ScreenState<Invoice>> {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/invoice/InvoiceStore.kt`

```kotlin
fun provideInvoiceStore(
```
Build the LEDGER read `Store` for invoices (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/linkableaccount/LinkableAccountKey.kt`

```kotlin
data class LinkableAccountKey(val clientId: Long)
```
Store5 key for the LEDGER `linkableAccounts` store — one page of accounts eligible to link into the client's pocket, per authenticated client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt:52</code></summary>

```kotlin
    @FromStore(AppStoreIds.Pocket) private val pocketStore: Store<PocketKey, List<DetailedPocketAccount>>?,
    @FromStore(AppStoreIds.LinkableAccounts)
    private val linkableAccountsStore: Store<LinkableAccountKey, List<LinkableAccount>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
    private val pocketDao: PocketDao?,
) : PocketRepository {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/linkableaccount/LinkableAccountStore.kt`

```kotlin
fun provideLinkableAccountsStore(
```
Build the LEDGER read `Store` for accounts that a client CAN link into a pocket (GOAL D13 — the `history` archetype variant for a client-scoped list, derived subset). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/notification/NotificationKey.kt`

```kotlin
data class NotificationKey(val clientId: Long)
```
Store5 key for the LEDGER `notifications` store — one page of notifications per authenticated client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/NotificationRepositoryImpl.kt:42</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Notification) private val notificationStore: Store<NotificationKey, List<Notification>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : NotificationRepository {
    override fun fetchNotifications(): Flow<ScreenState<List<Notification>>> {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/notification/NotificationStore.kt`

```kotlin
fun provideNotificationStore(
```
Build the LEDGER read `Store` for notifications (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/office/OfficeStore.kt`

```kotlin
fun provideOfficeStore(
```
Build the LEDGER read `Store` for the global Fineract office list (GOAL D13 — global-reference-data variant of the `history` archetype).

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/office/OfficesKey.kt`

```kotlin
data object OfficesKey
```
Store5 key for the LEDGER `offices` store — the WHOLE Fineract office list.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/OfficeRepositoryImpl.kt:40</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.Offices) private val officesStore: Store<OfficesKey, List<Office>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : OfficeRepository {
    override fun getOffices(): ScreenStateStream<List<Office>> {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/pocket/PocketKey.kt`

```kotlin
data class PocketKey(val clientId: Long)
```
Store5 key for the LEDGER `pockets` store — one page of linked pocket accounts per client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt:50</code></summary>

```kotlin
    private val dataManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    @FromStore(AppStoreIds.Pocket) private val pocketStore: Store<PocketKey, List<DetailedPocketAccount>>?,
    @FromStore(AppStoreIds.LinkableAccounts)
    private val linkableAccountsStore: Store<LinkableAccountKey, List<LinkableAccount>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/pocket/PocketStore.kt`

```kotlin
fun providePocketStore(
```
Build the LEDGER read `Store` for detailed pocket accounts (GOAL D13 — the `history` archetype variant for a client-scoped list).

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/recentpayee/RecentPayeeKey.kt`

```kotlin
data class RecentPayeeKey(val sourceAccountId: Long)
```
Store5 key for the LOCAL-DERIVED `recentPayee` store — one page of derived recent-payees per source savings-account.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/wallet/recentpayee/RecentPayeeStore.kt:62</code></summary>

```kotlin
 */
@StoreProvider(id = "recentPayee")
fun provideRecentPayeeStore(dao: RecentPayeeDao): Store<RecentPayeeKey, List<RecentPayeeEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: RecentPayeeKey ->
            run { dao.observeBySourceAccount(key.sourceAccountId) }
        },
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/recentpayee/RecentPayeeStore.kt`

```kotlin
fun provideRecentPayeeStore(dao: RecentPayeeDao): Store<RecentPayeeKey, List<RecentPayeeEntity>> = StoreFactory.createOfflineStore(
```
Build the LOCAL-DERIVED `Store` for recent-payee lists (GOAL D12 — persist-on-derive variant of the OFFLINE_LOCAL_ONLY archetype). ## Shape `createOfflineStore` — backed exclusively by `RecentPayeeDao`.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/savedcards/SavedCardKey.kt`

```kotlin
data class SavedCardKey(val clientId: Long)
```
Store5 key for the LEDGER `savedCards` store — one page of saved payment cards per client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavedCardRepositoryImpl.kt:44</code></summary>

```kotlin
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.SavedCards) private val savedCardStore: Store<SavedCardKey, List<SavedCard>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : SavedCardRepository {
    override fun getSavedCards(clientId: Long): Flow<ScreenState<List<SavedCard>>> {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/savedcards/SavedCardStore.kt`

```kotlin
fun provideSavedCardStore(
```
Build the LEDGER read `Store` for saved payment cards (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/selfaccounts/SelfAccountsKey.kt`

```kotlin
data class SelfAccountsKey(val clientId: Long)
```
Store5 key for the LEDGER `selfAccounts` store — one page of the authenticated user's own accounts per client.

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

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/selfaccounts/SelfAccountsStore.kt`

```kotlin
fun provideSelfAccountsStore(
```
Build the LEDGER read `Store` for the authenticated user's own self-accounts (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/standinginstruction/StandingInstructionKey.kt`

```kotlin
data class StandingInstructionKey(val clientId: Long)
```
Store5 key for the LEDGER `standingInstruction` store — one page of standing instructions per client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/StandingInstructionRepositoryImpl.kt:47</code></summary>

```kotlin
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.StandingInstruction)
    private val standingInstructionStore: Store<StandingInstructionKey, List<StandingInstruction>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
    private val fetchedAtRepository: FetchedAtRepository?,
) : StandingInstructionRepository {
    override fun getStandingInstructionTemplate(
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/standinginstruction/StandingInstructionStore.kt`

```kotlin
fun provideStandingInstructionStore(
```
Build the LEDGER read `Store` for standing instructions (GOAL D13 — the `history` archetype variant for a client-scoped list). ## Shape `createStore` — read-only Store5 factory.

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/transferdetail/TransferDetailKey.kt`

```kotlin
data class TransferDetailKey(val transferId: Long)
```
Store5 key for the SINGLE-ROW-PER-KEY `transferDetail` store — one account-transfer detail record per key. Mirrors `AccountDetailKey`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:63</code></summary>

```kotlin
    private val fetchedAtRepository: FetchedAtRepository?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.TransferDetail) private val transferDetailStore: Store<TransferDetailKey, TransferDetail>?,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    private val transactionDao: TransactionDao?,
) : AccountRepository {
```

</details>

### `core/store/src/commonMain/kotlin/kpt/core/store/wallet/transferdetail/TransferDetailStore.kt`

```kotlin
fun provideTransferDetailStore(
```
Build the SINGLE-ROW-PER-KEY read `Store` for account-transfer details (GOAL D13 — a variation of the `history` archetype). Mirrors `provideAccountDetailStore`. ## Shape `createStore` — read-only Store5 factory.

---

_19 type(s), 38 function(s)/property(ies); 57 carry KDoc at source; 4 authored example(s); 19 live call site(s)._
<!-- api-docs:end -->
