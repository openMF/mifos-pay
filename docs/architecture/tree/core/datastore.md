# `core/datastore`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATASTORE.md`
> **Measured:** 30 Kotlin files, 2 test files

## Principal types

`AppReviewPromptState`, `AppReviewPromptStore`, `AutoPayPreferencesDataSource`, `AutoPayPreferencesRepository`, `AutoPayPreferencesRepositoryImpl`, `BillDataSource`, `BillRepository`, `BillRepositoryImpl`, `BillerDataSource`, `BillerRepository`, `BillerRepositoryImpl`, `ChangeListVersions`, `ClientPreferences`, `ForkPreferencesRepository`  …and 10 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/datastore sha=1b83a0910bbb7c0620f7328c869a2dbcc30742c0 -->
## API reference

_Generated from `core/datastore` at tree `1b83a0910bbb` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/AutoPayPreferencesDataSource.kt`

```kotlin
class AutoPayPreferencesDataSource(
```
Settings-backed local cache for AutoPay: enabled flag, schedules, upcoming payments, history and last-sync time.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/AutoPayPreferencesRepositoryImpl.kt:24</code></summary>

```kotlin
 */
class AutoPayPreferencesRepositoryImpl(
    private val autoPayPreferencesDataSource: AutoPayPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
) : AutoPayPreferencesRepository {
    private val unconfinedScope = CoroutineScope(unconfinedDispatcher)
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/AutoPayPreferencesRepository.kt`

```kotlin
interface AutoPayPreferencesRepository
```
Repository contract for the locally cached AutoPay preferences and data.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/AutoPayPreferencesRepositoryImpl.kt:27</code></summary>

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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/AutoPayPreferencesRepositoryImpl.kt`

```kotlin
class AutoPayPreferencesRepositoryImpl(
```
`AutoPayPreferencesRepository` that delegates to `AutoPayPreferencesDataSource` and emits flows on the IO dispatcher.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/PreferenceModule.kt:56</code></summary>

```kotlin

    single<AutoPayPreferencesRepository> {
        AutoPayPreferencesRepositoryImpl(
            autoPayPreferencesDataSource = get(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
            unconfinedDispatcher = get(named(MifosDispatchers.Unconfined.name)),
        )
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillDataSource.kt`

```kotlin
class BillDataSource(
```
Settings-backed local store of `Bill`s (superseded by Room, see PreferencesModule).

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillRepositoryImpl.kt:22</code></summary>

```kotlin
 */
class BillRepositoryImpl(
    private val billDataSource: BillDataSource,
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
        return billDataSource.bills
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillRepository.kt`

```kotlin
interface BillRepository
```
Repository contract for saved bills.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:41</code></summary>

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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillRepositoryImpl.kt`

```kotlin
class BillRepositoryImpl(
```
TODO: This implementation currently uses local storage (Multiplatform Settings) for bill data.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt:44</code></summary>

```kotlin
 * models according to the actual API contract.
 */
class BillRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : BillRepository {
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillerDataSource.kt`

```kotlin
class BillerDataSource(
```
Settings-backed local store of `Biller`s.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillerRepositoryImpl.kt:23</code></summary>

```kotlin
 */
class BillerRepositoryImpl(
    private val billerDataSource: BillerDataSource,
) : BillerRepository {

    override fun getAllBillers(): Flow<List<Biller>> {
        return billerDataSource.billers
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillerRepository.kt`

```kotlin
interface BillerRepository
```
Repository contract for saved billers.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:42</code></summary>

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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/BillerRepositoryImpl.kt`

```kotlin
class BillerRepositoryImpl(
```
TODO: This implementation currently uses local storage (Multiplatform Settings) for biller data.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/fork/RepositoryModule.kt:292</code></summary>

```kotlin
    // kpt.core.datastore.BillerRepository below — unqualified, one replaces the other.
    single<BillerRepository>(named("kpt.core.data.repository.BillerRepository")) {
        BillerRepositoryImpl(get(), get(ioDispatcher))
    }

    // Phase-4 Batch-A OFFLINE_LOCAL_ONLY Bill store binding (GOAL D12).
    // Replaces the multiplatform-settings impl that was previously bound in
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/PreferencesMapper.kt`

```kotlin
fun ClientPreferences.toClientInfo(): Client
```
Maps stored `ClientPreferences` to the domain `Client`.

```kotlin
fun Client.toClientPreferences(): ClientPreferences
```
Maps the domain `Client` to its stored `ClientPreferences` form.

```kotlin
fun RolePreferences.toRoleInfo(): RoleInfo
```
Maps stored `RolePreferences` to the domain `RoleInfo`.

```kotlin
fun RoleInfo.toRolePreferences(): RolePreferences
```
Maps the domain `RoleInfo` to its stored `RolePreferences` form.

```kotlin
fun UserInfoPreferences.toUserInfo(): UserInfo
```
Maps stored `UserInfoPreferences` to the domain `UserInfo`.

```kotlin
fun UserInfo.toUserInfoPreferences(): UserInfoPreferences
```
Maps the domain `UserInfo` to its stored `UserInfoPreferences` form.

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesDataSource.kt`

```kotlin
class UserPreferencesDataSource(
```
Settings-backed store of the user session: user and client info, token, default account, selected server instances, external ids and language.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt:41</code></summary>

```kotlin
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get<DispatcherManager>(),
            preferenceManager = get<UserPreferencesDataSource>(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
            unconfinedDispatcher = get(named(MifosDispatchers.Unconfined.name)),
        )
    }
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/FastMpayProcessorTest.kt:49</code></summary>

```kotlin
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSelfServiceRepository: FakeSelfServiceRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeOfficeRepository: FakeOfficeRepository
    private lateinit var processor: FastMpayProcessor

    @BeforeTest
```

</details>

- `val userData: StateFlow<UserData>` — Current `UserData` (theme, language, auth and onboarding flags).
- `val authToken: String?` — Stored auth token, or null.
- `val passcode: String` — Stored app passcode.
- `val observeLanguage: Flow<LanguageConfig>` — Stream of the selected language.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Stream of the dark-theme setting.
- `val observeDynamicColorPreference: Flow<Boolean>` — Stream of whether dynamic color is enabled.
- `val observeScreenCapturePreference: Flow<Boolean>` — Stream of whether screen capture is allowed.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the selected `language`.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the selected `themeBrand`.
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)` — Persists the `darkThemeConfig`.
- `suspend fun setDynamicColorPreference(useDynamicColor: Boolean)` — Persists whether to use dynamic color.
- `suspend fun setIsAuthenticated(isAuthenticated: Boolean)` — Persists whether the user is authenticated.
- `suspend fun setIsUnlocked(isUnlocked: Boolean)` — Persists whether the app is currently unlocked.
- `suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)` — Persists whether passcode protection is enabled.
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
Splits user data storage between plain (UI preferences) and secure (credentials/auth state) Settings backends.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:274</code></summary>

```kotlin

/** The template's own preferences over in-memory settings — the fake's framework half. */
private fun templatePreferences(): UserPreferencesRepository = UserPreferencesRepositoryImpl(
    plainSettings = MapSettings(),
    secureSettings = MapSettings(),
    dispatcher = object : DispatcherManager {
        override val default = Dispatchers.Unconfined
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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/PreferenceModule.kt`

```kotlin
val PreferencesModule = module
```
Koin module providing Settings and the settings-backed preference data sources.

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

- `suspend fun read(): ChangeListVersions` — Reads the persisted versions, or an empty map if none.
- `suspend fun write(versions: ChangeListVersions)` — Persists `versions`.

```kotlin
class SettingsSyncStatePersister(
```
`SyncStatePersister` backed by plain multiplatform Settings.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:52</code></summary>

```kotlin
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/model/ClientPreferences.kt`

```kotlin
data class ClientPreferences(
```
Persisted snapshot of the logged-in client's profile.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/PreferencesMapper.kt:19</code></summary>

```kotlin
import kpt.core.model.user.UserInfo
/** Maps stored [ClientPreferences] to the domain [Client]. */
fun ClientPreferences.toClientInfo(): Client {
    return Client(
        id = id,
        accountNo = accountNo,
        externalId = externalId,
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/model/RolePreferences.kt`

```kotlin
data class RolePreferences(
```
Persisted snapshot of a user role.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/PreferencesMapper.kt:61</code></summary>

```kotlin

/** Maps stored [RolePreferences] to the domain [RoleInfo]. */
fun RolePreferences.toRoleInfo(): RoleInfo {
    return RoleInfo(
        id = id,
        name = name,
        description = description,
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/model/UserInfoPreferences.kt`

```kotlin
data class UserInfoPreferences(
```
Persisted snapshot of the authenticated user's login result.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/PreferencesMapper.kt:81</code></summary>

```kotlin

/** Maps stored [UserInfoPreferences] to the domain [UserInfo]. */
fun UserInfoPreferences.toUserInfo(): UserInfo {
    return UserInfo(
        username = username,
        userId = userId,
        base64EncodedAuthenticationKey = base64EncodedAuthenticationKey,
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
The wallet's preferences, extending the framework's `UserPreferencesRepository`.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:49</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val userRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
```

</details>

- `val userInfo: Flow<UserInfo>` — The signed-in self-service user.
- `val token: StateFlow<String?>` — The session's base64 authentication key, or null when signed out.
- `val client: StateFlow<Client?>` — The signed-in user's client record.
- `val clientId: StateFlow<Long?>` — The signed-in user's client id.
- `val defaultAccount: StateFlow<DefaultAccount?>` — The account payments default to.
- `val defaultAccountId: StateFlow<Long?>` — The id of `defaultAccount`.
- `val selectedInstance: StateFlow<ServerInstance?>` — The Fineract instance the app is connected to.
- `val selectedInterbankInstance: StateFlow<InterbankServer?>` — The interbank (payment hub) instance the app is connected to.
- `val accountExternalIds: StateFlow<Map<Long, String>>` — External ids of the user's accounts, by account id.
- `val language: StateFlow<Language>` — The wallet's display language.
- `suspend fun updateToken(token: String)` — Stores the session token after sign-in.
- `suspend fun updateUserInfo(user: UserInfo)` — Stores the signed-in user.
- `suspend fun setLanguage(language: Language)` — Stores the wallet's display language.
- `suspend fun updateClientInfo(client: Client)` — Stores the signed-in user's client record.
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ForkPreferencesRepositoryImpl.kt`

```kotlin
class ForkPreferencesRepositoryImpl(
```
Implementation of `ForkPreferencesRepository`. `owner: fork` — never synced. `by delegate` forwards every framework preference to the template's implementation, so this class stays complete when the template adds one.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt:34</code></summary>

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

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/FastMpayProcessorTest.kt:49</code></summary>

```kotlin
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSelfServiceRepository: FakeSelfServiceRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeOfficeRepository: FakeOfficeRepository
    private lateinit var processor: FastMpayProcessor

    @BeforeTest
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

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:274</code></summary>

```kotlin

/** The template's own preferences over in-memory settings — the fake's framework half. */
private fun templatePreferences(): UserPreferencesRepository = UserPreferencesRepositoryImpl(
    plainSettings = MapSettings(),
    secureSettings = MapSettings(),
    dispatcher = object : DispatcherManager {
        override val default = Dispatchers.Unconfined
```

</details>

---

_25 type(s), 84 function(s)/property(ies); 109 carry KDoc at source; 0 authored example(s); 25 live call site(s)._
<!-- api-docs:end -->
