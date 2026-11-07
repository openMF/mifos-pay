# `feature/autopay/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 25× `.kt`, 1× `.md`, 1× `.kts`

**25 Kotlin files** — 11 screen(s), 11 ViewModel(s).

Screens: `AddBillScreen`, `AddBillerScreen`, `AutoPayHistoryScreen`, `AutoPayScheduleDetailsScreen`, `AutoPayScheduleManagementScreen`, `AutoPayScreen`, `AutoPaySettingsScreen`, `BillListScreen`, `BillerListScreen`, `EditBillScreen`, `EditBillerScreen`

ViewModels: `AddBillViewModel`, `AddBillerViewModel`, `AutoPayHistoryViewModel`, `AutoPayScheduleDetailsViewModel`, `AutoPayScheduleManagementViewModel`, `AutoPaySettingsViewModel`, `AutoPayViewModel`, `BillListViewModel`, `BillerListViewModel`, `EditBillViewModel`, `EditBillerViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/autopay/README.md` | AutoPay Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/autopay sha=c40615fba6dad20f03c1aad48fff8a85855895f9 -->
## API reference

_Generated from `feature/autopay` at tree `c40615fba6da` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt`

```kotlin
fun AddBillScreen(
```
Screen with a form for adding a new bill, including recurrence and optional AutoPay settings.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:300</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_ADD_BILL_ROUTE) {
        AddBillScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBillList = {
                navController.navigateToBillList(
                    navOptions = navOptions {
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillViewModel.kt`

```kotlin
class AddBillViewModel(
```
View model for the add-bill form; validates input, computes upcoming payment dates and saves a new bill.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:81</code></summary>

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
UI state of the add-bill form.

```kotlin
sealed interface AddBillEvent
```
One-off events emitted by the add-bill view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:90</code></summary>

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
User actions handled by the add-bill view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillScreen.kt:97</code></summary>

```kotlin

    LaunchedEffect(Unit) {
        viewModel.trySendAction(AddBillAction.RefreshBillers)
    }

    ForkScaffold(
        topBar = {
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt`

```kotlin
fun AddBillerScreen(
```
Screen with a form for adding a new biller (payee) for bill payments.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:263</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerViewModel.kt`

```kotlin
class AddBillerViewModel(
```
View model for the add-biller form; validates input and saves a new biller.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:55</code></summary>

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
UI state of the add-biller form.

```kotlin
sealed interface AddBillerEvent
```
One-off events emitted by the add-biller view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:62</code></summary>

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
User actions handled by the add-biller view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AddBillerScreen.kt:102</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt`

```kotlin
fun AutoPayHistoryScreen(
```
Screen showing the read-only history of AutoPay payments, with search and filters.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:234</code></summary>

```kotlin
        ),
    ) { backStackEntry ->
        AutoPayHistoryScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryViewModel.kt`

```kotlin
class AutoPayHistoryViewModel(
```
ViewModel for AutoPay history screen. This ViewModel provides read-only access to AutoPay history data. Users can view, search, and filter history but cannot edit or delete entries.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt:50</code></summary>

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
UI state of the AutoPay history screen.

```kotlin
sealed interface AutoPayHistoryEvent
```
One-off events of the history screen; none are currently emitted.

```kotlin
sealed interface AutoPayHistoryAction
```
User actions handled by the history view model.

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt`

```kotlin
object AutoPayNavigation
```
Route names and argument keys for the AutoPay screens.

- `const val AUTO_PAY_ROUTE = "autopay_route"` — Route of the AutoPay dashboard.
- `const val AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE = "autopay_schedule_management_route"` — Route of the AutoPay schedule management screen.
- `const val AUTO_PAY_PREFERENCES_ROUTE = "autopay_preferences_route"` — Route of the AutoPay preferences screen.
- `const val AUTO_PAY_HISTORY_ROUTE = "autopay_history_route"` — Route of the AutoPay history screen.
- `const val AUTO_PAY_SCHEDULE_DETAILS_ROUTE = "autopay_schedule_details_route"` — Route of the AutoPay schedule details screen.
- `const val AUTO_PAY_ADD_BILLER_ROUTE = "autopay_add_biller_route"` — Route of the add-biller screen.
- `const val AUTO_PAY_BILLER_LIST_ROUTE = "autopay_biller_list_route"` — Route of the biller list screen.
- `const val AUTO_PAY_EDIT_BILLER_ROUTE = "autopay_edit_biller_route"` — Route of the edit-biller screen.
- `const val AUTO_PAY_ADD_BILL_ROUTE = "autopay_add_bill_route"` — Route of the add-bill screen.
- `const val AUTO_PAY_BILL_LIST_ROUTE = "autopay_bill_list_route"` — Route of the bill list screen.
- `const val AUTO_PAY_EDIT_BILL_ROUTE = "autopay_edit_bill_route"` — Route of the edit-bill screen.
- `const val SCHEDULE_ID_ARG = "scheduleId"` — Argument key for the schedule id.
- `const val BILLER_ID_ARG = "billerId"` — Argument key for the biller id.
- `const val BILL_ID_ARG = "billId"` — Argument key for the bill id.
  _…more members; read the file._

```kotlin
fun NavGraphBuilder.composableWithFadeTransitions(
```
Custom composable function that uses fade transitions to prevent the state issue where both screens are visible momentarily during navigation.

```kotlin
fun NavController.navigateToAutoPay(navOptions: NavOptions? = null)
```
Navigates to the AutoPay dashboard.

```kotlin
fun NavController.navigateToScheduleManagement(navOptions: NavOptions? = null)
```
Navigates to the AutoPay schedule management screen.

```kotlin
fun NavController.navigateToAutoPayPreferences(navOptions: NavOptions? = null)
```
Navigates to the AutoPay preferences screen.

```kotlin
fun NavController.navigateToAutoPayHistory(autoPayId: Long = 0L, navOptions: NavOptions? = null)
```
Navigates to the AutoPay history screen for the given AutoPay id.

```kotlin
fun NavController.navigateToAutoPayScheduleDetails(scheduleId: String, navOptions: NavOptions? = null)
```
Navigates to the details screen of an AutoPay schedule.

```kotlin
fun NavController.navigateToAddBiller(source: String = "direct", navOptions: NavOptions? = null)
```
Navigates to the add-biller screen, noting where the user came from.

```kotlin
fun NavController.navigateToBillerList(navOptions: NavOptions? = null)
```
Navigates to the biller list screen.

```kotlin
fun NavController.navigateToEditBiller(billerId: String, navOptions: NavOptions? = null)
```
Navigates to the edit-biller screen for a biller.

```kotlin
fun NavController.navigateToAddBill(navOptions: NavOptions? = null)
```
Navigates to the add-bill screen.

```kotlin
fun NavController.navigateToBillList(navOptions: NavOptions? = null)
```
Navigates to the bill list screen.

```kotlin
fun NavController.navigateToEditBill(billId: String, navOptions: NavOptions? = null)
```
Navigates to the edit-bill screen for a bill.

```kotlin
fun NavGraphBuilder.autoPayGraph(
```
Registers all AutoPay destinations in the nav graph and wires their callbacks.

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt`

```kotlin
fun AutoPayScheduleDetailsScreen(
```
Screen showing the details of one AutoPay schedule, with pause, resume, edit and cancel actions.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:248</code></summary>

```kotlin
        ),
    ) {
        AutoPayScheduleDetailsScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsViewModel.kt`

```kotlin
class AutoPayScheduleDetailsViewModel(
```
View model for the AutoPay schedule details screen; loads a schedule and handles pause, resume and cancel.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:49</code></summary>

```kotlin
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    ForkScaffold(
```

</details>

```kotlin
data class AutoPayScheduleDetailsState(
```
UI state of the schedule details screen.

```kotlin
sealed interface AutoPayScheduleDetailsEvent
```
One-off events emitted by the schedule details view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:118</code></summary>

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
User actions handled by the schedule details view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:105</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt`

```kotlin
fun AutoPayScheduleManagementScreen(
```
Screen listing bills that have AutoPay, with toggles and edit, delete and add actions.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:204</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_SCHEDULE_MANAGEMENT_ROUTE) {
        AutoPayScheduleManagementScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementViewModel.kt`

```kotlin
class AutoPayScheduleManagementViewModel(
```
View model for AutoPay schedule management; loads bills with AutoPay and handles toggling, editing and deleting.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt:69</code></summary>

```kotlin
    onNavigateToBillList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleManagementViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberForkPullToRefreshState(
```

</details>

```kotlin
data class AutoPayScheduleManagementState(
```
UI state of the schedule management screen.

```kotlin
sealed interface AutoPayScheduleManagementEvent
```
One-off events emitted by the schedule management view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt:81</code></summary>

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
User actions handled by the schedule management view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleManagementScreen.kt:76</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayScheduleManagementAction.RefreshSchedules) },
    )

    EventsEffect(viewModel) { event ->
        when (event) {
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt`

```kotlin
fun AutoPayScreen(
```
AutoPay dashboard screen showing active schedules and upcoming payments, with entry points to related screens.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:173</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt`

```kotlin
fun AutoPayPreferencesScreen(
```
Screen for editing AutoPay preferences: global, notification and security settings and rules.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:219</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_PREFERENCES_ROUTE) {
        AutoPayPreferencesScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsViewModel.kt`

```kotlin
class AutoPayPreferencesViewModel(
```
View model for AutoPay preferences; loads, edits and saves the global settings.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:48</code></summary>

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
UI state of the AutoPay preferences screen.

```kotlin
sealed interface AutoPayPreferencesEvent
```
One-off events emitted by the preferences view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:54</code></summary>

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
User actions handled by the preferences view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPaySettingsScreen.kt:89</code></summary>

```kotlin
                    settings = state.globalSettings,
                    onToggleAutoPay = { enabled ->
                        viewModel.trySendAction(AutoPayPreferencesAction.ToggleAutoPayEnabled(enabled))
                    },
                )

                NotificationSettingsSection(
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayUtils.kt`

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayViewModel.kt`

```kotlin
class AutoPayViewModel(
```
View model for the AutoPay dashboard; loads schedules and upcoming payments and routes to related screens.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:64</code></summary>

```kotlin
    showTopBar: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: AutoPayViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberForkPullToRefreshState(
```

</details>

```kotlin
data class AutoPayState(
```
UI state of the AutoPay dashboard.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:122</code></summary>

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
A recurring payment schedule.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:129</code></summary>

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
A payment due soon.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:524</code></summary>

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
Lifecycle status of an AutoPay schedule.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScheduleDetailsScreen.kt:104</code></summary>

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
Status of an individual payment.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayHistoryScreen.kt:146</code></summary>

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
One-off events emitted by the AutoPay dashboard view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:108</code></summary>

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
User actions handled by the AutoPay dashboard view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayScreen.kt:71</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayAction.RefreshDashboard) },
    )

    ForkScaffold(
        modifier = modifier,
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillListScreen.kt`

```kotlin
fun BillListScreen(
```
Screen listing the user's bills with add, edit and delete actions.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:318</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILL_LIST_ROUTE) {
        BillListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillListViewModel.kt`

```kotlin
class BillListViewModel(
```
View model for the bill list; loads bills and handles deletion.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillListScreen.kt:66</code></summary>

```kotlin
    onNavigateToAutopay: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberForkPullToRefreshState(
```

</details>

```kotlin
data class BillListState(
```
UI state of the bill list.

```kotlin
sealed interface BillListEvent
```
One-off events emitted by the bill list view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillListScreen.kt:134</code></summary>

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
User actions handled by the bill list view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillListScreen.kt:73</code></summary>

```kotlin
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(BillListAction.RefreshBills) },
    )

    Box(modifier = modifier.fillMaxSize()) {
        ForkScaffold(
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillerListScreen.kt`

```kotlin
fun BillerListScreen(
```
Screen listing saved billers with search, add, edit and delete actions.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:272</code></summary>

```kotlin

    composableWithFadeTransitions(AutoPayNavigation.AUTO_PAY_BILLER_LIST_ROUTE) {
        BillerListScreen(
            onNavigateBack = {
                navController.popBackStack(AutoPayNavigation.AUTO_PAY_ROUTE, false)
            },
            onNavigateToAddBiller = {
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillerListViewModel.kt`

```kotlin
class BillerListViewModel(
```
View model for the biller list; loads, searches and deletes billers.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillerListScreen.kt:61</code></summary>

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
UI state of the biller list.

```kotlin
sealed interface BillerListEvent
```
One-off events emitted by the biller list view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillerListScreen.kt:69</code></summary>

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
User actions handled by the biller list view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/BillerListScreen.kt:110</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillScreen.kt`

```kotlin
fun EditBillScreen(
```
Screen with a form for editing an existing bill, including recurrence and optional AutoPay settings.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:341</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillViewModel.kt`

```kotlin
class EditBillViewModel(
```
View model for the edit-bill form; validates input, computes upcoming payment dates and updates the bill.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillScreen.kt:79</code></summary>

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
UI state of the edit-bill form.

```kotlin
sealed interface EditBillEvent
```
One-off events emitted by the edit-bill view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillScreen.kt:88</code></summary>

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
User actions handled by the edit-bill view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillScreen.kt:122</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillerScreen.kt`

```kotlin
fun EditBillerScreen(
```
Screen with a form for editing an existing biller.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/AutoPayNavigation.kt:294</code></summary>

```kotlin
        ),
    ) {
        EditBillerScreen(
            onNavigateBack = onNavigateBack,
        )
    }
```

</details>

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillerViewModel.kt`

```kotlin
class EditBillerViewModel(
```
View model for the edit-biller form; validates input and updates the biller identified by the billerId argument.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillerScreen.kt:54</code></summary>

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
UI state of the edit-biller form.

```kotlin
sealed interface EditBillerEvent
```
One-off events emitted by the edit-biller view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillerScreen.kt:61</code></summary>

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
User actions handled by the edit-biller view model.

<details><summary>Used in the template — <code>feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/EditBillerScreen.kt:91</code></summary>

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

### `feature/autopay/src/commonMain/kotlin/kpt/feature/autopay/di/AutoPayModule.kt`

```kotlin
val AutoPayModule = module
```
Koin module that registers the AutoPay feature view models.

---

_49 type(s), 49 function(s)/property(ies); 98 carry KDoc at source; 0 authored example(s); 47 live call site(s)._
<!-- api-docs:end -->
