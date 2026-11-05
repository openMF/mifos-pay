# `core/datastore`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATASTORE.md`
> **Measured:** 32 Kotlin files, 2 test files

## Principal types

`AppReviewPromptState`, `AppReviewPromptStore`, `AutoPayPreferencesDataSource`, `AutoPayPreferencesRepository`, `AutoPayPreferencesRepositoryImpl`, `BillDataSource`, `BillRepository`, `BillRepositoryImpl`, `BillerDataSource`, `BillerRepository`, `BillerRepositoryImpl`, `ChangeListVersions`, `ClientPreferences`, `ForkPreferencesRepository`  …and 10 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/datastore sha=68d05ad049c3b9cc5cff76f36073274d55137d07 -->
## API reference

_Generated from `core/datastore` at tree `68d05ad049c3` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountViewModel.kt:46</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val userRepository: UserPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
```

</details>

- `val userData: StateFlow<UserData>`
- `val authToken: String?`
- `val passcode: String`
- `val observeLanguage: Flow<LanguageConfig>`
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>`
- `val observeDynamicColorPreference: Flow<Boolean>`
- `val observeScreenCapturePreference: Flow<Boolean>`
- `suspend fun setLanguage(language: LanguageConfig)`
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)`
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)`
- `suspend fun setDynamicColorPreference(useDynamicColor: Boolean)`
- `suspend fun setIsAuthenticated(isAuthenticated: Boolean)`
- `suspend fun setIsUnlocked(isUnlocked: Boolean)`
- `suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)`
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
Splits user data storage between plain (UI preferences) and secure (credentials/auth state) Settings backends.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:36</code></summary>

```kotlin

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get(),
        )
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt`

```kotlin
val DatastoreModule = module
```
Koin module for `core/datastore` — the fork's preference stores, over the plain and secure `Settings` instances that `DatastoreBaseModule` binds. Preferences only.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:39</code></summary>

```kotlin
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    // Every repository's Koin binding, GENERATED from `@RepositoryBinding` on the implementation.
    // Emitted into this same package, so this file needs no import and keeps its zero-demo-reference
    // property — which is what lets a template sync blind-copy it. A stripped fork simply generates
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt`

