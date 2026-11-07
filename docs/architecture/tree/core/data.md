# `core/data`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATA.md`
> **Measured:** 126 Kotlin files, 9 test files

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

<!-- api-docs:begin module=core/data sha=c250ebda7347b9cbbfa5b180368e05291a070ead -->
## API reference

_Generated from `core/data` at tree `c250ebda7347` by `scripts/docs/api-docs-gen.sh`._
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

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:136</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/di/PlatformDependentDataModule.kt`

```kotlin
interface PlatformDependentDataModule
```
Platform-specific data dependencies (network and time-zone monitors) supplied by each target.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/fork/di/AndroidPlatformDependentDataModule.kt:24</code></summary>

```kotlin
    private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope,
) : PlatformDependentDataModule {
    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityManagerNetworkMonitor(context, dispatcher)
    }
```

</details>

- `val networkMonitor: NetworkMonitor` — Monitor that reports whether the device currently has network connectivity.
- `val timeZoneMonitor: TimeZoneMonitor` — Monitor that emits the device's current time zone.

```kotlin
expect val platformModule: Module
```
Platform-specific Koin module with the monitors and bindings each target needs.

```kotlin
expect val getPlatformDataModule: PlatformDependentDataModule
```
Platform-specific `PlatformDependentDataModule` instance, resolved per target.

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/di/RepositoryModule.kt`

