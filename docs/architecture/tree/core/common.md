# `core/common`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_COMMON.md`
> **Measured:** 51 Kotlin files, 0 test files

**Defines annotations:** `@ApplicationScope`, `@Dispatcher`, `@IgnoredOnParcel`, `@OpenForMokkery`, `@Parcelize`, `@TypeParceler`

## Principal types

`ApplicationScope`, `CommonFileUtils`, `Constants`, `CurrencyFormatter`, `DateAsStringSerializer`, `DateHelper`, `DebugUtil`, `DefaultStringProvider`, `DialogManager`, `DialogMessage`, `Dispatcher`, `ErrorMessageProvider`, `FileUtils`, `GlobalAuthManager`  …and 17 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/common sha=1536573322c61a01494c290ad4683adab8ca6a0c -->
## API reference

_Generated from `core/common` at tree `1536573322c6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/common/src/commonMain/kotlin/kpt/core/common/FormatDate.kt`

```kotlin
fun formatDate(millis: Long): String
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/FormatDuration.kt`

```kotlin
fun formatTimeAgo(instant: Instant?): String?
```
Returns a human-readable "X ago" label for a past `instant`, e.g. "just now", "5m ago", "2h ago", "3d ago". Returns `null` when `instant` is null.

### `core/common/src/commonMain/kotlin/kpt/core/common/FormatNumber.kt`

```kotlin
fun Double.formatDecimal(places: Int): String
```
_No KDoc at source._

```kotlin
fun Double.formatGrouped(places: Int): String
```
_No KDoc at source._

```kotlin
fun Long.formatGrouped(): String
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDate.kt`

```kotlin
fun formatDate(millis: Long): String
```
Formats an epoch-millis timestamp for display in the device's locale.

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDuration.kt`

```kotlin
fun formatTimeAgo(instant: Instant?): String?
```
Returns a human-readable "X ago" label for a past `instant`, e.g. "just now", "5m ago", "2h ago", "3d ago". Returns `null` when `instant` is null.

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatNumber.kt`

```kotlin
fun Double.formatDecimal(places: Int): String
```
Formats to exactly `places` decimals, zero-padded so a column of figures stays aligned. Hand-rolled because `String.format` is JVM-only — it fails to link on Kotlin/JS and Native, so a shared UI cannot use it.

```kotlin
fun Double.formatGrouped(places: Int): String
```
Formats with `,` thousands separators and exactly `places` decimals — the money/quantity form.

```kotlin
fun Long.formatGrouped(): String
```
Formats a whole number with `,` thousands separators — counts and sizes, no fractional part.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/Constants.kt`

```kotlin
object Constants
```
Created by naman on 17/6/17.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/mobileVerify/MobileVerificationViewModel.kt:192</code></summary>

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
- `const val CLIENT_ID = "client_id"`
- `const val ACCOUNT_ID = "account_id"`
- `const val ACCOUNT = "account"`
- `const val TO_EXTERNAL_ID = "to_external_id"`
- `const val RUPEE = "₹"`
- `const val QR_DATA = "qr_data"`
- `const val MERCHANT_NAME = "merchant_name"`
- `const val MERCHANT_VPA = "merchant_vpa"`
- `const val MERCHANT_ACCOUNT_NO = "merchant_account_no"`
- `const val FILE = "file"`
- `const val MULTIPART_FORM_DATA = "multipart/form-data"`
- `const val CHOOSE_A_FILE_TO_UPLOAD = "Choose a file to upload."`
- `const val ERROR_UPLOADING_DOCS = "Error uploading docs."`
  _…more members; read the file._

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/CurrencyFormatter.kt`

```kotlin
expect object CurrencyFormatter
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:248</code></summary>

```kotlin
                Text(text = stringResource(Res.string.feature_accounts_detail_account_balance))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.accountBalance,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
