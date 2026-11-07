# `core/network`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_NETWORK.md`
> **Measured:** 84 Kotlin files, 14 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@ApiBinding` | `network-ksp` | di/GeneratedApiBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AccountTransfersService`, `ApiEndPoints`, `AppAccessPoints`, `AppConfigService`, `AppMultiUrlConfigProvider`, `AppUrlTypes`, `AuthenticationService`, `AutoPayService`, `AutoPayStatisticsResponse`, `BaseURL`, `BeneficiaryService`, `BillService`, `BillStatisticsResponse`, `BillerService`  …and 52 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/network sha=602b5c8bb23e6e74eeb5a988140fcd2aa4844b6c -->
## API reference

_Generated from `core/network` at tree `602b5c8bb23e` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/network/src/commonMain/kotlin/kpt/core/network/FineractApiManager.kt`

```kotlin
class FineractApiManager(
```
Entry point for the Fineract API services; exposes each service created by `KtorfitClient`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:55</code></summary>

```kotlin
@RepositoryBinding(binds = AccountRepository::class)
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.SelfAccounts) private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>?,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/InterBankApiManager.kt`

```kotlin
class InterBankApiManager(
```
Entry point for the inter-bank API service created by `KtorfitClient`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InterBankRepositoryImpl.kt:31</code></summary>

```kotlin
@RepositoryBinding(binds = InterBankRepository::class)
class InterBankRepositoryImpl(
    private val apiManager: InterBankApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : InterBankRepository {

    override suspend fun fetchParticipant(
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt`

```kotlin
class KtorfitClient(
```
Creates and caches the Ktorfit-generated Fineract service implementations from a single `Ktorfit` instance.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryTest.kt:156</code></summary>

```kotlin
 */
private val fakeSelfServiceApiManager = SelfServiceApiManager(
    KtorfitClient(
        Ktorfit.Builder()
            .httpClient(HttpClient())
            .baseUrl("https://example.com/")
            .build(),
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/PocketDataManager.kt`

```kotlin
interface PocketDataManager
```
Provides the network services needed to load a client's pocket accounts.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/SelfServiceApiManager.kt:15</code></summary>

```kotlin
class SelfServiceApiManager(
    private val ktorfitClient: KtorfitClient,
) : PocketDataManager {
    /** Authentication API. */
    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    override val clientsApi by lazy { ktorfitClient.clientsApi }
```

</details>

- `val clientsApi: ClientService` — Clients API.
- `val pocketApi: PocketService` — Pocket accounts API.
- `val shareAccountApi: ShareAccountService` — Share accounts API.

### `core/network/src/commonMain/kotlin/kpt/core/network/SelfServiceApiManager.kt`

```kotlin
class SelfServiceApiManager(
```
Entry point for the Fineract self-service API services created by `KtorfitClient`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:56</code></summary>

```kotlin
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
    // Cache wiring, resolved by the generated binding. Nullable: null keeps the network-only path.
    @FromStore(AppStoreIds.SelfAccounts) private val selfAccountsStore: Store<SelfAccountsKey, List<Account>>?,
    private val storeNetworkMonitor: StoreNetworkMonitor?,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/SupabaseApiManager.kt`

```kotlin
class SupabaseApiManager(
```
API manager for Supabase services. Similar to FineractApiManager but for Supabase-based APIs.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/config/SupabaseInstanceConfigLoader.kt:28</code></summary>

```kotlin
 */
class SupabaseInstanceConfigLoader(
    private val supabaseApiManager: SupabaseApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : InstanceConfigLoader {
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt`

```kotlin
object AppAccessPoints
```
The per-fork list of network access points this app talks to — REST and Supabase in ONE place.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:76</code></summary>

```kotlin
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    // + the app-wide network privacy policy (logging per build type, query redaction, web CORS relay).
    single { AccessPointRegistry(AppAccessPoints.points, AppAccessPoints.policy) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
```

</details>

- `val points: List<AccessPoint> = listOf(` — This project's declared access points, in app-profile order.
- `val policy: NetworkPolicy = NetworkPolicy(` — Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt`

```kotlin
class AppMultiUrlConfigProvider(
```
Concrete `MultiUrlConfigProvider` backed by the declarative `AccessPointRegistry`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:81</code></summary>

```kotlin
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Supabase clients are bound in core-base NetworkBaseModule (included above): each point's publishable
    // key is on its generated AccessPoint, so they need nothing from here.
    // Map<id, client> — every declared Supabase access point materializes exactly one client.
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppUrlTypes.kt`

```kotlin
object AppUrlTypes
```
Project-level catalogue of named API endpoints for runtime base-URL switching (see `kpt.core.base.network.MultiUrlConfigProvider` / `kpt.core.base.network.DynamicBaseUrlPlugin`).

- `val MAIN: UrlType = UrlType.MAIN` — `main` — REST access point.
- `val LWMSWHOXVVOAGZKQXIYD: UrlType = UrlType("LWMSWHOXVVOAGZKQXIYD")` — `lwmswhoxvvoagzkqxiyd` — Supabase access point.
- `val all: List<UrlType> = listOf(` — Every declared endpoint type, in app-profile order.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/KptNetworkHeaders.kt`

```kotlin
object KptNetworkHeaders : DefaultHeaderProvider
```
THE FORK'S default request headers. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object KptNetworkHeaders : DefaultHeaderProvider {
    override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
        "main" -> mapOf(
            "X-Client-Version" to BuildKonfig.VERSION_NAME,
            "X-Platform" to platformName(),
        )
        else -> emptyMap()
    }
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:61</code></summary>

```kotlin
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { KptNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/ForkNetworkModule.kt`

```kotlin
val ForkNetworkModule = module
```
THE FORK'S network DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyApiConfig> { MyApiConfig(apiKey = BuildKonfig.MY_API_KEY.takeIf { it.isNotBlank() }) }
single<SupabaseExtrasProvider> {
    SupabaseExtrasProvider { id ->
        when (id) {
            // Social sign-in: kmp-supabase-auth (io.github.mobilebytelabs:cmp-supabase-auth[-compose])
            // installs Auth + native Google/Apple onto this client. Never hand-write install(Auth).
            // libs.cmp.supabase.auth.compose (0.3.3). secureStorage keeps the session + PKCE verifier
            // in the encrypted named("secure") Settings instead of supabase-kt's plain default.
            "<your-project-ref>" -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
            // Several projects sharing a module — one branch, no repetition.
            "<project-b>", "<project-c>" -> {
                { install(Realtime) }
            }
            else -> {
                {}
            }
        }
    }
}
includes(kmpSupabaseAuthNetwork(authConfig) { get<SupabaseClientFactory>().requireClientFor("<ref>").client })
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt`

```kotlin
val NetworkModule = module
```
Koin module for `core/network` — the Ktorfit API bindings over the shared client.

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

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/ServerInstanceApi.kt`

```kotlin
interface ServerInstanceApi
```
The `app_server_config` table's API — one row per server instance. The CONTRACT, no Supabase types: a consumer is tested with a plain fake; `impl.ServerInstanceApiImpl` is the only Supabase-aware part.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt:26</code></summary>

```kotlin
class ServerInstanceApiImpl(
    private val supabase: SupabaseConfigClient,
) : ServerInstanceApi {

    override val isConfigured: Boolean get() = supabase.isConfigured

    override suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto> {
```

</details>

- `val isConfigured: Boolean` — True when a real project URL + publishable key are configured.
- `suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>` — The enabled instances whose `supported_apps` contains `app`, in `sort_order` — filtered BY THE SERVER, so one app never downloads another's servers. Empty when unconfigured.

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt`

```kotlin
class ServerInstanceApiImpl(
```
Supabase implementation of `ServerInstanceApi`. Row-level security limits the publishable key (the `anon` role) to enabled rows, read-only; the `supported_apps` containment filter runs in Postgres (GIN-indexed).

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/config/SupabaseAccessPointTest.kt:95</code></summary>

```kotlin
        // (`supabaseApi<ServerInstanceApi>("<id>") { ServerInstanceApiImpl(it) }`), so this asserts against
        // the contract a consumer injects rather than against the implementation.
        val api: ServerInstanceApi = ServerInstanceApiImpl(factory().requireClientFor(point.id))
        assertFalse(api.isConfigured)
        // The guard matters: without it this would reach `client`, build a Supabase client on an empty
        // key, and fail at app start-up on every unconfigured fork.
        assertTrue(api.fetchInstances("field-officer").isEmpty(), "an unconfigured client reads nothing")
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/dto/RemoteServerInstanceDto.kt`

```kotlin
data class RemoteServerInstanceDto(
```
One row of `public.app_server_config` — the Mifos server list, ONE ROW PER INSTANCE. It replaces the deprecated `app_config` table (a single row holding an `instances` blob).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/ServerInstanceApi.kt:26</code></summary>

```kotlin
     * SERVER, so one app never downloads another's servers. Empty when unconfigured.
     */
    suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>
}
```

</details>

```kotlin
data class RemoteDemoAccountDto(val role: String, val username: String, val password: String)
```
A PUBLISHED demo sign-in of a public demo instance — public by design, not a credential to protect.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/config/InstanceConfigLoader.kt`

```kotlin
interface InstanceConfigLoader
```
Loads the list of available server instances.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/config/SupabaseInstanceConfigLoader.kt:31</code></summary>

```kotlin
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : InstanceConfigLoader {

    override suspend fun fetchInstancesConfig(): InstancesConfig {
        if (!supabaseApiManager.isConfigured) {
            return loadFallbackConfig()
```

</details>

- `suspend fun fetchInstancesConfig(): InstancesConfig` — Fetches the server instances configuration once.
- `fun observeInstancesConfig(): Flow<ScreenState<InstancesConfig>>` — Streams the server instances configuration wrapped in a `ScreenState`.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/config/InstanceConfigManager.kt`

```kotlin
class InstanceConfigManager(
```
Provides the active Fineract server instance and interbank server, and resolves the API base URLs.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/login/LoginViewModel.kt:33</code></summary>

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val instanceConfigManager: InstanceConfigManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoginState, LoginEvent, LoginAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: LoginState(dialogState = null),
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/config/SupabaseInstanceConfigLoader.kt`

```kotlin
class SupabaseInstanceConfigLoader(
```
Instance config loader that fetches configuration from Supabase. Uses SupabaseApiManager for API interactions.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:61</code></summary>

```kotlin

    single<InstanceConfigLoader> {
        SupabaseInstanceConfigLoader(
            supabaseApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            json = get(),
        )
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/di/LocalModule.kt`

```kotlin
val LocalModule = module
```
Koin module providing the local asset manager and data source.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt`

```kotlin
val NetworkModule = module
```
Koin module providing the HTTP clients, Ktorfit instances, API managers and service wiring of the network layer.

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

### `core/network/src/commonMain/kotlin/kpt/core/network/main/di/Qualifier.kt`

```kotlin
val SelfClient = named("SelfClient")
```
Koin qualifier for the HTTP client targeting the self-service API.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:76</code></summary>

```kotlin
    }

    single<KtorfitClient>(qualifier = SelfClient) {
        val preferencesRepository = get<ForkPreferencesRepository>()
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
```

</details>

```kotlin
val BaseClient = named("BaseClient")
```
Koin qualifier for the HTTP client targeting the base Fineract API.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:114</code></summary>

```kotlin
    }

    single<KtorfitClient>(qualifier = BaseClient) {
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
                .httpClient(
```

</details>

```kotlin
val InterBankClient = named("InterBankClient")
```
Koin qualifier for the HTTP client targeting the inter-bank API.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:155</code></summary>

```kotlin
    }

    single<KtorfitClient>(qualifier = InterBankClient) {
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
                .httpClient(
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/localAssets/ForkLocalAssetDataSource.kt`

```kotlin
class ForkLocalAssetDataSource(
```
Default `LocalAssetDataSource`; currently returns empty lists.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/localAssets/LocalAssetDataSource.kt`

```kotlin
interface LocalAssetDataSource
```
Source of static reference data (countries, states, banks, cities) bundled with the app.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/localAssets/ForkLocalAssetDataSource.kt:21</code></summary>

```kotlin
    private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
) : LocalAssetDataSource {

    override suspend fun getCountries(): List<Country> {
        return emptyList()
    }
```

</details>

- `suspend fun getCountries(): List<Country>` — Returns the available countries.
- `suspend fun getStateList(): List<State>` — Returns the available states.
- `suspend fun getBanks(): List<String>` — Returns the available bank names.
- `suspend fun getCities(): List<City>` — Returns the available cities.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/localAssets/LocalAssetManager.kt`

```kotlin
fun interface LocalAssetManager
```
Reads bundled asset files by name.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/AccountTransfersService.kt`

```kotlin
interface AccountTransfersService
```
Ktorfit service for account transfers and account transactions.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:55</code></summary>

```kotlin
    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }
```

</details>

- `fun getTransaction(` — Fetches a transaction of a savings account.
- `fun getAccountTransfer(@Path("transferId") transferId: Int): Flow<TransferDetail>` — Fetches the details of an account transfer.
- `fun searchAccounts(` — Searches accounts matching `query` within the given `resource`.
- `suspend fun makeTransfer(` — Makes an account transfer.
- `fun getAccountTransferTemplate(): Flow<AccountOptionsTemplate>` — Fetches the template for creating an account transfer.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/AppConfigService.kt`

```kotlin
class AppConfigService(
```
Service for interacting with app_config table in Supabase.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/SupabaseApiManager.kt:23</code></summary>

```kotlin
    /** Service for fetching the app configuration (server instances) from Supabase. */
    val appConfigService by lazy {
        AppConfigService(supabaseClient.postgrest)
    }

    /** Whether Supabase credentials are present; without them every call is skipped. */
    val isConfigured: Boolean get() = supabaseClient.isConfigured
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/AuthenticationService.kt`

```kotlin
interface AuthenticationService
```
Ktorfit service for authentication.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:41</code></summary>

```kotlin
    ktorfit: Ktorfit,
) {
    internal val authenticationApi by lazy { ktorfit.createAuthenticationService() }

    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }
```

</details>

- `suspend fun authenticate(@Body authPayload: AuthenticationPayload): User` — Authenticates with the given payload and returns the `User`.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/AutoPayService.kt`

```kotlin
interface AutoPayService
```
TODO: Sync with backend team and update service layer according to finalized API contract, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:73</code></summary>

```kotlin
    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }
```

</details>

- `fun getAutoPayTemplate(` — Get AutoPay template for creating new AutoPay schedules
- `fun getAllAutoPaySchedules(` — Get all AutoPay schedules for a client
- `fun getAutoPaySchedule(` — Get AutoPay schedule by ID
- `suspend fun createAutoPaySchedule(` — Create a new AutoPay schedule
- `suspend fun updateAutoPaySchedule(` — Update an existing AutoPay schedule
- `suspend fun deleteAutoPaySchedule(` — Delete an AutoPay schedule
- `suspend fun pauseAutoPaySchedule(` — Pause an AutoPay schedule
- `suspend fun resumeAutoPaySchedule(` — Resume a paused AutoPay schedule
- `fun getAutoPayHistory(` — Get AutoPay payment history
- `fun getUpcomingPayments(` — Get upcoming payments for all AutoPay schedules
- `fun getAutoPayStatistics(` — Get AutoPay statistics for dashboard

```kotlin
data class AutoPayStatisticsResponse(
```
Network response with aggregate auto-pay schedule figures.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/BeneficiaryService.kt`

```kotlin
interface BeneficiaryService
```
Ktorfit service for beneficiaries.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:75</code></summary>

```kotlin
    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }
```

</details>

- `fun beneficiaryList(): Flow<List<Beneficiary>>` — Fetches the list of beneficiaries.
- `suspend fun beneficiaryTemplate(): Flow<BeneficiaryTemplate>` — Fetches the beneficiary template.
- `suspend fun createBeneficiary(@Body beneficiaryPayload: BeneficiaryPayload)` — Creates a beneficiary.
- `suspend fun updateBeneficiary(` — Updates a beneficiary.
- `suspend fun deleteBeneficiary(@Path("beneficiaryId") beneficiaryId: Long): Unit` — Deletes a beneficiary.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/BillService.kt`

```kotlin
interface BillService
```
TODO: Update endpoint paths when backend APIs are finalized, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:83</code></summary>

```kotlin
    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
```

</details>

- `fun getAllBills(@Query("clientId") clientId: Long): Flow<List<Bill>>` — Fetches all bills of a client.
- `suspend fun getBillById(@Path("billId") billId: String): Flow<Bill>` — Fetches a bill by id.
- `suspend fun createBill(@Body bill: Bill): Flow<Bill>` — Creates a bill.
- `suspend fun updateBill(` — Updates a bill.
- `suspend fun deleteBill(@Path("billId") billId: String): Flow<Unit>` — Deletes a bill.
- `suspend fun getBillsByStatus(@Path("status") status: BillStatus): Flow<List<Bill>>` — Fetches bills with the given status.
- `suspend fun getBillsByBillerId(@Path("billerId") billerId: String): Flow<List<Bill>>` — Fetches bills of the given biller.
- `suspend fun getBillsByRecurrencePattern(@Path("pattern") pattern: RecurrencePattern): Flow<List<Bill>>` — Fetches bills with the given recurrence pattern.
- `suspend fun searchBillsByName(@Query("query") query: String): Flow<List<Bill>>` — Searches bills by name.
- `suspend fun getOverdueBills(): Flow<List<Bill>>` — Fetches overdue bills.
- `suspend fun getUpcomingBills(` — Fetches upcoming bills.
- `suspend fun updateBillStatus(` — Updates the status of a bill.
- `fun getBillStatistics(@Query("clientId") clientId: Long): Flow<BillStatisticsResponse>` — Fetches aggregate bill statistics for a client.
- `suspend fun clearAllBills(): Flow<Unit>` — Deletes all bills.

```kotlin
data class BillStatisticsResponse(
```
Network response with aggregate bill figures.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt:168</code></summary>

```kotlin
        return apiManager.billApi
            .getBillStatistics(clientId)
            .map { response: BillStatisticsResponse ->
                BillStatistics(
                    totalBills = response.totalBills,
                    activeBills = response.activeBills,
                    overdueBills = response.overdueBills,
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/BillerService.kt`

```kotlin
interface BillerService
```
TODO: Update endpoint paths when backend APIs are finalized, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:81</code></summary>

```kotlin
    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }
```

</details>

- `fun getAllBillers(): Flow<List<Biller>>` — Fetches all billers.
- `suspend fun getBillerById(@Path("billerId") billerId: String): Flow<Biller>` — Fetches a biller by id.
- `suspend fun createBiller(@Body biller: Biller): Flow<Biller>` — Creates a biller.
- `suspend fun updateBiller(` — Updates a biller.
- `suspend fun deleteBiller(@Path("billerId") billerId: String): Flow<Unit>` — Deletes a biller.
- `suspend fun getBillersByCategory(` — Fetches billers of the given category.
- `suspend fun searchBillersByName(` — Searches billers by name.
- `suspend fun isBillerExists(` — Checks whether a biller exists.
- `suspend fun clearAllBillers(): Flow<Unit>` — Deletes all billers.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/ClientService.kt`

```kotlin
interface ClientService
```
Ktorfit service for clients.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:43</code></summary>

```kotlin
    internal val authenticationApi by lazy { ktorfit.createAuthenticationService() }

    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }
```

</details>

- `suspend fun clients(): Flow<Page<ClientEntity>>` — Fetches the page of clients.
- `suspend fun getClientForId(@Path("clientId") clientId: Long): ClientEntity` — Fetches a client's details once.
- `fun getClient(@Path("clientId") clientId: Long): Flow<ClientEntity>` — Streams a client's details.
- `suspend fun updateClient(` — Updates a client.
- `fun getClientImage(@Path("clientId") clientId: Long): Flow<String>` — Fetches a client's profile image.
- `suspend fun updateClientImage(` — Uploads a client's profile image.
- `suspend fun getClientAccounts(@Path("clientId") clientId: Long): ClientAccountsEntity` — Fetches the accounts of a client.
- `fun getAccounts(` — Fetches the accounts of a client filtered by account type.
- `suspend fun createClient(@Body newClient: NewClientEntity): ClientResponseEntity` — Creates a client.
- `suspend fun deleteClient(@Path("clientId") clientId: Int): ClientResponseEntity` — Deletes a client.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/DocumentService.kt`

```kotlin
interface DocumentService
```
Ktorfit service for documents.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:49</code></summary>

```kotlin
    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }
```

</details>

- `suspend fun getDocuments(` — Fetches the documents of an entity.
- `suspend fun createDocument(`
- `suspend fun createDocumentFile(` — Uploads a document file for an entity.
- `suspend fun downloadDocument(` — This Service is for downloading the Document with EntityType and EntityId and Document Id Rest End Point : https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/ {documentId}/attachment
- `suspend fun removeDocument(` — This Service is for Deleting the Document with EntityType and EntityId and Document Id.
- `suspend fun updateDocument(` — This Service for Updating the Document with EntityType and EntityId and Document Id.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/InterBankService.kt`

```kotlin
interface InterBankService
```
Ktorfit service for inter-bank transfers.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:77</code></summary>

```kotlin
    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }
```

</details>

- `suspend fun fetchParticipant(` — Fetches a participant.
- `suspend fun fetchPartyInfo(` — Fetches party information for an inter-bank transfer.
- `suspend fun interBankMakeTransfer(` — Executes an inter-bank transfer.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/InvoiceService.kt`

```kotlin
interface InvoiceService
```
Ktorfit service for invoices.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:61</code></summary>

```kotlin
    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }
```

</details>

- `fun getInvoices(@Path("clientId") clientId: Long): Flow<List<Invoice>>` — Fetches a client's invoices.
- `fun getInvoice(` — Fetches a single invoice.
- `suspend fun addInvoice(` — Adds an invoice.
- `suspend fun updateInvoice(` — Updates an invoice.
- `suspend fun deleteInvoice(` — Deletes an invoice.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/KYCLevel1Service.kt`

```kotlin
interface KYCLevel1Service
```
Ktorfit service for KYC level 1 details.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:59</code></summary>

```kotlin
    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }
```

</details>

- `fun fetchKYCLevel1Details(@Path("clientId") clientId: Long): Flow<List<KYCLevel1Details>>` — Fetches a client's KYC level 1 details.
- `suspend fun addKYCLevel1Details(` — Adds KYC level 1 details.
- `suspend fun updateKYCLevel1Details(` — Updates KYC level 1 details.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/NotificationService.kt`

```kotlin
interface NotificationService
```
Ktorfit service for notifications.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:67</code></summary>

```kotlin
    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }
```

</details>

- `fun fetchNotifications(@Query("isRead") isRead: Boolean): Flow<NotificationPayload>` — Fetches notifications filtered by read state.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/OfficeService.kt`

```kotlin
interface OfficeService
```
Ktorfit service for offices.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:79</code></summary>

```kotlin
    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }
```

</details>

- `fun getOffices(): Flow<List<Office>>` — Fetches the list of offices.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/PocketService.kt`

```kotlin
interface PocketService
```
Ktorfit service for pocket accounts.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:85</code></summary>

```kotlin
    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
}
```

</details>

- `suspend fun getPocketAccounts(): PocketResponseDto` — Fetches the accounts linked to the pocket.
- `suspend fun linkAccounts(` — Links accounts to the pocket.
- `suspend fun delinkAccounts(` — Delinks accounts from the pocket.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/RegistrationService.kt`

```kotlin
interface RegistrationService
```
Ktorfit service for user registration.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:45</code></summary>

```kotlin
    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }
```

</details>

- `suspend fun registerUser(@Body registerPayload: RegisterPayload)` — Registers a new user.
- `suspend fun verifyUser(@Body userVerify: UserVerify)` — Verifies a registered user.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/RunReportService.kt`

```kotlin
interface RunReportService
```
Ktorfit service for running reports.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:51</code></summary>

```kotlin
    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }
```

</details>

- `suspend fun getTransactionReceipt(` — Fetches a transaction receipt by running a report.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/SavedCardService.kt`

```kotlin
interface SavedCardService
```
Ktorfit service for saved cards.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:57</code></summary>

```kotlin
    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }
```

</details>

- `fun getSavedCards(@Path("clientId") clientId: Long): Flow<List<SavedCard>>` — Fetches a client's saved cards.
- `fun getSavedCard(` — Fetches a single saved card.
- `suspend fun addSavedCard(` — Adds a saved card.
- `suspend fun updateCard(` — Updates a saved card.
- `suspend fun deleteCard(` — Deletes a saved card.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/SavingsAccountsService.kt`

```kotlin
interface SavingsAccountsService
```
Ktorfit service for savings accounts.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:69</code></summary>

```kotlin
    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }
```

</details>

- `fun getSavingsWithAssociations(` — Fetches a savings account with its associations.
- `suspend fun getSavingsAccounts(` — Fetches savings accounts.
- `suspend fun createSavingsAccount(@Body savingAccount: CreateNewSavingEntity)` — Creates a savings account.
- `suspend fun updateSavingsAccount(` — Updates a savings account.
- `suspend fun blockUnblockAccount(` — Blocks or unblocks a savings account.
- `suspend fun getSavingAccountTransaction(` — Fetches a transaction of a savings account.
- `suspend fun payViaMobile(@Path("accountId") accountId: Long): Flow<TransactionsEntity>` — Initiates a pay-via-mobile transaction on the account.
- `fun getSavingAccountTemplate(@Query("clientId") clientId: Long): Flow<SavingAccountTemplate>` — Fetches the template for creating a savings account.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/SearchService.kt`

```kotlin
interface SearchService
```
Ktorfit service for searching resources.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:47</code></summary>

```kotlin
    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }
```

</details>

- `suspend fun searchResources(` — Searches Fineract resources.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/ShareAccountService.kt`

```kotlin
interface ShareAccountService
```
Ktorfit service for share accounts.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:87</code></summary>

```kotlin
    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
}
```

</details>

- `fun getShareAccountDetails(` — Fetches share account details.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/StandingInstructionService.kt`

```kotlin
interface StandingInstructionService
```
Ktorfit service for standing instructions.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:71</code></summary>

```kotlin
    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }
```

</details>

- `fun getStandingInstructionTemplate(` — Fetches the template for creating a standing instruction.
- `fun getAllStandingInstructions(`
- `fun getStandingInstruction(` — Fetches a single standing instruction.
- `suspend fun createStandingInstruction(` — Creates a standing instruction.
- `suspend fun deleteStandingInstruction(`
- `suspend fun updateStandingInstruction(` — Updates a standing instruction.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/ThirdPartyTransferService.kt`

```kotlin
interface ThirdPartyTransferService
```
Ktorfit service for third-party transfers.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:65</code></summary>

```kotlin
    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }
```

</details>

- `suspend fun accountTransferTemplate(): AccountOptionsTemplate` — Fetches the account options template for a transfer.
- `suspend fun makeTransfer(@Body transferPayload: TransferPayload): TPTResponse` — Makes a third-party transfer.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/TwoFactorAuthService.kt`

```kotlin
interface TwoFactorAuthService
```
Ktorfit service for two-factor authentication.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:53</code></summary>

```kotlin
    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }
```

</details>

- `suspend fun deliveryMethods(): Flow<List<DeliveryMethod>>` — Fetches the available OTP delivery methods.
- `suspend fun requestOTP(@Query("deliveryMethod") deliveryMethod: String): Flow<String>` — Requests an OTP through the given delivery method.
- `suspend fun validateToken(@Query("token") token: String): Flow<AccessToken>` — Validates an OTP token.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/services/UserService.kt`

```kotlin
interface UserService
```
Ktorfit service for users.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/KtorfitClient.kt:63</code></summary>

```kotlin
    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }
```

</details>

- `suspend fun users(): Flow<List<UserWithRole>>` — Fetches all users with their roles.
- `suspend fun createUser(@Body user: NewUserEntity): CommonResponse` — Creates a user.
- `suspend fun updateUser(` — Updates a user.
- `suspend fun updateUserPassword(` — Updates a user's password.
- `suspend fun deleteUser(` — Deletes a user.
- `suspend fun getUser(): Flow<UserWithRole>` — Fetches the signed-in user.
- `suspend fun assignClientToUser(` — Assigns a client to a user.

### `core/network/src/commonMain/kotlin/kpt/core/network/main/utils/ApiEndPoints.kt`

```kotlin
object ApiEndPoints
```
Relative endpoint paths of the Fineract APIs used by the services.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/services/AccountTransfersService.kt:28</code></summary>

```kotlin

    /** Fetches a transaction of a savings account. */
    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/transactions/{transactionId}")
    fun getTransaction(
        @Path("accountId") clientId: Long,
        @Path("transactionId") transactionId: Long,
    ): Flow<TransactionsEntity>
```

</details>

- `const val AUTHENTICATION = "authentication"` — Path segment for authentication.
- `const val AUTHENTICATION_OAUTH = "oauth/token"` — Path segment for OAuth token requests.
- `const val REGISTRATION = "registration"` — Path segment for registration.
- `const val CLIENTS = "clients"` — Path segment for clients.
- `const val SAVINGS_ACCOUNTS = "savingsaccounts"` — Path segment for savings accounts.
- `const val TRANSACTIONS = "transactions"` — Path segment for transactions.
- `const val RECURRING_ACCOUNTS = "recurringdepositaccounts"` — Path segment for recurring deposit accounts.
- `const val SEARCH = "search"` — Path segment for search.
- `const val BENEFICIARIES = "beneficiaries"` — Path segment for beneficiaries.
- `const val ACCOUNT_TRANSFER = "accounttransfers"` — Path segment for account transfers.
- `const val DOCUMENTS = "documents"` — Path segment for documents.
- `const val DATATABLES = "datatables"` — Path segment for data tables.
- `const val TWOFACTOR = "twofactor"` — Path segment for two-factor authentication.
- `const val RUN_REPORT = "runreports"` — Path segment for running reports.
  _…more members; read the file._

### `core/network/src/commonMain/kotlin/kpt/core/network/main/utils/BaseURL.kt`

```kotlin
class BaseURL(
```
Exposes the base URLs and tenant id of the active server instance from `InstanceConfigManager`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:73</code></summary>

```kotlin

    single {
        BaseURL(configManager = get())
    }

    single<KtorfitClient>(qualifier = SelfClient) {
        val preferencesRepository = get<ForkPreferencesRepository>()
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/utils/FlowConverterFactory.kt`

```kotlin
public class FlowConverterFactory : Converter.Factory
```
Factory that enables the use of Flow<T> as return type

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:109</code></summary>

```kotlin
                    },
                )
                .converterFactories(FlowConverterFactory())
                .build(),
        )
    }
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/main/utils/KtorInterceptor.kt`

```kotlin
class KtorInterceptor(
```
Ktor client plugin adding JSON, tenant and Basic auth headers and flagging 401 responses as unauthorized.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:103</code></summary>

```kotlin
                            urlType = MultiUrlConfigProvider.UrlType.SELF_SERVICE
                        }
                        install(KtorInterceptor) {
                            getToken = { preferencesRepository.authToken }
                            this.configManager = configManager
                        }
                    },
```

</details>

```kotlin
class Config
```
Configuration for `KtorInterceptor`.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt:276</code></summary>

```kotlin
                    htmlContent = html,
                    fileName = "transaction_history.pdf",
                    pageConfig = PageConfig(),
                )
            } catch (e: PdfError) {
                sendEvent(HistoryEvent.PdfExportError(message = "Couldn't export PDF: ${e.message}"))
            }
```

</details>

```kotlin
class KtorInterceptorRe(
```
Variant of `KtorInterceptor` that reads the auth token from the preferences repository.

```kotlin
class ConfigRe
```
Configuration for `KtorInterceptorRe`.

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/DynamicBaseUrlPlugin.kt`

```kotlin
class DynamicBaseUrlPlugin private constructor(
```
Ktor plugin that dynamically sets the base URL for each request based on a `DynamicUrlConfigProvider` or `MultiUrlConfigProvider`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:99</code></summary>

```kotlin
                            socketTimeoutMillis = 30_000
                        }
                        install(DynamicBaseUrlPlugin) {
                            multiConfigProvider = configManager
                            urlType = MultiUrlConfigProvider.UrlType.SELF_SERVICE
                        }
                        install(KtorInterceptor) {
```

</details>

```kotlin
class DynamicBaseUrlConfig
```
Configuration class for `DynamicBaseUrlPlugin`.

<details><summary>Used in the template — <code>core-base/network/src/commonMain/kotlin/kpt/core/base/network/DynamicBaseUrlPlugin.kt:69</code></summary>

```kotlin
     * needs no per-call wiring.
     */
    companion object Plugin : HttpClientPlugin<DynamicBaseUrlConfig, DynamicBaseUrlPlugin> {
        override val key: AttributeKey<DynamicBaseUrlPlugin> =
            AttributeKey("DynamicBaseUrlPlugin")

        override fun prepare(block: DynamicBaseUrlConfig.() -> Unit): DynamicBaseUrlPlugin {
```

</details>

```kotlin
class DynamicLoggableHosts(
```
A dynamic list that provides loggable hosts from a `DynamicUrlConfigProvider`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/main/di/NetworkModule.kt:87</code></summary>

```kotlin
                            baseUrl = "https://placeholder.local/",
                            // Dynamic loggable hosts - reads current endpoints from configManager
                            loggableHosts = DynamicLoggableHosts(configManager),
                        ),
                    ).config {
                        // Fail-fast connect/request timeouts. The corebase default sets
                        // request+socket to 60s but no connectTimeout; a shorter connect
```

</details>

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/DynamicUrlConfigProvider.kt`

```kotlin
interface DynamicUrlConfigProvider
```
Interface for providing dynamic URL configuration at runtime. Implementations of this interface allow the HTTP client to dynamically switch between different server endpoints without recreating the client.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/DynamicBaseUrlPlugin.kt:29</code></summary>

```kotlin
 */
class DynamicBaseUrlPlugin private constructor(
    private val configProvider: DynamicUrlConfigProvider?,
    private val multiConfigProvider: MultiUrlConfigProvider?,
    private val urlType: MultiUrlConfigProvider.UrlType,
) {
    /** Installs [DynamicBaseUrlPlugin] into a Ktor client. */
```

</details>

- `fun getBaseUrl(): String` — Returns the current base URL to use for API requests.
- `fun getLoggableHosts(): List<String>` — Returns the list of hostnames that should have HTTP logging enabled.

```kotlin
interface MultiUrlConfigProvider : DynamicUrlConfigProvider
```
Extension of `DynamicUrlConfigProvider` for applications with multiple URL types (e.g., main API, self-service API, interbank API).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt:29</code></summary>

```kotlin
class AppMultiUrlConfigProvider(
    private val registry: AccessPointRegistry,
) : MultiUrlConfigProvider {

    override fun getBaseUrl(type: UrlType): String =
        registry.restBaseUrl(type)
            ?: registry.restBaseUrl(UrlType.MAIN)
```

</details>

- `fun getBaseUrl(type: UrlType): String` — Returns the base URL for the specified URL type.

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/KtorHttpClient.kt`

```kotlin
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
```
Creates a platform-specific `HttpClient` configured by `config`.

```kotlin
fun setupDefaultHttpClient(
```
Provides a default `HttpClientConfig` setup for use with a Ktor-based HTTP client.

<details><summary>Example</summary>

```kotlin
val client = httpClient(setupDefaultHttpClient(baseUrl = "https://api.example.com"))
```

</details>

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/NetworkError.kt`

```kotlin
enum class NetworkError
```
Represents standardized error types for remote or network operations. This enum is typically used with the `NetworkResult.Error` variant to describe what kind of failure occurred.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/kpt/core/common/ForkScreenStateErrorMapper.kt:56</code></summary>

```kotlin
                ScreenState.Unauthenticated
            } else {
                ScreenState.Error(error = throwable, isNetworkError = false)
            }

        is NetworkException, is IOException -> ScreenState.NoNetwork(isCaptivePortal = false)
```

</details>

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/NetworkResult.kt`

```kotlin
sealed interface NetworkResult<out D, out E : NetworkError>
```
Represents the result of a network or remote operation, encapsulating either a success or an error.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/factory/ResultSuspendConverterFactory.kt:64</code></summary>

```kotlin
        ktorfit: Ktorfit,
    ): Converter.SuspendResponseConverter<HttpResponse, *>? {
        if (typeData.typeInfo.type == NetworkResult::class) {
            val successType = typeData.typeArgs.first().typeInfo
            return object :
                Converter.SuspendResponseConverter<HttpResponse, NetworkResult<Any, NetworkError>> {
```

</details>

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/SupabaseConfigClient.kt`

```kotlin
class SupabaseConfigClient(
```
Generic Supabase client wrapper for configuration/data fetching. Projects provide their own credentials implementation.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/SupabaseApiManager.kt:19</code></summary>

```kotlin
 */
class SupabaseApiManager(
    private val supabaseClient: SupabaseConfigClient,
) {
    /** Service for fetching the app configuration (server instances) from Supabase. */
    val appConfigService by lazy {
        AppConfigService(supabaseClient.postgrest)
```

</details>

```kotlin
interface SupabaseCredentials
```
Interface for providing Supabase credentials. Projects implement this with their own credential sources.

<details><summary>Used in the template — <code>core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseClientFactory.kt:58</code></summary>

```kotlin
            ?: registry.byId(id)?.supabase?.publishableKey.orEmpty()
        val client = SupabaseConfigClient(
            credentials = object : SupabaseCredentials {
                override val url: String = url
                override val publishableKey: String = key
            },
            logLevel = logLevel,
```

</details>

- `val url: String` — Supabase project URL.
- `val anonKey: String` — Supabase anonymous API key.
- `val isConfigured: Boolean` — Whether the credentials are configured.

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/factory/ResultSuspendConverterFactory.kt`

```kotlin
class ResultSuspendConverterFactory : Converter.Factory
```
A custom `Converter.Factory` for Ktorfit that provides a suspend response converter which wraps successful or error HTTP responses into a sealed `NetworkResult` type.

<details><summary>Example</summary>

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Result<List<User>, RemoteError>
}
```

</details>

<details><summary>Used in the template — <code>core-base/network/src/commonMain/kotlin/kpt/core/base/network/factory/ResultSuspendConverterFactory.kt:50</code></summary>

```kotlin
 */
@Suppress("NestedBlockDepth")
class ResultSuspendConverterFactory : Converter.Factory {

    /**
     * Creates a [Converter.SuspendResponseConverter] that wraps an HTTP
     * response into a [NetworkResult] type.
```

</details>

---

_63 type(s), 159 function(s)/property(ies); 219 carry KDoc at source; 4 authored example(s); 62 live call site(s)._
<!-- api-docs:end -->