```kotlin
val ForkDatastoreModule = module
```
THE FORK'S datastore DI seam. Empty on the neutral template — this is yours to fill. `DatastoreModule` beside it is `owner: template` and FULL-COPIES on a sync, so a binding added there is replaced on the next adopt.

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/infra/ChangeListVersions.kt`

```kotlin
data class ChangeListVersions(val versions: Map<String, Long> = emptyMap())
```
Per-feature last-synced version map.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/ChangeListVersionsTest.kt:26</code></summary>

```kotlin
    @Test
    fun round_trips_through_kotlinx_serialization() {
        val original = ChangeListVersions(
            mapOf("currency-rates" to 1_700_000_000L, "macro-indicators" to 42L),
        )
        val encoded = Json.encodeToString(ChangeListVersions.serializer(), original)
        val decoded = Json.decodeFromString(ChangeListVersions.serializer(), encoded)
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/infra/SyncStatePersister.kt`

```kotlin
interface SyncStatePersister
```
Persistence seam for the `Synchronizer`'s per-feature last-synced version map. Backed by Multiplatform Settings (same store used by `UserPreferencesRepositoryImpl` for plain user prefs) — survives process restart but not data wipe.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:51</code></summary>

```kotlin
    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
```

</details>

- `suspend fun read(): ChangeListVersions`
- `suspend fun write(versions: ChangeListVersions)`

```kotlin
class SettingsSyncStatePersister(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:52</code></summary>

```kotlin
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/AppReviewPromptStore.kt`

```kotlin
data class AppReviewPromptState(
```
What the review policy needs to know at app open, derived from `AppReviewPromptStore`.

```kotlin
interface AppReviewPromptStore
```
Persisted state behind the in-app review prompt — when it was last shown and whether the user already responded.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:45</code></summary>

```kotlin
    // Review-prompt counters — Settings-backed, DEVICE-scoped (survives sign-out, unlike user prefs).
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
```

</details>

- `fun recordLaunch(): AppReviewPromptState` — Record an app launch and return the resulting state. Called once per launch from the app shell.
- `fun recordPromptShown()` — Stamp that a prompt was just requested, starting the cooldown. Recorded on REQUEST, not on a completed review, because neither Play nor StoreKit reports whether the user actually reviewed.

```kotlin
class SettingsAppReviewPromptStore(
```
`Settings`-backed `AppReviewPromptStore`, using the same plain store as user preferences.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:46</code></summary>

```kotlin
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ForkPreferencesRepository.kt`

```kotlin
interface ForkPreferencesRepository : UserPreferencesRepository
```
THE FORK'S preferences. Extends the framework's — this is yours to fill.

<details><summary>Example</summary>

```kotlin
interface ForkPreferencesRepository : UserPreferencesRepository {
    val observeMyFlag: Flow<Boolean>
    suspend fun setMyFlag(enabled: Boolean)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt:31</code></summary>

```kotlin
 */
val ForkDatastoreModule = module {
    single<ForkPreferencesRepository> {
        ForkPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ForkPreferencesRepositoryImpl.kt`

```kotlin
class ForkPreferencesRepositoryImpl(
```
Fork implementation of `ForkPreferencesRepository`. `owner: fork` — never synced.

<details><summary>Example</summary>

```kotlin
override val observeMyFlag: Flow<Boolean> =
    MutableStateFlow(plainSettings.getBoolean(KEY_MY_FLAG, false))

override suspend fun setMyFlag(enabled: Boolean) = withContext(dispatcher.io) {
    plainSettings.putBoolean(KEY_MY_FLAG, enabled)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt:32</code></summary>

```kotlin
val ForkDatastoreModule = module {
    single<ForkPreferencesRepository> {
        ForkPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
            plainSettings = get<Settings>(named("plain")),
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountViewModel.kt:46</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val userRepository: UserPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
```

</details>

- `val userData: StateFlow<UserData>` — The whole preference aggregate as hot state. A StateFlow, not a Flow, because almost every screen reads it during composition and needs a value immediately.
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read for a caller that already has one in hand; prefer `observeAuthToken` when the value can change under you.
- `val observeAuthToken: Flow<String?>` — The credential as a stream, re-emitting on sign-in and sign-out.
- `val passcode: String` — The app-lock passcode. Synchronous — the lock screen compares it on each keystroke.
- `val observeLanguage: Flow<LanguageConfig>` — Selected language, re-emitting on change so the locale switch takes effect without a restart.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Dark-mode preference as a stream.
- `val observeDynamicColorPreference: Flow<Boolean>` — Whether to derive the palette from platform dynamic colour.
- `val observeScreenCapturePreference: Flow<Boolean>` — Whether screenshots and screen recording are permitted.
- `val observeAnalyticsConsent: Flow<Boolean>` — The user's consent to share usage analytics.
- `val observeCrashReportingConsent: Flow<Boolean>` — The user's consent to send crash reports.
- `suspend fun setAuthToken(token: String?)` — Persist `token` into the ENCRYPTED store, or clear it when null. Call on sign-in with the value the auth endpoint returned (for Basic, the base64 of `user:password`; for OAuth, the access token) and on sign-out with null.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the app language.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the colour brand.
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)` — Persists the dark-mode preference.
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
Splits user data storage between plain (UI preferences) and secure (credentials/auth state) Settings backends.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepositoryImpl.kt:41</code></summary>

```kotlin
 * stores using a write-before-delete strategy to prevent data loss.
 */
class UserPreferencesRepositoryImpl(
    private val plainSettings: Settings,
    private val secureSettings: Settings,
    private val dispatcher: DispatcherManager,
) : UserPreferencesRepository {
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/AutoPayPreferencesDataSource.kt`

```kotlin
class AutoPayPreferencesDataSource(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/AutoPayPreferencesRepositoryImpl.kt:22</code></summary>

```kotlin

class AutoPayPreferencesRepositoryImpl(
    private val autoPayPreferencesDataSource: AutoPayPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
) : AutoPayPreferencesRepository {
    private val unconfinedScope = CoroutineScope(unconfinedDispatcher)
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/AutoPayPreferencesRepository.kt`

```kotlin
interface AutoPayPreferencesRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/AutoPayPreferencesRepositoryImpl.kt:25</code></summary>

```kotlin
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
) : AutoPayPreferencesRepository {
    private val unconfinedScope = CoroutineScope(unconfinedDispatcher)

    override val isAutoPayEnabled: StateFlow<Boolean> = autoPayPreferencesDataSource.isAutoPayEnabled
```

</details>

- `val isAutoPayEnabled: StateFlow<Boolean>` — AutoPay enabled state
- `val cachedAutoPaySchedules: Flow<List<AutoPay>>` — Cached AutoPay schedules
- `val cachedUpcomingPayments: Flow<List<UpcomingPayment>>` — Cached upcoming payments
- `val cachedAutoPayHistory: Flow<List<AutoPayHistory>>` — Cached AutoPay history
- `val lastSyncTimestamp: StateFlow<Long>` — Last sync timestamp
- `suspend fun updateAutoPayEnabled(enabled: Boolean)` — Update AutoPay enabled state
- `suspend fun cacheAutoPaySchedules(schedules: List<AutoPay>)` — Cache AutoPay schedules
- `suspend fun cacheUpcomingPayments(payments: List<UpcomingPayment>)` — Cache upcoming payments
- `suspend fun cacheAutoPayHistory(history: List<AutoPayHistory>)` — Cache AutoPay history
- `suspend fun updateLastSyncTimestamp(timestamp: Long)` — Update last sync timestamp
- `suspend fun clearCache()` — Clear all cached data
- `suspend fun getCachedAutoPaySchedule(autoPayId: Long): AutoPay?` — Get cached AutoPay schedule by ID
- `suspend fun isCacheStale(maxAgeMinutes: Long = 30): Boolean` — Check if cache is stale (older than specified time)

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/AutoPayPreferencesRepositoryImpl.kt`

```kotlin
class AutoPayPreferencesRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/di/PreferenceModule.kt:65</code></summary>

```kotlin

    single<AutoPayPreferencesRepository> {
        AutoPayPreferencesRepositoryImpl(
            autoPayPreferencesDataSource = get(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
            unconfinedDispatcher = get(named(MifosDispatchers.Unconfined.name)),
        )
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillDataSource.kt`

```kotlin
class BillDataSource(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillRepositoryImpl.kt:23</code></summary>

```kotlin
 */
class BillRepositoryImpl(
    private val billDataSource: BillDataSource,
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
        return billDataSource.bills
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillRepository.kt`

```kotlin
interface BillRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillViewModel.kt:40</code></summary>

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

- `fun getAllBills(): Flow<List<Bill>>` — Get all saved bills
- `suspend fun getBillById(id: String): Bill?` — Get bill by ID
- `suspend fun saveBill(bill: Bill): Bill` — Save a new bill. Returns the saved bill or throws on failure.
- `suspend fun updateBill(bill: Bill): Bill` — Update an existing bill. Returns the updated bill or throws on failure.
- `suspend fun deleteBill(id: String)` — Delete a bill. Throws on failure.
- `suspend fun searchBillsByName(query: String): List<Bill>` — Search bills by name
- `suspend fun clearAllBills()` — Clear all bills. Throws on failure.

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillRepositoryImpl.kt`

```kotlin
class BillRepositoryImpl(
```
TODO: This implementation currently uses local storage (Multiplatform Settings) for bill data.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/BillRepositoryImpl.kt:44</code></summary>

```kotlin
 * models according to the actual API contract.
 */
class BillRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : BillRepository {
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillerDataSource.kt`

```kotlin
class BillerDataSource(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillerRepositoryImpl.kt:24</code></summary>

```kotlin
 */
class BillerRepositoryImpl(
    private val billerDataSource: BillerDataSource,
) : BillerRepository {

    override fun getAllBillers(): Flow<List<Biller>> {
        return billerDataSource.billers
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillerRepository.kt`

```kotlin
interface BillerRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillViewModel.kt:41</code></summary>

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

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/BillerRepositoryImpl.kt`

```kotlin
class BillerRepositoryImpl(
```
TODO: This implementation currently uses local storage (Multiplatform Settings) for biller data.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/di/RepositoryModule.kt:285</code></summary>

```kotlin
    // singleton until it is resolved. When the Fineract endpoint lands, a
    // hybrid impl can bridge both interfaces.
    single<BillerRepository> { BillerRepositoryImpl(get(), get(ioDispatcher)) }

    // Phase-4 Batch-A OFFLINE_LOCAL_ONLY Bill store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
    // `core/datastore/di/PreferenceModule.kt` — that binding is REMOVED in
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/PreferencesMapper.kt`

```kotlin
fun ClientPreferences.toClientInfo(): Client
```
_No KDoc at source._

```kotlin
fun Client.toClientPreferences(): ClientPreferences
```
_No KDoc at source._

```kotlin
fun RolePreferences.toRoleInfo(): RoleInfo
```
_No KDoc at source._

```kotlin
fun RoleInfo.toRolePreferences(): RolePreferences
```
_No KDoc at source._

```kotlin
fun UserInfoPreferences.toUserInfo(): UserInfo
```
_No KDoc at source._

```kotlin
fun UserInfo.toUserInfoPreferences(): UserInfoPreferences
```
_No KDoc at source._

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/UserPreferencesDataSource.kt`

```kotlin
class UserPreferencesDataSource(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/UserPreferencesRepositoryImpl.kt:29</code></summary>

```kotlin

class UserPreferencesRepositoryImpl(
    private val preferenceManager: UserPreferencesDataSource,
    // manage-pocket linkable-accounts Store5 migration: the pre-migration
    // `pocketPreferencesDataSource` constructor arg (upstream PR #2057
    // multiplatform-settings cache for pocket_accounts / detailed_pocket_accounts /
    // linkable_accounts) is REMOVED. This branch never wired that DataSource
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountViewModel.kt:46</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val userRepository: UserPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
```

</details>

- `val userInfo: Flow<UserInfo>`
- `val token: StateFlow<String?>`
- `val client: StateFlow<Client?>`
- `val clientId: StateFlow<Long?>`
- `val authToken: String?`
- `val defaultAccount: StateFlow<DefaultAccount?>`
- `val defaultAccountId: StateFlow<Long?>`
- `val selectedInstance: StateFlow<ServerInstance?>`
- `val selectedInterbankInstance: StateFlow<InterbankServer?>`
- `val accountExternalIds: StateFlow<Map<Long, String>>`
- `val language: StateFlow<Language>`
- `suspend fun updateToken(token: String)`
- `suspend fun updateUserInfo(user: UserInfo)`
- `suspend fun setLanguage(language: Language)`
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepositoryImpl.kt:41</code></summary>

```kotlin
 * stores using a write-before-delete strategy to prevent data loss.
 */
class UserPreferencesRepositoryImpl(
    private val plainSettings: Settings,
    private val secureSettings: Settings,
    private val dispatcher: DispatcherManager,
) : UserPreferencesRepository {
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/di/PreferenceModule.kt`

```kotlin
val PreferencesModule = module
```
_No KDoc at source._

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/model/ClientPreferences.kt`

```kotlin
data class ClientPreferences(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/PreferencesMapper.kt:19</code></summary>

```kotlin
import org.mifospay.core.model.user.UserInfo

fun ClientPreferences.toClientInfo(): Client {
    return Client(
        id = id,
        accountNo = accountNo,
        externalId = externalId,
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/model/RolePreferences.kt`

```kotlin
data class RolePreferences(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/PreferencesMapper.kt:59</code></summary>

```kotlin
}

fun RolePreferences.toRoleInfo(): RoleInfo {
    return RoleInfo(
        id = id,
        name = name,
        description = description,
```

</details>

### `core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/model/UserInfoPreferences.kt`

```kotlin
data class UserInfoPreferences(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/org/mifospay/core/datastore/PreferencesMapper.kt:77</code></summary>

```kotlin
}

fun UserInfoPreferences.toUserInfo(): UserInfo {
    return UserInfo(
        username = username,
        userId = userId,
        base64EncodedAuthenticationKey = base64EncodedAuthenticationKey,
```

</details>

---

_27 type(s), 84 function(s)/property(ies); 60 carry KDoc at source; 2 authored example(s); 27 live call site(s)._
<!-- api-docs:end -->
