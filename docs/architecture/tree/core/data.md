# `core/data`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATA.md`
> **Measured:** 125 Kotlin files, 9 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@RepositoryBinding` | `data-ksp` | di/GeneratedRepositoryBindings |
| `@DataProvider` | `data-ksp` | di/GeneratedRepositoryBindings, config/AppOutboxQualifiers |
| `@FromStore` | `data-ksp` |  |
| `@FromQualifier` | `data-ksp` |  |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AccountRepository`, `AccountRepositoryImpl`, `AndroidPlatformDependentDataModule`, `AppLockOption`, `AppLockRepository`, `AppLockRepositoryImpl`, `AssetRepository`, `AssetRepositoryImpl`, `AuthenticationRepository`, `AuthenticationRepositoryImpl`, `AutoPayHistoryRepository`, `AutoPayHistoryRepositoryImpl`, `AutoPayHistoryStatistics`, `AutoPayRepository`  …and 81 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/data sha=84030a90cc3afd916563303272e1901f0b6c52db -->
## API reference

_Generated from `core/data` at tree `84030a90cc3a` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/data/src/commonMain/kotlin/kpt/core/data/di/ForkRepositoryModule.kt`

```kotlin
val ForkRepositoryModule = module
```
THE FORK'S repository DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyRepository> { MyRepositoryImpl(get(), get()) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/OutboxQualifiers.kt`

```kotlin
object OutboxQualifiers
```
Named qualifiers for the app's `SubmitOutbox<*>` bindings. Why qualifiers? Koin indexes `single<T>` definitions by `T::class` (the raw type), not by the full `KType`.

<details><summary>Example</summary>

```kotlin
val MyThing = named("outbox.myThing")
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt`

```kotlin
val DataModule = module
```
DataModule — the INFRA-ONLY (framework) data aggregator, `owner: template` (E1 / C1).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:139</code></summary>

```kotlin
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getSelfAccountsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getSelfAccountsStream requires FetchedAtRepository. Verify DataModule bound it."
        }
```

</details>

```kotlin
expect val platformModule: Module
```
Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in common code.

### `core/data/src/commonMain/kotlin/kpt/core/data/di/fork/PlatformDependentDataModule.kt`

```kotlin
interface PlatformDependentDataModule
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/AndroidPlatformDependentDataModule.kt:24</code></summary>

```kotlin
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context, dispatcher)
    }
```

</details>

- `val networkMonitor: NetworkMonitor`
- `val timeZoneMonitor: TimeZoneMonitor`

```kotlin
expect val platformModule: Module
```
_No KDoc at source._

```kotlin
expect val getPlatformDataModule: PlatformDependentDataModule
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt`

```kotlin
val RepositoryModule = module
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:136</code></summary>

```kotlin
        val store = checkNotNull(selfAccountsStore) {
            "getSelfAccountsStream requires the `selfAccounts` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.SelfAccounts and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getSelfAccountsStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/AccountMapper.kt`

```kotlin
fun ClientAccountsEntity.toAccount(): List<Account>
```
_No KDoc at source._

```kotlin
fun List<SavingAccountEntity>.toAccount(): List<Account>
```
_No KDoc at source._

```kotlin
fun NetworkAccountType.toModelAccountType(): AccountType
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/BillMapper.kt`

```kotlin
object BillMapper
```
Utility class for mapping between different bill data models

- `fun formDataToBill(formData: BillFormData): Bill` — Converts BillFormData to Bill
- `fun billToFormData(bill: Bill): BillFormData` — Converts Bill to BillFormData
- `fun updateBillWithFormData(existingBill: Bill, formData: BillFormData): Bill` — Updates an existing Bill with BillFormData
- `fun updateBillStatus(bill: Bill, newStatus: BillStatus): Bill` — Creates a copy of Bill with updated status
- `fun updateBillActiveState(bill: Bill, isActive: Boolean): Bill` — Creates a copy of Bill with updated active state
- `fun createNextRecurrenceBill(bill: Bill): Bill?` — Creates a new Bill from existing Bill with next recurrence date
- `val nextDueDate = calculateNextDueDate(bill)`
- `val daysToAdd = when (bill.recurrencePattern)`
- `fun createBillSummary(bill: Bill): String` — Creates a summary string for a bill
- `fun getRecurrenceDisplayName(pattern: RecurrencePattern): String` — Creates a display name for recurrence pattern
- `fun getStatusDisplayName(status: BillStatus): String` — Creates a display name for bill status

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/ClientDetailsMapper.kt`

```kotlin
fun ClientEntity.toModel(): Client
```
_No KDoc at source._

```kotlin
fun List<ClientEntity>.toModel(): List<Client> = map { it.toModel() }
```
_No KDoc at source._

```kotlin
fun Page<ClientEntity>.toModel(): Page<Client>
```
_No KDoc at source._

```kotlin
fun NewClient.toEntity(): NewClientEntity
```
_No KDoc at source._

```kotlin
fun ClientAddress.toEntity(): Address
```
_No KDoc at source._

```kotlin
fun Status.toModel(): ClientStatus
```
_No KDoc at source._

```kotlin
fun ClientTimelineEntity.toModel(): ClientTimeline
```
_No KDoc at source._

```kotlin
fun UpdatedClient.toEntity(): UpdateClientEntity
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/SavingAccountMapper.kt`

```kotlin
fun SavingAccountEntity.toModel(): SavingAccount
```
_No KDoc at source._

```kotlin
fun SavingsWithAssociationsEntity.toSavingDetail(): SavingAccountDetail
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/SearchedEntitiesMapper.kt`

```kotlin
fun SearchedEntity.toModel(): SearchResult
```
_No KDoc at source._

```kotlin
fun List<SearchedEntity>.toSearchResult(): List<SearchResult> = map { it.toModel() }
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/TransactionMapper.kt`

```kotlin
fun SavingsWithAssociationsEntity.toTransactionList(): List<Transaction>
```
_No KDoc at source._

```kotlin
fun TransactionsEntity.toModel(): Transaction
```
_No KDoc at source._

```kotlin
fun PaymentType.toModalUi(): Transaction.PaymentType
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/UserMapper.kt`

```kotlin
val NEW_USER_ROLE_IDS: ArrayList<Int> = arrayListOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/util/Constants.kt:22</code></summary>

```kotlin
    private const val SUPER_USER_ROLE_ID = 1

    val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)
    const val ENTITY_TYPE_CLIENTS = "clients"
    const val FETCH_ACCOUNT_TRANSFER_USECASE = "FetchAccountTransfer"
    const val NO_SAVED_CARDS = "No saved cards."
    const val MIFOS_PASSWORD = "mifos:password"
```

</details>

```kotlin
fun NewUser.toEntity(): NewUserEntity
```
_No KDoc at source._

```kotlin
fun User.toUserInfo() = UserInfo(
```
_No KDoc at source._

```kotlin
fun Role.toRoleInfo() = RoleInfo(
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt`

```kotlin
fun PocketResponseDto.toDomainList(): List<PocketAccount>
```
_No KDoc at source._

```kotlin
fun kpt.core.model.network.entity.loanAccount.LoanStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
_No KDoc at source._

```kotlin
fun kpt.core.model.savingsaccount.Status.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
_No KDoc at source._

```kotlin
fun kpt.core.model.network.entity.shareAccount.ShareStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
_No KDoc at source._

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AccountRepository.kt`

