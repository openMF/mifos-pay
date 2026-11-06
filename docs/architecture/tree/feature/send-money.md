# `feature/send-money/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 74 tracked files — 72× `.kt`, 1× `.kts`, 1× `.xml`

**72 Kotlin files** — 13 screen(s), 12 ViewModel(s).

Screens: `BankTransferScreen`, `ContactsPickerScreen`, `PayAnyoneScreen`, `PayeeDetailsScreen`, `PaymentChatHistoryScreen`, `PaymentDetailsScreen`, `PaymentProcessingScreen`, `PaymentSuccessScreen`, `SearchIfscScreen`, `SendMoneyOptionsScreen`, `SendMoneyScreen`, `UpiPinScreen`, `UpiTransactionHistoryScreen`

ViewModels: `BankTransferViewModel`, `PayAnyoneViewModel`, `PayeeDetailsViewModel`, `PaymentChatHistoryViewModel`, `PaymentDetailsViewModel`, `PaymentProcessingViewModel`, `PaymentSuccessViewModel`, `SearchIfscViewModel`, `SendMoneyOptionsViewModel`, `SendMoneyViewModel`, `UpiPinViewModel`, `UpiTransactionHistoryViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/send-money sha=f6d285e705bbaefe4feab18f5000d2a5c2ff1c0f -->
## API reference

_Generated from `feature/send-money` at tree `f6d285e705bb` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/AmountUtils.kt`

```kotlin
object AmountUtils
```
Utility functions for converting between paise and rupees Navigation parameters use paise (stored as string) for precision UI displays use rupees with 2 decimal places

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:567</code></summary>

```kotlin
    val isAmountValid = if (state.isUpiCode) {
        state.amount.isNotEmpty() &&
            AmountUtils.isValidPaise(state.amount) &&
            AmountUtils.paiseToRupees(state.amount).toDoubleOrNull()?.let { it >= 0 } == true &&
            !state.isAmountExceedingMax &&
            !state.isAmountBelowMin
    } else {
```

</details>

- `fun rupeesToPaise(rupees: String): String` — Converts rupees (as string) to paise (as string)
- `val amount = rupees.toDoubleOrNull() ?: 0.0`
- `fun paiseToRupees(paise: String): String` — Converts paise (as string) to rupees (as string) with 2 decimal places
- `val amount = paise.toLongOrNull() ?: 0L`
- `val rupees = amount / 100.0`
- `val formatted = CurrencyFormatter.format(`
- `fun formatRupeesForUI(rupees: String): String` — Formats rupees amount for UI display with proper formatting
- `val amount = rupees.toDoubleOrNull() ?: 0.0`
- `fun formatPaiseForUI(paise: String): String` — Formats paise amount for UI display by converting to rupees first
- `val rupees = paiseToRupees(paise)`
- `fun isValidPaise(paise: String): Boolean` — Validates if a paise amount is valid
- `val amount = paise.toLongOrNull()`
- `fun isValidRupees(rupees: String): Boolean` — Validates if a rupees amount is valid
- `val amount = rupees.toDoubleOrNull()`
  _…more members; read the file._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt`

```kotlin
fun BankTransferScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:266</code></summary>

```kotlin
        route = BANK_TRANSFER_ROUTE,
    ) {
        BankTransferScreen(
            onBackClick = onBackClick,
            onSearchIfscClick = onSearchIfscClick,
        )
    }
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferViewModel.kt`

```kotlin
class BankTransferViewModel : BaseViewModel<BankTransferState, BankTransferEvent, BankTransferAction>(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:92</code></summary>

```kotlin
    onSearchIfscClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BankTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