```kotlin
val RepositoryModule = module
```
Koin module for the shared data-layer dependencies that are not repositories (the repositories bind themselves through `@RepositoryBinding`).

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt:133</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/AppLockOption.kt`

```kotlin
enum class AppLockOption
```
How the app is locked on launch.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/fork/util/Helpers.kt:22</code></summary>

```kotlin

    /** Converts an [AppLockOption] to the string stored in preferences. */
    fun authOptionToStringMapperFunction(option: AppLockOption): String {
        return when (option) {
            AppLockOption.Passcode -> PASSCODE_LOCK_VALUE
            AppLockOption.DeviceLock -> DEVICE_AUTHENTICATION_METHOD_VALUE
            AppLockOption.None -> ""
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/AutoPayValidator.kt`

```kotlin
object AutoPayValidator
```
Client-side validation of auto-pay payloads before they are submitted.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt:153</code></summary>

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

- `fun validateAutoPayPayload(payload: AutoPayPayload): ValidationResult` — Validates a new auto-pay payload and returns the first violation, if any.
- `val startDate = parseDate(payload.validFrom)`
- `val endDate = parseDate(payload.validTill)`
- `fun validateAutoPayUpdatePayload(payload: AutoPayUpdatePayload): ValidationResult` — Validates an auto-pay update payload and returns the first violation, if any.
- `val startDate = parseDate(payload.validFrom!!)`
- `val endDate = parseDate(payload.validTill!!)`
- `val validFrequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")`

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/BillErrorHandler.kt`

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/BillValidator.kt`

```kotlin
object BillValidator
```
Utility class for validating bill data before submission

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt:150</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/BillerValidator.kt`

```kotlin
object BillerValidator
```
Validator for biller form data with comprehensive validation rules.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerViewModel.kt:127</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/Constants.kt`

```kotlin
object Constants
```
Shared string and numeric constants used by the data layer.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModel.kt:193</code></summary>

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

- `const val BASIC = "Basic "` — Prefix of the HTTP Basic authorization header value.
- `const val SAVINGS = "savingsAccounts"` — Resource name for savings accounts.
- `const val TRANSACTIONS = "transactions"` — Resource name for transactions.
- `const val WALLET_ACCOUNT_SAVINGS_PRODUCT_ID = 1` — Savings product id of the default wallet account.
- `const val MIFOS_MERCHANT_SAVINGS_PRODUCT_ID = 165 // 372` — Savings product id used for merchant accounts.
- `const val MIFOS_CONSUMER_SAVINGS_PRODUCT_ID = 165 // 373` — Savings product id used for consumer accounts.
- `val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)` — Role ids assigned to newly created users: the mobile-wallet role and the super-user role.
- `const val ENTITY_TYPE_CLIENTS = "clients"` — Entity type name for clients.
- `const val FETCH_ACCOUNT_TRANSFER_USECASE = "FetchAccountTransfer"` — Use-case name used when fetching an account transfer.
- `const val NO_SAVED_CARDS = "No saved cards."` — Message shown when the client has no saved cards.
- `const val MIFOS_PASSWORD = "mifos:password"` — Default password for new mifos users.
- `const val ERROR_VERIFYING_USER = "Error verifying user"` — Error message shown when verifying a user fails.
- `const val ERROR_REGISTERING_USER = "Error registering user"` — Error message shown when registering a user fails.
- `const val NO_CLIENTS_FOUND = "No clients found"` — Message shown when no clients are found.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/FineractErrorParser.kt`

```kotlin
val parseFineractError: ErrorBodyParser = { responseBody, statusCode ->
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
fun extractFineractErrorMessage(responseBody: String, statusCode: Int): String
```
Extracts a user-friendly error message from a Mifos/Fineract error response. Priority order: 1. First error's defaultUserMessage (for validation errors) 2. Root level defaultUserMessage 3. Root level developerMessage 4.

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/Helpers.kt`

```kotlin
object Helpers
```
Helpers for persisting `AppLockOption` values as strings.

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

- `fun authOptionToStringMapperFunction(option: AppLockOption): String` — Converts an `AppLockOption` to the string stored in preferences.
- `fun stringToAuthOptionMapperFunction(option: String): AppLockOption` — Converts a stored string value back to an `AppLockOption`, defaulting to `AppLockOption.None`.

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/MpayQrCodeProcessor.kt`

```kotlin
object MpayQrCodeProcessor
```
MPay QR Code Encoder and Decoder with validation.

<details><summary>Example</summary>

```kotlin
mpay://pay?qt=0&fsp=mifos-bank-1&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2&ae=EXT123
```

</details>

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayViewModel.kt:76</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/NetworkMonitor.kt`

```kotlin
interface NetworkMonitor
```
Utility for reporting app connectivity status

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

- `val isOnline: Flow<Boolean>` — Emits true while the device has network connectivity.

```kotlin
val ForkNetworkMonitorQualifier = named("kpt.core.data.fork.util.NetworkMonitor")
```
Koin qualifier for this `NetworkMonitor`.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/fork/di/AndroidPlatformModule.kt:29</code></summary>

```kotlin
    // `NetworkMonitorProvider.install()` (cmp-network-monitor 3.6.0+, which carries the
    // AndroidNetworkMonitor cold-start seed fix) — no per-platform override needed here.
    single<NetworkMonitor>(ForkNetworkMonitorQualifier) {
        ConnectivityManagerNetworkMonitor(androidContext(), get(named(ForkDispatchers.IO.name)))
    }

    single<TimeZoneMonitor>(ForkTimeZoneMonitorQualifier) {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/QrTransferRouter.kt`

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

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrViewModel.kt:139</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/ScreenStateBridge.kt`

```kotlin
fun <T> KptScreenState<T>.toForkScreenState(): ForkScreenState<T> = when (this)
```
Bridge extension: converts a single `kpt.core.base.store.screen.ScreenState<T>` (emitted by Store5 `asScreenStream(...)`) into the fork's `kpt.core.common.ScreenState<T>`.

```kotlin
fun <T> Flow<KptScreenState<T>>.toForkScreenStateFlow(): Flow<ForkScreenState<T>> =
```
Flow-level convenience wrapper — every Store5 impl calls `store.asScreenStream(...).state.toForkScreenStateFlow()` to satisfy the fork repository return type.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:108</code></summary>

```kotlin
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/StandardUpiQrCodeProcessor.kt`

```kotlin
object StandardUpiQrCodeProcessor
```
Standard UPI QR Code Processor Handles parsing of standard UPI QR codes according to UPI specification

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsViewModel.kt:34</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/TimeZoneMonitor.kt`

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

- `val currentTimeZone: Flow<TimeZone>` — Emits the device's current time zone, then each change.

```kotlin
val ForkTimeZoneMonitorQualifier = named("kpt.core.data.fork.util.TimeZoneMonitor")
```
Koin qualifier for this `TimeZoneMonitor`.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/fork/di/AndroidPlatformModule.kt:33</code></summary>

```kotlin
    }

    single<TimeZoneMonitor>(ForkTimeZoneMonitorQualifier) {
        TimeZoneBroadcastMonitor(
            context = androidContext(),
            appScope = get(named("ApplicationScope")),
            ioDispatcher = get(named(ForkDispatchers.IO.name)),
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/fork/util/UpiQrCodeProcessor.kt`

```kotlin
object UpiQrCodeProcessor
```
UPI QR Code Encoder and Decoder with validation

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyViewModel.kt:190</code></summary>

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

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/AccountMapper.kt`

```kotlin
fun ClientAccountsEntity.toAccount(): List<Account>
```
Maps the savings accounts of a client's accounts response to domain `Account`s.

```kotlin
fun List<SavingAccountEntity>.toAccount(): List<Account>
```
Maps a list of savings account entities to domain `Account`s.

```kotlin
fun NetworkAccountType.toModelAccountType(): AccountType
```
Maps a network account type to the domain `AccountType`.

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
Maps a client network entity to the domain `Client`.

```kotlin
fun List<ClientEntity>.toModel(): List<Client> = map { it.toModel() }
```
Maps a list of client entities to domain `Client`s.

```kotlin
fun Page<ClientEntity>.toModel(): Page<Client>
```
Maps a page of client entities to a page of domain `Client`s.

```kotlin
fun NewClient.toEntity(): NewClientEntity
```
Maps a domain `NewClient` to the create-client network entity.

```kotlin
fun ClientAddress.toEntity(): Address
```
Maps a domain `ClientAddress` to the network `Address` entity.

```kotlin
fun Status.toModel(): ClientStatus
```
Maps a network client `Status` to the domain `ClientStatus`.

```kotlin
fun ClientTimelineEntity.toModel(): ClientTimeline
```
Maps a client timeline entity to the domain `ClientTimeline`.

```kotlin
fun UpdatedClient.toEntity(): UpdateClientEntity
```
Maps a domain `UpdatedClient` to the update-client network entity.

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/SavingAccountMapper.kt`

```kotlin
fun SavingAccountEntity.toModel(): SavingAccount
```
Maps a savings account network entity to the domain `SavingAccount`.

```kotlin
fun SavingsWithAssociationsEntity.toSavingDetail(): SavingAccountDetail
```
Maps a savings-with-associations entity to a `SavingAccountDetail` including its transactions.

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/SearchedEntitiesMapper.kt`

```kotlin
fun SearchedEntity.toModel(): SearchResult
```
Maps a searched entity to a domain `SearchResult`.

```kotlin
fun List<SearchedEntity>.toSearchResult(): List<SearchResult> = map { it.toModel() }
```
Maps a list of searched entities to domain `SearchResult`s.

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/TransactionMapper.kt`

```kotlin
fun SavingsWithAssociationsEntity.toTransactionList(): List<Transaction>
```
Extracts the transactions of a savings-with-associations entity as domain `Transaction`s.

```kotlin
fun TransactionsEntity.toModel(): Transaction
```
Maps a transactions network entity to a domain `Transaction`.

```kotlin
fun PaymentType.toModalUi(): Transaction.PaymentType
```
Maps a network `PaymentType` to the domain `Transaction.PaymentType`.

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/UserMapper.kt`

```kotlin
val NEW_USER_ROLE_IDS: ArrayList<Int> = arrayListOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)
```
Role ids assigned to newly created users: the mobile-wallet role and the super-user role.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/fork/util/Constants.kt:35</code></summary>

```kotlin

    /** Role ids assigned to newly created users: the mobile-wallet role and the super-user role. */
    val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)

    /** Entity type name for clients. */
    const val ENTITY_TYPE_CLIENTS = "clients"
```

</details>

```kotlin
fun NewUser.toEntity(): NewUserEntity
```
Maps a domain `NewUser` to the create-user network entity.

```kotlin
fun User.toUserInfo() = UserInfo(
```
Maps a `User` to a `UserInfo`.

```kotlin
fun Role.toRoleInfo() = RoleInfo(
```
Maps a `Role` to a `RoleInfo`.

### `core/data/src/commonMain/kotlin/kpt/core/data/mapper/pocket/PocketAccountMapper.kt`

```kotlin
fun PocketResponseDto.toDomainList(): List<PocketAccount>
```
Flattens a pocket response (loan, savings and share accounts) into a list of domain `PocketAccount`s.

```kotlin
fun kpt.core.model.network.entity.loanAccount.LoanStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
Maps a loan `LoanStatusResponseDto` to a pocket `AccountStatus`.

```kotlin
fun kpt.core.model.savingsaccount.Status.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
Maps a savings account `Status` to a pocket `AccountStatus`.

```kotlin
fun kpt.core.model.network.entity.shareAccount.ShareStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
```
Maps a share account `ShareStatusResponseDto` to a pocket `AccountStatus`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AccountRepository.kt`

```kotlin
interface AccountRepository
```
Repository for account transfers, transactions and account lookups.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt:42</code></summary>

```kotlin
    private val repository: SelfServiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val accountRepository: AccountRepository,
) : BaseViewModel<HistoryState, HistoryEvent, HistoryAction>(
    initialState = run {
        /** Id of the signed-in client, read from preferences. */
        val clientId = requireNotNull(preferencesRepository.clientId.value)
```

</details>

- `fun getTransaction(accountId: Long, transactionId: Long): Flow<ScreenState<Transaction>>` — Streams a single transaction of an account.
- `fun getAccountTransfer(transferId: Long): Flow<ScreenState<TransferDetail>>` — Streams the details of the account transfer identified by `transferId`.
- `fun getAccountTransferStream(` — transfer-detail Store5 vertical — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>>` — Streams accounts matching the search `query`.
- `fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>` — Streams the self-service accounts of the given client.
- `fun getSelfAccountsStream(` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `suspend fun makeTransfer(payload: AccountTransferPayload)` — Submits an account-to-account transfer.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AppLockRepository.kt`

```kotlin
interface AppLockRepository
```
App-wide lock flag, persisted across process restarts.

<details><summary>Used in the template — <code>feature/passcode/src/commonMain/kotlin/kpt/feature/passcode/PasscodeViewModel.kt:51</code></summary>

```kotlin
class PasscodeViewModel(
    private val passcodeManager: PasscodeManager,
    private val appLockRepository: AppLockRepository,
) : BaseViewModel<PasscodeState, PasscodeEvent, PasscodeAction>(
    initialState = PasscodeState(),
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
Repository for static geographic reference data.

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

- `fun getCountriesWithStates(): ScreenStateStream<Map<String, List<String>>>` — Streams the supported countries mapped to their states.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AuthenticationRepository.kt`

```kotlin
interface AuthenticationRepository
```
Repository for user authentication.

<details><summary>Used in the template — <code>feature/auth/src/commonTest/kotlin/kpt/feature/auth/login/LoginViewModelTest.kt:57</code></summary>

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

- `suspend fun authenticate(username: String, password: String): UserInfo` — Authenticates with the given credentials and returns the signed-in `UserInfo`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/AutoPayHistoryRepository.kt`

```kotlin
interface AutoPayHistoryRepository
```
Repository for the execution history of auto-pay schedules.

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
Aggregate execution counts and amounts of auto-pay history.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryViewModel.kt:555</code></summary>

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
Repository for managing auto-pay (scheduled recurring payment) schedules.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt:32</code></summary>

```kotlin
import kpt.core.network.FineractApiManager
/** Default [AutoPayRepository]. */
@RepositoryBinding(binds = AutoPayRepository::class)
class AutoPayRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : AutoPayRepository {
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
Aggregate figures for a client's auto-pay schedules.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt:130</code></summary>

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
Repository for the beneficiaries a client can transfer to.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BeneficiaryRepositoryImpl.kt:27</code></summary>

```kotlin
import kpt.core.network.SelfServiceApiManager
/** Default [BeneficiaryRepository] backed by the beneficiary API. */
@RepositoryBinding(binds = BeneficiaryRepository::class)
class BeneficiaryRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : BeneficiaryRepository {
```

</details>

- `suspend fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>` — Streams the client's saved beneficiaries.
- `suspend fun getBeneficiaryTemplate(): Flow<ScreenState<BeneficiaryTemplate>>` — Streams the template used to create or update beneficiaries.
- `suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)` — Creates a new beneficiary from the payload.
- `suspend fun updateBeneficiary(` — Updates an existing beneficiary.
- `suspend fun deleteBeneficiary(beneficiaryId: Long)` — Deletes the beneficiary with the given id.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/BillRepository.kt`

```kotlin
interface BillRepository
```
Repository for bills and their statuses.

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
Aggregate figures for a client's bills.

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
Repository for the billers a wallet user can pay bills to.

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

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/ClientRepository.kt`

```kotlin
interface ClientRepository
```
Repository for clients, their profile images and accounts.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:76</code></summary>

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

- `fun getClientInfo(clientId: Long): Flow<ScreenState<Client>>` — Streams the details of the given client.
- `fun getClientInfoStream(` — Phase-5 Batch-2 **SINGLE-ROW-PER-KEY read** for the `clientDetail` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<Client>>` consumed through the `clientDetail` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + single-row upsert writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default). Distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getClientInfo` path uses; both signatures return `Flow<ScreenState<Client>>` so a consumer can swap between them without any downstream shape change. ### Write path (GOAL D1) This is a READ path. Profile mutation writes (`updateClient`, `updateClientImage`) continue to flow through their existing online paths and appear here on the next refresh cycle. `getClientImage` remains on the transitional shim — its blob-URL caching story is out of scope for this batch.
- `suspend fun getClients(): Flow<ScreenState<Page<Client>>>` — Streams the page of clients visible to the signed-in user.
- `suspend fun getClient(clientId: Long): Client` — Fetches a single client once.
- `suspend fun updateClient(clientId: Long, client: UpdatedClient)` — Updates the client's details.
- `fun getClientImage(clientId: Long): Flow<ScreenState<String>>` — Streams the client's profile image.
- `suspend fun updateClientImage(clientId: Long, image: String)` — Uploads a new profile image for the client.
- `suspend fun getClientAccounts(clientId: Long): ClientAccountsEntity` — Fetches the savings accounts of a client.
- `suspend fun getAccounts(clientId: Long, accountType: String): Flow<ScreenState<List<Account>>>` — Streams the client's accounts of the given account type.
- `suspend fun createClient(newClient: NewClient): Int` — Creates a client and returns the new client id.
- `suspend fun deleteClient(clientId: Int): Int` — Deletes the client with the given id and returns the server response code.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/DocumentRepository.kt`

```kotlin
interface DocumentRepository
```
Repository for documents attached to wallet entities such as clients.

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

- `suspend fun getDocuments(entityType: String, entityId: Int): ScreenStateStream<List<Document>>` — Streams the documents attached to the given entity.
- `suspend fun createDocument(` — Uploads a new document for an entity.
- `suspend fun createDocument(` — Uploads a new document for an entity from a file.
- `suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Document>` — Downloads a document of the given entity.
- `suspend fun deleteDocument(entityType: String, entityId: Int, documentId: Int): ScreenStateStream<Unit>` — Deletes a document of the given entity.
- `suspend fun updateDocument(` — Updates an existing document.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/InterBankRepository.kt`

```kotlin
interface InterBankRepository
```
Repository for inter-bank (cross-institution) transfers.

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt:73</code></summary>

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

- `suspend fun fetchParticipant(` — Fetches the participant that owns the given identifier.
- `suspend fun fetchPartyInfo(` — Fetches party information for an inter-bank transfer.
- `fun findParticipant(` — Finds a participant for the given identifier.
- `suspend fun interBankMakeTransfer(` — Executes an inter-bank transfer.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/InvoiceRepository.kt`

```kotlin
interface InvoiceRepository
```
Repository for client invoices.

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoicesViewModel.kt:28</code></summary>

```kotlin
/** ViewModel that loads the current client's invoices and emits navigation to invoice detail. */
class InvoicesViewModel(
    invoiceRepository: InvoiceRepository,
    repository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<InvoiceState, InvoiceEvent, InvoiceAction>(
    initialState = savedStateHandle.get(KEY_STATE) ?: run {
```

</details>

- `fun getInvoice(clientId: Long, invoiceId: Long): Flow<ScreenState<Invoice>>` — Streams a single invoice of the given client.
- `fun getInvoices(clientId: Long): Flow<ScreenState<List<Invoice>>>` — Streams the invoices of the given client.
- `fun getInvoicesStream(` — Phase-5 Batch-2 **LEDGER read** for the `invoice` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Invoice>>>` consumed through the `invoice` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.invoice.InvoiceDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getInvoices` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Write path (GOAL D1) This is a READ path. Invoice CRUD writes (`createInvoice`, `updateInvoice`, `deleteInvoice`) continue to flow through their existing online paths and appear here on the next refresh cycle — the LEDGER cache is server-truth.
- `suspend fun createInvoice(clientId: Long, invoice: InvoiceEntity)` — Creates an invoice for the given client.
- `suspend fun updateInvoice(` — Updates an invoice of the given client.
- `suspend fun deleteInvoice(clientId: Long, invoiceId: Long)` — Deletes an invoice of the given client.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/KycLevelRepository.kt`

```kotlin
interface KycLevelRepository
```
Repository for KYC (know-your-customer) level 1 details.

<details><summary>Used in the template — <code>feature/kyc/src/commonMain/kotlin/kpt/feature/kyc/KYCDescriptionViewModel.kt:32</code></summary>

```kotlin
class KYCDescriptionViewModel(
    private val repository: ForkPreferencesRepository,
    kycLevelRepository: KycLevelRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycState, KycEvent, KycAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        /** Id of the logged-in client, read from the stored fork preferences. */
```

</details>

- `fun fetchKYCLevel1Details(clientId: Long): ScreenStateStream<KYCLevel1Details?>` — Streams the client's KYC level 1 details, or null when none exist.
- `suspend fun addKYCLevel1Details(` — Adds KYC level 1 details for the client.
- `suspend fun updateKYCLevel1Details(` — Updates the client's KYC level 1 details.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/LocalAssetRepository.kt`

```kotlin
interface LocalAssetRepository
```
Repository for static reference data bundled with the app.

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

- `val localeList: StateFlow<List<Locale>>` — Locales available in the app.
- `val currencyList: StateFlow<List<CurrencyCode>>` — Currency codes available in the app.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/NotificationRepository.kt`

```kotlin
interface NotificationRepository
```
Repository for user notifications.

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

- `fun fetchNotifications(): Flow<ScreenState<List<Notification>>>` — Streams the user's notifications.
- `fun fetchNotificationsStream(` — Phase-5 Batch-2 **LEDGER read** for the `notification` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Notification>>>` consumed through the `notification` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.notification.NotificationDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `fetchNotifications` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Cache-key scoping The Fineract `notifications/?isRead=true` endpoint returns the list for the authenticated session (no `clientId` parameter). The `clientId` here is used purely for CACHE SCOPING — the store key partitions the Room SoT so a logout-then-login as a different client never surfaces the previous user's cached list. Returns the native `ScreenDataStream` (template idiom) — the ViewModel holds it directly and exposes `stream.state` to the Screen (rendered by `core-base/ui` `ScreenContent`) and `stream.refresh()` for pull-to-refresh / retry. No fork-`ScreenState` bridge, no per-ViewModel `when` fold — `DecisionEngine` inside the stream pre-decides every render branch.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/OfficeRepository.kt`

```kotlin
interface OfficeRepository
```
Repository for Fineract offices.

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

- `fun getOffices(): ScreenStateStream<List<Office>>` — Streams the list of offices.
- `fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>>` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/PocketRepository.kt`

```kotlin
interface PocketRepository
```
Repository for pocket accounts, which group a client's loan, savings and share accounts.

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

- `fun getLinkedPocketAccountsStream(` — Streams the accounts currently linked to the client's pocket.
- `fun getDetailedPocketAccountsStream(` — Phase-5 Batch-2 **LEDGER read** for the `pocket` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<DetailedPocketAccount>>>` consumed through the `pocket` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.pocket.PocketDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Migration from the in-memory cache This method REPLACES the pre-store `getDetailedPocketAccounts(clientId, forceRefresh): Flow<DataState<List<DetailedPocketAccount>>>` that was Phase-4-deferred out of a Store5 migration. The `forceRefresh` parameter is gone — callers refresh by RE-SUBSCRIBING (i.e. wrapping this call in a `MutableSharedFlow<Unit>().flatMapLatest { ... }` in the VM, the same pattern `BeneficiaryListViewModel` uses). The in-memory `MutableStateFlow<DataState<...>>` cache in `PocketRepositoryImp` is REMOVED — Room is the SoT. ### Write path (GOAL D1) This is a READ path. Pocket CRUD writes (`linkAccounts`, `delinkAccounts`) continue to flow through their existing online paths and appear here on the next refresh cycle. The pre-store optimistic in-memory cache update inside those writes is REMOVED — the momentary "already-updated" impression is replaced by an SWR / manual re-subscribe pull through this store.
- `fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>>` — Observes the client's linked pocket accounts from the local cache.
- `fun getAvailableAccountsToLinkStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<List<LinkableAccount>>` — Streams the accounts the client can link to a pocket.
- `suspend fun linkAccounts(` — Links accounts to the client's pocket.
- `suspend fun delinkAccounts(` — Delinks accounts from the client's pocket.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/RecentPayeeRepository.kt`

```kotlin
interface RecentPayeeRepository
```
Repository for fetching recent paid beneficiaries. Derives recent payees from transaction history by: 1. Fetching DEBIT transactions from savings account 2. Getting transfer details for each transaction 3.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubViewModel.kt:35</code></summary>

```kotlin
 */
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
Repository for user registration and verification.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RegistrationRepositoryImpl.kt:21</code></summary>

```kotlin
import kpt.core.network.FineractApiManager
/** Default [RegistrationRepository] backed by the registration API. */
@RepositoryBinding(binds = RegistrationRepository::class)
class RegistrationRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : RegistrationRepository {
```

</details>

- `suspend fun registerUser(registerPayload: RegisterPayload)` — Registers a new user.
- `suspend fun verifyUser(userVerify: UserVerify)` — Verifies a newly registered user.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/RunReportRepository.kt`

```kotlin
interface RunReportRepository
```
Repository for running Fineract reports.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RunReportRepositoryImpl.kt:24</code></summary>

```kotlin
import kpt.core.network.FineractApiManager
/** Default [RunReportRepository] backed by the run-report API. */
@RepositoryBinding(binds = RunReportRepository::class)
class RunReportRepositoryImpl(
    private val apiManager: FineractApiManager,
    @FromQualifier("IO") private val ioDispatcher: CoroutineDispatcher,
) : RunReportRepository {
```

</details>

- `suspend fun getTransactionReceipt(` — Fetches a transaction receipt by running a report.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SavedCardRepository.kt`

```kotlin
interface SavedCardRepository
```
Repository for a client's saved payment cards.

<details><summary>Used in the template — <code>feature/savedcards/src/commonMain/kotlin/kpt/feature/savedcards/CardsScreenViewModel.kt:42</code></summary>

```kotlin
 */
class CardsScreenViewModel(
    private val repository: SavedCardRepository,
    userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CardState, CardEvent, CardAction>(
    initialState = savedStateHandle.getSerialized(key = KEY_STATE) ?: run {
```

</details>

- `fun getSavedCards(clientId: Long): Flow<ScreenState<List<SavedCard>>>` — Streams the client's saved cards.
- `fun getSavedCardsStream(` — Phase-5 Batch-1 **LEDGER read** for the `savedCards` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<SavedCard>>>` consumed through the `savedCards` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.savedcards.SavedCardDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getSavedCards` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) — subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge. ### Write path (GOAL D1) This is a READ path. Card CRUD writes (`addSavedCard`, `updateCard`, `deleteCard`) continue to flow through their existing online paths and appear here on the next refresh cycle — the LEDGER cache is server-truth.
- `fun getSavedCard(clientId: Long, cardId: Long): Flow<ScreenState<SavedCard>>` — Streams a single saved card of the client.
- `suspend fun addSavedCard(clientId: Long, card: CardPayload)` — Adds a saved card for the client.
- `suspend fun deleteCard(clientId: Long, cardId: Long)` — Deletes a saved card.
- `suspend fun updateCard(clientId: Long, cardId: Long, card: CardPayload)` — Updates a saved card.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SavingsAccountRepository.kt`

```kotlin
interface SavingsAccountRepository
```
Repository for savings accounts and their transactions.

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

- `suspend fun getSavingsAccounts(limit: Int): ScreenStateStream<Page<SavingsWithAssociationsEntity>>` — Fetches savings accounts up to the given limit.
- `suspend fun getSavingsWithAssociations(` — Fetches a savings account together with its associations.
- `fun getAccountDetail(accountId: Long): ScreenStateStream<SavingAccountDetail>` — Streams the details of a savings account.
- `fun getAccountDetailStream(` — Phase-5 Batch-1 **SINGLE-ROW-PER-KEY read** for the `accountDetail` archetype (GOAL D13) — returns an offline-first `ScreenStateStream<SavingAccountDetail>` consumed through the `accountDetail` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + single-row upsert writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream`. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default). Distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getAccountDetail` path uses; both signatures return `ScreenStateStream<SavingAccountDetail>` so a consumer can swap between them without any downstream shape change. ### Write path (GOAL D1) This is a READ path. Account management writes (`blockAccount`, `unblockAccount`, `createSavingsAccount`, `updateSavingsAccount`) continue to flow through their existing online paths and appear here on the next refresh cycle.
- `suspend fun getSavingAccountTransaction(` — Fetches a transaction of a savings account.
- `suspend fun payViaMobile(accountId: Long): ScreenStateStream<Transaction>` — Initiates a pay-via-mobile transaction on the account.
- `fun getSavingAccountTemplate(clientId: Long): ScreenStateStream<SavingAccountTemplate>` — Streams the template used to create a savings account for the client.
- `suspend fun createSavingsAccount(savingAccount: CreateNewSavingEntity)` — Creates a savings account.
- `suspend fun updateSavingsAccount(` — Updates a savings account.
- `suspend fun unblockAccount(` — Unblocks the savings account.
- `suspend fun blockAccount(` — Blocks the savings account.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SearchRepository.kt`

```kotlin
interface SearchRepository
```
Repository for searching Fineract resources.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/mobileVerify/MobileVerificationViewModel.kt:29</code></summary>

```kotlin
/** View model that verifies a mobile number: requests an OTP for the number, then verifies the entered OTP. */
class MobileVerificationViewModel(
    private val searchRepository: SearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MobileVerificationState, MobileVerificationEvent, MobileVerificationAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: MobileVerificationState.VerifyPhoneState(),
```

</details>

- `suspend fun searchResources(` — Searches Fineract resources by query.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/SelfServiceRepository.kt`

```kotlin
interface SelfServiceRepository
```
Repository over the Fineract self-service API: login, clients, accounts, transactions and beneficiaries.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:50</code></summary>

```kotlin
class AccountViewModel(
    private val userRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<AccountState, AccountEvent, AccountAction>(
    initialState = run {
        /** Client id read from user preferences when the view model is created. */
```

</details>

- `suspend fun loginSelf(payload: AuthenticationPayload): User` — Logs in through the self-service API and returns the authenticated `User`.
- `fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>>` — Streams the details of the given self-service client.
- `suspend fun getSelfClientDetails(): Flow<ScreenState<Page<Client>>>` — Streams the page of clients visible to the self-service user.
- `fun getSelfAccountTransactions(` — Streams the transactions of the given account.
- `suspend fun getSelfAccountTransactionFromId(` — Fetches one transaction of an account.
- `fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>` — Streams the client's self-service accounts.
- `fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>` — Streams the user's beneficiaries.
- `fun getActiveAccountsWithTransactionsPerAccount(` — Streams active accounts mapped to their recent transactions, limited to `limit` when set.
- `fun getActiveAccounts(` — Streams the client's active accounts.
- `fun getActiveAccountsWithAccountTransferTemplate(` — Streams active accounts with the account-transfer template used to start a transfer.
- `fun getAccountsTransactions(clientId: Long): Flow<ScreenState<List<Transaction>>>` — Streams the transactions across all of the client's accounts.
- `fun getTransactions(accountId: List<Long>, limit: Int?): Flow<List<Transaction>>` — Streams the transactions of several accounts, limited to `limit` when set.
- `fun getTransactions(accountId: Long, limit: Int?): Flow<ScreenState<List<Transaction>>>` — Streams the transactions of a single account, limited to `limit` when set.
- `fun getTransactionsStream(` — Phase-4 Batch-A **LEDGER read** for the `history` archetype (GOAL D13) — returns an offline-first `Flow<ScreenState<List<Transaction>>>` consumed through the `history` Store5 [`Store`]`org.mobilenativefoundation.store.store5.Store` (`createStore` + Room [`SourceOfTruth`]`org.mobilenativefoundation.store.store5.SourceOfTruth` + atomic [`replacePage`]`kpt.core.database.wallet.transaction.TransactionDao.replacePage` writer) via [`Store.asScreenStream`]`kpt.core.base.store.screen.asScreenStream` — the Store5-native bridge, distinct from the transitional [`asScreenStateFlow`]`kpt.core.common.asScreenStateFlow` the legacy `getTransactions` path uses. The store is driven by [`FetchPolicy.CACHE_FIRST_SWR`]`kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR` (Phase-5 T10 default) so subscribers see the cached page instantly and a background revalidation fires on the Stale/VeryStale band edge — no reload spinner on tab-switch. ### Why the scope parameter `Store.asScreenStream(...)` launches child coroutines (reconnect debounce, periodic refresh, CACHE_FIRST_SWR side-fetch). It needs a lifecycle-bound scope so those coroutines stop when the ViewModel is cleared. Pass `viewModelScope` from the call site. ### Write path (GOAL D1) This is a READ path. Money-movement writes (transfer, deposit, withdrawal) continue to flow through their existing online repositories (`ThirdPartyTransferRepository`, `SavingsAccountRepository`, etc.) and appear here on the next refresh cycle — the LEDGER cache is server-truth.
  _…more members; read the file._

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/StandingInstructionRepository.kt`

```kotlin
interface StandingInstructionRepository
```
Repository for standing instructions (recurring transfers).

<details><summary>Used in the template — <code>feature/standing-instruction/src/commonMain/kotlin/kpt/feature/standing/instruction/StandingInstructionViewModel.kt:43</code></summary>

```kotlin
class StandingInstructionViewModel(
    private val repository: ForkPreferencesRepository,
    private val siRepository: StandingInstructionRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SIUiState, SIEvent, SIAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        /** Client id taken from the stored preferences; required to build the initial state. */
```

</details>

- `fun getStandingInstructionTemplate(` — Streams the template used to create a standing instruction.
- `fun getAllStandingInstructions(` — Streams all standing instructions matching the given filters.
- `fun getAllStandingInstructionsStream(` — Phase-5 Batch-3 LEDGER read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
- `fun getStandingInstruction(instructionId: Long): Flow<ScreenState<StandingInstruction>>` — Streams a single standing instruction.
- `suspend fun createStandingInstruction(` — Creates a standing instruction.
- `suspend fun updateStandingInstruction(` — Updates a standing instruction.
- `suspend fun deleteStandingInstruction(instructionId: Long)` — Deletes a standing instruction.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/ThirdPartyTransferRepository.kt`

```kotlin
interface ThirdPartyTransferRepository
```
Repository for transfers to third-party accounts.

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

- `suspend fun getTransferTemplate(): AccountOptionsTemplate` — Fetches the account options template for a third-party transfer.
- `fun makeTransfer(payload: TransferPayload): ScreenStateStream<TPTResponse>` — Makes a third-party transfer and streams the response.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/TwoFactorAuthRepository.kt`

```kotlin
interface TwoFactorAuthRepository
```
Repository for two-factor authentication.

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

- `suspend fun deliveryMethods(): ScreenStateStream<List<DeliveryMethod>>` — Streams the available OTP delivery methods.
- `suspend fun requestOTP(deliveryMethod: String): ScreenStateStream<String>` — Requests an OTP through the given delivery method.
- `suspend fun validateToken(token: String): ScreenStateStream<AccessToken>` — Validates the OTP token and streams the resulting `AccessToken`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repository/UserRepository.kt`

```kotlin
interface UserRepository
```
Repository for users and their roles.

<details><summary>Used in the template — <code>feature/auth/src/commonMain/kotlin/kpt/feature/auth/signup/SignupViewModel.kt:74</code></summary>

```kotlin
/** View model for the signup screen; validates the form and registers a new user and client. */
class SignupViewModel(
    private val userRepository: UserRepository,
    private val searchRepository: SearchRepository,
    private val clientRepository: ClientRepository,
    private val assetRepository: AssetRepository,
    private val stringProvider: StringProvider,
```

</details>

- `suspend fun getUsers(): ScreenStateStream<List<UserWithRole>>` — Streams all users with their roles.
- `suspend fun getUser(): ScreenStateStream<UserWithRole>` — Streams the signed-in user with role.
- `suspend fun updateUser(userId: Int, updatedUser: NewUser): ScreenStateStream<GenericResponse>` — Updates a user.
- `suspend fun createUser(newUser: NewUser): Int` — Creates a user and returns its id.
- `suspend fun updateUserPassword(userId: Long, password: String)` — Updates a user's password.
- `suspend fun deleteUser(userId: Int): CommonResponse` — Deletes a user.
- `suspend fun assignClientToUser(userId: Int, clientId: Int)` — Assigns a client to a user.

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
        /** Client taken from the stored user preferences; required to build the initial state. */
```

</details>

- `fun recordVerification()` — Stamp "verified just now" with the current monotonic time.
- `fun consumeVerification(): Boolean` — Returns `true` iff a `recordVerification` happened within the implementation's validity window (currently 30 s) **and** has not been consumed yet. Always clears the stored mark before returning.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AccountRepositoryImpl.kt`

```kotlin
class AccountRepositoryImpl(
```
Default `AccountRepository` backed by the network data sources.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AppLockRepositoryImpl.kt`

```kotlin
class AppLockRepositoryImpl(
```
`AppLockRepository` implementation backed by `com.russhwolf.settings.Settings` (single boolean entry under `APP_LOCK_FLAG`).

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AssetRepositoryImpl.kt`

```kotlin
class AssetRepositoryImpl : AssetRepository
```
Default `AssetRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AuthenticationRepositoryImpl.kt`

```kotlin
class AuthenticationRepositoryImpl(
```
Default `AuthenticationRepository` backed by the authentication API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayHistoryRepositoryImpl.kt`

```kotlin
class AutoPayHistoryRepositoryImpl(
```
Default `AutoPayHistoryRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/AutoPayRepositoryImpl.kt`

```kotlin
class AutoPayRepositoryImpl(
```
Default `AutoPayRepository`.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BeneficiaryRepositoryImpl.kt`

```kotlin
class BeneficiaryRepositoryImpl(
```
Default `BeneficiaryRepository` backed by the beneficiary API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillDataProviders.kt`

```kotlin
fun provideNetworkBillerRepository(
```
The network-backed bill and biller repositories, bound under their fully qualified names. Both share a simple name with the offline `kpt.core.datastore` interfaces the screens use (`BillOfflineRepositoryImpl`, `BillerOfflineRepositoryImpl`).

```kotlin
fun provideNetworkBillRepository(
```
The network-backed `BillRepository`; see `provideNetworkBillerRepository` for why it is qualified.

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

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillRepositoryImpl.kt`

```kotlin
class BillRepositoryImpl(
```
Network-based implementation of BillRepository. TODO: This implementation uses placeholder API endpoints.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillDataProviders.kt:38</code></summary>

```kotlin
    apiManager: FineractApiManager,
    @FromQualifier("IO") ioDispatcher: CoroutineDispatcher,
): BillRepository = BillRepositoryImpl(apiManager, ioDispatcher)
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

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillerRepositoryImpl.kt`

```kotlin
class BillerRepositoryImpl(
```
Network-based implementation of BillerRepository. TODO: This implementation uses placeholder API endpoints.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BillDataProviders.kt:31</code></summary>

```kotlin
    apiManager: FineractApiManager,
    @FromQualifier("IO") ioDispatcher: CoroutineDispatcher,
): BillerRepository = BillerRepositoryImpl(apiManager, ioDispatcher)

/** The network-backed [BillRepository]; see [provideNetworkBillerRepository] for why it is qualified. */
@DataProvider(qualifier = "kpt.core.data.repository.BillRepository")
fun provideNetworkBillRepository(
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/BiometricsSetupAdapterImpl.kt`

```kotlin
class BiometricsSetupAdapterImpl(
```
`BiometricStorageAdapter` implementation backed by `com.russhwolf.settings.Settings`. Two-key layout: a boolean "registered" flag plus the registration blob string.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ClientRepositoryImpl.kt`

```kotlin
class ClientRepositoryImpl(
```
Default `ClientRepository` backed by the client API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/DocumentRepositoryImpl.kt`

```kotlin
class DocumentRepositoryImpl(
```
Default `DocumentRepository` backed by the document API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ForkPasscodeAdapterImpl.kt`

```kotlin
class ForkPasscodeAdapterImpl(
```
`PasscodeStorageAdapter` implementation backed by `com.russhwolf.settings.Settings`. Single-key layout — just the passcode itself.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InterBankRepositoryImpl.kt`

```kotlin
class InterBankRepositoryImpl(
```
Default `InterBankRepository` backed by the inter-bank API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/InvoiceRepositoryImpl.kt`

```kotlin
class InvoiceRepositoryImpl(
```
Default `InvoiceRepository` backed by the invoice API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/KycLevelRepositoryImpl.kt`

```kotlin
class KycLevelRepositoryImpl(
```
Default `KycLevelRepository` backed by the KYC level 1 API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/LocalAssetRepositoryImpl.kt`

```kotlin
class LocalAssetRepositoryImpl(
```
Default `LocalAssetRepository` providing locales and currencies.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/NotificationRepositoryImpl.kt`

```kotlin
class NotificationRepositoryImpl(
```
Default `NotificationRepository` backed by the notification API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/OfficeRepositoryImpl.kt`

```kotlin
class OfficeRepositoryImpl(
```
Default `OfficeRepository` backed by the office API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryImp.kt`

```kotlin
class PocketRepositoryImp(
```
Default `PocketRepository` backed by the pocket and client APIs.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/repositoryImpl/PocketRepositoryTest.kt:123</code></summary>

```kotlin
        pocketStore: Store<PocketKey, List<DetailedPocketAccount>>? = null,
        linkableAccountsStore: Store<LinkableAccountKey, List<LinkableAccount>>? = null,
    ) = PocketRepositoryImp(
        dataManager = fakeSelfServiceApiManager,
        ioDispatcher = Dispatchers.Default,
        pocketStore = pocketStore,
        linkableAccountsStore = linkableAccountsStore,
```

</details>

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RecentPayeeRepositoryImpl.kt`

```kotlin
class RecentPayeeRepositoryImpl(
```
Implementation of `RecentPayeeRepository` that fetches recent payees from transaction history and caches the DERIVED result in Room via the Store5 `recentPayee` offline store (Phase-5 Batch-4, GOAL D12/D13 hybrid).

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RegistrationRepositoryImpl.kt`

```kotlin
class RegistrationRepositoryImpl(
```
Default `RegistrationRepository` backed by the registration API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/RunReportRepositoryImpl.kt`

```kotlin
class RunReportRepositoryImpl(
```
Default `RunReportRepository` backed by the run-report API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavedCardRepositoryImpl.kt`

```kotlin
class SavedCardRepositoryImpl(
```
Default `SavedCardRepository` backed by the saved-card API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SavingsAccountRepositoryImpl.kt`

```kotlin
class SavingsAccountRepositoryImpl(
```
Default `SavingsAccountRepository` backed by the savings accounts API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SearchRepositoryImpl.kt`

```kotlin
class SearchRepositoryImpl(
```
Default `SearchRepository` backed by the search API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/SelfServiceRepositoryImpl.kt`

```kotlin
class SelfServiceRepositoryImpl(
```
Default `SelfServiceRepository` backed by the self-service API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/StandingInstructionRepositoryImpl.kt`

```kotlin
class StandingInstructionRepositoryImpl(
```
Default `StandingInstructionRepository` backed by the standing-instruction API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/ThirdPartyTransferRepositoryImpl.kt`

```kotlin
class ThirdPartyTransferRepositoryImpl(
```
Default `ThirdPartyTransferRepository` backed by the third-party transfer API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/TwoFactorAuthRepositoryImpl.kt`

```kotlin
class TwoFactorAuthRepositoryImpl(
```
Default `TwoFactorAuthRepository` backed by the two-factor API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserRepositoryImpl.kt`

```kotlin
class UserRepositoryImpl(
```
Default `UserRepository` backed by the user API.

### `core/data/src/commonMain/kotlin/kpt/core/data/repositoryImpl/UserVerificationRepositoryImpl.kt`

```kotlin
class UserVerificationRepositoryImpl : UserVerificationRepository
```
`UserVerificationRepository` implementation backed by `TimeSource.Monotonic`, held in-process for the lifetime of this singleton. A wall-clock-based implementation (e.g.

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

### `core/data/src/commonMain/kotlin/kpt/core/data/util/SharedFlowExtensions.kt`

```kotlin
fun <T> bufferedMutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = MutableSharedFlow(
```
Creates a `MutableSharedFlow` with a buffer of `Int.MAX_VALUE` and the given `replay` count.

---

_90 type(s), 322 function(s)/property(ies); 358 carry KDoc at source; 6 authored example(s); 62 live call site(s)._
<!-- api-docs:end -->