```kotlin
interface AccountRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt:41</code></summary>

```kotlin
    private val repository: SelfServiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val accountRepository: AccountRepository,
) : BaseViewModel<HistoryState, HistoryEvent, HistoryAction>(
    initialState = run {
        val clientId = requireNotNull(preferencesRepository.clientId.value)
        HistoryState(
```

</details>

- `fun getTransaction(accountId: Long, transactionId: Long): Flow<ScreenState<Transaction>>`
- `fun getAccountTransfer(transferId: Long): Flow<ScreenState<TransferDetail>>`
- `fun getAccountTransferStream(` — transfer-detail Store5 vertical — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>>`
- `fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>`
- `fun getSelfAccountsStream(` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `suspend fun makeTransfer(payload: AccountTransferPayload)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AppLockRepository.kt`

```kotlin
interface AppLockRepository
```
App-wide lock flag, persisted across process restarts.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/org/mifos/feature/passcode/MifosPasscodeViewModel.kt:51</code></summary>

```kotlin
class MifosPasscodeViewModel(
    private val passcodeManager: PasscodeManager,
    private val appLockRepository: AppLockRepository,
) : BaseViewModel<MifosPasscodeState, MifosPasscodeEvent, MifosPasscodeAction>(
    initialState = MifosPasscodeState(),
) {
```

</details>

- `fun lockApp()` — Mark the app as locked. Persisted.
- `fun unlockApp()` — Mark the app as unlocked. Persisted.
- `fun deleteLock()` — Clear the flag entirely (logout / forgot-passcode).
- `fun isAppLocked(): Boolean?` — Snapshot read of the lock flag.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AssetRepository.kt`

```kotlin
interface AssetRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:59</code></summary>

```kotlin
    private val repository: SavingsAccountRepository,
    private val userRepository: ForkPreferencesRepository,
    localAssetRepository: LocalAssetRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AESState, AESEvent, AESAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_SAVING_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
```

</details>

- `fun getCountriesWithStates(): ScreenStateStream<Map<String, List<String>>>`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AuthenticationRepository.kt`

```kotlin
interface AuthenticationRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:55</code></summary>

```kotlin
    // wired in this module), so it cannot be `mock()`ed. Build a REAL use-case over
    // mocked collaborators and stub the underlying repository calls instead.
    private val authenticationRepository: AuthenticationRepository = mock()
    private val clientRepository: ClientRepository = mock()
    private val userPreferencesRepository: ForkPreferencesRepository = mock {
        every { selectedInstance } returns MutableStateFlow<ServerInstance?>(null)
        every { selectedInterbankInstance } returns MutableStateFlow<InterbankServer?>(null)
```

</details>

- `suspend fun authenticate(username: String, password: String): UserInfo`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayHistoryRepository.kt`

```kotlin
interface AutoPayHistoryRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryViewModel.kt:38</code></summary>

```kotlin
 */
class AutoPayHistoryViewModel(
    private val autoPayHistoryRepository: AutoPayHistoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayHistoryUiState, AutoPayHistoryEvent, AutoPayHistoryAction>(
    initialState = AutoPayHistoryUiState(),
) {
```

</details>

- `fun getAutoPayHistory(autoPayId: Long): ScreenStateStream<List<AutoPayHistory>>` — Get AutoPay history for a specific AutoPay schedule
- `fun getAutoPayHistoryWithPagination(` — Get AutoPay history with pagination.
- `suspend fun getHistoryById(id: Long): AutoPayHistory` — Get AutoPay history by ID (point-lookup). Returns the entry or throws on failure / when the lookup is unsupported.
- `fun getHistoryByStatus(status: String): ScreenStateStream<List<AutoPayHistory>>` — Get AutoPay history by status
- `fun getHistoryByDateRange(` — Get AutoPay history by date range
- `fun searchHistory(query: String): ScreenStateStream<List<AutoPayHistory>>` — Search AutoPay history
- `fun getHistoryStatistics(autoPayId: Long): ScreenStateStream<AutoPayHistoryStatistics>` — Get history statistics

```kotlin
data class AutoPayHistoryStatistics(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryViewModel.kt:541</code></summary>

```kotlin
    }

    private fun createDummyStatistics(): AutoPayHistoryStatistics {
        return AutoPayHistoryStatistics(
            totalTransactions = 10,
            successfulTransactions = 6,
            failedTransactions = 1,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayRepository.kt`

```kotlin
interface AutoPayRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:230</code></summary>

```kotlin
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

- `fun getAutoPayTemplate(` — Get AutoPay template for creating new AutoPay schedules
- `fun getAllAutoPaySchedules(` — Get all AutoPay schedules for a client
- `fun getAutoPaySchedule(autoPayId: Long): ScreenStateStream<AutoPay>` — Get AutoPay schedule by ID
- `suspend fun createAutoPaySchedule(` — Create a new AutoPay schedule. Throws on failure.
- `suspend fun updateAutoPaySchedule(` — Update an existing AutoPay schedule. Throws on failure.
- `suspend fun deleteAutoPaySchedule(autoPayId: Long)` — Delete an AutoPay schedule. Throws on failure.
- `suspend fun pauseAutoPaySchedule(autoPayId: Long)` — Pause an AutoPay schedule. Throws on failure.
- `suspend fun resumeAutoPaySchedule(autoPayId: Long)` — Resume a paused AutoPay schedule. Throws on failure.
- `fun getAutoPayHistory(` — Get AutoPay payment history. Keeps the `Page` wrapper inside Content (mirrors `AutoPayHistoryRepository.getAutoPayHistoryWithPagination` treatment).
- `fun getUpcomingPayments(` — Get upcoming payments for all AutoPay schedules
- `fun getAutoPayStatistics(` — Get AutoPay statistics for dashboard
- `suspend fun validateAutoPayPayload(payload: AutoPayPayload)` — Validate AutoPay payload before submission (pure-local validation, no I/O). Throws on invalid input.

```kotlin
data class AutoPayStatistics(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt:126</code></summary>

```kotlin
    }

    override fun getAutoPayStatistics(
        clientId: Long,
    ): ScreenStateStream<AutoPayStatistics> {
        return apiManager.autoPayApi
            .getAutoPayStatistics(clientId)
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/BeneficiaryRepository.kt`

```kotlin
interface BeneficiaryRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:118</code></summary>

```kotlin
        AuthenticationRepositoryImpl(get(), get(ioDispatcher))
    }
    single<BeneficiaryRepository> { BeneficiaryRepositoryImpl(get(), get(ioDispatcher)) }
    single<ClientRepository> {
        ClientRepositoryImpl(
            apiManager = get(),
            fineractApiManager = get(),
```

</details>

- `suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>`
- `suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>>`
- `suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)`
- `suspend fun updateBeneficiary(`
- `suspend fun deleteBeneficiary(beneficiaryId: Long)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/BillRepository.kt`

```kotlin
interface BillRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:40</code></summary>

```kotlin
class AddBillViewModel(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val billerRepository: BillerRepository,
) : BaseViewModel<AddBillState, AddBillEvent, AddBillAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AddBillState(),
) {
```

</details>

- `fun getAllBills(clientId: Long): ScreenStateStream<List<Bill>>` — Get all bills for a client
- `suspend fun getBillById(id: String): Bill` — Get bill by ID. Returns the bill or throws on failure.
- `suspend fun createBill(bill: Bill): Bill` — Create a new bill. Returns the created bill or throws on failure.
- `suspend fun updateBill(bill: Bill): Bill` — Update an existing bill. Returns the updated bill or throws on failure.
- `suspend fun deleteBill(id: String)` — Delete a bill. Throws on failure.
- `suspend fun getBillsByStatus(status: BillStatus): List<Bill>` — Get bills by status
- `suspend fun getBillsByBillerId(billerId: String): List<Bill>` — Get bills by biller ID
- `suspend fun getBillsByRecurrencePattern(pattern: RecurrencePattern): List<Bill>` — Get bills by recurrence pattern
- `suspend fun searchBillsByName(query: String): List<Bill>` — Search bills by name
- `suspend fun getOverdueBills(): List<Bill>` — Get overdue bills
- `suspend fun getUpcomingBills(fromDate: Long, toDate: Long): List<Bill>` — Get upcoming bills within a date range
- `suspend fun updateBillStatus(billId: String, status: BillStatus): Bill` — Update bill status. Returns the updated bill or throws on failure.
- `suspend fun validateBill(bill: Bill)` — Validate bill data before submission. Throws on invalid input.
- `fun getBillStatistics(clientId: Long): ScreenStateStream<BillStatistics>` — Get bill statistics for dashboard
  _…more members; read the file._

```kotlin
data class BillStatistics(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt:165</code></summary>

```kotlin
    }

    override fun getBillStatistics(clientId: Long): ScreenStateStream<BillStatistics> {
        return apiManager.billApi
            .getBillStatistics(clientId)
            .map { response: BillStatisticsResponse ->
                BillStatistics(
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/BillerRepository.kt`

```kotlin
interface BillerRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:41</code></summary>

```kotlin
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val billerRepository: BillerRepository,
) : BaseViewModel<AddBillState, AddBillEvent, AddBillAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AddBillState(),
) {
```

</details>

- `fun getAllBillers(): Flow<List<Biller>>` — Get all saved billers
- `suspend fun getBillerById(id: String): Biller?` — Get biller by ID
- `suspend fun saveBiller(biller: Biller): Biller` — Save a new biller. Returns the saved biller or throws on failure.
- `suspend fun updateBiller(biller: Biller): Biller` — Update an existing biller. Returns the updated biller or throws on failure.
- `suspend fun deleteBiller(id: String)` — Delete a biller. Throws on failure.
- `suspend fun getBillersByCategory(category: BillerCategory): List<Biller>` — Get billers by category
- `suspend fun searchBillersByName(query: String): List<Biller>` — Search billers by name
- `suspend fun isBillerExists(name: String, accountNumber: String): Boolean` — Check if biller exists by name and account number
- `suspend fun clearAllBillers()` — Clear all billers. Throws on failure.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/ClientRepository.kt`

```kotlin
interface ClientRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:75</code></summary>

```kotlin
    private val userRepository: UserRepository,
    private val searchRepository: SearchRepository,
    private val clientRepository: ClientRepository,
    private val assetRepository: AssetRepository,
    private val stringProvider: StringProvider,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpAction>(
```

</details>

- `fun getClientInfo(clientId: Long): Flow<ScreenState<Client>>`
- `fun getClientInfoStream(` — Phase-5 Batch-2 **SINGLE-ROW-PER-KEY read** for the `clientDetail` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<Client>>` consumed through the `clientDetail` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + single-row upsert writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default). Distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getClientInfo` path uses; both signatures return `Flow<ScreenState<Client>>` so a consumer can swap between them without any downstream shape change. ### Write path (GOAL D1) This is a READ path. Profile mutation writes (`updateClient`, `updateClientImage`) continue to flow through their existing online paths and appear here on the next refresh cycle. `getClientImage` remains on the transitional shim — its blob-URL caching story is out of scope for this batch.
- `suspend fun getClients(): Flow<ScreenState<Page<Client>>>`
- `suspend fun getClient(clientId: Long): Client`
- `suspend fun updateClient(clientId: Long, client: UpdatedClient)`
- `fun getClientImage(clientId: Long): Flow<ScreenState<String>>`
- `suspend fun updateClientImage(clientId: Long, image: String)`
- `suspend fun getClientAccounts(clientId: Long): ClientAccountsEntity`
- `suspend fun getAccounts(clientId: Long, accountType: String): Flow<ScreenState<List<Account>>>`
- `suspend fun createClient(newClient: NewClient): Int`
- `suspend fun deleteClient(clientId: Int): Int`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/DocumentRepository.kt`

```kotlin
interface DocumentRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCLevel2ViewModel.kt:39</code></summary>

```kotlin
import org.jetbrains.compose.resources.StringResource
internal class KYCLevel2ViewModel(
    private val repository: DocumentRepository,
    private val userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycLevel2State, KycLevel2Event, KycLevel2Action>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
```

</details>

- `suspend fun getDocuments(entityType: String, entityId: Int): ScreenStateStream<List<Document>>`
- `suspend fun createDocument(`
- `suspend fun createDocument(`
- `suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Document>`
- `suspend fun deleteDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Unit>`
- `suspend fun updateDocument(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/InterBankRepository.kt`

```kotlin
interface InterBankRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:72</code></summary>

```kotlin
class InterbankTransferViewModel(
    private val selfServiceRepository: SelfServiceRepository,
    private val interBankRepository: InterBankRepository,
    private val pocketRepository: PocketRepository,
    private val preferencesRepository: ForkPreferencesRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<InterbankTransferState, InterbankTransferEvent, InterbankTransferAction>(
```

</details>

- `suspend fun fetchParticipant(`
- `suspend fun fetchPartyInfo(`
- `fun findParticipant(`
- `suspend fun interBankMakeTransfer(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/InvoiceRepository.kt`

```kotlin
interface InvoiceRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoicesViewModel.kt:27</code></summary>

```kotlin
import kpt.core.ui.utils.BaseViewModel
class InvoicesViewModel(
    invoiceRepository: InvoiceRepository,
    repository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<InvoiceState, InvoiceEvent, InvoiceAction>(
    initialState = savedStateHandle.get(KEY_STATE) ?: run {
```

</details>

- `fun getInvoice(clientId: Long, invoiceId: Long): Flow<ScreenState<Invoice>>`
- `fun getInvoices(clientId: Long): Flow<ScreenState<List<Invoice>>>`
- `fun getInvoicesStream(` — Phase-5 Batch-2 **LEDGER read** for the `invoice` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Invoice>>>` consumed through the `invoice` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.invoice.InvoiceDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getInvoices` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Write path (GOAL D1) This is a READ path. Invoice CRUD writes (`createInvoice`, `updateInvoice`, `deleteInvoice`) continue to flow through their existing online paths and appear here on the next refresh cycle — the LEDGER cache is server-truth.
- `suspend fun createInvoice(clientId: Long, invoice: InvoiceEntity)`
- `suspend fun updateInvoice(`
- `suspend fun deleteInvoice(clientId: Long, invoiceId: Long)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/KycLevelRepository.kt`

```kotlin
interface KycLevelRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionViewModel.kt:31</code></summary>

```kotlin
class KYCDescriptionViewModel(
    private val repository: ForkPreferencesRepository,
    kycLevelRepository: KycLevelRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycState, KycEvent, KycAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(repository.clientId.value)
```

</details>

- `fun fetchKYCLevel1Details(clientId: Long): ScreenStateStream<KYCLevel1Details?>`
- `suspend fun addKYCLevel1Details(`
- `suspend fun updateKYCLevel1Details(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/LocalAssetRepository.kt`

```kotlin
interface LocalAssetRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:59</code></summary>

```kotlin
    private val repository: SavingsAccountRepository,
    private val userRepository: ForkPreferencesRepository,
    localAssetRepository: LocalAssetRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AESState, AESEvent, AESAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_SAVING_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
```

</details>

- `val localeList: StateFlow<List<Locale>>`
- `val currencyList: StateFlow<List<CurrencyCode>>`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/NotificationRepository.kt`

```kotlin
interface NotificationRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/NotificationViewModel.kt:22</code></summary>

```kotlin
import kpt.core.model.notification.Notification
internal class NotificationViewModel(
    repository: NotificationRepository,
    // Injected to source the clientId for the store's cache key. The Fineract
    // `notifications/` endpoint is session-scoped (no clientId parameter), but
    // the store still partitions the Room SoT per client so a logout-then-login
    // as a different client cannot surface the previous user's cached list.
```

</details>

- `fun fetchNotifications(): Flow<ScreenState<List<Notification>>>`
- `fun fetchNotificationsStream(` — Phase-5 Batch-2 **LEDGER read** for the `notification` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Notification>>>` consumed through the `notification` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.notification.NotificationDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `fetchNotifications` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Cache-key scoping The Fineract `notifications/?isRead=true` endpoint returns the list for the authenticated session (no `clientId` parameter). The `clientId` here is used purely for CACHE SCOPING — the store key partitions the Room SoT so a logout-then-login as a different client never surfaces the previous user's cached list. Returns the native `ScreenDataStream` (template idiom) — the ViewModel holds it directly and exposes `stream.state` to the Screen (rendered by `core-base/ui` `ScreenContent`) and `stream.refresh()` for pull-to-refresh / retry. No fork-`ScreenState` bridge, no per-ViewModel `when` fold — `DecisionEngine` inside the stream pre-decides every render branch.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/OfficeRepository.kt`

```kotlin
interface OfficeRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:60</code></summary>

```kotlin
    private val localAssetRepository: LocalAssetRepository,
    private val repository: SelfServiceRepository,
    private val officeRepository: OfficeRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AEBState, AEBEvent, AEBAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_BENEFICIARY_KEY) ?: run {
```

</details>

- `fun getOffices(): ScreenStateStream<List<Office>>`
- `fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>>` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/PocketRepository.kt`

```kotlin
interface PocketRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt:82</code></summary>

```kotlin
    repository: ForkPreferencesRepository,
    private val accountRepository: AccountRepository,
    private val pocketRepository: PocketRepository,
    savedStateHandle: SavedStateHandle,
    private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel<MpayQrState, MpayQrEvent, MpayQrAction>(
    initialState = run {
```

</details>

- `fun getLinkedPocketAccountsStream(`
- `fun getDetailedPocketAccountsStream(` — Phase-5 Batch-2 **LEDGER read** for the `pocket` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<DetailedPocketAccount>>>` consumed through the `pocket` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.pocket.PocketDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Migration from the in-memory cache This method REPLACES the pre-store `getDetailedPocketAccounts(clientId, forceRefresh): Flow<DataState<List<DetailedPocketAccount>>>` that was Phase-4-deferred out of a Store5 migration. The `forceRefresh` parameter is gone — callers refresh by RE-SUBSCRIBING (i.e. wrapping this call in a `MutableSharedFlow<Unit>().flatMapLatest { ... }` in the VM, the same pattern `BeneficiaryListViewModel` uses). The in-memory `MutableStateFlow<DataState<...>>` cache in `PocketRepositoryImp` is REMOVED — Room is the SoT. ### Write path (GOAL D1) This is a READ path. Pocket CRUD writes (`linkAccounts`, `delinkAccounts`) continue to flow through their existing online paths and appear here on the next refresh cycle. The pre-store optimistic in-memory cache update inside those writes is REMOVED — the momentary "already-updated" impression is replaced by an SWR / manual re-subscribe pull through this store.
- `fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>>`
- `fun getAvailableAccountsToLinkStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<List<LinkableAccount>>`
- `suspend fun linkAccounts(`
- `suspend fun delinkAccounts(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/RecentPayeeRepository.kt`

```kotlin
interface RecentPayeeRepository
```
Repository for fetching recent paid beneficiaries. Derives recent payees from transaction history by: 1. Fetching DEBIT transactions from savings account 2. Getting transfer details for each transaction 3.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubViewModel.kt:31</code></summary>

```kotlin
import kpt.core.ui.utils.BaseViewModel
class IntraBankHubViewModel(
    private val recentPayeeRepository: RecentPayeeRepository,
    private val repository: ThirdPartyTransferRepository,
    private val selfServiceRepository: SelfServiceRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<IntraBankHubState, IntraBankHubEvent, IntraBankHubAction>(
```

</details>

- `fun getRecentPayeesScreen(` — Phase-5 Batch-4 **store-backed derived read** for recent-payees (GOAL D12 — LOCAL-DERIVED persist-on-derive variant of the OFFLINE_LOCAL_ONLY archetype).

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/RegistrationRepository.kt`

```kotlin
interface RegistrationRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:170</code></summary>

```kotlin
        )
    }
    single<RegistrationRepository> { RegistrationRepositoryImpl(get(), get(ioDispatcher)) }
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
```

</details>

- `suspend fun registerUser(registerPayload: RegisterPayload)`
- `suspend fun verifyUser(userVerify: UserVerify)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/RunReportRepository.kt`

```kotlin
interface RunReportRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:171</code></summary>

```kotlin
    }
    single<RegistrationRepository> { RegistrationRepositoryImpl(get(), get(ioDispatcher)) }
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

- `suspend fun getTransactionReceipt(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SavedCardRepository.kt`

```kotlin
interface SavedCardRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreenViewModel.kt:38</code></summary>

```kotlin
import org.jetbrains.compose.resources.StringResource
class CardsScreenViewModel(
    private val repository: SavedCardRepository,
    userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CardState, CardEvent, CardAction>(
    initialState = savedStateHandle.getSerialized(key = KEY_STATE) ?: run {
```

</details>

- `fun getSavedCards(clientId: Long): Flow<ScreenState<List<SavedCard>>>`
- `fun getSavedCardsStream(` — Phase-5 Batch-1 **LEDGER read** for the `savedCards` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<SavedCard>>>` consumed through the `savedCards` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.savedcards.SavedCardDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getSavedCards` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Write path (GOAL D1) This is a READ path. Card CRUD writes (`addSavedCard`, `updateCard`, `deleteCard`) continue to flow through their existing online paths and appear here on the next refresh cycle — the LEDGER cache is server-truth.
- `fun getSavedCard(clientId: Long, cardId: Long): Flow<ScreenState<SavedCard>>`
- `suspend fun addSavedCard(clientId: Long, card: CardPayload)`
- `suspend fun deleteCard(clientId: Long, cardId: Long)`
- `suspend fun updateCard(clientId: Long, cardId: Long, card: CardPayload)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SavingsAccountRepository.kt`

```kotlin
interface SavingsAccountRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:57</code></summary>

```kotlin
import kpt.feature.accounts.savingsaccount.AESState.DialogState.Error as DialogStateError
internal class AddEditSavingViewModel(
    private val repository: SavingsAccountRepository,
    private val userRepository: ForkPreferencesRepository,
    localAssetRepository: LocalAssetRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AESState, AESEvent, AESAction>(
```

</details>

- `suspend fun getSavingsAccounts(limit: Int): ScreenStateStream<Page<SavingsWithAssociationsEntity>>`
- `suspend fun getSavingsWithAssociations(`
- `fun getAccountDetail(accountId: Long): ScreenStateStream<SavingAccountDetail>`
- `fun getAccountDetailStream(` — Phase-5 Batch-1 **SINGLE-ROW-PER-KEY read** for the `accountDetail` archetype (GOAL D13) — returns an offline-first `ScreenStateStream<SavingAccountDetail>` consumed through the `accountDetail` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + single-row upsert writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default). Distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getAccountDetail` path uses; both signatures return `ScreenStateStream<SavingAccountDetail>` so a consumer can swap between them without any downstream shape change. ### Write path (GOAL D1) This is a READ path. Account management writes (`blockAccount`, `unblockAccount`, `createSavingsAccount`, `updateSavingsAccount`) continue to flow through their existing online paths and appear here on the next refresh cycle.
- `suspend fun getSavingAccountTransaction(`
- `suspend fun payViaMobile(accountId: Long): ScreenStateStream<Transaction>`
- `fun getSavingAccountTemplate(clientId: Long): ScreenStateStream<SavingAccountTemplate>`
- `suspend fun createSavingsAccount(savingAccount: CreateNewSavingEntity)`
- `suspend fun updateSavingsAccount(`
- `suspend fun unblockAccount(`
- `suspend fun blockAccount(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SearchRepository.kt`

```kotlin
interface SearchRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModel.kt:28</code></summary>

```kotlin
import kpt.core.ui.utils.BaseViewModel
class MobileVerificationViewModel(
    private val searchRepository: SearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MobileVerificationState, MobileVerificationEvent, MobileVerificationAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: MobileVerificationState.VerifyPhoneState(),
```

</details>

- `suspend fun searchResources(`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SelfServiceRepository.kt`

```kotlin
interface SelfServiceRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:46</code></summary>

```kotlin
class AccountViewModel(
    private val userRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        val clientId = userRepository.clientId.value
```

</details>

- `suspend fun loginSelf(payload: AuthenticationPayload): User`
- `fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>>`
- `suspend fun getSelfClientDetails(): Flow<ScreenState<Page<Client>>>`
- `fun getSelfAccountTransactions(`
- `suspend fun getSelfAccountTransactionFromId(`
- `fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>`
- `fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>`
- `fun getActiveAccountsWithTransactionsPerAccount(`
- `fun getActiveAccounts(`
- `fun getActiveAccountsWithAccountTransferTemplate(`
- `fun getAccountsTransactions(clientId: Long): Flow<ScreenState<List<Transaction>>>`
- `fun getTransactions(accountId: List<Long>, limit: Int?): Flow<List<Transaction>>`
- `fun getTransactions(accountId: Long, limit: Int?): Flow<ScreenState<List<Transaction>>>`
- `fun getTransactionsStream(` — Phase-4 Batch-A **LEDGER read** for the `history` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Transaction>>>` consumed through the `history` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.transaction.TransactionDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getTransactions` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) so subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge — no reload spinner on tab-switch. ### Why the scope parameter `Store.asScreenStream(...)` launches child coroutines (reconnect debounce, periodic refresh, CACHE_FIRST_SWR side-fetch). It needs a lifecycle-bound scope so those coroutines stop when the ViewModel is cleared. Pass `viewModelScope` from the call site. ### Write path (GOAL D1) This is a READ path. Money-movement writes (transfer, deposit, withdrawal) continue to flow through their existing online repositories (`ThirdPartyTransferRepository`, `SavingsAccountRepository`, etc.) and appear here on the next refresh cycle — the LEDGER cache is server-truth.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/StandingInstructionRepository.kt`

```kotlin
interface StandingInstructionRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionViewModel.kt:39</code></summary>

```kotlin
class StandingInstructionViewModel(
    private val repository: ForkPreferencesRepository,
    private val siRepository: StandingInstructionRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SIUiState, SIEvent, SIAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(repository.clientId.value)
```

</details>

- `fun getStandingInstructionTemplate(`
- `fun getAllStandingInstructions(`
- `fun getAllStandingInstructionsStream(` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `fun getStandingInstruction(instructionId: Long): Flow<ScreenState<StandingInstruction>>`
- `suspend fun createStandingInstruction(`
- `suspend fun updateStandingInstruction(`
- `suspend fun deleteStandingInstruction(instructionId: Long)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/ThirdPartyTransferRepository.kt`

```kotlin
interface ThirdPartyTransferRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmViewModel.kt:93</code></summary>

```kotlin
 */
internal class TransferConfirmViewModel(
    private val repository: ThirdPartyTransferRepository,
    private val pocketRepository: PocketRepository,
    private val clientRepo: ClientRepository,
    private val userVerificationRepository: UserVerificationRepository,
    savedStateHandle: SavedStateHandle,
```

</details>

- `suspend fun getTransferTemplate(): AccountOptionsTemplate`
- `fun makeTransfer(payload: TransferPayload): ScreenStateStream<TPTResponse>`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/TwoFactorAuthRepository.kt`

```kotlin
interface TwoFactorAuthRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/upi-setup/src/commonMain/kotlin/kpt/feature/upi/setup/viewmodel/SetUpUpiViewModal.kt:28</code></summary>

```kotlin
 */
class SetUpUpiViewModal(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
) : BaseViewModel<SetUpUpiState, SetUpUpiEvent, SetUpUpiAction>(
    initialState = SetUpUpiState(),
) {
```

</details>

- `suspend fun deliveryMethods(): ScreenStateStream<List<DeliveryMethod>>`
- `suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>`
- `suspend fun validateToken(token: String): ScreenStateStream<AccessToken>`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/UserRepository.kt`

```kotlin
interface UserRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:73</code></summary>

```kotlin
import org.jetbrains.compose.resources.StringResource
class SignupViewModel(
    private val userRepository: UserRepository,
    private val searchRepository: SearchRepository,
    private val clientRepository: ClientRepository,
    private val assetRepository: AssetRepository,
    private val stringProvider: StringProvider,
```

</details>

- `suspend fun getUsers(): ScreenStateStream<List<UserWithRole>>`
- `suspend fun getUser(): ScreenStateStream<UserWithRole>`
- `suspend fun updateUser(userId: Int, updatedUser: NewUser): ScreenStateStream<GenericResponse>`
- `suspend fun createUser(newUser: NewUser): Int`
- `suspend fun updateUserPassword(userId: Long, password: String)`
- `suspend fun deleteUser(userId: Int): CommonResponse`
- `suspend fun assignClientToUser(userId: Int, clientId: Int)`

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/UserVerificationRepository.kt`

```kotlin
interface UserVerificationRepository
```
One-shot, time-bounded "user passed a passcode/biometric verification just now" token.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/fork/SettingsViewModel.kt:79</code></summary>

```kotlin
    private val repository: SavingsAccountRepository,
    private val passcodeManager: PasscodeManager,
    private val userVerificationRepository: UserVerificationRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsAction>(
    initialState = run {
        val client = requireNotNull(userPreferencesRepository.client.value)
```

</details>

- `fun recordVerification()` — Stamp "verified just now" with the current monotonic time.
- `fun consumeVerification(): Boolean` — Returns `true` iff a `recordVerification` happened within the implementation's validity window (currently 30 s) **and** has not been consumed yet. Always clears the stored mark before returning.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt`

```kotlin
class AccountRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:95</code></summary>

```kotlin
    single<AssetRepository> { AssetRepositoryImpl() }
    single<AccountRepository> {
        AccountRepositoryImpl(
            apiManager = get(),
            selfManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AppLockRepositoryImpl.kt`

```kotlin
class AppLockRepositoryImpl(
```
`AppLockRepository` implementation backed by `com.russhwolf.settings.Settings` (single boolean entry under `APP_LOCK_FLAG`).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:268</code></summary>

```kotlin
    singleOf(::MifosPasscodeAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricsSetupAdapterImpl).bind<BiometricStorageAdapter>()
    singleOf(::AppLockRepositoryImpl).bind<AppLockRepository>()
    singleOf(::UserVerificationRepositoryImpl).bind<UserVerificationRepository>()

    // QR Transfer Router for smart intra/inter-bank routing
    single { QrTransferRouter(userPreferencesRepository = get()) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AssetRepositoryImpl.kt`

```kotlin
class AssetRepositoryImpl : AssetRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:93</code></summary>

```kotlin
    single<Json> { Json { ignoreUnknownKeys = true } }

    single<AssetRepository> { AssetRepositoryImpl() }
    single<AccountRepository> {
        AccountRepositoryImpl(
            apiManager = get(),
            selfManager = get(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AuthenticationRepositoryImpl.kt`

```kotlin
class AuthenticationRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:116</code></summary>

```kotlin
    }
    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(get(), get(ioDispatcher))
    }
    single<BeneficiaryRepository> { BeneficiaryRepositoryImpl(get(), get(ioDispatcher)) }
    single<ClientRepository> {
        ClientRepositoryImpl(
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayHistoryRepositoryImpl.kt`

```kotlin
class AutoPayHistoryRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:273</code></summary>

```kotlin
    // QR Transfer Router for smart intra/inter-bank routing
    single { QrTransferRouter(userPreferencesRepository = get()) }
    single<AutoPayHistoryRepository> { AutoPayHistoryRepositoryImpl(get(), get(ioDispatcher)) }

    // NOTE: `core/data/BillerRepository` (this line) is the NETWORK-based
    // Fineract biller interface. The binding is REGISTERED but currently
    // has NO CONSUMERS in the app — feature ViewModels resolve the
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt`

```kotlin
class AutoPayRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:230</code></summary>

```kotlin
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BeneficiaryRepositoryImpl.kt`

```kotlin
class BeneficiaryRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:118</code></summary>

```kotlin
        AuthenticationRepositoryImpl(get(), get(ioDispatcher))
    }
    single<BeneficiaryRepository> { BeneficiaryRepositoryImpl(get(), get(ioDispatcher)) }
    single<ClientRepository> {
        ClientRepositoryImpl(
            apiManager = get(),
            fineractApiManager = get(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillOfflineRepositoryImpl.kt`

```kotlin
class BillOfflineRepositoryImpl(
```
Phase-4 Batch-A Room-backed implementation of the historic [`kpt.core.datastore.BillRepository`]`BillRepository` contract (GOAL D12 — OFFLINE_LOCAL_ONLY archetype).

<details><summary>Example</summary>

```kotlin
single<BillRepository> { BillOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher)) }
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:297</code></summary>

```kotlin
    // the same commit to avoid a double-registration on `BillRepository`.
    single<kpt.core.datastore.BillRepository> {
        BillOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher))
    }

    // Phase-5 Batch-4 OFFLINE_LOCAL_ONLY Biller store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt`

```kotlin
class BillRepositoryImpl(
```
Network-based implementation of BillRepository. TODO: This implementation uses placeholder API endpoints.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillRepositoryImpl.kt:21</code></summary>

```kotlin
 * (e.g., UserRepositoryImpl, BeneficiaryRepositoryImpl) with proper API integration.
 */
class BillRepositoryImpl(
    private val billDataSource: BillDataSource,
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillerOfflineRepositoryImpl.kt`

```kotlin
class BillerOfflineRepositoryImpl(
```
Phase-5 Batch-4 Room-backed implementation of the historic [`kpt.core.datastore.BillerRepository`]`BillerRepository` contract (GOAL D12 — OFFLINE_LOCAL_ONLY archetype).

<details><summary>Example</summary>

```kotlin
single<BillerRepository> { BillerOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher)) }
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:307</code></summary>

```kotlin
    // `BillOfflineRepositoryImpl` cutover above.
    single<kpt.core.datastore.BillerRepository> {
        BillerOfflineRepositoryImpl(dao = get(), ioDispatcher = get(ioDispatcher))
    }

    includes(platformModule)
    single<PlatformDependentDataModule> { getPlatformDataModule }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillerRepositoryImpl.kt`

```kotlin
class BillerRepositoryImpl(
```
Network-based implementation of BillerRepository. TODO: This implementation uses placeholder API endpoints.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:289</code></summary>

```kotlin
    // kpt.core.datastore.BillerRepository below — unqualified, one replaces the other.
    single<BillerRepository>(named("kpt.core.data.repository.BillerRepository")) {
        BillerRepositoryImpl(get(), get(ioDispatcher))
    }

    // Phase-4 Batch-A OFFLINE_LOCAL_ONLY Bill store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BiometricsSetupAdapterImpl.kt`

```kotlin
class BiometricsSetupAdapterImpl(
```
`BiometricStorageAdapter` implementation backed by `com.russhwolf.settings.Settings`. Two-key layout: a boolean "registered" flag plus the registration blob string.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:267</code></summary>

```kotlin
    // token). See KDoc on the bound types for the contracts.
    singleOf(::MifosPasscodeAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricsSetupAdapterImpl).bind<BiometricStorageAdapter>()
    singleOf(::AppLockRepositoryImpl).bind<AppLockRepository>()
    singleOf(::UserVerificationRepositoryImpl).bind<UserVerificationRepository>()

    // QR Transfer Router for smart intra/inter-bank routing
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ClientRepositoryImpl.kt`

```kotlin
class ClientRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:120</code></summary>

```kotlin
    single<BeneficiaryRepository> { BeneficiaryRepositoryImpl(get(), get(ioDispatcher)) }
    single<ClientRepository> {
        ClientRepositoryImpl(
            apiManager = get(),
            fineractApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 SINGLE-ROW-PER-KEY store wiring (GOAL D13). Named-qualifier
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/DocumentRepositoryImpl.kt`

```kotlin
class DocumentRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:132</code></summary>

```kotlin
        )
    }
    single<DocumentRepository> { DocumentRepositoryImpl(get(), get(ioDispatcher)) }
    single<InvoiceRepository> {
        InvoiceRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InterBankRepositoryImpl.kt`

```kotlin
class InterBankRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:145</code></summary>

```kotlin
        )
    }
    single<InterBankRepository> { InterBankRepositoryImpl(get(), get(ioDispatcher)) }
    single<KycLevelRepository> { KycLevelRepositoryImpl(get(), get(ioDispatcher)) }
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            apiManager = get(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InvoiceRepositoryImpl.kt`

```kotlin
class InvoiceRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:134</code></summary>

```kotlin
    single<DocumentRepository> { DocumentRepositoryImpl(get(), get(ioDispatcher)) }
    single<InvoiceRepository> {
        InvoiceRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Invoice; NetworkMonitor + FetchedAtRepository
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/KycLevelRepositoryImpl.kt`

```kotlin
class KycLevelRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:146</code></summary>

```kotlin
    }
    single<InterBankRepository> { InterBankRepositoryImpl(get(), get(ioDispatcher)) }
    single<KycLevelRepository> { KycLevelRepositoryImpl(get(), get(ioDispatcher)) }
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/LocalAssetRepositoryImpl.kt`

```kotlin
class LocalAssetRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:315</code></summary>

```kotlin
    single<TimeZoneMonitor>(MifosTimeZoneMonitorQualifier) { getPlatformDataModule.timeZoneMonitor }
    single<LocalAssetRepository> {
        LocalAssetRepositoryImpl(
            ioDispatcher = get(qualifier = ioDispatcher),
            unconfinedDispatcher = get(unconfined),
            networkJson = get(),
        )
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/MifosPasscodeAdapterImpl.kt`

```kotlin
class MifosPasscodeAdapterImpl(
```
`PasscodeStorageAdapter` implementation backed by `com.russhwolf.settings.Settings`. Single-key layout — just the passcode itself.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:266</code></summary>

```kotlin
    // (the two adapters) plus the in-app re-auth machinery (lock + verification
    // token). See KDoc on the bound types for the contracts.
    singleOf(::MifosPasscodeAdapterImpl).bind<PasscodeStorageAdapter>()
    singleOf(::BiometricsSetupAdapterImpl).bind<BiometricStorageAdapter>()
    singleOf(::AppLockRepositoryImpl).bind<AppLockRepository>()
    singleOf(::UserVerificationRepositoryImpl).bind<UserVerificationRepository>()
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/NotificationRepositoryImpl.kt`

```kotlin
class NotificationRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:148</code></summary>

```kotlin
    single<KycLevelRepository> { KycLevelRepositoryImpl(get(), get(ioDispatcher)) }
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Notification; NetworkMonitor + FetchedAtRepository
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/OfficeRepositoryImpl.kt`

```kotlin
class OfficeRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:232</code></summary>

```kotlin
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.Offices; NetworkMonitor + FetchedAtRepository
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt`

```kotlin
class PocketRepositoryImp(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:244</code></summary>

```kotlin
    }
    single<PocketRepository> {
        PocketRepositoryImp(
            dataManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-2 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.Pocket; NetworkMonitor + FetchedAtRepository bound
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RecentPayeeRepositoryImpl.kt`

```kotlin
class RecentPayeeRepositoryImpl(
```
Implementation of `RecentPayeeRepository` that fetches recent payees from transaction history and caches the DERIVED result in Room via the Store5 `recentPayee` offline store (Phase-5 Batch-4, GOAL D12/D13 hybrid).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:160</code></summary>

```kotlin
    }
    single<RecentPayeeRepository> {
        RecentPayeeRepositoryImpl(
            selfServiceApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-4 LOCAL-DERIVED store wiring (GOAL D12/D13
            // hybrid). The DAO is bound by StoreModule
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RegistrationRepositoryImpl.kt`

```kotlin
class RegistrationRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:170</code></summary>

```kotlin
        )
    }
    single<RegistrationRepository> { RegistrationRepositoryImpl(get(), get(ioDispatcher)) }
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RunReportRepositoryImpl.kt`

```kotlin
class RunReportRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:171</code></summary>

```kotlin
    }
    single<RegistrationRepository> { RegistrationRepositoryImpl(get(), get(ioDispatcher)) }
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavedCardRepositoryImpl.kt`

```kotlin
class SavedCardRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:173</code></summary>

```kotlin
    single<RunReportRepository> { RunReportRepositoryImpl(get(), get(ioDispatcher)) }
    single<SavedCardRepository> {
        SavedCardRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-1 LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.SavedCards; NetworkMonitor + FetchedAtRepository
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavingsAccountRepositoryImpl.kt`

```kotlin
class SavingsAccountRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:185</code></summary>

```kotlin
    }
    single<SavingsAccountRepository> {
        SavingsAccountRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-1 SINGLE-ROW-PER-KEY store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.AccountDetail; NetworkMonitor + FetchedAtRepository
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SearchRepositoryImpl.kt`

```kotlin
class SearchRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:196</code></summary>

```kotlin
        )
    }
    single<SearchRepository> { SearchRepositoryImpl(get(), get(ioDispatcher)) }
    single<SelfServiceRepository> {
        SelfServiceRepositoryImpl(
            apiManager = get(),
            dispatcher = get(ioDispatcher),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SelfServiceRepositoryImpl.kt`

```kotlin
class SelfServiceRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:198</code></summary>

```kotlin
    single<SearchRepository> { SearchRepositoryImpl(get(), get(ioDispatcher)) }
    single<SelfServiceRepository> {
        SelfServiceRepositoryImpl(
            apiManager = get(),
            dispatcher = get(ioDispatcher),
            // Phase-4 Batch-A LEDGER store wiring (GOAL D13). Named-qualifier from
            // AppStoreRegistry.History; NetworkMonitor + FetchedAtRepository bound by
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/StandingInstructionRepositoryImpl.kt`

```kotlin
class StandingInstructionRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:214</code></summary>

```kotlin
    }
    single<StandingInstructionRepository> {
        StandingInstructionRepositoryImpl(
            apiManager = get(),
            ioDispatcher = get(ioDispatcher),
            // Phase-5 Batch-3 LEDGER store wiring (GOAL D13). Named-qualifier
            // from AppStoreRegistry.StandingInstruction; NetworkMonitor +
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ThirdPartyTransferRepositoryImpl.kt`

```kotlin
class ThirdPartyTransferRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:226</code></summary>

```kotlin
    }
    single<ThirdPartyTransferRepository> {
        ThirdPartyTransferRepositoryImpl(get(), get(ioDispatcher))
    }
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/TwoFactorAuthRepositoryImpl.kt`

```kotlin
class TwoFactorAuthRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:228</code></summary>

```kotlin
        ThirdPartyTransferRepositoryImpl(get(), get(ioDispatcher))
    }
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserRepositoryImpl.kt`

```kotlin
class UserRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:229</code></summary>

```kotlin
    }
    single<TwoFactorAuthRepository> { TwoFactorAuthRepositoryImpl(get(), get(ioDispatcher)) }
    single<UserRepository> { UserRepositoryImpl(get(), get(ioDispatcher)) }
    single<AutoPayRepository> { AutoPayRepositoryImpl(get(), get(ioDispatcher)) }
    single<OfficeRepository> {
        OfficeRepositoryImpl(
            apiManager = get(),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserVerificationRepositoryImpl.kt`

```kotlin
class UserVerificationRepositoryImpl : UserVerificationRepository
```
`UserVerificationRepository` implementation backed by `TimeSource.Monotonic`, held in-process for the lifetime of this singleton. A wall-clock-based implementation (e.g.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:269</code></summary>

```kotlin
    singleOf(::BiometricsSetupAdapterImpl).bind<BiometricStorageAdapter>()
    singleOf(::AppLockRepositoryImpl).bind<AppLockRepository>()
    singleOf(::UserVerificationRepositoryImpl).bind<UserVerificationRepository>()

    // QR Transfer Router for smart intra/inter-bank routing
    single { QrTransferRouter(userPreferencesRepository = get()) }
    single<AutoPayHistoryRepository> { AutoPayHistoryRepositoryImpl(get(), get(ioDispatcher)) }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt`

```kotlin
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
```
Binds the network's `AuthTokenSource` port to where the credential actually lives.

```kotlin
fun provideAuthHeaderBridge(
```
Eager: starts collecting the credential at graph construction. `createdAtStart` is load-bearing. Built lazily, this would construct on first injection — and nothing injects it, because its whole job is a side effect.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutEvent.kt`

```kotlin
data class LogoutEvent(
```
Result class to share the `loggedOutUserId` of a user that was successfully logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:24</code></summary>

```kotlin
     * Observable flow of [LogoutEvent]s
     */
    val logoutEventFlow: SharedFlow<LogoutEvent>

    /**
     * Completely logs out the given [userId], removing all data. The [reason] indicates why the
     * user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/LogoutReason.kt`

```kotlin
sealed class LogoutReason
```
Indicates the reason that the user is being logged out.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt:30</code></summary>

```kotlin
     * user is being logged out.
     */
    fun logout(userId: Long, reason: LogoutReason)

    /**
     * Partially logs out the given [userId]. All data for the given [userId] will be removed with
     * the exception of basic account data. The [reason] indicates why the user is being logged out.
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserDataRepository.kt`

```kotlin
interface UserDataRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingViewModel.kt:33</code></summary>

```kotlin
 */
class OnboardingViewModel(
    private val userDataRepository: UserDataRepository,
) : BaseViewModel<OnboardingState, Nothing, OnboardingAction>(OnboardingState()) {

    override fun handleAction(action: OnboardingAction) {
        when (action) {
```

</details>

- `val userData: StateFlow<UserData>` — The whole preference aggregate as hot state, so a screen has a value at first composition.
- `fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData>` — Store5-backed read of the same preferences, as a `ScreenDataStream`. `userData` stays for the many call sites that just want the current value (auth, theme bootstrap).
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read; prefer a stream when the value can change under you.
- `val passcode: String` — The app-lock passcode.
- `val observeLanguage: Flow<LanguageConfig>` — Selected language, re-emitting on change.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Dark-mode preference as a stream.
- `val observeDynamicColorPreference: Flow<Boolean>` — Whether to derive the palette from platform dynamic colour.
- `val observeScreenCapturePreference: Flow<Boolean>` — Whether screenshots and screen recording are permitted.
- `val observeAnalyticsConsent: Flow<Boolean>` — The user's consent to share usage analytics.
- `val observeCrashReportingConsent: Flow<Boolean>` — The user's consent to send crash reports.
- `suspend fun setAnalyticsConsent(granted: Boolean)` — Records the user's analytics consent — given or withdrawn.
- `suspend fun setCrashReportingConsent(granted: Boolean)` — Records the user's crash-reporting consent — given or withdrawn.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the app language.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the colour brand.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/user/UserLogoutManager.kt`

```kotlin
interface UserLogoutManager
```
Coordinates logout across the app: wipes local state and notifies observers. Centralised so "what must be cleared on logout" is answered once. A feature clearing its own slice is how one gets missed.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:60</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

- `val logoutEventFlow: SharedFlow<LogoutEvent>` — Observable flow of `LogoutEvent`s
- `fun logout(userId: Long, reason: LogoutReason)` — Completely logs out the given `userId`, removing all data. The `reason` indicates why the user is being logged out.
- `fun softLogout(userId: Long, reason: LogoutReason)` — Partially logs out the given `userId`. All data for the given `userId` will be removed with the exception of basic account data. The `reason` indicates why the user is being logged out.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserDataRepositoryImpl.kt`

```kotlin
class UserDataRepositoryImpl(
```
Default `UserDataRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/user/impl/UserLogoutManagerImpl.kt`

```kotlin
class UserLogoutManagerImpl(
```
Default `UserLogoutManager`: clears every Store cache and draft row, then emits the logout event. Cache-clearing is the load-bearing half — a Store that survives logout would serve the previous user's data to the next one.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:60</code></summary>

```kotlin
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<UserLogoutManager> { UserLogoutManagerImpl(get(), get(), get()) }
}

/**
 * Per-target repository bindings supplied by each `actual` — the pieces that cannot be expressed in
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/AppLockOption.kt`

```kotlin
enum class AppLockOption
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/util/Helpers.kt:17</code></summary>

```kotlin
object Helpers {

    fun authOptionToStringMapperFunction(option: AppLockOption): String {
        return when (option) {
            AppLockOption.MifosPasscode -> MIFOS_PASSCODE_VALUE
            AppLockOption.DeviceLock -> DEVICE_AUTHENTICATION_METHOD_VALUE
            AppLockOption.None -> ""
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/AutoPayValidator.kt`

```kotlin
object AutoPayValidator
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt:149</code></summary>

```kotlin
    ) {
        withContext(ioDispatcher) {
            when (val validationResult = AutoPayValidator.validateAutoPayPayload(payload)) {
                is AutoPayValidator.ValidationResult.Valid -> Unit
                is AutoPayValidator.ValidationResult.Invalid -> {
                    throw IllegalArgumentException(validationResult.errorMessage)
                }
```

</details>

- `fun validateAutoPayPayload(payload: AutoPayPayload): ValidationResult`
- `val startDate = parseDate(payload.validFrom)`
- `val endDate = parseDate(payload.validTill)`
- `fun validateAutoPayUpdatePayload(payload: AutoPayUpdatePayload): ValidationResult`
- `val startDate = parseDate(payload.validFrom!!)`
- `val endDate = parseDate(payload.validTill!!)`
- `val validFrequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")`

### `core/data/src/commonMain/kotlin/kpt/core/data/util/BillErrorHandler.kt`

```kotlin
object BillErrorHandler
```
Utility class for handling bill-related errors

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt:160</code></summary>

```kotlin
        val validationResult = BillValidator.validateBill(bill)
        if (!validationResult.isValid) {
            val errorMessage = BillErrorHandler.handleValidationError(validationResult)
            throw IllegalArgumentException(errorMessage)
        }
    }
```

</details>

- `fun handleNetworkError(throwable: Throwable): String` — Handles network errors and converts them to user-friendly messages
- `fun handleValidationError(validationResult: kpt.core.model.autopay.BillValidationResult): String` — Handles validation errors and converts them to user-friendly messages
- `fun createOperationErrorMessage(operation: String, error: String): String` — Creates a user-friendly error message for specific bill operations
- `fun isRetryableError(throwable: Throwable): Boolean` — Checks if an error is retryable
- `fun getRetryDelay(throwable: Throwable): Long` — Gets retry delay in milliseconds based on error type

### `core/data/src/commonMain/kotlin/kpt/core/data/util/BillValidator.kt`

```kotlin
object BillValidator
```
Utility class for validating bill data before submission

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:148</code></summary>

```kotlin

    private fun updateBillName(name: String) {
        val nameError = BillValidator.validateBillFormData(
            BillFormData(name = name),
        ).nameError
        mutableStateFlow.update {
            it.copy(
```

</details>

- `fun validateBill(bill: Bill): BillValidationResult` — Validates a Bill object
- `val nameError = validateName(bill.name)`
- `val amountError = validateAmount(bill.amount)`
- `val dueDateError = validateDueDate(bill.dueDate)`
- `val recurrencePatternError = validateRecurrencePattern(bill.recurrencePattern)`
- `val billerError = validateBiller(bill.billerId, bill.billerName)`
- `val isValid = nameError == null &&`
- `fun validateBillFormData(formData: BillFormData): BillValidationResult` — Validates BillFormData object
- `val nameError = validateName(formData.name)`
- `val amountError = validateAmountString(formData.amount)`
- `val dueDateError = validateDueDate(formData.dueDate)`
- `val recurrencePatternError = validateRecurrencePattern(formData.recurrencePattern)`
- `val billerError = validateBiller(formData.billerId, formData.billerName)`
- `val autoPayPaymentMethodError = validateAutoPayPaymentMethod(formData.enableAutoPay, formData.autoPayPaymentMethod)`
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/util/BillerValidator.kt`

```kotlin
object BillerValidator
```
Validator for biller form data with comprehensive validation rules.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerViewModel.kt:124</code></summary>

```kotlin

    private fun updateBillerName(name: String) {
        val nameError = BillerValidator.validateNameField(name)
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(name = name),
                validationResult = it.validationResult.copy(nameError = nameError),
```

</details>

- `fun validateBillerForm(formData: BillerFormData): BillerValidationResult` — Validates the complete biller form data and returns validation result.
- `val nameError = validateName(formData.name)`
- `val accountNumberError = validateAccountNumber(formData.accountNumber)`
- `val contactNumberError = validateContactNumber(formData.contactNumber)`
- `val emailError = validateEmail(formData.email)`
- `val categoryError = validateCategory(formData.category)`
- `val addressError = validateAddress(formData.address)`
- `val isValid = nameError == null &&`
- `val validationResult = BillerValidationResult(`
- `fun validateNameField(name: String): String? = validateName(name)` — Validates individual name field for real-time validation.
- `fun validateAccountNumberField(accountNumber: String): String? = validateAccountNumber(accountNumber)` — Validates individual account number field for real-time validation.
- `fun validateContactNumberField(contactNumber: String): String? = validateContactNumber(contactNumber)` — Validates individual contact number field for real-time validation.
- `fun validateEmailField(email: String): String? = validateEmail(email)` — Validates individual email field for real-time validation.

### `core/data/src/commonMain/kotlin/kpt/core/data/util/Constants.kt`

```kotlin
object Constants
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModel.kt:191</code></summary>

```kotlin
            val result = searchRepository.searchResources(
                query = phoneNo,
                resources = Constants.CLIENTS,
                exactMatch = true,
            )
            if (result.isEmpty()) {
                // TODO:: Call repository request an otp to this phone no.
```

</details>

- `const val BASIC = "Basic "`
- `const val SAVINGS = "savingsAccounts"`
- `const val TRANSACTIONS = "transactions"`
- `const val WALLET_ACCOUNT_SAVINGS_PRODUCT_ID = 1`
- `const val MIFOS_MERCHANT_SAVINGS_PRODUCT_ID = 165 // 372`
- `const val MIFOS_CONSUMER_SAVINGS_PRODUCT_ID = 165 // 373`
- `val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)`
- `const val ENTITY_TYPE_CLIENTS = "clients"`
- `const val FETCH_ACCOUNT_TRANSFER_USECASE = "FetchAccountTransfer"`
- `const val NO_SAVED_CARDS = "No saved cards."`
- `const val MIFOS_PASSWORD = "mifos:password"`
- `const val ERROR_VERIFYING_USER = "Error verifying user"`
- `const val ERROR_REGISTERING_USER = "Error registering user"`
- `const val NO_CLIENTS_FOUND = "No clients found"`
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/util/Helpers.kt`

```kotlin
object Helpers
```
_No KDoc at source._

<details><summary>Used in the template — <code>core-base/crypto/src/wasmJsMain/kotlin/kpt/core/base/crypto/WebSecureCrypto.wasmJs.kt:92</code></summary>

```kotlin
)""",
)
private external fun installHelpers(): KptSecureHelpers

/**
 * Browser crypto for the WasmJS target. Same WebCrypto surface as the JS actual, kept separate only because the two
 * targets do not share an interop layer.
```

</details>

- `fun authOptionToStringMapperFunction(option: AppLockOption): String`
- `fun stringToAuthOptionMapperFunction(option: String): AppLockOption`

### `core/data/src/commonMain/kotlin/kpt/core/data/util/MifosErrorParser.kt`

```kotlin
val parseMifosError: ErrorBodyParser = { responseBody, statusCode ->
```
Parses the Mifos/Fineract server error response body to extract a user-friendly message. This parser follows the mifos-mobile pattern for extracting error messages from the standard Mifos/Fineract error response format.

<details><summary>Example</summary>

```kotlin
{
  "developerMessage": "The resource with id 123 was not found.",
  "httpStatusCode": "404",
  "defaultUserMessage": "Account not found",
  "userMessageGlobalisationCode": "error.msg.account.not.found",
  "errors": [
    {
      "developerMessage": "Account ID is invalid",
      "defaultUserMessage": "The account does not exist",
      "parameterName": "accountId"
    }
  ]
}
```

</details>

```kotlin
fun extractMifosErrorMessage(responseBody: String, statusCode: Int): String
```
Extracts a user-friendly error message from a Mifos/Fineract error response. Priority order: 1. First error's defaultUserMessage (for validation errors) 2. Root level defaultUserMessage 3. Root level developerMessage 4.

### `core/data/src/commonMain/kotlin/kpt/core/data/util/MpayQrCodeProcessor.kt`

```kotlin
object MpayQrCodeProcessor
```
MPay QR Code Encoder and Decoder with validation.

<details><summary>Example</summary>

```kotlin
mpay://pay?qt=0&fsp=mifos-bank-1&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2&ae=EXT123
```

</details>

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayViewModel.kt:70</code></summary>

```kotlin

            try {
                val qrData = MpayQrCodeProcessor.decodeMpayString(encodedQrData)
                // Phase-5 Batch-3: processor now consumes the store-backed
                // `getBeneficiaryListScreen(clientId, scope)` (batch-1 reuse)
                // and `getOfficesScreen(scope)` (batch-3 new), so it needs the
                // caller's CoroutineScope for Store5 refresh-trigger wiring.
```

</details>

- `fun encodeMpayString(qrCodeData: QrCodeData): String` — Encodes MPay payment details into a Base64 encoded string.
- `val requestPaymentString = when (qrCodeData.type)`
- `fun decodeMpayString(encodedString: String): QrCodeData` — Decodes a Base64 encoded MPay or UPI payment string. Supports both new mpay:// and legacy upi:// formats for backwards compatibility.
- `val normalizedInput = encodedString.trim()`
- `val decodedString = try`
- `val params = when`
- `val fspId = params["fsp"]?.takeIf { it.isNotBlank() }`
- `val accountExternalId = params["ae"]?.takeIf { it.isNotBlank() }`
- `val phoneNumber = params["pn"]?.takeIf { it.isNotBlank() }`
- `val clientId = params["ci"]?.toLongOrNull() ?: 0L`
- `val accountId = params["ai"]?.toLongOrNull() ?: 0L`
- `val type = params["qt"]?.toIntOrNull()?.let { QrCodeType.fromOrdinal(it) }`
- `val hasInternalIds = clientId > 0 && accountId > 0`
- `val hasAccountExternalId = accountExternalId != null`
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/util/NetworkMonitor.kt`

```kotlin
interface NetworkMonitor
```
Utility for reporting app connectivity status

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/AndroidPlatformDependentDataModule.kt:25</code></summary>

```kotlin
    private val scope: CoroutineScope,
) : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context, dispatcher)
    }

    override val timeZoneMonitor: TimeZoneMonitor by lazy {
```

</details>

- `val isOnline: Flow<Boolean>`

```kotlin
val MifosNetworkMonitorQualifier = named("kpt.core.data.util.NetworkMonitor")
```
Koin qualifier for this `NetworkMonitor`.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/fork/AndroidPlatformModule.kt:29</code></summary>

```kotlin
    // `NetworkMonitorProvider.install()` (cmp-network-monitor 3.6.0+, which carries the
    // AndroidNetworkMonitor cold-start seed fix) — no per-platform override needed here.
    single<NetworkMonitor>(MifosNetworkMonitorQualifier) {
        ConnectivityManagerNetworkMonitor(androidContext(), get(named(MifosDispatchers.IO.name)))
    }

    single<TimeZoneMonitor>(MifosTimeZoneMonitorQualifier) {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/QrTransferRouter.kt`

```kotlin
class QrTransferRouter(
```
Routes QR code scans to appropriate transfer flow based on: 1. FSP ID comparison (same bank vs different bank) 2.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrViewModel.kt:36</code></summary>

```kotlin
 */
class ScanQrViewModel(
    private val qrTransferRouter: QrTransferRouter,
) : BaseViewModel<ScanQrState, ScanQrEvent, ScanQrAction>(
    initialState = ScanQrState,
) {
```

</details>

```kotlin
sealed interface QrRouteResult
```
Result of QR code routing decision.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrViewModel.kt:119</code></summary>

```kotlin
            // to avoid StateFlow conflation issues)
            when (routeResult) {
                is QrRouteResult.IntraBank -> {
                    Logger.d { "QR Route -> Intra-bank transfer, emitting event..." }
                    sendEvent(ScanQrEvent.OnNavigateToIntraBankTransfer(routeResult.qrData))
                    Logger.d { "QR Route -> Event emitted: OnNavigateToIntraBankTransfer" }
                }
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/ScreenStateBridge.kt`

```kotlin
fun <T> KptScreenState<T>.toForkScreenState(): ForkScreenState<T> = when (this)
```
Bridge extension: converts a single `kpt.core.base.store.screen.ScreenState<T>` (emitted by Store5 `asScreenStream(...)`) into the fork's `kpt.core.common.ScreenState<T>`.

```kotlin
fun <T> Flow<KptScreenState<T>>.toForkScreenStateFlow(): Flow<ForkScreenState<T>> =
```
Flow-level convenience wrapper — every Store5 impl calls `store.asScreenStream(...).state.toForkScreenStateFlow()` to satisfy the fork repository return type.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:107</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/SharedFlowExtensions.kt`

```kotlin
fun <T> bufferedMutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(
```
Creates a `MutableSharedFlow` with a buffer of `Int.MAX_VALUE` and the given `replay` count.

### `core/data/src/commonMain/kotlin/kpt/core/data/util/StandardUpiQrCodeProcessor.kt`

```kotlin
object StandardUpiQrCodeProcessor
```
Standard UPI QR Code Processor Handles parsing of standard UPI QR codes according to UPI specification

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsViewModel.kt:30</code></summary>

```kotlin
        if (safeQrCodeDataString.isNotEmpty()) {
            val qrCodeDataString = safeQrCodeDataString.urlDecode()
            val isUpiCode = StandardUpiQrCodeProcessor.isValidUpiQrCode(qrCodeDataString)

            val qrCodeData = if (isUpiCode) {
                StandardUpiQrCodeProcessor.parseUpiQrCode(qrCodeDataString)
            } else {
```

</details>

- `fun isValidUpiQrCode(qrData: String): Boolean` — Checks if the given string is a valid UPI QR code
- `fun parseUpiQrCode(qrData: String): StandardUpiQrData` — Parses a standard UPI QR code string
- `val paramsString = qrData.substringAfter("upi://").substringAfter("UPI://")`
- `val parts = paramsString.split("?", limit = 2)`
- `val params = if (parts.size > 1) parseParams(parts[1]) else emptyMap()`
- `val payeeVpa = params["pa"] ?: run`
- `val payeeName = params["pn"] ?: "Unknown"`
- `val vpaParts = payeeVpa.split("@", limit = 2)`
- `val actualVpa = if (vpaParts.size == 2) payeeVpa else payeeVpa`
- `val keyValue = param.split("=", limit = 2)`
- `fun toPaymentQrData(standardData: StandardUpiQrData): PaymentQrData`

### `core/data/src/commonMain/kotlin/kpt/core/data/util/TimeZoneMonitor.kt`

```kotlin
interface TimeZoneMonitor
```
Utility for reporting current timezone the device has set. It always emits at least once with default setting and then for each TZ change.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/AndroidPlatformDependentDataModule.kt:29</code></summary>

```kotlin
    }

    override val timeZoneMonitor: TimeZoneMonitor by lazy {
        TimeZoneBroadcastMonitor(context, scope, dispatcher)
    }
}
```

</details>

- `val currentTimeZone: Flow<TimeZone>`

```kotlin
val MifosTimeZoneMonitorQualifier = named("kpt.core.data.util.TimeZoneMonitor")
```
Koin qualifier for this `TimeZoneMonitor`.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/fork/AndroidPlatformModule.kt:33</code></summary>

```kotlin
    }

    single<TimeZoneMonitor>(MifosTimeZoneMonitorQualifier) {
        TimeZoneBroadcastMonitor(
            context = androidContext(),
            appScope = get(named("ApplicationScope")),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/util/UpiQrCodeProcessor.kt`

```kotlin
object UpiQrCodeProcessor
```
UPI QR Code Encoder and Decoder with validation

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyViewModel.kt:184</code></summary>

```kotlin
            }

            val paymentString = UpiQrCodeProcessor.encodeUpiString(state.paymentQrData)

            sendEvent(SendMoneyEvent.NavigateToTransferScreen(paymentString))
        }
    }
```

</details>

- `fun encodeUpiString(paymentQrData: PaymentQrData): String` — Encodes UPI payment details into a Base64 encoded string
- `val requestPaymentString = buildString`
- `fun decodeUpiString(encodedString: String): PaymentQrData` — Decodes a Base64 encoded UPI payment string
- `val decodedString = encodedString.decodeBase64String()`
- `val params = parseUpiString(decodedString)`
- `val requestQrData = PaymentQrData(`
- `val amountValue = data.amount.toDoubleOrNull()`
- `val (key, value) = it.split("=")`

---

_90 type(s), 320 function(s)/property(ies); 146 carry KDoc at source; 6 authored example(s); 93 live call site(s)._
<!-- api-docs:end -->
