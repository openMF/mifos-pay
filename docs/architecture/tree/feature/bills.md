# `feature/bills/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 42 tracked files — 20× `.xml`, 20× `.kt`, 1× `.md`, 1× `.kts`

**20 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `AddOrEditBillReminderScreen`, `BillRemindersListScreen`

ViewModels: `BillRemindersListViewModel`, `EditBillReminderViewModel`

**Consumes:** `core/store/banking`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/bills/README.md` | :feature:bills |

<!-- tree-scaffold:end -->
## Significance

Recurring bill reminders, and the showcase for **offline-resilient form submission**. The form is
`DraftSubmitHandler`-backed and the reminder itself is scheduled through the platform notification
seam, so the feature spans `core/data` and `core/platform` rather than staying in one layer.

`dueDay` is a day-of-month clamped to the month's length at read time, so 31 is safe in February — the
kind of detail a screen must not re-derive per render.

<!-- api-docs:begin module=feature/bills sha=1f225a6dbb4b9f908a602baabbd64f6a9363814d -->
## API reference

_Generated from `feature/bills` at tree `1f225a6dbb4b` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt`

```kotlin
val BillsModule = module
```
Koin module for the Bill Reminders feature.

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/navigation/BillsNavigation.kt`

```kotlin
data object BillsGraphRoute
```
Route for the entire Bill Reminders graph — entry for `NavController.navigateToBills`.

```kotlin
data object BillRemindersListRoute
```
Bill list dashboard.

```kotlin
data class AddOrEditBillReminderRoute(val billId: String? = null)
```
Add-or-edit screen. `billId == null` means "create new"; non-null means "edit existing".

```kotlin
fun NavController.navigateToBills(navOptions: NavOptions? = null)
```
Entry point — call this from `AuthenticatedNavigation` to jump into the bills graph.

```kotlin
fun NavGraphBuilder.billsGraph(navController: NavController)
```
Wires the bills graph into a parent `NavGraphBuilder`. Mirrors `alertsGraph` — push-transition routes, back-pop navigation, type-safe arg passing.

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/notification/BillNotificationGateway.kt`

```kotlin
interface BillNotificationGateway
```
Feature-local boundary over bill-reminder scheduling.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt:35</code></summary>

```kotlin
    // BillNotificationGateway maps bill reminders onto the cross-platform `sync` WorkScheduler
    // (provided by SyncModule) — no per-platform scheduler module to include here anymore.
    single<BillNotificationGateway> { BillNotificationGatewayImpl(get()) }

    viewModel { BillRemindersListViewModel(repository = get(), scheduler = get()) }
    viewModel { (billId: String?) ->
        EditBillReminderViewModel(
```

</details>

- `suspend fun schedule(bill: BillReminderSchedule)` — Schedule (or replace) a notification for the given bill.
- `suspend fun cancel(billId: String)` — Cancel a single bill's pending notification.
- `suspend fun cancelAll()` — Cancel every pending bill-reminder notification.

```kotlin
class BillNotificationGatewayImpl(
```
Production gateway — maps `BillReminderSchedule` onto the `sync` `WorkScheduler`: - **schedule** enqueues a unique-named notification (REPLACE on re-schedule) tagged both with a per-bill tag and the shared `TAG_BILL_REMINDER`, with `initialDelay = triggerAt - now`. - **cancel** cancels the per-bill tag; **cancelAll** cancels the shared tag. Wired in `kpt.feature.bills.di.BillsModule`.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt:35</code></summary>

```kotlin
    // BillNotificationGateway maps bill reminders onto the cross-platform `sync` WorkScheduler
    // (provided by SyncModule) — no per-platform scheduler module to include here anymore.
    single<BillNotificationGateway> { BillNotificationGatewayImpl(get()) }

    viewModel { BillRemindersListViewModel(repository = get(), scheduler = get()) }
    viewModel { (billId: String?) ->
        EditBillReminderViewModel(
```