```

</details>

```kotlin
data class BankAccount(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:619</code></summary>

```kotlin
            BankAccountSelectionList(
                bankAccounts = state.bankAccounts,
                selectedBankAccount = if (isFromAccount) state.selectedFromBankAccount else state.selectedToBankAccount,
                onBankAccountSelect = { bankAccount ->
                    if (isFromAccount) {
                        viewModel.trySendAction(BankTransferAction.SelectFromBankAccount(bankAccount))
                    } else {
```

</details>

```kotlin
data class BankTransferState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:214</code></summary>

```kotlin
@Composable
private fun BankTransferToOthersContent(
    state: BankTransferState,
    onAccountNumberChange: (String) -> Unit,
    onIfscCodeChange: (String) -> Unit,
    onSearchIfscClick: () -> Unit,
    onContinueClick: () -> Unit,
```

</details>

```kotlin
sealed interface BankTransferEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:99</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            BankTransferEvent.NavigateBack -> {
                onBackClick.invoke()
            }
            BankTransferEvent.ShowIfscSearch -> {
                onSearchIfscClick.invoke()
```

</details>

```kotlin
sealed interface BankTransferAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:124</code></summary>

```kotlin
                    topBarTitle = stringResource(Res.string.feature_send_money_bank_transfer),
                    backPress = {
                        viewModel.trySendAction(BankTransferAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/Contact.kt`

```kotlin
enum class ContactType
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:434</code></summary>

```kotlin
                name = contact.name,
                size = 48,
                backgroundColor = if (contact.type == ContactType.BUSINESS) {
                    KptTheme.colorScheme.secondaryContainer
                } else {
                    KptTheme.colorScheme.primaryContainer
                },
```

</details>

```kotlin
data class Contact(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:124</code></summary>

```kotlin

                MifosOutlinedTextField(
                    label = "Contact Number *",
                    value = state.formData.contactNumber,
                    onValueChange = { viewModel.trySendAction(AddBillerAction.UpdateContactNumber(it)) },
                    isError = state.validationResult.contactNumberError != null,
                    errorMessage = state.validationResult.contactNumberError,
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactNotFoundDialog.kt`

```kotlin
fun ContactNotFoundDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:297</code></summary>

```kotlin

    // Contact Not Found Dialog
    ContactNotFoundDialog(
        showDialog = state.showContactNotFoundDialog,
        selectedOption = state.selectedSharingOption,
        onOptionSelected = { option ->
            viewModel.trySendAction(PayAnyoneAction.SharingOptionSelected(option))
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactNotFoundLoading.kt`

```kotlin
fun ContactNotFoundLoading(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:264</code></summary>

```kotlin
                }

                if (state.showContactNotFoundLoading) {
                    ContactNotFoundLoading()
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactPermissionState.kt`

```kotlin
interface ContactPermissionState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionState.android.kt:26</code></summary>

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberContactPermissionState(): ContactPermissionState {
    val accPermissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
```

</details>

- `val status: ContactPermissionStatus`
- `fun requestContactPermission()`
- `fun goToSettings()`

```kotlin
enum class ContactPermissionStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionState.android.kt:42</code></summary>

```kotlin
    private val context: Context,
) : ContactPermissionState {
    override val status: ContactPermissionStatus
        get() = permissionState.status.toContactPermissionStatus()

    override fun requestContactPermission() {
        permissionState.launchPermissionRequest()
```

</details>

```kotlin
expect fun rememberContactPermissionState(): ContactPermissionState
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactRepository.kt`

```kotlin
interface ContactRepository
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactRepository.android.kt:19</code></summary>

```kotlin

@Composable
actual fun rememberContactRepository(): ContactRepository {
    val context = LocalContext.current
    return remember { AndroidContactRepository(context.contentResolver) }
}
```

</details>

- `suspend fun getContacts(): List<Contact>`
- `suspend fun searchContacts(query: String): List<Contact>`

```kotlin
expect fun rememberContactRepository(): ContactRepository
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactsPickerScreen.kt`

```kotlin
expect fun ContactPermissionHandler()
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionHandler.android.kt:15</code></summary>

```kotlin
import kpt.core.designsystem.component.PermissionBox
@Composable
actual fun ContactPermissionHandler() {
    PermissionBox(
        title = "Contact Permission Required",
        confirmButtonText = "Grant Permission",
        dismissButtonText = "Open Settings",
```

</details>

```kotlin
fun ContactsPickerScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:318</code></summary>

```kotlin
        route = CONTACTS_PICKER_ROUTE,
    ) {
        ContactsPickerScreen(
            onBackClick = onBackClick,
            onContactSelected = onContactSelected,
        )
    }
```

</details>

```kotlin
fun ContactsPickerScreenPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt`

```kotlin
fun PayAnyoneScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:303</code></summary>

```kotlin
    ) { backStackEntry ->

        PayAnyoneScreen(
            onBackClick = onBackClick,
            onContactPickerClick = onContactPickerClick,
            onContactSelected = onContactSelected,
        )
```

</details>

```kotlin
fun PayAnyoneScreenPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneViewModel.kt`

```kotlin
class PayAnyoneViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:75</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    val viewModel: PayAnyoneViewModel = koinViewModel()
    val state by viewModel.stateFlow.collectAsState()

    var currentPlaceholderIndex by remember { mutableStateOf(0) }
```

</details>

```kotlin
data class PayAnyoneState(
```
_No KDoc at source._

```kotlin
enum class SharingOption
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/SharingHelper.android.kt:30</code></summary>

```kotlin
     */
    override fun shareInviteMessage(
        sharingOption: SharingOption,
        phoneNumber: String,
        message: String,
    ) {
        when (sharingOption) {
```

</details>

```kotlin
data class ContactSearchResult(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:317</code></summary>

```kotlin
@Composable
private fun SearchResultsContent(
    searchResults: ContactSearchResult,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
```

</details>

```kotlin
sealed interface PayAnyoneEvent
```
_No KDoc at source._

```kotlin
sealed interface PayAnyoneAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:135</code></summary>

```kotlin
                        value = state.inputValue,
                        onValueChange = {
                            viewModel.trySendAction(PayAnyoneAction.InputValueChanged(it))
                        },
                        label = "",
                        placeholder = {
                            AnimatedContent(
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt`

```kotlin
fun PayeeDetailsScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:340</code></summary>

```kotlin
        ),
    ) {
        PayeeDetailsScreen(
            onBackClick = onBackClick,
            onNavigateToPaymentProcessing = onNavigateToUpiPin,
            onNavigateToUpiPayment = onNavigateToUpiPayment,
            onNavigateToFineractPayment = onNavigateToFineractPayment,
```

</details>

```kotlin
fun PayeeDetailsScreenPreview()
```
_No KDoc at source._

```kotlin
fun PayeeProfileSectionPreview()
```
_No KDoc at source._

```kotlin
fun PaymentDetailsSectionPreview()
```
_No KDoc at source._

```kotlin
fun ExpandableAmountInputPreview()
```
_No KDoc at source._

```kotlin
fun ExpandableNoteInputPreview()
```
_No KDoc at source._

```kotlin
fun ProceedButtonPreview()
```
_No KDoc at source._

```kotlin
fun AccountSelectionBottomSheetPreview()
```
_No KDoc at source._

```kotlin
fun AccountItemPreview()
```
_No KDoc at source._

```kotlin
fun SelectedAccountSectionPreview()
```
_No KDoc at source._

```kotlin
fun AddBankAccountItemPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsViewModel.kt`

```kotlin
class PayeeDetailsViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:102</code></summary>

```kotlin
    onNavigateToFineractPayment: (PayeeDetailsState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PayeeDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class PayeeDetailsState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:98</code></summary>

```kotlin
fun PayeeDetailsScreen(
    onBackClick: () -> Unit,
    onNavigateToPaymentProcessing: (PayeeDetailsState) -> Unit,
    onNavigateToUpiPayment: (PayeeDetailsState) -> Unit,
    onNavigateToFineractPayment: (PayeeDetailsState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PayeeDetailsViewModel = koinViewModel(),
```

</details>

```kotlin
sealed interface PayeeDetailsEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:108</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            PayeeDetailsEvent.NavigateBack -> onBackClick.invoke()
            is PayeeDetailsEvent.NavigateToUpiPayment -> onNavigateToPaymentProcessing.invoke(event.state)
            is PayeeDetailsEvent.NavigateToFineractPayment -> onNavigateToPaymentProcessing.invoke(event.state)
        }
    }
```

</details>

```kotlin
sealed interface PayeeDetailsAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:121</code></summary>

```kotlin
                    topBarTitle = stringResource(Res.string.feature_send_money_payee_details_title),
                    backPress = {
                        viewModel.trySendAction(PayeeDetailsAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryScreen.kt`

```kotlin
fun PaymentChatHistoryScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:461</code></summary>

```kotlin
        route = PAYMENT_CHAT_HISTORY_ROUTE,
    ) {
        PaymentChatHistoryScreen(
            onBackClick = onBackClick,
            onPaymentClick = onPaymentClick,
            onTransactionClick = onTransactionClick,
        )
```

</details>

```kotlin
enum class UserType
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:51</code></summary>

```kotlin
        private fun createPlaceholderState(): PaymentChatHistoryState {
            return PaymentChatHistoryState(
                userType = UserType.INDIVIDUAL,
                businessName = "TechCorp Solutions",
                bankingName = "Jane Smith",
                upiId = "jane.smith@upi",
                profileImageUrl = null,
```

</details>

```kotlin
data class PaymentTransaction(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:73</code></summary>

```kotlin
                    date = "25 Sept 2025, 3:15 PM",
                    transactions = listOf(
                        PaymentTransaction(
                            transactionId = "txn_005",
                            recipientName = "Jane Smith",
                            amount = "3,200.00",
                            note = "Shopping payment",
```

</details>

```kotlin
data class PaymentHistoryGroup(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:68</code></summary>

```kotlin
         */
        @OptIn(ExperimentalTime::class)
        private fun createPlaceholderPaymentHistory(): List<PaymentHistoryGroup> {
            return listOf(
                PaymentHistoryGroup(
                    date = "25 Sept 2025, 3:15 PM",
                    transactions = listOf(
```

</details>

```kotlin
data class PaymentChatHistoryState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:22</code></summary>

```kotlin
 */
class PaymentChatHistoryViewModel :
    BaseViewModel<PaymentChatHistoryState, PaymentChatHistoryEvent, PaymentChatHistoryAction>(
        initialState = createPlaceholderState(),
    ) {

    override fun handleAction(action: PaymentChatHistoryAction) {
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt`

```kotlin
class PaymentChatHistoryViewModel :
```
ViewModel for the Payment Chat History screen Manages payment history state and user interactions Currently uses placeholder data for demonstration

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryScreen.kt:67</code></summary>

```kotlin
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentChatHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    MifosScaffold(
```

</details>

```kotlin
sealed interface PaymentChatHistoryEvent
```
_No KDoc at source._

```kotlin
sealed interface PaymentChatHistoryAction
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.kt`

```kotlin
expect fun PaymentDetailsScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.android.kt:30</code></summary>

```kotlin
 */
@Composable
actual fun PaymentDetailsScreen(
    onBackClick: () -> Unit,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareScreenshot: () -> Unit,
```

</details>

```kotlin
fun PaymentDetailsScreenPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsState.kt`

```kotlin
data class PaymentDetailsState(
```
Data class representing the state of the Payment Details screen Contains all necessary information for displaying comprehensive payment details

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.kt:222</code></summary>

```kotlin
@Composable
private fun ProfileAndRecipientSection(
    state: PaymentDetailsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsViewModel.kt`

```kotlin
class PaymentDetailsViewModel :
```
ViewModel for the Payment Details screen Manages payment details state and provides comprehensive transaction information Currently uses placeholder data for demonstration purposes

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.android.kt:36</code></summary>

```kotlin
    onShareScreenshot: () -> Unit,
    modifier: Modifier,
    viewModel: PaymentDetailsViewModel,
    transactionId: String,
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
```

</details>

```kotlin
sealed interface PaymentDetailsEvent
```
_No KDoc at source._

```kotlin
sealed interface PaymentDetailsAction
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt`

```kotlin
fun PaymentProcessingScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:412</code></summary>

```kotlin
        ),
    ) {
        PaymentProcessingScreen(
            onPaymentComplete = onPaymentComplete,
            onPaymentFailed = onPaymentFailed,
        )
    }
```

</details>

```kotlin
fun PaymentProcessingScreenPreview()
```
_No KDoc at source._

```kotlin
fun PaymentProcessingContentPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingViewModel.kt`

```kotlin
class PaymentProcessingViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:45</code></summary>

```kotlin
    onPaymentFailed: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentProcessingViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class PaymentProcessingState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:82</code></summary>

```kotlin
@Composable
private fun PaymentProcessingContent(
    state: PaymentProcessingState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
```

</details>

```kotlin
sealed interface PaymentProcessingEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:51</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is PaymentProcessingEvent.PaymentComplete -> onPaymentComplete.invoke(
                event.payeeName,
                event.amount,
                event.upiName,
                event.transactionTimestamp,
```

</details>

```kotlin
sealed interface PaymentProcessingAction
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt`

```kotlin
expect fun PaymentSuccessScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.android.kt:31</code></summary>

```kotlin
 */
@Composable
actual fun PaymentSuccessScreen(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier,
```

</details>

```kotlin
fun PaymentSuccessScreenPreview()
```
_No KDoc at source._

```kotlin
fun PaymentSuccessHeaderPreview()
```
_No KDoc at source._

```kotlin
fun PaymentDetailsCardPreview()
```
_No KDoc at source._

```kotlin
fun PlaceholderBannerPreview()
```
_No KDoc at source._

```kotlin
fun UpilogoPreview()
```
_No KDoc at source._

```kotlin
fun ActionButtonsPreview()
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessViewModel.kt`

```kotlin
class PaymentSuccessViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.android.kt:36</code></summary>

```kotlin
    onNavigateToSendMoneyOptions: () -> Unit,
    modifier: Modifier,
    viewModel: PaymentSuccessViewModel,
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    val coroutineScope = rememberCoroutineScope()
```

</details>

```kotlin
data class PaymentSuccessState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:148</code></summary>

```kotlin
@Composable
private fun PaymentSuccessHeader(
    state: PaymentSuccessState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
```

</details>

```kotlin
sealed interface PaymentSuccessEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:76</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            PaymentSuccessEvent.ShareScreenshot -> onShareScreenshot.invoke()
            PaymentSuccessEvent.NavigateToHome -> onDone.invoke()
            PaymentSuccessEvent.NavigateToSendMoneyOptions -> onNavigateToSendMoneyOptions.invoke()
        }
    }
```

</details>

```kotlin
sealed interface PaymentSuccessAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:133</code></summary>

```kotlin
                ActionButtons(
                    onShareScreenshot = {
                        viewModel.trySendAction(PaymentSuccessAction.ShareScreenshot)
                    },
                    onDone = {
                        viewModel.trySendAction(PaymentSuccessAction.Done)
                    },
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PhoneNumberUtils.kt`

```kotlin
object PhoneNumberUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactRepository.android.kt:30</code></summary>

```kotlin
    override suspend fun getContacts(): List<Contact> {
        val rawContacts = queryContacts(null)
        return PhoneNumberUtils.filterAndFormatContacts(rawContacts)
    }

    override suspend fun searchContacts(query: String): List<Contact> {
        val normalizedQuery = PhoneNumberUtils.normalizeSearchQuery(query)
```

</details>

- `fun formatIndianMobileNumber(phoneNumber: String): String` — Formats an Indian mobile number to the format +91 88888 88888
- `val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")`
- `val number = digitsOnly.substring(2)`
- `fun isValidIndianMobileNumber(phoneNumber: String): Boolean` — Checks if a phone number is a valid Indian mobile number
- `val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")`
- `val number = digitsOnly.substring(2)`
- `val numberToCheck = if (digitsOnly.length == 12 && digitsOnly.startsWith("91"))`
- `fun filterAndFormatContacts(contacts: List<Contact>): List<Contact>` — Filters and formats a list of contacts to only include valid mobile numbers
- `fun normalizePhoneNumberForSearch(phoneNumber: String): String` — Normalizes a phone number for search purposes by removing country code and spaces This allows searching with or without +91 prefix
- `fun normalizeSearchQuery(query: String): String` — Normalizes a search query for phone number matching Removes +91 prefix and spaces to match against normalized phone numbers

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/QrScanner.kt`

```kotlin
interface QrScanner
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PlayScannerModule.kt:27</code></summary>

```kotlin
    }

    single<QrScanner> {
        QrScannerImp(
            scanner = get(),
            playModule = get(),
        )
```

</details>

- `fun startScanning(): Flow<String?>`

```kotlin
expect val ScannerModule: Module
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/QrScanner.android.kt:57</code></summary>

```kotlin
}

actual val ScannerModule: Module
    get() = PlayScannerModule
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt`

```kotlin
fun SearchIfscScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:280</code></summary>

```kotlin
        route = SEARCH_IFSC_ROUTE,
    ) {
        SearchIfscScreen(
            onBackClick = onBackClick,
            onIfscSelected = onIfscSelected,
        )
    }
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscViewModel.kt`

```kotlin
class SearchIfscViewModel : BaseViewModel<SearchIfscState, SearchIfscEvent, SearchIfscAction>(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:68</code></summary>

```kotlin
    onIfscSelected: (IfscCode) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchIfscViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val selectedBank = state.selectedBank
    val selectedBranch = state.selectedBranch
```

</details>

```kotlin
data class IfscCode(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferViewModel.kt:230</code></summary>

```kotlin
    data object NavigateToNext : BankTransferEvent
    data object AddBankAccount : BankTransferEvent
    data class IfscCodeSelected(val ifscCode: IfscCode) : BankTransferEvent
}

sealed interface BankTransferAction {
    data object NavigateBack : BankTransferAction
```

</details>

```kotlin
data class DummyBank(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:281</code></summary>

```kotlin
@Composable
private fun BankList(
    banks: List<DummyBank>,
    onBankSelected: (DummyBank) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
```

</details>

```kotlin
data class DummyBankBranch(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:353</code></summary>

```kotlin
@Composable
private fun BranchList(
    branches: List<DummyBankBranch>,
    onBranchSelected: (DummyBankBranch) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
```

</details>

```kotlin
data class SearchIfscState(
```
_No KDoc at source._

```kotlin
sealed interface SearchIfscEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:79</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            SearchIfscEvent.NavigateBack -> {
                onBackClick.invoke()
            }

            is SearchIfscEvent.IfscSelected -> {
```

</details>

```kotlin
sealed interface SearchIfscAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:101</code></summary>

```kotlin
                    topBarTitle = stringResource(Res.string.feature_send_money_search_ifsc),
                    backPress = {
                        viewModel.trySendAction(SearchIfscAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt`

```kotlin
fun SendMoneyOptionsScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:244</code></summary>

```kotlin
        route = SEND_MONEY_OPTIONS_ROUTE,
    ) {
        SendMoneyOptionsScreen(
            onBackClick = onBackClick,
            onScanQrClick = onScanQrClick,
            onPayAnyoneClick = onPayAnyoneClick,
            onBankTransferClick = onBankTransferClick,
```

</details>

```kotlin
data class TransactionItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/components/TransactionList.kt:69</code></summary>

```kotlin
            key = { _, it -> it.transactionId },
        ) { i, items ->
            TransactionItem(
                transaction = items,
                modifier = Modifier,
                onClick = {
                    onAction(HistoryAction.ViewTransaction(it))
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsViewModel.kt`

```kotlin
class SendMoneyOptionsViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:76</code></summary>

```kotlin
    onUpiTransactionHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendMoneyOptionsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class SendMoneyOptionsState(
```
_No KDoc at source._

```kotlin
sealed interface SendMoneyOptionsEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:82</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            SendMoneyOptionsEvent.NavigateBack -> {
                onBackClick.invoke()
            }
            SendMoneyOptionsEvent.NavigateToPayAnyone -> {
                onPayAnyoneClick.invoke()
```

</details>

```kotlin
sealed interface SendMoneyOptionsAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:112</code></summary>

```kotlin
                    topBarTitle = stringResource(Res.string.feature_send_money_title),
                    backPress = {
                        viewModel.trySendAction(SendMoneyOptionsAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt`

```kotlin
fun SendMoneyScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:220</code></summary>

```kotlin
        ),
    ) {
        SendMoneyScreen(
            onBackClick = onBackClick,
            navigateToTransferScreen = navigateToTransferScreen,
            navigateToScanQrScreen = navigateToScanQrScreen,
            navigateToPayeeDetails = navigateToPayeeDetailsScreen,
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyViewModel.kt`

```kotlin
class SendMoneyViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:95</code></summary>

```kotlin
    showTopBar: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: SendMoneyViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountListState.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class SendMoneyState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:141</code></summary>

```kotlin
@Composable
private fun SendMoneyScreen(
    state: SendMoneyState,
    accountState: ViewState,
    showTopBar: Boolean,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
```

</details>

```kotlin
sealed interface ViewState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:81</code></summary>

```kotlin
        .mapLatest { screenState ->
            when (screenState) {
                is ScreenState.Loading -> AccountState.ViewState.Loading

                is ScreenState.Empty ->
                    // Empty account-list branch → render an empty Content
                    // (screen still shows the FAB + empty-state copy; no
```

</details>

```kotlin
sealed interface SendMoneyEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:102</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            SendMoneyEvent.OnNavigateBack -> onBackClick.invoke()

            is SendMoneyEvent.NavigateToTransferScreen -> {
                navigateToTransferScreen(event.data)
            }
```

</details>

```kotlin
sealed interface SendMoneyAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:124</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SendMoneyAction.DismissDialog) }
        },
    )

    SendMoneyScreen(
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SharingHelper.kt`

```kotlin
interface SharingHelper
```
Common interface for sharing invite messages across platforms

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/SharingHelper.android.kt:20</code></summary>

```kotlin
 * Android-specific implementation for sharing invite messages via SMS and WhatsApp
 */
class AndroidSharingHelper(private val context: android.content.Context) : SharingHelper {

    /**
     * Shares the invite message via the selected sharing option
     *
```

</details>

- `fun shareInviteMessage(` — Shares the invite message via the selected sharing option

```kotlin
object DefaultSharingHelper : SharingHelper
```
Default implementation that does nothing (for platforms without sharing support)

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/SharingHelper.android.kt:108</code></summary>

```kotlin
        AndroidSharingHelper(context)
    } else {
        DefaultSharingHelper
    }
}
```

</details>

```kotlin
expect fun getSharingHelper(): SharingHelper
```
Factory function to get the appropriate sharing helper for the current platform

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinScreen.kt`

```kotlin
data class PaymentDetails(
```
Data class representing payment details for UPI transactions

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinViewModel.kt:38</code></summary>

```kotlin
        }

        val paymentDetails = PaymentDetails(
            payeeName = payeeName,
            bankName = bankName,
            accountNumber = accountNo,
            amount = amountInRupees,
```

</details>

```kotlin
fun UpiPinScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:382</code></summary>

```kotlin
        ),
    ) {
        UpiPinScreen(
            onBackClick = onBackClick,
            onUpiPinEntered = { payeeName, amount, pin, isUpiCode ->
                onNavigateToPaymentProcessing(payeeName, amount, isUpiCode)
            },
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinViewModel.kt`

```kotlin
class UpiPinViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinScreen.kt:79</code></summary>

```kotlin
    onUpiPinEntered: (String, String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpiPinViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val isDropdownExpanded = rememberSaveable { mutableStateOf(false) }
```

</details>

```kotlin
data class UpiPinState(
```
_No KDoc at source._

```kotlin
sealed interface UpiPinEvent
```
_No KDoc at source._

```kotlin
sealed interface UpiPinAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinScreen.kt:124</code></summary>

```kotlin
                        bankAccountDetails = state.paymentDetails,
                        onUpiPinEntered = { pin ->
                            viewModel.trySendAction(UpiPinAction.UpiPinEntered(pin))
                            onUpiPinEntered(
                                state.paymentDetails.payeeName,
                                state.amount.toString(),
                                pin,
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt`

```kotlin
fun UpiTransactionHistoryScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:475</code></summary>

```kotlin
        route = UPI_TRANSACTION_HISTORY_ROUTE,
    ) {
        UpiTransactionHistoryScreen(
            onBackClick = onBackClick,
        )
    }
}
```

</details>

```kotlin
data class UpiTransaction(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryViewModel.kt:86</code></summary>

```kotlin
    }

    private fun groupTransactionsByMonth(transactions: List<UpiTransaction>): List<MonthTransactionGroup> {
        return transactions
            .groupBy { transaction ->
                "${transaction.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${transaction.date.year}"
            }
```

</details>

```kotlin
data class MonthTransactionGroup(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryViewModel.kt:86</code></summary>

```kotlin
    }

    private fun groupTransactionsByMonth(transactions: List<UpiTransaction>): List<MonthTransactionGroup> {
        return transactions
            .groupBy { transaction ->
                "${transaction.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${transaction.date.year}"
            }
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryViewModel.kt`

```kotlin
class UpiTransactionHistoryViewModel() : BaseViewModel<UpiTransactionHistoryState, UpiTransactionHistoryEvent, UpiTransactionHistoryAction>(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:56</code></summary>

```kotlin
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpiTransactionHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class UpiTransactionHistoryState(
```
_No KDoc at source._

```kotlin
sealed class UpiTransactionHistoryAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:75</code></summary>

```kotlin
                searchQuery = state.searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.trySendAction(UpiTransactionHistoryAction.SearchQueryChanged(query))
                },
                onSearch = {
                    viewModel.trySendAction(UpiTransactionHistoryAction.SearchPerformed)
                },
```

</details>

```kotlin
sealed class UpiTransactionHistoryEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:62</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            UpiTransactionHistoryEvent.NavigateBack -> {
                onBackClick.invoke()
            }
        }
    }
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/di/SendMoneyModule.kt`

```kotlin
val SendMoneyModule = module
```
_No KDoc at source._

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt`

```kotlin
fun NavController.navigateToPaymentDetailsScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPayAnyoneScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToContactsPickerScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSendMoneyScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSendMoneyOptionsScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToBankTransferScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSearchIfscScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPayeeDetailsScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToUpiPinScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPaymentProcessingScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPaymentSuccessScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPaymentChatHistoryScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToUpiTransactionHistoryScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.sendMoneyScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.sendMoneyOptionsScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.bankTransferScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.searchIfscScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.payAnyoneScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.contactsPickerScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.payeeDetailsScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.upiPinScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.paymentProcessingScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.paymentSuccessScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.paymentChatHistoryScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.upiTransactionHistoryScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.paymentDetailsScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSendMoneyScreen(
```
_No KDoc at source._

---

_72 type(s), 100 function(s)/property(ies); 20 carry KDoc at source; 0 authored example(s); 77 live call site(s)._
<!-- api-docs:end -->
