# `feature/send-money/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 76 tracked files — 74× `.kt`, 1× `.kts`, 1× `.xml`

**74 Kotlin files** — 13 screen(s), 12 ViewModel(s).

Screens: `BankTransferScreen`, `ContactsPickerScreen`, `PayAnyoneScreen`, `PayeeDetailsScreen`, `PaymentChatHistoryScreen`, `PaymentDetailsScreen`, `PaymentProcessingScreen`, `PaymentSuccessScreen`, `SearchIfscScreen`, `SendMoneyOptionsScreen`, `SendMoneyScreen`, `UpiPinScreen`, `UpiTransactionHistoryScreen`

ViewModels: `BankTransferViewModel`, `PayAnyoneViewModel`, `PayeeDetailsViewModel`, `PaymentChatHistoryViewModel`, `PaymentDetailsViewModel`, `PaymentProcessingViewModel`, `PaymentSuccessViewModel`, `SearchIfscViewModel`, `SendMoneyOptionsViewModel`, `SendMoneyViewModel`, `UpiPinViewModel`, `UpiTransactionHistoryViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/send-money sha=be78c4d2c380ef2f583ead59e9ee991885c0eb0d -->
## API reference

_Generated from `feature/send-money` at tree `be78c4d2c380` by `scripts/docs/api-docs-gen.sh`._
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

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeAccountSelection.kt:157</code></summary>

```kotlin
                        text = stringResource(
                            Res.string.feature_send_money_pay_amount,
                            AmountUtils.formatPaiseForUI(state.amount),
                        ),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
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
Screen for entering a beneficiary account number and IFSC code, or transferring between own accounts.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:186</code></summary>

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
ViewModel for the bank transfer screen; validates account details and handles IFSC selection and self transfers.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:93</code></summary>

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
A linked bank account of the user.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:620</code></summary>

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
State of the bank transfer screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:215</code></summary>

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
One-off events emitted by the bank transfer screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:100</code></summary>

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
User actions on the bank transfer screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferScreen.kt:125</code></summary>

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
Whether a `Contact` is an individual or a business.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:435</code></summary>

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
A device contact that can be picked as a payment recipient.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:125</code></summary>

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
Dialog shown when a contact is not on the app, letting the user pick a channel to send an invite.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:298</code></summary>

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
Centered progress indicator with a "Checking..." label shown while a contact lookup is in progress.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:265</code></summary>

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
Platform-agnostic holder for the contacts permission state and the actions to change it.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionState.android.kt:27</code></summary>

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberContactPermissionState(): ContactPermissionState {
    val accPermissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
```

</details>

- `val status: ContactPermissionStatus` — Current contacts permission status.
- `fun requestContactPermission()` — Launches the system prompt requesting contacts permission.
- `fun goToSettings()` — Opens the system settings page where the user can change the contacts permission.

```kotlin
enum class ContactPermissionStatus
```
Granted or denied status of the contacts permission.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionState.android.kt:48</code></summary>

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
Remembers a platform-specific `ContactPermissionState` for the current composition.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactRepository.kt`

```kotlin
interface ContactRepository
```
Source of the device contacts used when choosing a payee.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactRepository.android.kt:20</code></summary>

```kotlin
/** Android actual: remembers an [AndroidContactRepository] bound to the current context. */
@Composable
actual fun rememberContactRepository(): ContactRepository {
    val context = LocalContext.current
    return remember { AndroidContactRepository(context.contentResolver) }
}
```

</details>

- `suspend fun getContacts(): List<Contact>` — Returns all device contacts that have a valid phone number.
- `suspend fun searchContacts(query: String): List<Contact>` — Returns contacts whose name or phone number matches `query`.

```kotlin
expect fun rememberContactRepository(): ContactRepository
```
Remembers a platform-specific `ContactRepository` for the current composition.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/ContactsPickerScreen.kt`

```kotlin
expect fun ContactPermissionHandler()
```
Composable that requests contacts permission on platforms that need it.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactPermissionHandler.android.kt:16</code></summary>

```kotlin
/** Android actual: shows a permission box prompting for READ_CONTACTS access. */
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
Screen that lists and searches device contacts so the user can pick a payment recipient.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:241</code></summary>

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
Preview of `ContactsPickerScreen`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt`

```kotlin
fun PayAnyoneScreen(
```
Screen for paying any recipient by UPI ID, phone number or QR scan.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:225</code></summary>

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
Preview of `PayAnyoneScreen`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneViewModel.kt`

```kotlin
class PayAnyoneViewModel(
```
ViewModel for the pay-anyone screen; handles input, contact search and invite sharing.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:76</code></summary>

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
State of the pay-anyone screen.

```kotlin
enum class SharingOption
```
Channel used to share an invite to a contact who is not on the app.

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
Contact search results grouped by kind.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:318</code></summary>

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
One-off events emitted by the pay-anyone screen.

```kotlin
sealed interface PayAnyoneAction
```
User actions on the pay-anyone screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayAnyoneScreen.kt:136</code></summary>

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

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeAccountSelection.kt`

```kotlin
fun AccountSelectionBottomSheetPreview()
```
Preview of the account selection bottom sheet.

```kotlin
fun AccountItemPreview()
```
Preview of a single account item.

```kotlin
fun SelectedAccountSectionPreview()
```
Preview of the selected account section.

```kotlin
fun AddBankAccountItemPreview()
```
Preview of the add-bank-account item.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt`

```kotlin
fun PayeeDetailsScreen(
```
Screen where the user reviews the payee, enters amount and note, and selects the paying account.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:264</code></summary>

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
Preview of `PayeeDetailsScreen`.

```kotlin
fun PayeeProfileSectionPreview()
```
Preview of the payee profile section.

```kotlin
fun PaymentDetailsSectionPreview()
```
Preview of the payment details section.

```kotlin
fun ExpandableAmountInputPreview()
```
Preview of the expandable amount input.

```kotlin
fun ExpandableNoteInputPreview()
```
Preview of the expandable note input.

```kotlin
fun ProceedButtonPreview()
```
Preview of the proceed button.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsViewModel.kt`

```kotlin
class PayeeDetailsViewModel(
```
ViewModel for the payee details screen; parses the payee from the navigation arguments and manages amount, note and account selection.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:93</code></summary>

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
State of the payee details screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeAccountSelection.kt:58</code></summary>

```kotlin
@Composable
internal fun AccountSelectionBottomSheet(
    state: PayeeDetailsState,
    onAccountSelected: (BankAccount) -> Unit,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
sealed interface PayeeDetailsEvent
```
One-off events emitted by the payee details screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:99</code></summary>

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
User actions on the payee details screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PayeeDetailsScreen.kt:112</code></summary>

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
Chat-style screen showing the payment history with a payee, grouped by date.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/PaymentFlowNavigation.kt:317</code></summary>

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
Kind of payee shown in the payment chat history.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:52</code></summary>

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
A single payment shown as a chat bubble.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:74</code></summary>

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
Payments that share the same date.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryViewModel.kt:69</code></summary>

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
State of the payment chat history screen.

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

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentChatHistoryScreen.kt:68</code></summary>

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
One-off events emitted by the payment chat history screen (none currently).

```kotlin
sealed interface PaymentChatHistoryAction
```
User actions handled by `PaymentChatHistoryViewModel`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.kt`

```kotlin
expect fun PaymentDetailsScreen(
```
Screen showing the full details of a past payment with share, pay-again and retry actions.

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
Preview of `PaymentDetailsScreen`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsState.kt`

```kotlin
data class PaymentDetailsState(
```
Data class representing the state of the Payment Details screen Contains all necessary information for displaying comprehensive payment details

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentDetailsScreen.kt:223</code></summary>

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
One-off events emitted by the payment details screen (none currently).

```kotlin
sealed interface PaymentDetailsAction
```
User actions handled by `PaymentDetailsViewModel`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt`

```kotlin
fun PaymentProcessingScreen(
```
Screen shown while a payment is being processed.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/PaymentFlowNavigation.kt:266</code></summary>

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
Preview of `PaymentProcessingScreen`.

```kotlin
fun PaymentProcessingContentPreview()
```
Preview of the payment processing content.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingViewModel.kt`

```kotlin
class PaymentProcessingViewModel(
```
ViewModel for the payment processing screen; builds its state from the navigation arguments.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:46</code></summary>

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
State of the payment processing screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:83</code></summary>

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
One-off events emitted by the payment processing screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentProcessingScreen.kt:52</code></summary>

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
User actions on the payment processing screen.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt`

```kotlin
expect fun PaymentSuccessScreen(
```
Screen confirming a completed payment, with share, done and send-again actions.

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
Preview of `PaymentSuccessScreen`.

```kotlin
fun PaymentSuccessHeaderPreview()
```
Preview of the payment success header.

```kotlin
fun PaymentDetailsCardPreview()
```
Preview of the payment details card.

```kotlin
fun PlaceholderBannerPreview()
```
Preview of the placeholder banner.

```kotlin
fun UpilogoPreview()
```
Preview of the UPI logo.

```kotlin
fun ActionButtonsPreview()
```
Preview of the action buttons.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessViewModel.kt`

```kotlin
class PaymentSuccessViewModel(
```
ViewModel for the payment success screen; builds its state from the navigation arguments.

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
State of the payment success screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:149</code></summary>

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
One-off events emitted by the payment success screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:77</code></summary>

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
User actions on the payment success screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/PaymentSuccessScreen.kt:134</code></summary>

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
Helpers for formatting, validating, searching and filtering phone numbers from the contact list.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/ContactRepository.android.kt:35</code></summary>

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
Platform-agnostic QR code scanner.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/PlayScannerModule.kt:28</code></summary>

```kotlin
    }

    single<QrScanner> {
        QrScannerImp(
            scanner = get(),
            playModule = get(),
        )
```

</details>

- `fun startScanning(): Flow<String?>` — Starts a scan and emits the raw scanned value, or null if scanning failed.

```kotlin
expect val ScannerModule: Module
```
Platform-specific Koin module that provides the `QrScanner` implementation.

<details><summary>Used in the template — <code>feature/send-money/src/androidMain/kotlin/kpt/feature/send/money/QrScanner.android.kt:63</code></summary>

```kotlin

/** Android actual: provides [PlayScannerModule] as the QR scanner Koin module. */
actual val ScannerModule: Module
    get() = PlayScannerModule
```

</details>

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt`

```kotlin
fun SearchIfscScreen(
```
Screen for searching a bank branch by IFSC code, bank, state, district and branch.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:201</code></summary>

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
ViewModel for the IFSC search screen; filters banks and branches and emits the chosen IFSC code.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:69</code></summary>

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
A bank branch IFSC record.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/BankTransferViewModel.kt:285</code></summary>

```kotlin
     * @property ifscCode The chosen record.
     */
    data class IfscCodeSelected(val ifscCode: IfscCode) : BankTransferEvent
}

/** User actions on the bank transfer screen. */
sealed interface BankTransferAction {
```

</details>

```kotlin
data class DummyBank(
```
Placeholder bank entry used by the IFSC search.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:282</code></summary>

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
Placeholder bank branch entry used by the IFSC search.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:354</code></summary>

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
State of the IFSC search screen.

```kotlin
sealed interface SearchIfscEvent
```
One-off events emitted by the IFSC search screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:80</code></summary>

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
User actions on the IFSC search screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SearchIfscScreen.kt:102</code></summary>

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
Landing screen of the send-money flow offering the ways to pay, plus recent payees.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:163</code></summary>

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
A recent payment shown in the recent-payees list.

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
ViewModel for the send-money options screen; maps option taps and QR scans to navigation events.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:77</code></summary>

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
State of the send-money options screen.

```kotlin
sealed interface SendMoneyOptionsEvent
```
One-off navigation events emitted by the send-money options screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:83</code></summary>

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
User actions on the send-money options screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyOptionsScreen.kt:113</code></summary>

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
Screen for entering an amount and sending money to a selected payee.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt:138</code></summary>

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
ViewModel for the Fineract send-money screen; searches accounts by number and builds the payment QR data.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:96</code></summary>

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
State of the send-money screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:142</code></summary>

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
State of the account search results list.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:90</code></summary>

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
One-off events emitted by the send-money screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:103</code></summary>

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
User actions on the send-money screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/SendMoneyScreen.kt:125</code></summary>

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

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinViewModel.kt:51</code></summary>

```kotlin

        /** Payment details shown on the PIN screen. */
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
Screen where the user enters their UPI PIN to authorize a payment.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/PaymentFlowNavigation.kt:235</code></summary>

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
ViewModel for the UPI PIN screen; builds its state from the navigation arguments.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinScreen.kt:80</code></summary>

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
State of the UPI PIN screen.

```kotlin
sealed interface UpiPinEvent
```
One-off events emitted by the UPI PIN screen.

```kotlin
sealed interface UpiPinAction
```
User actions on the UPI PIN screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiPinScreen.kt:125</code></summary>

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
Screen listing UPI transactions grouped by month, with search.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/PaymentFlowNavigation.kt:332</code></summary>

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
A UPI transaction in the history list.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryViewModel.kt:87</code></summary>

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
UPI transactions for one month.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryViewModel.kt:87</code></summary>

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
ViewModel for the UPI transaction history screen; supports searching grouped transactions.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:57</code></summary>

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
State of the UPI transaction history screen.

```kotlin
sealed class UpiTransactionHistoryAction
```
User actions on the UPI transaction history screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:76</code></summary>

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
One-off events emitted by the UPI transaction history screen.

<details><summary>Used in the template — <code>feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/UpiTransactionHistoryScreen.kt:63</code></summary>

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
Koin module that registers the send-money view models and the platform `ScannerModule`.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/PaymentFlowNavigation.kt`

```kotlin
fun NavController.navigateToPaymentDetailsScreen(
```
Navigates to the payment details screen for `transactionId`.

```kotlin
fun NavController.navigateToUpiPinScreen(
```
Navigates to the UPI PIN screen.

```kotlin
fun NavController.navigateToPaymentProcessingScreen(
```
Navigates to the payment processing screen.

```kotlin
fun NavController.navigateToPaymentSuccessScreen(
```
Navigates to the payment success screen.

```kotlin
fun NavController.navigateToPaymentChatHistoryScreen(
```
Navigates to the payment chat history screen.

```kotlin
fun NavController.navigateToUpiTransactionHistoryScreen(
```
Navigates to the UPI transaction history screen.

```kotlin
fun NavGraphBuilder.upiPinScreen(
```
Registers the UPI PIN screen in the navigation graph.

```kotlin
fun NavGraphBuilder.paymentProcessingScreen(
```
Registers the payment processing screen in the navigation graph.

```kotlin
fun NavGraphBuilder.paymentSuccessScreen(
```
Registers the payment success screen in the navigation graph.

```kotlin
fun NavGraphBuilder.paymentChatHistoryScreen(
```
Registers the payment chat history screen in the navigation graph.

```kotlin
fun NavGraphBuilder.upiTransactionHistoryScreen(
```
Registers the UPI transaction history screen in the navigation graph.

```kotlin
fun NavGraphBuilder.paymentDetailsScreen(
```
Registers the payment details screen in the navigation graph.

### `feature/send-money/src/commonMain/kotlin/kpt/feature/send/money/navigation/SendNavigation.kt`

```kotlin
fun NavController.navigateToPayAnyoneScreen(
```
Navigates to the pay-anyone screen.

```kotlin
fun NavController.navigateToContactsPickerScreen(
```
Navigates to the contacts picker screen.

```kotlin
fun NavController.navigateToSendMoneyScreen(
```
Navigates to the send-money screen.

```kotlin
fun NavController.navigateToSendMoneyOptionsScreen(
```
Navigates to the send-money options screen.

```kotlin
fun NavController.navigateToBankTransferScreen(
```
Navigates to the bank transfer screen.

```kotlin
fun NavController.navigateToSearchIfscScreen(
```
Navigates to the IFSC search screen.

```kotlin
fun NavController.navigateToPayeeDetailsScreen(
```
Navigates to the payee details screen.

```kotlin
fun NavGraphBuilder.sendMoneyScreen(
```
Registers the send-money screen in the navigation graph.

```kotlin
fun NavGraphBuilder.sendMoneyOptionsScreen(
```
Registers the send-money options screen in the navigation graph.

```kotlin
fun NavGraphBuilder.bankTransferScreen(
```
Registers the bank transfer screen in the navigation graph.

```kotlin
fun NavGraphBuilder.searchIfscScreen(
```
Registers the IFSC search screen in the navigation graph.

```kotlin
fun NavGraphBuilder.payAnyoneScreen(
```
Registers the pay-anyone screen in the navigation graph.

```kotlin
fun NavGraphBuilder.contactsPickerScreen(
```
Registers the contacts picker screen in the navigation graph.

```kotlin
fun NavGraphBuilder.payeeDetailsScreen(
```
Registers the payee details screen in the navigation graph.

```kotlin
fun NavController.navigateToSendMoneyScreen(
```
Navigates to the send-money screen with `requestData`, clearing it from the back stack first.

---

_72 type(s), 100 function(s)/property(ies); 159 carry KDoc at source; 0 authored example(s); 77 live call site(s)._
<!-- api-docs:end -->
