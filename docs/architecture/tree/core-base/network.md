# `core-base/network`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_NETWORK.md`
> **Measured:** 32 Kotlin files, 0 test files

**Defines annotations:** `@ApiBinding`

## Principal types

`AccessPoint`, `AccessPointKind`, `AccessPointRegistry`, `ApiBinding`, `AuthHeaderBridge`, `AuthProviders`, `AuthScheme`, `AuthTokenSource`, `DefaultHeaderProvider`, `DynamicBaseUrlConfig`, `DynamicBaseUrlPlugin`, `DynamicLoggableHosts`, `DynamicUrlConfigProvider`, `HeaderSpec`  …and 22 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

### Runtime network configs — N declared, one main, all switchable

`app-profile/app.yaml#network.access_points` declares the app's servers. `NetworkConfigRepository`
(`NetworkConfig.kt`, bound by `di/NetworkConfigModule`, included from core/network's `NetworkModule`)
turns every one of them into a **runtime network config** — `{id, kind, baseUrl, headers, enabled}` —
that the running app can change without a rebuild:

| operation | what it does | refused when |
|---|---|---|
| `update(id, baseUrl, headers)` | edit a config's URL / declared-header values | unknown id |
| `add(config)` | a config that app-profile never declared (`runtimeAdded = true`) | id already exists |
| `setMain(id)` | make another config the main one | different `kind`, or disabled |
| `disable(id)` / `enable(id)` | a disabled config fails its requests rather than silently falling back | it is the main |
| `remove(id)` | delete a runtime-added config | declared in app-profile, or it is the main |
| `reset(id)` / `resetAll()` | back to exactly what app-profile declares | — |

**Main vs sub.** At most one access point is `main: true` (the generator refuses two). Clients bound to
that point follow `mainId`, so `setMain("staging")` repoints every main-bound client at once; every
other config is a *sub* that keeps serving its own clients. Any kind can be main — a Supabase-backed
fork marks its Supabase point — but the main only switches between configs of the same kind.

**Where it reaches requests.** `ktorfitFor` / `restApi` resolve `getOrNull<NetworkConfigRepository>()`
and pass it to `restClientConfig`, which reads the URL (`DynamicBaseUrlPlugin`, with `staticBaseUrl` so
the declared base path is replaced, not stacked) and the header values **per request**. So a change
reaches a client that already exists — the shared per-access-point client is never rebuilt.
`SupabaseClientFactory` rebuilds its cached client when the effective URL changes (the Supabase SDK
fixes its URL at construction).

**Persistence.** Only the *difference* from app-profile is stored (`NetworkConfigOverrides`, key
`kpt.network.configs` in the `plain` `Settings`); an unchanged config stores nothing, and a persisted
`mainId` that app-profile no longer declares falls back to the declared main. With no `plain`
Settings bound it runs in memory (`InMemoryNetworkConfigStore`), which is what tests use.

**What a fork does with it.** Put a screen over it — a "server" setting is a read of `mainConfig()` and
a write through `update(mainId, …)`. Do **not** keep a second copy of the server in preferences: that is
exactly how a settings screen and the HTTP client end up disagreeing about which server is in use.
Changing the server usually invalidates the session, so sign out on a change.

Tests: `core/network/src/commonTest/.../config/{NetworkConfigRepositoryTest,NetworkConfigRequestTest,DynamicBaseUrlTest}.kt`.

<!-- api-docs:begin module=core-base/network sha=a7112889dc8d9eabe44ed1b46e3a06857ae7d445 -->
## API reference

_Generated from `core-base/network` at tree `a7112889dc8d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AccessPointRegistry.kt`

```kotlin
enum class AccessPointKind
```
Transport kind of a declared network access point.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:37</code></summary>

```kotlin
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.openmf.org/",
            loggableHost = "api.openmf.org",
        ),
        AccessPoint(
```

</details>

```kotlin
data class AccessPoint(
```
One declared network access point — a named endpoint the app talks to.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:34</code></summary>

```kotlin
    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.
    /** This project's declared access points, in app-profile order. */
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            id = "main",
            kind = AccessPointKind.REST,
            baseUrl = "https://api.openmf.org/",
```

</details>

```kotlin
class AccessPointRegistry(
```
Template registry MECHANISM over a fork-provided list of `points`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt:46</code></summary>

```kotlin
@DataProvider(createdAtStart = true)
fun provideAuthHeaderBridge(
    registry: AccessPointRegistry,
    tokenSource: AuthTokenSource,
    headers: RuntimeHeaderStore,
    preferences: UserPreferencesRepository,
    scope: CoroutineScope,
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AuthScheme.kt`

```kotlin
enum class AuthScheme
```
How an access point authenticates, declared as `auth:` in `app-profile/app.yaml#network.access_points[]`.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt:81</code></summary>

```kotlin
    // compose, let alone fetch.
    registry.points
        .filter { point -> point.auth != AuthScheme.NONE }
        .forEach { point ->
            val key = AuthScheme.runtimeKeyFor(point.id)
            when (val value = point.auth.format(preferences.authToken)) {
                null -> headers.clear(key)
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/AuthTokenSource.kt`

```kotlin
fun interface AuthTokenSource
```
The stored credential, as a stream. A PORT, implemented where the credential actually lives (`core/data`, over `core/datastore`'s encrypted store).

```kotlin
class AuthHeaderBridge(
```
Keeps `RuntimeHeaderStore` in step with the stored credential, so `Authorization` is automatic.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt:45</code></summary>

```kotlin
 */
@DataProvider(createdAtStart = true)
fun provideAuthHeaderBridge(
    registry: AccessPointRegistry,
    tokenSource: AuthTokenSource,
    headers: RuntimeHeaderStore,
    preferences: UserPreferencesRepository,
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DefaultHeaderProvider.kt`

```kotlin
interface DefaultHeaderProvider
```
Headers this app sends on EVERY request, resolved per access point.

<details><summary>Example</summary>

```kotlin
object KptNetworkHeaders : DefaultHeaderProvider {
    override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
        "main" -> mapOf("X-Client-Version" to BuildKonfig.VERSION_NAME)
        else -> emptyMap()
    }
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/KptNetworkHeaders.kt:44</code></summary>

```kotlin
 * this map is read once per client rather than once per call.
 */
object KptNetworkHeaders : DefaultHeaderProvider
```

</details>

- `fun headersFor(accessPointId: String): Map<String, String> = emptyMap()` — Headers to append to every request for `accessPointId` — the `id:` from `app-profile/app.yaml#network.access_points`. Returns empty by default: the neutral template sends nothing extra.
- `val None: DefaultHeaderProvider = object : DefaultHeaderProvider {}` — The no-op default. Bound by `NetworkModule` unless a fork binds its own.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DynamicBaseUrlPlugin.kt`

```kotlin
class DynamicBaseUrlPlugin private constructor(
```
Ktor plugin that dynamically sets the base URL for each request based on a `DynamicUrlConfigProvider` or `MultiUrlConfigProvider`.

<details><summary>Example</summary>

```kotlin
val client = httpClient {
    install(DynamicBaseUrlPlugin) {
        configProvider = myConfigProvider
    }
}
// one client per named endpoint
val server1Client = httpClient {
    install(DynamicBaseUrlPlugin) {
        multiConfigProvider = myMultiConfigProvider
        urlType = AppUrlTypes.SERVER1   // == UrlType("SERVER1")
    }
}

val stagingClient = httpClient {
    install(DynamicBaseUrlPlugin) {
        multiConfigProvider = myMultiConfigProvider
        urlType = AppUrlTypes.STAGING
    }
}
```

</details>

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

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/DynamicBaseUrlPlugin.kt:34</code></summary>

```kotlin
) {
    /** Installs [DynamicBaseUrlPlugin] into a Ktor client. */
    companion object Plugin : HttpClientPlugin<DynamicBaseUrlConfig, DynamicBaseUrlPlugin> {
        override val key: AttributeKey<DynamicBaseUrlPlugin> =
            AttributeKey("DynamicBaseUrlPlugin")

        override fun prepare(block: DynamicBaseUrlConfig.() -> Unit): DynamicBaseUrlPlugin {
```

</details>

```kotlin
class DynamicLoggableHosts(
```
A dynamic list implementation that provides loggable hosts from a `DynamicUrlConfigProvider`. This list is evaluated each time it's iterated, so it reflects the currently configured loggable hosts.

<details><summary>Example</summary>

```kotlin
val client = httpClient(
    setupDefaultHttpClient(
        baseUrl = "https://placeholder.local/",
        loggableHosts = DynamicLoggableHosts(myConfigProvider),
    )
) {
    install(DynamicBaseUrlPlugin) {
        configProvider = myConfigProvider
    }
}
```

</details>

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

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/DynamicUrlConfigProvider.kt`

```kotlin
interface DynamicUrlConfigProvider
```
Interface for providing dynamic URL configuration at runtime. Implementations of this interface allow the HTTP client to dynamically switch between different server endpoints without recreating the client.

<details><summary>Example</summary>

```kotlin
class MyConfigProvider(
    private val preferencesRepository: UserPreferencesRepository
) : DynamicUrlConfigProvider {
    override fun getBaseUrl(): String = preferencesRepository.selectedServer.value?.url
        ?: "https://default.api.com"

    override fun getLoggableHosts(): List<String> = listOf(
        preferencesRepository.selectedServer.value?.host ?: "default.api.com"
    )
}
```

</details>

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

- `fun getBaseUrl(): String` — Returns the current base URL to use for API requests. This is called for each request, allowing runtime URL switching.
- `fun getLoggableHosts(): List<String>` — Returns the list of hostnames that should have HTTP logging enabled. This is called dynamically, allowing the logging filter to adapt to the currently selected server.

```kotlin
value class UrlType(val key: String)
```
Open, extensible identifier for a named API endpoint.

<details><summary>Example</summary>

```kotlin
// in core/ (your project) — name them however you like, add as many as you need
object AppUrlTypes {
    val MAIN    = UrlType.MAIN
    val SERVER1 = UrlType("SERVER1")
    val STAGING = UrlType("STAGING")
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt:31</code></summary>

```kotlin
) : MultiUrlConfigProvider {

    override fun getBaseUrl(type: UrlType): String =
        registry.restBaseUrl(type)
            ?: registry.restBaseUrl(UrlType.MAIN)
            ?: error("No REST access point registered for $type or MAIN")
```

</details>

```kotlin
interface MultiUrlConfigProvider : DynamicUrlConfigProvider
```
Extension of `DynamicUrlConfigProvider` for applications that expose more than one endpoint (identified by an open `UrlType` — the project names them in `core/`).

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

- `fun getBaseUrl(type: UrlType): String` — Returns the base URL for the specified `type`.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/HeaderSpec.kt`

```kotlin
data class HeaderSpec(
```
A header an access point sends on every request, declared in `app-profile/app.yaml#network.access_points[].headers[]`. Two kinds, because they have different lifetimes: - **static** (`value` set) — known at build time.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/NetworkConfigRepositoryTest.kt:40</code></summary>

```kotlin
        isMain = true,
        headers = listOf(
            HeaderSpec(name = "X-Tenant-Id", value = "tenant-a"),
            HeaderSpec(name = "Authorization", runtimeKey = "main.auth"),
        ),
    )
    private val staging = AccessPoint(
```

</details>

```kotlin
class RuntimeHeaderStore
```
Values for the `runtimeKey` headers, written when they become known and read on every request.

<details><summary>Example</summary>

```kotlin
// after a successful sign-in
runtimeHeaders[FineractHeaders.AUTH] = "Basic ${base64("$user:$password")}"

// on sign-out — clearing is as important as setting
runtimeHeaders.clear(FineractHeaders.AUTH)
```

</details>

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/user/AuthTokenBridge.kt:48</code></summary>

```kotlin
    registry: AccessPointRegistry,
    tokenSource: AuthTokenSource,
    headers: RuntimeHeaderStore,
    preferences: UserPreferencesRepository,
    scope: CoroutineScope,
): AuthHeaderBridge = AuthHeaderBridge(
    points = registry.points,
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/KtorHttpClient.kt`

```kotlin
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
```
Creates a Ktor `HttpClient` on the target's own engine (OkHttp, Darwin, JS fetch, CIO).

```kotlin
data class AuthProviders(
```
The auth-credential providers for `setupDefaultHttpClient`, grouped so the client builder stays under the parameter-count limit. All null (the default) installs no `Auth` plugin.

```kotlin
data class HttpTimeouts(
```
The three Ktor timeouts for `setupDefaultHttpClient`, grouped for the same reason `AuthProviders` is: the builder is capped at 20 parameters by detekt's `LongParameterList`, and adding `connectTimeout` as a 21st flat parameter crossed it.

```kotlin
fun setupDefaultHttpClient(
```
Provides a default `HttpClientConfig` setup for use with a Ktor-based HTTP client.

<details><summary>Example</summary>

```kotlin
val client = httpClient(setupDefaultHttpClient(baseUrl = "https://api.example.com"))
```

</details>

```kotlin
val DEFAULT_SENSITIVE_HEADERS: List<String> = listOf(
```
Headers masked in HTTP logs by default — every credential or session carrier the template sends.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkConfig.kt`

```kotlin
data class NetworkConfig(
```
One network config as the app runs it — a declared access point (`app-profile/app.yaml`) with any runtime changes applied, or a config added at runtime. `baseUrl` is the full URL requests resolve against (origin + API path).

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/NetworkConfigRepositoryTest.kt:105</code></summary>

```kotlin
        val r = repo()
        r.add(
            NetworkConfig(
                id = "custom",
                kind = AccessPointKind.REST,
                baseUrl = "http://10.0.2.2:8080/fineract-provider/api/v1/",
            ),
```

</details>

```kotlin
data class NetworkConfigOverrides(
```
The persisted half: only what differs from app-profile.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/NetworkConfigRepositoryTest.kt:146</code></summary>

```kotlin

        // A persisted main that app-profile no longer declares.
        val stale = repo(InMemoryNetworkConfigStore(NetworkConfigOverrides(mainId = "gone")))
        assertEquals("main", stale.mainId)
    }

    @Test
```

</details>

```kotlin
interface NetworkConfigStore
```
Where `NetworkConfigRepository` keeps its overrides.

- `fun load(): NetworkConfigOverrides` — The saved overrides, or empty ones when nothing was saved. Called once, at construction.
- `fun save(overrides: NetworkConfigOverrides)` — Replace the saved overrides with `overrides`.

```kotlin
class InMemoryNetworkConfigStore(
```
Not persisted — for tests and for an app that wants changes to last one session only.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/NetworkConfigRepositoryTest.kt:60</code></summary>

```kotlin
    private val registry = AccessPointRegistry(listOf(main, staging, supabase))

    private fun repo(store: InMemoryNetworkConfigStore = InMemoryNetworkConfigStore()) =
        NetworkConfigRepository(registry, store)

    @Test
    fun withNoChangesEveryPointUsesWhatAppProfileDeclares() {
```

</details>

```kotlin
class SettingsNetworkConfigStore(private val settings: Settings, private val key: String = KEY) : NetworkConfigStore
```
Persists the overrides as JSON in `settings` (the app's plain settings). A corrupt value reads as none.

<details><summary>Used in the template — <code>core-base/network/src/commonMain/kotlin/kpt/core/base/network/di/NetworkConfigModule.kt:31</code></summary>

```kotlin
        NetworkConfigRepository(
            registry = get(),
            store = getOrNull<Settings>(named("plain"))?.let(::SettingsNetworkConfigStore)
                ?: InMemoryNetworkConfigStore(),
        )
    }
}
```

</details>

```kotlin
class NetworkConfigRepository(
```
The app's network configs at runtime: every access point app-profile declares, changeable, plus any added at runtime — with one of them MAIN. **Main.** App-profile marks one access point `main: true`.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/config/NetworkConfigRepositoryTest.kt:61</code></summary>

```kotlin

    private fun repo(store: InMemoryNetworkConfigStore = InMemoryNetworkConfigStore()) =
        NetworkConfigRepository(registry, store)

    @Test
    fun withNoChangesEveryPointUsesWhatAppProfileDeclares() {
        val r = repo()
```

</details>

```kotlin
fun AccessPoint.toNetworkConfig(): NetworkConfig = NetworkConfig(
```
A declared point as a `NetworkConfig`: its URL and its static header values.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkDsl.kt`

```kotlin
fun AccessPointRegistry.ktorfitFor(
```
Build a Ktorfit for the REST access point declared under `accessPointId`. Base URL + loggable host + proxied host come from this `AccessPointRegistry`; throws if the id isn't declared.

```kotlin
fun restClientConfig(
```
The client configuration `ktorfitFor` builds an access point's shared client with — public so it can be exercised on a test engine exactly as the app runs it.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkError.kt`

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

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkPolicy.kt`

```kotlin
enum class HttpLogDetail(internal val level: LogLevel)
```
How much of an HTTP exchange may be written to the device log. Ordered least to most revealing.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:54</code></summary>

```kotlin
    /** Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`. */
    val policy: NetworkPolicy = NetworkPolicy(
        releaseLogging = HttpLogDetail.NONE,
        debugLogging = HttpLogDetail.BODY,
        webCorsProxy = "/cors",
    )
    // syncForkConfig:access-points:end
```

</details>

```kotlin
data class NetworkPolicy(
```
App-wide network privacy policy — GENERATED from `app-profile/app.yaml#network` (`logging`, `web_cors_proxy`) onto `AppAccessPoints.policy` by `syncForkConfig`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:53</code></summary>

```kotlin

    /** Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`. */
    val policy: NetworkPolicy = NetworkPolicy(
        releaseLogging = HttpLogDetail.NONE,
        debugLogging = HttpLogDetail.BODY,
        webCorsProxy = "/cors",
    )
```

</details>

```kotlin
fun maskQueryParams(message: String, params: List<String>): String
```
Mask the values of `params` in `message` — any `name=value` pair after `?` or `&`, so a logged URL keeps its shape (useful for debugging) without the credential.

```kotlin
expect fun Scope.isDebugBuild(): Boolean
```
True only for a DEBUG build of the running app — never "a debugger is attached" (that is `kpt.core.base.security.isReleaseBuild`, a tamper signal, and wrong for this: a debug build with no debugger would read as release).

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/NetworkResult.kt`

```kotlin
sealed interface NetworkResult<out D, out E : NetworkError>
```
Represents the result of a network or remote operation, encapsulating either a success or an error.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/NetworkResult.kt:22</code></summary>

```kotlin
 * @param E The type of error returned on failure, constrained to [NetworkError].
 */
sealed interface NetworkResult<out D, out E : NetworkError> {

    /**
     * Represents a successful result.
     *
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseClientFactory.kt`

```kotlin
class SupabaseClientFactory(
```
Per-point Supabase client factory. Builds one `SupabaseConfigClient` per declared Supabase `AccessPoint`, entirely from that point's generated `AccessPoint.supabase`: URL from `AccessPoint.baseUrl`, key from `SupabaseSpec.publishableKey`.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/config/SupabaseAccessPointTest.kt:47</code></summary>

```kotlin
    }

    private fun factory() = SupabaseClientFactory(
        registry = AccessPointRegistry(unconfiguredPoints),
    )

    @Test
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseConfigClient.kt`

```kotlin
class SupabaseConfigClient(
```
Generic Supabase client wrapper for KMP projects.

<details><summary>Example</summary>

```kotlin
// Consumers do NOT construct this directly — SupabaseClientFactory builds one per declared
// SUPABASE access point, and `supabaseApi("<id>")` injects it into the fork's API type.
val configClient = supabaseClientFactory.requireClientFor("project")

// Use the client for queries
if (configClient.isConfigured) {
    val result = configClient.postgrest
        .from("app_server_config")
        .select()
        .decodeList<MyServer>()
}
```

</details>

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
data class SupabaseSpec(
```
A Supabase access point's client config — GENERATED onto `AccessPoint.supabase` by `syncForkConfig` from `app-profile/app.yaml#network.access_points[]`, so a fork configures Supabase in app-profile only.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt:46</code></summary>

```kotlin
            baseUrl = "https://lwmswhoxvvoagzkqxiyd.supabase.co",
            loggableHost = "lwmswhoxvvoagzkqxiyd.supabase.co",
            supabase = kpt.core.base.network.SupabaseSpec(
                publishableKey = kpt.core.network.BuildKonfig.SUPABASE_PUBLISHABLE_KEY,
            ),
        ),
    )
```

</details>

```kotlin
interface SupabaseCredentials
```
Supabase URL + client key for one `SupabaseConfigClient`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/SupabaseConfigClient.kt:23</code></summary>

```kotlin
 */
class SupabaseConfigClient(
    private val credentials: SupabaseCredentials,
    private val logLevel: LogLevel = LogLevel.INFO,
) {
    /** Lazily created Supabase client with the Postgrest plugin installed. */
    val client: SupabaseClient by lazy {
```

</details>

- `val url: String` — The Supabase project URL, e.g. `https://<ref>.supabase.co`.
- `val publishableKey: String` — The project's publishable (client) key. Safe in client code; never a `service_role` / `sb_secret_` key, which bypasses RLS (enforced by `scripts/product-health/checks/supabase-client-security.sh`).
- `val isConfigured: Boolean` — False while the URL or key is blank, a `YOUR_` placeholder, or the URL is not https.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseExtrasProvider.kt`

```kotlin
fun interface SupabaseExtrasProvider
```
Fork seam for the supabase-kt modules the default exposer does not install. `SupabaseConfigClient` installs Postgrest only — Auth / Realtime / Storage are opt-in.

<details><summary>Example</summary>

```kotlin
single<SupabaseExtrasProvider> {
    SupabaseExtrasProvider { id ->
        when (id) {
            // Sign-in (Google, Apple, guest): io.github.mobilebytelabs:cmp-supabase-auth installs Auth +
            // ComposeAuth onto THIS client. Pair it with `kmpSupabaseAuthNetwork(config) {
            //   get<SupabaseClientFactory>().requireClientFor(AUTH_PROJECT).client }` — see mbs/cappy.
            // secureStorage (kmp-supabase-auth 0.3.3) keeps the session in encrypted storage.
            AUTH_PROJECT -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
            ANALYTICS_PROJECT -> {
                { install(Realtime) }
            }
            // Shared across several projects — a `when` branch takes a comma-separated list, which
            // an if/else chain cannot express without repeating the block.
            PROJECT_A, PROJECT_B -> {
                { install(Storage) }
            }
            else -> {
                {}
            }
        }
    }
}
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/UnauthorizedHandler.kt`

```kotlin
fun interface UnauthorizedHandler
```
Told when a request that CARRIED credentials is answered 401 — the session the app holds has been rejected (expired, revoked, password changed, or issued by another server).

```kotlin
fun HttpClientConfig<*>.notifyUnauthorized(accessPointId: String, handler: UnauthorizedHandler)
```
Notifies `handler` of a 401 on a request that sent an `Authorization` header. A 401 without one — a mistyped password on the sign-in call itself — is not a rejected session and is ignored.

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/annotation/ApiBinding.kt`

```kotlin
annotation class ApiBinding(val accessPoint: String)
```
Binds this API type to a declared access point, so its Koin binding is derived rather than written.

<details><summary>Example</summary>

```kotlin
@ApiBinding("coingecko")
interface CoinGeckoApi { ... }
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt:23</code></summary>

```kotlin
 * Inert without a key: returns empty, never builds a client on an empty URL.
 */
@ApiBinding("lwmswhoxvvoagzkqxiyd")
class ServerInstanceApiImpl(
    private val supabase: SupabaseConfigClient,
) : ServerInstanceApi {
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/di/NetworkBaseModule.kt`

```kotlin
val NetworkBaseModule = module
```
Framework network wiring — the half of the network graph a fork never configures. ## Why these bindings are here and not in `core/network` A binding belongs in `core-base` when it touches nothing the fork generates.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:53</code></summary>

```kotlin
val NetworkModule = module {
    // Framework half — bindings that touch nothing app-profile generates.
    includes(NetworkBaseModule)

    // Runtime header values — written at login (Basic / OAuth), read on EVERY request. A singleton,
    // because the whole point is that a value set after the clients were built still reaches them.
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/di/NetworkConfigModule.kt`

```kotlin
val NetworkConfigModule = module
```
Runtime network configs, bound by core-base — an app includes this module and gets the whole feature: every declared access point editable and switchable at runtime, persisted, with `restApi` and `SupabaseClientFactory` following it.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:71</code></summary>

```kotlin
    includes(GeneratedApiBindings)
    // Runtime network configs (core-base): every access point editable/switchable at runtime, persisted.
    includes(NetworkConfigModule)

    // The fork's generated access points, wrapped by the framework registry mechanism (core-base/network).
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    // + the app-wide network privacy policy (logging per build type, query redaction, web CORS relay).
```

</details>

### `core-base/network/src/commonMain/kotlin/kpt/core/base/network/factory/ResultSuspendConverterFactory.kt`

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

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifos/corebase/network/factory/ResultSuspendConverterFactory.kt:49</code></summary>

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

_32 type(s), 24 function(s)/property(ies); 56 carry KDoc at source; 11 authored example(s); 31 live call site(s)._
<!-- api-docs:end -->
