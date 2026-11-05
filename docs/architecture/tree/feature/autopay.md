# `feature/autopay/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 25× `.kt`, 1× `.md`, 1× `.kts`

**25 Kotlin files** — 11 screen(s), 11 ViewModel(s).

Screens: `AddBillScreen`, `AddBillerScreen`, `AutoPayHistoryScreen`, `AutoPayScheduleDetailsScreen`, `AutoPayScheduleManagementScreen`, `AutoPayScreen`, `AutoPaySettingsScreen`, `BillListScreen`, `BillerListScreen`, `EditBillScreen`, `EditBillerScreen`

ViewModels: `AddBillViewModel`, `AddBillerViewModel`, `AutoPayHistoryViewModel`, `AutoPayScheduleDetailsViewModel`, `AutoPayScheduleManagementViewModel`, `AutoPaySettingsViewModel`, `AutoPayViewModel`, `BillListViewModel`, `BillerListViewModel`, `EditBillViewModel`, `EditBillerViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/autopay/README.md` | AutoPay Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/autopay sha=811e4601062c27648327ebd8d782b91624b107b0 -->
## API reference

_Generated from `feature/autopay` at tree `811e4601062c` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillScreen.kt`

```kotlin
fun AddBillScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:256</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_ADD_BILL_ROUTE) {
        AddBillScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBillList = {
                navController.navigateToBillList(
                    navOptions = navOptions {
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillViewModel.kt`

```kotlin
class AddBillViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillScreen.kt:80</code></summary>

```kotlin
    onNavigateToBillList: () -> Unit,
    onNavigateToAddBiller: () -> Unit,
    viewModel: AddBillViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showRecurrenceDropdown by remember { mutableStateOf(false) }
    var showBillerDropdown by remember { mutableStateOf(false) }
```

</details>

```kotlin
data class AddBillState(
```
_No KDoc at source._

```kotlin
sealed interface AddBillEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillScreen.kt:89</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AddBillEvent.BillSaved -> {
                onNavigateToBillList()
            }
        }
    }
```

</details>

```kotlin
sealed interface AddBillAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillScreen.kt:96</code></summary>

```kotlin

    LaunchedEffect(Unit) {
        viewModel.trySendAction(AddBillAction.RefreshBillers)
    }

    MifosScaffold(
        topBar = {
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillerScreen.kt`

```kotlin
fun AddBillerScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:219</code></summary>

```kotlin
        ),
    ) {
        AddBillerScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBillerList = {
                navController.navigateToBillerList()
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillerViewModel.kt`

```kotlin
class AddBillerViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillerScreen.kt:55</code></summary>

```kotlin
    onNavigateBack: () -> Unit,
    onNavigateToBillerList: () -> Unit,
    viewModel: AddBillerViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showCategoryDropdown by remember { mutableStateOf(false) }
```

</details>

```kotlin
data class AddBillerState(
```
_No KDoc at source._

```kotlin
sealed interface AddBillerEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillerScreen.kt:62</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AddBillerEvent.BillerSaved -> {
                // Check if we should go back or to biller list based on source
                val source = viewModel.getSource()
                if (source == "bill_creation") {
                    onNavigateBack()
```

</details>

```kotlin
sealed interface AddBillerAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AddBillerScreen.kt:102</code></summary>

```kotlin
                    label = "Biller Name *",
                    value = state.formData.name,
                    onValueChange = { viewModel.trySendAction(AddBillerAction.UpdateBillerName(it)) },
                    isError = state.validationResult.nameError != null,
                    errorMessage = state.validationResult.nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayHistoryScreen.kt`

```kotlin
fun AutoPayHistoryScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:190</code></summary>

```kotlin
        ),
    ) { backStackEntry ->
        AutoPayHistoryScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayHistoryViewModel.kt`

```kotlin
class AutoPayHistoryViewModel(
```
ViewModel for AutoPay history screen. This ViewModel provides read-only access to AutoPay history data. Users can view, search, and filter history but cannot edit or delete entries.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayHistoryScreen.kt:50</code></summary>

```kotlin
fun AutoPayHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AutoPayHistoryViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
```

</details>

```kotlin
data class AutoPayHistoryUiState(
```
_No KDoc at source._

```kotlin
sealed interface AutoPayHistoryEvent
```
_No KDoc at source._

```kotlin
sealed interface AutoPayHistoryAction
```
_No KDoc at source._

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt`

```kotlin
object AutoPayNavigation
```
_No KDoc at source._

- `const val AUTO_PAY_ROUTE = "autopay_route"`
- `const val AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE = "autopay_schedule_management_route"`
- `const val AUTO_PAY_PREFERENCES_ROUTE = "autopay_preferences_route"`
- `const val AUTO_PAY_HISTORY_ROUTE = "autopay_history_route"`
- `const val AUTO_PAY_SCHEDULE_DETAILS_ROUTE = "autopay_schedule_details_route"`
- `const val AUTO_PAY_ADD_BILLER_ROUTE = "autopay_add_biller_route"`
- `const val AUTO_PAY_BILLER_LIST_ROUTE = "autopay_biller_list_route"`
- `const val AUTO_PAY_EDIT_BILLER_ROUTE = "autopay_edit_biller_route"`
- `const val AUTO_PAY_ADD_BILL_ROUTE = "autopay_add_bill_route"`
- `const val AUTO_PAY_BILL_LIST_ROUTE = "autopay_bill_list_route"`
- `const val AUTO_PAY_EDIT_BILL_ROUTE = "autopay_edit_bill_route"`
- `const val SCHEDULE_ID_ARG = "scheduleId"`
- `const val BILLER_ID_ARG = "billerId"`
- `const val BILL_ID_ARG = "billId"`
  _…more members; read the file._

```kotlin
fun NavGraphBuilder.composableWithFadeTransitions(
```
Custom composable function that uses fade transitions to prevent the state issue where both screens are visible momentarily during navigation.

```kotlin
fun NavController.navigateToAutoPay(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToScheduleManagement(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAutoPayPreferences(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAutoPayHistory(autoPayId: Long = 0L, navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAutoPayScheduleDetails(scheduleId: String, navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAddBiller(source: String = "direct", navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToBillerList(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToEditBiller(billerId: String, navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAddBill(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToBillList(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToEditBill(billId: String, navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.autoPayGraph(
```
_No KDoc at source._

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt`

```kotlin
fun AutoPayScheduleDetailsScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:204</code></summary>

```kotlin
        ),
    ) {
        AutoPayScheduleDetailsScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsViewModel.kt`

```kotlin
class AutoPayScheduleDetailsViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt:49</code></summary>

```kotlin
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    MifosScaffold(
```

</details>

```kotlin
data class AutoPayScheduleDetailsState(
```
_No KDoc at source._

```kotlin
sealed interface AutoPayScheduleDetailsEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt:118</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayScheduleDetailsEvent.NavigateBack -> onNavigateBack()
            is AutoPayScheduleDetailsEvent.SchedulePaused -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.ScheduleResumed -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.ScheduleCancelled -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.NavigateToEdit -> { /* TODO: Navigate to edit screen */ }
```

</details>

```kotlin
sealed interface AutoPayScheduleDetailsAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt:105</code></summary>

```kotlin
                onPauseResume = {
                    if (state.schedule!!.status == AutoPayStatus.ACTIVE) {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.PauseSchedule)
                    } else {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.ResumeSchedule)
                    }
                },
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementScreen.kt`

```kotlin
fun AutoPayScheduleManagementScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:160</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE) {
        AutoPayScheduleManagementScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementViewModel.kt`

```kotlin
class AutoPayScheduleManagementViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementScreen.kt:68</code></summary>

```kotlin
    onNavigateToBillList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleManagementViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
```

</details>

```kotlin
data class AutoPayScheduleManagementState(
```
_No KDoc at source._

```kotlin
sealed interface AutoPayScheduleManagementEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementScreen.kt:80</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayScheduleManagementEvent.NavigateToAddBill -> onNavigateToAddBill()
            is AutoPayScheduleManagementEvent.NavigateToEditBill -> onNavigateToEditBill(event.billId)
            is AutoPayScheduleManagementEvent.NavigateToBillList -> onNavigateToBillList()
            is AutoPayScheduleManagementEvent.AutoPayToggled -> {
                // AutoPay toggled successfully
```

</details>

```kotlin
sealed interface AutoPayScheduleManagementAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleManagementScreen.kt:75</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayScheduleManagementAction.RefreshSchedules) },
    )

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt`

```kotlin
fun AutoPayScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:129</code></summary>

```kotlin
) {
    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_ROUTE) {
        AutoPayScreen(
            onNavigateToScheduleManagement = {
                navController.navigateToScheduleManagement()
            },
            onNavigateToPreferences = {
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPaySettingsScreen.kt`

```kotlin
fun AutoPayPreferencesScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:175</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_PREFERENCES_ROUTE) {
        AutoPayPreferencesScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPaySettingsViewModel.kt`

```kotlin
class AutoPayPreferencesViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPaySettingsScreen.kt:48</code></summary>

```kotlin
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayPreferencesViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class AutoPayPreferencesState(
```
_No KDoc at source._

```kotlin
sealed interface AutoPayPreferencesEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPaySettingsScreen.kt:54</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayPreferencesEvent.SettingsSaved -> {
                onNavigateBack()
            }
            is AutoPayPreferencesEvent.ShowError -> {
                // Handle error display
```

</details>

```kotlin
sealed interface AutoPayPreferencesAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPaySettingsScreen.kt:89</code></summary>

```kotlin
                    settings = state.globalSettings,
                    onToggleAutoPay = { enabled ->
                        viewModel.trySendAction(AutoPayPreferencesAction.ToggleAutoPayEnabled(enabled))
                    },
                )

                NotificationSettingsSection(
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayUtils.kt`

```kotlin
fun Bill.toAutoPaySchedule(): AutoPaySchedule
```
Converts a Bill with AutoPay enabled to an AutoPaySchedule

```kotlin
fun Bill.toUpcomingPayment(): UpcomingPayment
```
Converts a Bill with AutoPay enabled to an UpcomingPayment

```kotlin
fun mapBillStatusToAutoPayStatus(billStatus: BillStatus): AutoPayStatus
```
Maps BillStatus to AutoPayStatus

```kotlin
fun mapBillStatusToPaymentStatus(billStatus: BillStatus): PaymentStatus
```
Maps BillStatus to PaymentStatus

```kotlin
fun calculateNextPaymentDate(bill: Bill): Long
```
Calculates the next payment date based on the bill's due date and recurrence pattern

```kotlin
fun formatDateForDisplay(timestamp: Long): String
```
Formats a timestamp to a readable date string

```kotlin
fun isBillOverdue(bill: Bill): Boolean
```
Checks if a bill is overdue

```kotlin
fun List<Bill>.getBillsWithAutoPay(): List<Bill>
```
Gets bills with AutoPay enabled from a list of bills

```kotlin
fun List<Bill>.getActiveBillsWithAutoPay(): List<Bill>
```
Gets active bills with AutoPay enabled

```kotlin
fun List<Bill>.calculateUpcomingPayments(): List<UpcomingPayment>
```
Calculates upcoming payments for bills with AutoPay enabled

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayViewModel.kt`

```kotlin
class AutoPayViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt:64</code></summary>

```kotlin
    showTopBar: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: AutoPayViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
```

</details>

```kotlin
data class AutoPayState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt:122</code></summary>

```kotlin
@Composable
private fun AutoPayDashboardContent(
    state: AutoPayState,
    viewModel: AutoPayViewModel,
    onRefresh: () -> Unit,
    onViewScheduleDetails: (String) -> Unit,
    onNavigateToScheduleManagement: () -> Unit,
```

</details>

```kotlin
data class AutoPaySchedule(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt:129</code></summary>

```kotlin
@Composable
private fun ScheduleDetailsContent(
    schedule: AutoPaySchedule,
    onPauseResume: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
data class UpcomingPayment(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt:524</code></summary>

```kotlin
@Composable
private fun UpcomingPaymentCard(
    payment: UpcomingPayment,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
```

</details>

```kotlin
enum class AutoPayStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScheduleDetailsScreen.kt:104</code></summary>

```kotlin
                modifier = Modifier.padding(paddingValues),
                onPauseResume = {
                    if (state.schedule!!.status == AutoPayStatus.ACTIVE) {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.PauseSchedule)
                    } else {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.ResumeSchedule)
                    }
```

</details>

```kotlin
enum class PaymentStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayHistoryScreen.kt:146</code></summary>

```kotlin
    val statusColor = when (currentStatus) {
        // Green color for success
        PaymentStatus.COMPLETED -> Color(0xFF4CAF50)
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error
        PaymentStatus.PROCESSING -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.UPCOMING -> MaterialTheme.colorScheme.primary
```

</details>

```kotlin
sealed interface AutoPayEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt:108</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayEvent.NavigateToScheduleManagement -> onNavigateToScheduleManagement()
            is AutoPayEvent.NavigateToPreferences -> onNavigateToPreferences()
            is AutoPayEvent.NavigateToHistory -> onNavigateToHistory()
            is AutoPayEvent.NavigateToAddBiller -> onNavigateToAddBiller()
            is AutoPayEvent.NavigateToBillerList -> onNavigateToBillerList()
```

</details>

```kotlin
sealed interface AutoPayAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayScreen.kt:71</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayAction.RefreshDashboard) },
    )

    MifosScaffold(
        modifier = modifier,
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillListScreen.kt`

```kotlin
fun BillListScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:274</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILL_LIST_ROUTE) {
        BillListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillListViewModel.kt`

```kotlin
class BillListViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillListScreen.kt:65</code></summary>

```kotlin
    onNavigateToAutopay: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
```

</details>

```kotlin
data class BillListState(
```
_No KDoc at source._

```kotlin
sealed interface BillListEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillListScreen.kt:133</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is BillListEvent.NavigateToAddBill -> onNavigateToAddBill()
            is BillListEvent.NavigateToEditBill -> onNavigateToEditBill(event.billId)
            is BillListEvent.BillDeleted -> {
                // Bill deleted successfully, no action needed
            }
```

</details>

```kotlin
sealed interface BillListAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillListScreen.kt:72</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(BillListAction.RefreshBills) },
    )

    Box(modifier = modifier.fillMaxSize()) {
        MifosScaffold(
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillerListScreen.kt`

```kotlin
fun BillerListScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:228</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILLER_LIST_ROUTE) {
        BillerListScreen(
            onNavigateBack = {
                navController.popBackStack(AutoPayNavigation.AUTO_PAY_ROUTE, false)
            },
            onNavigateToAddBiller = {
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillerListViewModel.kt`

```kotlin
class BillerListViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillerListScreen.kt:61</code></summary>

```kotlin
    onNavigateToAddBiller: () -> Unit,
    onNavigateToEditBiller: (String) -> Unit,
    viewModel: BillerListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
```

</details>

```kotlin
data class BillerListState(
```
_No KDoc at source._

```kotlin
sealed interface BillerListEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillerListScreen.kt:69</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is BillerListEvent.NavigateToAddBiller -> onNavigateToAddBiller()
            is BillerListEvent.NavigateToEditBiller -> onNavigateToEditBiller(event.billerId)
            is BillerListEvent.BillerDeleted -> {
                // Biller deleted successfully, no action needed
            }
```

</details>

```kotlin
sealed interface BillerListAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/BillerListScreen.kt:110</code></summary>

```kotlin
                        onValueChange = {
                            searchQuery = it
                            viewModel.trySendAction(BillerListAction.SearchBillers(it))
                        },
                        placeholder = { Text("Search billers...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillScreen.kt`

```kotlin
fun EditBillScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:297</code></summary>

```kotlin
        ),
    ) {
        EditBillScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBiller = {
                navController.navigateToAddBiller(source = "bill_creation")
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillViewModel.kt`

```kotlin
class EditBillViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillScreen.kt:78</code></summary>

```kotlin
    onNavigateBack: () -> Unit,
    onNavigateToAddBiller: () -> Unit,
    viewModel: EditBillViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showRecurrenceDropdown by remember { mutableStateOf(false) }
    var showBillerDropdown by remember { mutableStateOf(false) }
```

</details>

```kotlin
data class EditBillState(
```
_No KDoc at source._

```kotlin
sealed interface EditBillEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillScreen.kt:87</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is EditBillEvent.BillUpdated -> {
                onNavigateBack()
            }
        }
    }
```

</details>

```kotlin
sealed interface EditBillAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillScreen.kt:121</code></summary>

```kotlin
                    label = "Bill Name *",
                    value = state.formData.name,
                    onValueChange = { viewModel.trySendAction(EditBillAction.UpdateBillName(it)) },
                    isError = state.validationResult.nameError != null,
                    errorMessage = state.validationResult.nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillerScreen.kt`

```kotlin
fun EditBillerScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/AutoPayNavigation.kt:250</code></summary>

```kotlin
        ),
    ) {
        EditBillerScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillerViewModel.kt`

```kotlin
class EditBillerViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillerScreen.kt:54</code></summary>

```kotlin
fun EditBillerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditBillerViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showCategoryDropdown by remember { mutableStateOf(false) }
```

</details>

```kotlin
data class EditBillerState(
```
_No KDoc at source._

```kotlin
sealed interface EditBillerEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillerScreen.kt:61</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is EditBillerEvent.BillerUpdated -> {
                onNavigateBack()
            }
        }
    }
```

</details>

```kotlin
sealed interface EditBillerAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/EditBillerScreen.kt:91</code></summary>

```kotlin
                label = "Biller Name *",
                value = state.formData.name,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateBillerName(it)) },
                isError = state.validationResult.nameError != null,
                errorMessage = state.validationResult.nameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
```

</details>

### `feature/autopay/src/commonMain/kotlin/org/mifospay/feature/autopay/di/AutoPayModule.kt`

```kotlin
val AutoPayModule = module
```
_No KDoc at source._

---

_49 type(s), 49 function(s)/property(ies); 12 carry KDoc at source; 0 authored example(s); 47 live call site(s)._
<!-- api-docs:end -->
