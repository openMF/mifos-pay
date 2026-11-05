# `core/network`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_NETWORK.md`
> **Measured:** 80 Kotlin files, 13 test files

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

<!-- api-docs:begin module=core/network sha=7f2411dd1d29836044c23b53a5073ed9dc1f810b -->
## API reference

_Generated from `core/network` at tree `7f2411dd1d29` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

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

### `core/network/src/commonMain/kotlin/org/mifos/corebase/network/DynamicBaseUrlPlugin.kt`

```kotlin
class DynamicBaseUrlPlugin private constructor(
```
Ktor plugin that dynamically sets the base URL for each request based on a `DynamicUrlConfigProvider` or `MultiUrlConfigProvider`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:94</code></summary>

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

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:82</code></summary>

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
    companion object Plugin : HttpClientPlugin<DynamicBaseUrlConfig, DynamicBaseUrlPlugin> {
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
_No KDoc at source._

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

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/KptErrorMapper.kt:56</code></summary>

```kotlin
                ScreenState.Unauthenticated
            } else {
                ScreenState.Error(error = throwable, isNetworkError = false)
            }
        }
        if (throwable is NetworkException) {
            return ScreenState.NoNetwork(isCaptivePortal = false)
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

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt:25</code></summary>

```kotlin
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class ServerInstanceApiImpl(
    private val supabase: SupabaseConfigClient,
) : ServerInstanceApi {

    override val isConfigured: Boolean get() = supabase.isConfigured
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

- `val url: String`
- `val anonKey: String`
- `val isConfigured: Boolean`

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

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/FineractApiManager.kt`

```kotlin
class FineractApiManager(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/AccountRepositoryImpl.kt:48</code></summary>

```kotlin
// TODO use self api for account operations later
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-3 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getSelfAccountsScreen(...) can consume Store5 via
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/InterBankApiManager.kt`

```kotlin
class InterBankApiManager(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/InterBankRepositoryImpl.kt:28</code></summary>

```kotlin

class InterBankRepositoryImpl(
    private val apiManager: InterBankApiManager,
    private val ioDispatcher: CoroutineDispatcher,
) : InterBankRepository {

    override suspend fun fetchParticipant(
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt`

```kotlin
class KtorfitClient(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/org/mifospay/core/data/repositoryImpl/PocketRepositoryTest.kt:155</code></summary>

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

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/PocketDataManager.kt`

```kotlin
interface PocketDataManager
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/SelfServiceApiManager.kt:14</code></summary>

```kotlin
class SelfServiceApiManager(
    private val ktorfitClient: KtorfitClient,
) : PocketDataManager {
    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    override val clientsApi by lazy { ktorfitClient.clientsApi }
```

</details>

- `val clientsApi: ClientService`
- `val pocketApi: PocketService`
- `val shareAccountApi: ShareAccountService`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/SelfServiceApiManager.kt`

```kotlin
class SelfServiceApiManager(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/AccountRepositoryImpl.kt:49</code></summary>

```kotlin
class AccountRepositoryImpl(
    private val apiManager: FineractApiManager,
    private val selfManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-3 LEDGER wiring — injected by RepositoryModule so the
    // store-backed getSelfAccountsScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without the
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/SupabaseApiManager.kt`

```kotlin
class SupabaseApiManager(
```
API manager for Supabase services. Similar to FineractApiManager but for Supabase-based APIs.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/config/SupabaseInstanceConfigLoader.kt:29</code></summary>

```kotlin
 */
class SupabaseInstanceConfigLoader(
    private val supabaseApiManager: SupabaseApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : InstanceConfigLoader {
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/config/InstanceConfigLoader.kt`

```kotlin
interface InstanceConfigLoader
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/config/SupabaseInstanceConfigLoader.kt:32</code></summary>

```kotlin
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : InstanceConfigLoader {

    override suspend fun fetchInstancesConfig(): InstancesConfig {
        if (!supabaseApiManager.isConfigured) {
            return loadFallbackConfig()
```

</details>

- `suspend fun fetchInstancesConfig(): InstancesConfig`
- `fun observeInstancesConfig(): Flow<ScreenState<InstancesConfig>>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/config/InstanceConfigManager.kt`

```kotlin
class InstanceConfigManager(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/login/LoginViewModel.kt:33</code></summary>

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

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/config/SupabaseInstanceConfigLoader.kt`

```kotlin
class SupabaseInstanceConfigLoader(
```
Instance config loader that fetches configuration from Supabase. Uses SupabaseApiManager for API interactions.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:56</code></summary>

```kotlin

    single<InstanceConfigLoader> {
        SupabaseInstanceConfigLoader(
            supabaseApiManager = get(),
            ioDispatcher = get(ioDispatcher),
            json = get(),
        )
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/di/LocalModule.kt`

```kotlin
val LocalModule = module
```
_No KDoc at source._

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt`

```kotlin
val NetworkModule = module
```
_No KDoc at source._

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

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/di/Qualifier.kt`

```kotlin
val SelfClient = named("SelfClient")
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:71</code></summary>

```kotlin
    }

    single<KtorfitClient>(qualifier = SelfClient) {
        val preferencesRepository = get<UserPreferencesRepository>()
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
```

</details>

```kotlin
val BaseClient = named("BaseClient")
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:109</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:150</code></summary>

```kotlin
    }

    single<KtorfitClient>(qualifier = InterBankClient) {
        val configManager = get<InstanceConfigManager>()
        KtorfitClient(
            Ktorfit.Builder()
                .httpClient(
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/localAssets/LocalAssetDataSource.kt`

```kotlin
interface LocalAssetDataSource
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/localAssets/MifosLocalAssetDataSource.kt:21</code></summary>

```kotlin
    private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
) : LocalAssetDataSource {

    override suspend fun getCountries(): List<Country> {
        return emptyList()
    }
```

</details>

- `suspend fun getCountries(): List<Country>`
- `suspend fun getStateList(): List<State>`
- `suspend fun getBanks(): List<String>`
- `suspend fun getCities(): List<City>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/localAssets/LocalAssetManager.kt`

```kotlin
fun interface LocalAssetManager
```
_No KDoc at source._

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/localAssets/MifosLocalAssetDataSource.kt`

```kotlin
class MifosLocalAssetDataSource(
```
_No KDoc at source._

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/AccountTransfersService.kt`

```kotlin
interface AccountTransfersService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:55</code></summary>

```kotlin
    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }
```

</details>

- `fun getTransaction(`
- `fun getAccountTransfer(@Path("transferId") transferId: Int): Flow<TransferDetail>`
- `fun searchAccounts(`
- `suspend fun makeTransfer(`
- `fun getAccountTransferTemplate(): Flow<AccountOptionsTemplate>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/AppConfigService.kt`

```kotlin
class AppConfigService(
```
Service for interacting with app_config table in Supabase.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/SupabaseApiManager.kt:23</code></summary>

```kotlin
) {
    val appConfigService by lazy {
        AppConfigService(supabaseClient.postgrest)
    }

    val isConfigured: Boolean get() = supabaseClient.isConfigured
}
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/AuthenticationService.kt`

```kotlin
interface AuthenticationService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:41</code></summary>

```kotlin
    ktorfit: Ktorfit,
) {
    internal val authenticationApi by lazy { ktorfit.createAuthenticationService() }

    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }
```

</details>

- `suspend fun authenticate(@Body authPayload: AuthenticationPayload): User`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/AutoPayService.kt`

```kotlin
interface AutoPayService
```
TODO: Sync with backend team and update service layer according to finalized API contract, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:73</code></summary>

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
_No KDoc at source._

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/BeneficiaryService.kt`

```kotlin
interface BeneficiaryService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:75</code></summary>

```kotlin
    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }
```

</details>

- `fun beneficiaryList(): Flow<List<Beneficiary>>`
- `suspend fun beneficiaryTemplate(): Flow<BeneficiaryTemplate>`
- `suspend fun createBeneficiary(@Body beneficiaryPayload: BeneficiaryPayload)`
- `suspend fun updateBeneficiary(`
- `suspend fun deleteBeneficiary(@Path("beneficiaryId") beneficiaryId: Long): Unit`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/BillService.kt`

```kotlin
interface BillService
```
TODO: Update endpoint paths when backend APIs are finalized, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:83</code></summary>

```kotlin
    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
```

</details>

- `fun getAllBills(@Query("clientId") clientId: Long): Flow<List<Bill>>`
- `suspend fun getBillById(@Path("billId") billId: String): Flow<Bill>`
- `suspend fun createBill(@Body bill: Bill): Flow<Bill>`
- `suspend fun updateBill(`
- `suspend fun deleteBill(@Path("billId") billId: String): Flow<Unit>`
- `suspend fun getBillsByStatus(@Path("status") status: BillStatus): Flow<List<Bill>>`
- `suspend fun getBillsByBillerId(@Path("billerId") billerId: String): Flow<List<Bill>>`
- `suspend fun getBillsByRecurrencePattern(@Path("pattern") pattern: RecurrencePattern): Flow<List<Bill>>`
- `suspend fun searchBillsByName(@Query("query") query: String): Flow<List<Bill>>`
- `suspend fun getOverdueBills(): Flow<List<Bill>>`
- `suspend fun getUpcomingBills(`
- `suspend fun updateBillStatus(`
- `fun getBillStatistics(@Query("clientId") clientId: Long): Flow<BillStatisticsResponse>`
- `suspend fun clearAllBills(): Flow<Unit>`

```kotlin
data class BillStatisticsResponse(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repositoryImpl/BillRepositoryImpl.kt:168</code></summary>

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

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/BillerService.kt`

```kotlin
interface BillerService
```
TODO: Update endpoint paths when backend APIs are finalized, also use Flow only in get operations where List<T> is returned, do not use Flow for one-shot operations

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:81</code></summary>

```kotlin
    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }
```

</details>

- `fun getAllBillers(): Flow<List<Biller>>`
- `suspend fun getBillerById(@Path("billerId") billerId: String): Flow<Biller>`
- `suspend fun createBiller(@Body biller: Biller): Flow<Biller>`
- `suspend fun updateBiller(`
- `suspend fun deleteBiller(@Path("billerId") billerId: String): Flow<Unit>`
- `suspend fun getBillersByCategory(`
- `suspend fun searchBillersByName(`
- `suspend fun isBillerExists(`
- `suspend fun clearAllBillers(): Flow<Unit>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/ClientService.kt`

```kotlin
interface ClientService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:43</code></summary>

```kotlin
    internal val authenticationApi by lazy { ktorfit.createAuthenticationService() }

    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }
```

</details>

- `suspend fun clients(): Flow<Page<ClientEntity>>`
- `suspend fun getClientForId(@Path("clientId") clientId: Long): ClientEntity`
- `fun getClient(@Path("clientId") clientId: Long): Flow<ClientEntity>`
- `suspend fun updateClient(`
- `fun getClientImage(@Path("clientId") clientId: Long): Flow<String>`
- `suspend fun updateClientImage(`
- `suspend fun getClientAccounts(@Path("clientId") clientId: Long): ClientAccountsEntity`
- `fun getAccounts(`
- `suspend fun createClient(@Body newClient: NewClientEntity): ClientResponseEntity`
- `suspend fun deleteClient(@Path("clientId") clientId: Int): ClientResponseEntity`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/DocumentService.kt`

```kotlin
interface DocumentService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:49</code></summary>

```kotlin
    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }
```

</details>

- `suspend fun getDocuments(`
- `suspend fun createDocument(`
- `suspend fun createDocumentFile(`
- `suspend fun downloadDocument(` — This Service is for downloading the Document with EntityType and EntityId and Document Id Rest End Point : https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/ {documentId}/attachment
- `suspend fun removeDocument(` — This Service is for Deleting the Document with EntityType and EntityId and Document Id.
- `suspend fun updateDocument(` — This Service for Updating the Document with EntityType and EntityId and Document Id.

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/InterBankService.kt`

```kotlin
interface InterBankService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:77</code></summary>

```kotlin
    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }
```

</details>

- `suspend fun fetchParticipant(`
- `suspend fun fetchPartyInfo(`
- `suspend fun interBankMakeTransfer(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/InvoiceService.kt`

```kotlin
interface InvoiceService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:61</code></summary>

```kotlin
    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }
```

</details>

- `fun getInvoices(@Path("clientId") clientId: Long): Flow<List<Invoice>>`
- `fun getInvoice(`
- `suspend fun addInvoice(`
- `suspend fun updateInvoice(`
- `suspend fun deleteInvoice(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/KYCLevel1Service.kt`

```kotlin
interface KYCLevel1Service
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:59</code></summary>

```kotlin
    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }
```

</details>

- `fun fetchKYCLevel1Details(@Path("clientId") clientId: Long): Flow<List<KYCLevel1Details>>`
- `suspend fun addKYCLevel1Details(`
- `suspend fun updateKYCLevel1Details(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/NotificationService.kt`

```kotlin
interface NotificationService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:67</code></summary>

```kotlin
    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }
```

</details>

- `fun fetchNotifications(@Query("isRead") isRead: Boolean): Flow<NotificationPayload>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/OfficeService.kt`

```kotlin
interface OfficeService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:79</code></summary>

```kotlin
    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }
```

</details>

- `fun getOffices(): Flow<List<Office>>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/PocketService.kt`

```kotlin
interface PocketService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:85</code></summary>

```kotlin
    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
}
```

</details>

- `suspend fun getPocketAccounts(): PocketResponseDto`
- `suspend fun linkAccounts(`
- `suspend fun delinkAccounts(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/RegistrationService.kt`

```kotlin
interface RegistrationService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:45</code></summary>

```kotlin
    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }
```

</details>

- `suspend fun registerUser(@Body registerPayload: RegisterPayload)`
- `suspend fun verifyUser(@Body userVerify: UserVerify)`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/RunReportService.kt`

```kotlin
interface RunReportService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:51</code></summary>

```kotlin
    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }
```

</details>

- `suspend fun getTransactionReceipt(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/SavedCardService.kt`

```kotlin
interface SavedCardService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:57</code></summary>

```kotlin
    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }
```

</details>

- `fun getSavedCards(@Path("clientId") clientId: Long): Flow<List<SavedCard>>`
- `fun getSavedCard(`
- `suspend fun addSavedCard(`
- `suspend fun updateCard(`
- `suspend fun deleteCard(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/SavingsAccountsService.kt`

```kotlin
interface SavingsAccountsService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:69</code></summary>

```kotlin
    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }
```

</details>

- `fun getSavingsWithAssociations(`
- `suspend fun getSavingsAccounts(`
- `suspend fun createSavingsAccount(@Body savingAccount: CreateNewSavingEntity)`
- `suspend fun updateSavingsAccount(`
- `suspend fun blockUnblockAccount(`
- `suspend fun getSavingAccountTransaction(`
- `suspend fun payViaMobile(@Path("accountId") accountId: Long): Flow<TransactionsEntity>`
- `fun getSavingAccountTemplate(@Query("clientId") clientId: Long): Flow<SavingAccountTemplate>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/SearchService.kt`

```kotlin
interface SearchService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:47</code></summary>

```kotlin
    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }
```

</details>

- `suspend fun searchResources(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/ShareAccountService.kt`

```kotlin
interface ShareAccountService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:87</code></summary>

```kotlin
    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
}
```

</details>

- `fun getShareAccountDetails(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/StandingInstructionService.kt`

```kotlin
interface StandingInstructionService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:71</code></summary>

```kotlin
    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }
```

</details>

- `fun getStandingInstructionTemplate(`
- `fun getAllStandingInstructions(`
- `fun getStandingInstruction(`
- `suspend fun createStandingInstruction(`
- `suspend fun deleteStandingInstruction(`
- `suspend fun updateStandingInstruction(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/ThirdPartyTransferService.kt`

```kotlin
interface ThirdPartyTransferService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:65</code></summary>

```kotlin
    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }
```

</details>

- `suspend fun accountTransferTemplate(): AccountOptionsTemplate`
- `suspend fun makeTransfer(@Body transferPayload: TransferPayload): TPTResponse`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/TwoFactorAuthService.kt`

```kotlin
interface TwoFactorAuthService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:53</code></summary>

```kotlin
    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }
```

</details>

- `suspend fun deliveryMethods(): Flow<List<DeliveryMethod>>`
- `suspend fun requestOTP(@Query("deliveryMethod") deliveryMethod: String): Flow<String>`
- `suspend fun validateToken(@Query("token") token: String): Flow<AccessToken>`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/services/UserService.kt`

```kotlin
interface UserService
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/KtorfitClient.kt:63</code></summary>

```kotlin
    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }
```

</details>

- `suspend fun users(): Flow<List<UserWithRole>>`
- `suspend fun createUser(@Body user: NewUserEntity): CommonResponse`
- `suspend fun updateUser(`
- `suspend fun updateUserPassword(`
- `suspend fun deleteUser(`
- `suspend fun getUser(): Flow<UserWithRole>`
- `suspend fun assignClientToUser(`

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/ApiEndPoints.kt`

```kotlin
object ApiEndPoints
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/services/AccountTransfersService.kt:27</code></summary>

```kotlin
interface AccountTransfersService {

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/transactions/{transactionId}")
    fun getTransaction(
        @Path("accountId") clientId: Long,
        @Path("transactionId") transactionId: Long,
    ): Flow<TransactionsEntity>
```

</details>

- `const val AUTHENTICATION = "authentication"`
- `const val AUTHENTICATION_OAUTH = "oauth/token"`
- `const val REGISTRATION = "registration"`
- `const val CLIENTS = "clients"`
- `const val SAVINGS_ACCOUNTS = "savingsaccounts"`
- `const val TRANSACTIONS = "transactions"`
- `const val RECURRING_ACCOUNTS = "recurringdepositaccounts"`
- `const val SEARCH = "search"`
- `const val BENEFICIARIES = "beneficiaries"`
- `const val ACCOUNT_TRANSFER = "accounttransfers"`
- `const val DOCUMENTS = "documents"`
- `const val DATATABLES = "datatables"`
- `const val TWOFACTOR = "twofactor"`
- `const val RUN_REPORT = "runreports"`
  _…more members; read the file._

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/BaseURL.kt`

```kotlin
class BaseURL(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:68</code></summary>

```kotlin

    single {
        BaseURL(configManager = get())
    }

    single<KtorfitClient>(qualifier = SelfClient) {
        val preferencesRepository = get<UserPreferencesRepository>()
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/FlowConverterFactory.kt`

```kotlin
public class FlowConverterFactory : Converter.Factory
```
Factory that enables the use of Flow<T> as return type

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:104</code></summary>

```kotlin
                    },
                )
                .converterFactories(FlowConverterFactory())
                .build(),
        )
    }
```

</details>

### `core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/KtorInterceptor.kt`

```kotlin
class KtorInterceptor(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/di/NetworkModule.kt:98</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/org/mifospay/feature/history/HistoryViewModel.kt:275</code></summary>

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
_No KDoc at source._

```kotlin
class ConfigRe
```
_No KDoc at source._

---

_63 type(s), 159 function(s)/property(ies); 52 carry KDoc at source; 4 authored example(s); 62 live call site(s)._
<!-- api-docs:end -->