```

</details>

- `fun format(balance: Double?, currencyCode: String?, maximumFractionDigits: Int?): String`
- `fun format(balance: Double?, maximumFractionDigits: Int?): String`

```kotlin
fun <T> List<T>.toArrayList(): ArrayList<T>
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/DateAsStringSerializer.kt`

```kotlin
object DateAsStringSerializer : KSerializer<String>
```
A custom `KSerializer` for date fields that can be represented in JSON either as a string (`"YYYY-MM-DD"`) or an array of integers (`[YYYY, MM, DD]`).

<details><summary>Example</summary>

```kotlin
@Serializable
data class Transfer(
    @Serializable(with = DateAsStringSerializer::class)
    val transferDate: String
)
```

</details>

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/org/mifospay/core/model/savingsaccount/Transfer.kt:21</code></summary>

```kotlin
    val currency: Currency? = null,
    val transferAmount: Double? = null,
    @Serializable(with = DateAsStringSerializer::class)
    val transferDate: String? = null,
    val transferDescription: String? = null,
)
```

</details>

- `val input = decoder as? JsonDecoder ?: error("Only JSON supported")`
- `val element = input.decodeJsonElement()`
- `val parts = element.map { it.jsonPrimitive.int }`

```kotlin
object ImmutableListSerializer : KSerializer<ImmutableList<String>>
```
A custom `KSerializer` for serializing and deserializing an `ImmutableList<String>` using kotlinx.serialization.

<details><summary>Example</summary>

```kotlin
@Serializable
data class UserPreferences(
    @Serializable(with = ImmutableListSerializer::class)
    val favoriteTags: ImmutableList<String>
)
```

</details>

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupViewModel.kt:575</code></summary>

```kotlin
    @Transient val dialogState: DialogState? = null,
    val passwordStrengthState: PasswordStrengthState = PasswordStrengthState.NONE,
    @Serializable(with = ImmutableListSerializer::class)
    val passwordFeedback: ImmutableList<String> = persistentListOf(),
    val countriesWithStates: Map<String, List<String>> = emptyMap(),
) {
    @Transient
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/DateHelper.kt`

```kotlin
object DateHelper
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:429</code></summary>

```kotlin
            val externalId: String = "",
            val locale: String = "en_IN",
            val submittedOnDate: String = DateHelper.formattedShortDate,
            val dateFormat: String = DateHelper.SHORT_MONTH,
            val clientId: String = template.clientId,
            val productId: Long = template.productOptions.first().id,
            val nominalAnnualInterestRate: Double = template.nominalAnnualInterestRate,
```

</details>

- `const val FULL_MONTH = "dd MM yyyy"` — /* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
- `const val SHORT_MONTH = "dd-MM-yyyy"` — /* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
- `const val MONTH_FORMAT = "dd MMMM"`
- `fun getDateAsString(integersOfDate: List<Int>): String` — the result string uses the list given in a reverse order ([x, y, z] results in "z y x")
- `val stringBuilder = StringBuilder()`
- `fun getDateAsString(integersOfDate: List<Long>, pattern: String): String`
- `fun getDateAsString(dateString: String): String` — Parses a date string with an auto-detected delimiter and returns it in "dd MMM yyyy" format.
- `val delimiters = listOf("-", "/", ".")`
- `val detectedDelimiter = delimiters.find { dateString.contains(it) }`
- `val dateComponents = dateString.split(detectedDelimiter).mapNotNull { it.toIntOrNull() }`
- `val (year, month, day) = dateComponents`
- `fun getSpecificFormat(format: String, dateString: String): String` — This Method converting the dd-MM-yyyy format type date string into dd MMMM yyyy
- `val pickerFormat = shortMonthFormat`
- `val finalFormat = LocalDateTime.Format { byUnicodePattern(format) }`
  _…more members; read the file._

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/DebugUtil.kt`

```kotlin
object DebugUtil
```
_No KDoc at source._

- `fun log(vararg objects: Any): Array<out Any>`
- `val stringToPrint = objects.joinToString(", ")`

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/ErrorBodyParser.kt`

```kotlin
typealias ErrorBodyParser = suspend (responseBody: String, statusCode: Int) -> String
```
Function type for parsing an error response body into a user-friendly message. Repositories provide custom parsing logic (e.g.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/KptErrorMapper.kt:105</code></summary>

```kotlin
    suspend fun <T> mapErrorBody(
        throwable: Throwable,
        errorBodyParser: ErrorBodyParser = defaultErrorBodyParser,
    ): ScreenState<T> {
        return when (throwable) {
            is HttpStatusException, is NetworkException, is IOException -> mapError(throwable)
            is ClientRequestException -> {
```

</details>

```kotlin
val defaultErrorBodyParser: ErrorBodyParser = { body, _ ->
```
Default error body parser that returns the response body as-is or a generic message.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/ErrorHandler.kt`

```kotlin
interface ErrorMessageProvider
```
Interface for providing localized error messages. Implement this interface in the UI module to provide string resources.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/org/mifospay/core/ui/ErrorMessageProviderImpl.kt:54</code></summary>

```kotlin
 * ```
 */
class ErrorMessageProviderImpl : ErrorMessageProvider {

    override fun getHttpErrorTitle(statusCode: Int): String {
        return getHttpErrorTitleSync(statusCode)
    }
```

</details>

- `fun getHttpErrorTitle(statusCode: Int): String`
- `fun getHttpErrorMessage(statusCode: Int, fallbackMessage: String?): String`
- `fun getNetworkErrorTitle(): String`
- `fun getNetworkErrorMessage(): String`
- `fun getGenericErrorTitle(): String`
- `fun getGenericErrorMessage(fallbackMessage: String?): String`

```kotlin
data class UiError(
```
A data class representing a user-facing error with title, message, and retry capability. This class is designed to be used directly in UI layers without any additional mapping.

<details><summary>Example</summary>

```kotlin
// In ViewModel
val uiError = UiError.from(exception, errorMessageProvider)

// In Composable
MifosDialogBox(
    showDialogState = error != null,
    title = error?.title ?: "",
    message = error?.message ?: "",
    confirmButtonText = if (error?.canRetry == true) "Retry" else "OK",
    dismissButtonText = if (error?.canRetry == true) "Cancel" else null,
    onConfirm = if (error?.canRetry == true) onRetry else onDismiss,
    onDismiss = onDismiss,
)
```

</details>

<details><summary>Used in the template — <code>feature/editpassword/src/commonMain/kotlin/org/mifospay/feature/editpassword/EditPasswordViewModel.kt:95</code></summary>

```kotlin

                    is SubmitState.Failed -> {
                        val uiError = submitState.error.toUiError(DefaultErrorMessageProvider)
                        mutableStateFlow.update {
                            it.copy(dialogState = EditPasswordDialog.ApiError(uiError))
                        }
                        submitPassword.reset()
```

</details>

```kotlin
fun Throwable.toUiError(provider: ErrorMessageProvider): UiError = UiError.from(this, provider)
```
Extension function to convert any `Throwable` to a `UiError`.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/Exceptions.kt`

```kotlin
class HttpStatusException(
```
Exception thrown when an HTTP request fails with a specific status code. Use this to differentiate between client errors (4xx) and server errors (5xx).

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/ErrorHandler.kt:66</code></summary>

```kotlin
        fun from(throwable: Throwable, provider: ErrorMessageProvider): UiError {
            return when (throwable) {
                is HttpStatusException -> UiError(
                    title = provider.getHttpErrorTitle(throwable.statusCode),
                    message = provider.getHttpErrorMessage(throwable.statusCode, throwable.message),
                    canRetry = throwable.canRetry,
                    errorCode = throwable.statusCode,
```

</details>

```kotlin
class NetworkException(message: String) : Exception(message)
```
Exception thrown when a network operation fails due to connectivity issues.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/ErrorHandler.kt:73</code></summary>

```kotlin
                )

                is NetworkException -> UiError(
                    title = provider.getNetworkErrorTitle(),
                    message = throwable.message ?: provider.getNetworkErrorMessage(),
                    canRetry = true,
                )
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/FileUtils.kt`

```kotlin
interface FileUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/FileUtils.android.kt:13</code></summary>

```kotlin

// JVM and Android implementation
actual fun createPlatformFileUtils(): FileUtils = CommonFileUtils()
```

</details>

- `suspend fun writeInputStreamDataToFile(inputStream: ByteArray, filePath: String): Boolean`
- `val logger = Logger.withTag("FileUtils")`

```kotlin
expect fun createPlatformFileUtils(): FileUtils
```
_No KDoc at source._

```kotlin
class CommonFileUtils : FileUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/FileUtils.android.kt:13</code></summary>

```kotlin

// JVM and Android implementation
actual fun createPlatformFileUtils(): FileUtils = CommonFileUtils()
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/GlobalAuthManager.kt`

```kotlin
object GlobalAuthManager
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/org/mifospay/core/network/utils/KtorInterceptor.kt:46</code></summary>

```kotlin
            scope.responsePipeline.intercept(HttpResponsePipeline.After) {
                if (context.response.status == HttpStatusCode.Unauthorized) {
                    GlobalAuthManager.markUnauthorized()
                }
                proceedWith(subject)
            }
        }
```

</details>

- `val isUnauthorized = MutableStateFlow(false)`
- `fun markUnauthorized()`
- `fun reset()`

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/KptErrorMapper.kt`

```kotlin
object KptErrorMapper
```
Central mapper — Throwable / feature error handlers → `ScreenState.Error` / `ScreenState.NoNetwork` / `ScreenState.Unauthenticated`.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/ScreenStateFlow.kt:39</code></summary>

```kotlin
    }
        .onStart { emit(ScreenState.Loading) }
        .catch { e -> emit(KptErrorMapper.mapError(e)) }

/**
 * Body-parsing overload — routes ktor 4xx/5xx exceptions through
 * [KptErrorMapper.mapErrorBody] so the server's Mifos-error JSON is
```

</details>

- `fun <T> mapError(` — Blocking (non-suspending) mapper — use when no response body is available or the caller has already parsed a user-facing message.
- `val status = throwable.response.status.value`
- `val status = throwable.response.status.value`
- `suspend fun <T> mapErrorBody(` — Suspending mapper — parses the ktor response body via `errorBodyParser` to extract a user-facing message when possible.
- `val status = throwable.response.status.value`
- `val body = readBodyOrEmpty(throwable)`
- `val parsed = errorBodyParser(body, status)`
- `val status = throwable.response.status.value`
- `val body = readBodyOrEmpty(throwable)`
- `val parsed = errorBodyParser(body, status)`

```kotlin
fun <T> Throwable.toScreenState(userMessage: String? = null): ScreenState<T> =
```
Convert a raw `Throwable` to a `ScreenState` via `KptErrorMapper`.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/MifosDispatchers.kt`

```kotlin
annotation class Dispatcher(val mifosDispatcher: MifosDispatchers)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/org/mifospay/feature/auth/login/LoginViewModelTest.kt:52</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()

    // `LoginUseCase` is a final class (the `@OpenForMokkery` all-open plugin is not
    // wired in this module), so it cannot be `mock()`ed. Build a REAL use-case over
    // mocked collaborators and stub the underlying repository calls instead.
```

</details>

```kotlin
enum class MifosDispatchers
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/org/mifospay/feature/mpay/qr/di/MpayQrModule.kt:25</code></summary>

```kotlin
            pocketRepository = get(),
            savedStateHandle = get(),
            ioDispatcher = get(named(MifosDispatchers.IO.name)),
        )
    }
}
```

</details>

```kotlin
annotation class ApplicationScope
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/di/DispatchersModule.kt:31</code></summary>

```kotlin
    single<CoroutineDispatcher>(named(MifosDispatchers.Default.name)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(MifosDispatchers.Unconfined.name)) { Dispatchers.Unconfined }
    single<CoroutineScope>(named("ApplicationScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/Parcelize.kt`

```kotlin
expect annotation class Parcelize()
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:19</code></summary>

```kotlin
import kotlinx.parcelize.TypeParceler

actual typealias Parcelize = Parcelize

actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel
```

</details>

```kotlin
expect interface Parcelable
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:21</code></summary>

```kotlin
actual typealias Parcelize = Parcelize

actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>
```

</details>

```kotlin
expect annotation class IgnoredOnParcel()
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:23</code></summary>

```kotlin
actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>
```

</details>

```kotlin
expect interface Parceler<P>
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:25</code></summary>

```kotlin
actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

- `fun create(parcel: Parcel): P`
- `fun P.write(parcel: Parcel, flags: Int)`

```kotlin
expect annotation class TypeParceler<T, P : Parceler<in T>>()
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:27</code></summary>

```kotlin
actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

```kotlin
expect class Parcel
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/org/mifospay/core/common/Parcelize.kt:29</code></summary>

```kotlin
actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/ScreenState.kt`

```kotlin
sealed interface ScreenState<out T>
```
Unified UI state produced by fork repositories (and, once Phase 4 lands, by `Store<Key, Output>.asScreenStream(...)`). Replaces the legacy `DataState` sealed class as the single fork-wide read-side model.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountViewModel.kt:76</code></summary>

```kotlin
            } else {
                flowOf(
                    ScreenState.Error(IllegalStateException("Client ID not available")) as ScreenState<org.mifospay.core.model.account.AccountContent>,
                )
            }
        }
        .mapLatest { screenState ->
```

</details>

```kotlin
val <T> ScreenState<T>.dataOrNull: T?
```
Extracts data from `ScreenState.Content` or null for other states.

```kotlin
val ScreenState<*>.isTerminal: Boolean
```
`true` when the underlying state is one of the terminal loaded variants.

```kotlin
val ScreenState<*>.hasData: Boolean
```
`true` when the underlying state carries no data (any error/loading/empty variant).

```kotlin
val ScreenState<*>.errorOrNull: Throwable?
```
Extract the wrapped throwable when the state is `ScreenState.Error`.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/ScreenStateFlow.kt`

```kotlin
typealias ScreenStateStream<T> = Flow<ScreenState<T>>
```
Fork-wide alias for `Flow<ScreenState<T>>` — the read-side stream shape every Phase-3 repository exposes to its ViewModels.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/org/mifospay/core/data/repository/AssetRepository.kt:15</code></summary>

```kotlin

interface AssetRepository {
    fun getCountriesWithStates(): ScreenStateStream<Map<String, List<String>>>
}
```

</details>

```kotlin
fun <T> Flow<T>.asScreenStateFlow(
```
Convert a `Flow<T>` (typically the raw Ktorfit API flow) into a `Flow<ScreenState<T>>`, emitting `ScreenState` variants and routing terminal errors through `KptErrorMapper`.

```kotlin
fun <T> Flow<T>.asScreenStateFlow(
```
Body-parsing overload — routes ktor 4xx/5xx exceptions through `KptErrorMapper.mapErrorBody` so the server's Mifos-error JSON is lifted into `HttpStatusException.userMessage`.

```kotlin
fun <T, R> Flow<ScreenState<T>>.mapContent(transform: (T) -> R): Flow<ScreenState<R>> =
```
Extension on `Flow<ScreenState<T>>` mirroring the template's `mapContent { data, freshness -> R }` — the freshness slot is dropped because Phase-3 lacks the FreshnessSignal engine. Phase-4 replaces this with the template's two-arg lambda.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:108</code></summary>

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
Template-parity `emptyIfContent` — flips `ScreenState.Content` whose data satisfies `predicate` to `ScreenState.Empty`. Applied AFTER the upstream repository has already emitted; use for business-level empty (e.g. `list.isEmpty()`).

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/org/mifospay/feature/accounts/AccountsScreen.kt:108</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/StringProvider.kt`

```kotlin
interface StringProvider
```
Provides an abstraction layer for accessing localized string resources.

<details><summary>Example</summary>

```kotlin
Method getSystem in android.content.res.Resources not mocked.
val message = stringProvider.get(Res.string.feature_auth_error_email_required)
```

</details>

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/org/mifospay/feature/auth/signup/SignupViewModel.kt:78</code></summary>

```kotlin
    private val clientRepository: ClientRepository,
    private val assetRepository: AssetRepository,
    private val stringProvider: StringProvider,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: SignUpState(),
) {
```

</details>

- `suspend fun get(resource: StringResource, vararg formatArgs: Any = emptyArray()): String` — Resolves the given `StringResource` into a localized string.

```kotlin
class DefaultStringProvider : StringProvider
```
Default implementation of `StringProvider` that uses Compose Multiplatform’s `getString`. This class is meant to be used in runtime environments where resource resolution is supported.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/di/DispatchersModule.kt:24</code></summary>

```kotlin

val stringProviderModule = module {
    single<StringProvider> { DefaultStringProvider() }
}

val DispatchersModule = module {
    includes(ioDispatcherModule)
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/StringResourceSerializer.kt`

```kotlin
object StringResourceSerializer : KSerializer<StringResource>
```
A custom `KSerializer` for `StringResource` that enables serialization of StringResource objects.

<details><summary>Example</summary>

```kotlin
@Serializable
data class DialogState(
    @Serializable(with = StringResourceSerializer::class)
    val message: StringResource
)
```

</details>

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/org/mifospay/feature/profile/edit/EditProfileViewModel.kt:402</code></summary>

```kotlin
            @Serializable
            data class ResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
            ) : Error()
        }
    }
```

</details>

- `val resourceKey = extractResourceKey(value)`
- `val resourceKey = decoder.decodeString()`

```kotlin
object StringResourceRegistry
```
A registry that maps resource keys to their corresponding StringResource objects. Used during deserialization to reconstruct StringResource objects from their serialized resource keys.

- `fun register(resourceKey: String, stringResource: StringResource)`
- `fun getResource(resourceKey: String): StringResource?`

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/di/DispatchersModule.kt`

```kotlin
val stringProviderModule = module
```
_No KDoc at source._

```kotlin
val DispatchersModule = module
```
_No KDoc at source._

```kotlin
expect val ioDispatcherModule: Module
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/dialogManager/DialogManager.kt`

```kotlin
object DialogManager
```
A singleton for managing dialog state across the app. It works with `DialogMessage` to determine what kind of dialog should be shown. This is typically observed in the UI to display a loading spinner or error message.

- `val dialogMessage: StateFlow<DialogMessage> = _dialogMessage.asStateFlow()` — Public read-only dialog message flow. UI layers should collect this to react to dialog state changes.
- `fun dismissDialog()` — Dismisses any currently shown dialog.
- `fun showLoading()` — Shows a loading dialog.
- `fun showMessage(message: String)` — Shows a plain string message in a dialog.
- `fun showMessage(message: StringResource)` — Shows a localized string resource message in a dialog.
- `fun showMessage(message: DialogMessage)` — Allows setting any `DialogMessage` manually.

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/dialogManager/DialogMessage.kt`

```kotlin
sealed interface DialogMessage
```
Represents the possible types of dialog messages shown in the application. Used in combination with `DialogManager` to control dialog visibility and content.

<details><summary>Used in the template — <code>core/common/src/commonMain/kotlin/org/mifospay/core/common/dialogManager/DialogManager.kt:25</code></summary>

```kotlin
object DialogManager {

    private val _dialogMessage = MutableStateFlow<DialogMessage>(DialogMessage.None)

    /**
     * Public read-only dialog message flow.
     * UI layers should collect this to react to dialog state changes.
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/utils/ListExtensions.kt`

```kotlin
fun formatAsBulletPoints(lines: List<String>): String
```
Formats a list of strings into a bullet-pointed multiline string. Each non-blank line is trimmed and prefixed with a Unicode bullet (•) followed by a space. Empty or whitespace-only strings are ignored.

<details><summary>Example</summary>

```kotlin
val feedback = listOf(
    "The password must be at least 12 characters long",
    "Include at least one number"
)

val result = formatAsBulletPoints(feedback)
println(result)
• The password must be at least 12 characters long
• Include at least one number
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/utils/OpenForMokkery.kt`

```kotlin
annotation class OpenForMokkery
```
Annotation to mark a class as open for mocking during tests using Mokkery.

<details><summary>Example</summary>

```kotlin
@OpenForMokkery
class SomeService {
    fun doWork() = ...
}
plugins {
    kotlin("plugin.allopen")
}

allOpen {
    annotation("your.package.OpenForMokkery")
}
```

</details>

<details><summary>Used in the template — <code>core/domain/src/commonMain/kotlin/org/mifospay/core/domain/LoginUseCase.kt:20</code></summary>

```kotlin
import org.mifospay.core.model.user.UserInfo

@OpenForMokkery
class LoginUseCase(
    private val repository: AuthenticationRepository,
    private val clientRepository: ClientRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
```

</details>

### `core/common/src/commonMain/kotlin/org/mifospay/core/common/utils/StringExtensions.kt`

```kotlin
fun String.isValidEmail(): Boolean = contains("@")
```
Whether or not string is a valid email address. This just checks if the string contains the "@" symbol.

```kotlin
fun maskString(input: String, maskChar: Char = '*'): String
```
_No KDoc at source._

```kotlin
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it ->
```
_No KDoc at source._

```kotlin
fun String.hasSpaces(): Boolean
```
_No KDoc at source._

```kotlin
fun String.hasConsecutiveRepetitions(): Boolean
```
_No KDoc at source._

---

_33 type(s), 101 function(s)/property(ies); 51 carry KDoc at source; 7 authored example(s); 32 live call site(s)._
<!-- api-docs:end -->