</details>

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/notification/BillReminderSchedule.kt`

```kotlin
data class BillReminderSchedule(
```
Feature-local payload describing a single bill-reminder notification.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/notification/BillNotificationGateway.kt:30</code></summary>

```kotlin

    /** Schedule (or replace) a notification for the given bill. */
    suspend fun schedule(bill: BillReminderSchedule)

    /** Cancel a single bill's pending notification. */
    suspend fun cancel(billId: String)
```

</details>

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/AddOrEditBillReminderScreen.kt`

```kotlin
fun AddOrEditBillReminderScreen(
```
Add-or-edit screen for a single bill reminder. - When `billId` is `null` the screen creates a brand-new row on submit.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/navigation/BillsNavigation.kt:63</code></summary>

```kotlin
        composableWithPushTransitions<AddOrEditBillReminderRoute> { backStack ->
            val route = backStack.toRoute<AddOrEditBillReminderRoute>()
            AddOrEditBillReminderScreen(
                onBackClick = { navController.popBackStackSafely() },
                billId = route.billId,
            )
        }
```

</details>

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt`

```kotlin
fun BillRemindersListScreen(
```
The bill-reminder list, grouped by urgency.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/navigation/BillsNavigation.kt:55</code></summary>

```kotlin
    navigation<BillsGraphRoute>(startDestination = BillRemindersListRoute) {
        composableWithPushTransitions<BillRemindersListRoute> {
            BillRemindersListScreen(
                onBackClick = { navController.popBackStackSafely() },
                onAddBillClick = { navController.navigate(AddOrEditBillReminderRoute()) },
                onEditBillClick = { id -> navController.navigate(AddOrEditBillReminderRoute(id)) },
            )
```

</details>

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListViewModel.kt`

```kotlin
class BillRemindersListViewModel(
```
Read-side ViewModel for the Bill Reminders dashboard.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt:37</code></summary>

```kotlin
    single<BillNotificationGateway> { BillNotificationGatewayImpl(get()) }

    viewModel { BillRemindersListViewModel(repository = get(), scheduler = get()) }
    viewModel { (billId: String?) ->
        EditBillReminderViewModel(
            repository = get(),
            scheduler = get(),
```

</details>

```kotlin
data class BillRemindersUiState(
```
Combined dashboard state — surfaced as the `data` slot of a single `ScreenState.Content`.

<details><summary>Used in the template — <code>feature/bills/src/commonTest/kotlin/kpt/feature/bills/ui/BillRemindersListViewModelTest.kt:76</code></summary>

```kotlin
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.screenState.first { it is ScreenState.Content<*> }
        val content = state as ScreenState.Content<BillRemindersUiState>
        assertEquals(2, content.data.all.size)
        // Sum is whatever the fake's "upcoming amount" returns; the fake aggregates ALL rows
        // for simplicity.
        assertEquals(150.0, content.data.totalUpcomingAmount)
```

</details>

```kotlin
sealed interface BillRemindersAction
```
One-shot actions accepted by `BillRemindersListViewModel`.

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/EditBillReminderViewModel.kt`

```kotlin
class EditBillReminderViewModel(
```
**Second multi-formKey `BaseMutationViewModel` (`MutationMode.Draft`) showcase** (after `EditLoanViewModel`).

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt:39</code></summary>

```kotlin
    viewModel { BillRemindersListViewModel(repository = get(), scheduler = get()) }
    viewModel { (billId: String?) ->
        EditBillReminderViewModel(
            repository = get(),
            scheduler = get(),
            billId = billId,
            outbox = get(qualifier = AppOutboxQualifiers.BillReminder),
```

</details>

```kotlin
data class BillReminderFormState(
```
UI form state — observable so the screen re-renders on every keystroke / dropdown change. Defaults to "Electricity bill, $50, day 1, monthly, utilities, enabled, 1 day before".

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/AddOrEditBillReminderScreen.kt:275</code></summary>

```kotlin
@Composable
internal fun BasicInfoSection(
    form: BillReminderFormState,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onAmountChange: (Double) -> Unit,
    onDueDayChange: (Int) -> Unit,
```

</details>

### `feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the bills feature. Consumed by: - Compose UI tests in `feature/bills/src/commonTest/` (`BillRemindersListScreenUiTest`, `AddOrEditBillReminderScreenUiTest`).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt:51</code></summary>

```kotlin
    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
```

</details>

- `const val FAB: String = "bills_list_fab"` — Extended FAB that opens the "New bill" screen.
- `const val SAVE_BUTTON: String = "bills_addedit_save_button"` — Save / submit button at the bottom of the form.

---

_12 type(s), 10 function(s)/property(ies); 22 carry KDoc at source; 0 authored example(s); 10 live call site(s)._
<!-- api-docs:end -->
