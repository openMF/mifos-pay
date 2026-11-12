# `feature/loans/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 48 tracked files — 26× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**26 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `AddOrEditLoanScreen`, `LoanDetailScreen`, `PersonalLoansListScreen`

ViewModels: `EditLoanViewModel`, `LoanDetailViewModel`, `PersonalLoansListViewModel`

**Consumes:** `core/store/banking`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/loans/README.md` | :feature:loans |

<!-- tree-scaffold:end -->
## Significance

Personal loan tracking — and the template's reference for a **paging list plus a draft-backed edit
form**. `PersonalLoansListScreen` is `PagingScreenContent`; `AddOrEditLoanScreen` is
`DraftSubmitHandler`, so a part-filled loan survives process death.

It is a *tracker*, not a ledger: `principalRemaining`, `monthsRemaining` and `totalPaid` are
user-maintained, not derived from a payment history. That is a deliberate scope line — a "log a payment"
feature would recompute them, and until it exists the fields must not pretend to be computed.

<!-- api-docs:begin module=feature/loans sha=22c8da3f13f14a956a3c37ee33ae240728e17a72 -->
## API reference

_Generated from `feature/loans` at tree `22c8da3f13f1` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/LoanReminderUseCase.kt`

```kotlin
public class LoanReminderUseCase(
```
Cross-module proof-of-concept demonstrating consumer use of `WorkScheduler`.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt:53</code></summary>

```kotlin

    // Cross-module use case — depends on WorkScheduler from kpt.sync.di.SyncModule.
    singleOf(::LoanReminderUseCase)
}
```

</details>

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt`

```kotlin
val LoansModule = module
```
Koin module for the personal-loans feature. Wired into the app graph by `cmp-navigation/.../KoinModules.kt` (`featureModule.includes(LoansModule)`).

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt`

```kotlin
data object LoansGraphRoute
```
Route for the loans nested graph.

```kotlin
data object PersonalLoansListRoute
```
Route for the loan list — the graph's start destination.

```kotlin
data class LoanDetailRoute(val loanId: String)
```
Route for one loan's detail.

```kotlin
data class AddOrEditLoanRoute(val loanId: String? = null)
```
Route for the add/edit form. One route for both, since the form is identical and the id decides which.

```kotlin
fun NavController.navigateToLoans(navOptions: NavOptions? = null)
```
Navigates to the loans graph.

```kotlin
fun NavGraphBuilder.loansGraph(navController: NavController)
```
Registers the loans nested graph.

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/AddOrEditLoanScreen.kt`

```kotlin
fun AddOrEditLoanScreen(
```
Combined add/edit screen. `loanId == null` → add a brand-new loan. `loanId != null` → edit.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt:92</code></summary>

```kotlin
        composableWithPushTransitions<AddOrEditLoanRoute> { entry ->
            val route = entry.toRoute<AddOrEditLoanRoute>()
            AddOrEditLoanScreen(
                loanId = route.loanId,
                onBackClick = { navController.popBackStackSafely() },
                onSaved = { navController.popBackStackSafely() },
            )
```

</details>

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/EditLoanViewModel.kt`

```kotlin
class EditLoanViewModel(
```
**The canonical multi-formKey `BaseMutationViewModel` (`MutationMode.Draft`) showcase.** Each instance is scoped to a single loan via `uniqueKey = loanId`.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt:45</code></summary>

```kotlin
    }
    viewModel { params ->
        EditLoanViewModel(
            repository = get(),
            outbox = get(qualifier = AppOutboxQualifiers.Loan),
            loanId = params.getOrNull(),
        )
```

</details>

```kotlin
data class LoanFormState(
```
Multi-field form state for the add/edit loan screen. Default values render a sensible "blank form" — kind defaults to PERSONAL, dates default to a stable epoch so the form is deterministic in tests.

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailScreen.kt`

```kotlin
fun LoanDetailScreen(
```
One loan's detail — figures, next due-date and a link to its amortization schedule.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt:79</code></summary>

```kotlin
        composableWithPushTransitions<LoanDetailRoute> { entry ->
            val route = entry.toRoute<LoanDetailRoute>()
            LoanDetailScreen(
                loanId = route.loanId,
                onBackClick = { navController.popBackStackSafely() },
                onEditClick = { loanId ->
                    navController.navigate(AddOrEditLoanRoute(loanId = loanId))
```

</details>

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/LoanDetailViewModel.kt`

```kotlin
class LoanDetailViewModel(
```
Read-side ViewModel for `LoanDetailScreen`.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt:42</code></summary>

```kotlin
    viewModel { PersonalLoansListViewModel(repository = get()) }
    viewModel { params ->
        LoanDetailViewModel(repository = get(), loanId = params.get())
    }
    viewModel { params ->
        EditLoanViewModel(
            repository = get(),
```

</details>

```kotlin
sealed interface LoanDetailAction
```
Action sealed-hierarchy for the loan-detail MVI loop.

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/PersonalLoansListScreen.kt`

```kotlin
fun PersonalLoansListScreen(
```
The loan list, with per-row swipe actions and a summary header.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/navigation/LoansNavigation.kt:71</code></summary>

```kotlin
    navigation<LoansGraphRoute>(startDestination = PersonalLoansListRoute) {
        composableWithPushTransitions<PersonalLoansListRoute> {
            PersonalLoansListScreen(
                onBackClick = { navController.popBackStackSafely() },
                onAddLoanClick = { navController.navigate(AddOrEditLoanRoute()) },
                onLoanClick = { loanId -> navController.navigate(LoanDetailRoute(loanId)) },
            )
```

</details>

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/PersonalLoansListViewModel.kt`

```kotlin
class PersonalLoansListViewModel(
```
Read-side ViewModel for `PersonalLoansListScreen`.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/di/LoansModule.kt:40</code></summary>

```kotlin
 */
val LoansModule = module {
    viewModel { PersonalLoansListViewModel(repository = get()) }
    viewModel { params ->
        LoanDetailViewModel(repository = get(), loanId = params.get())
    }
    viewModel { params ->
```

</details>

```kotlin
data class LoansListUiState(
```
Aggregated read-model for the loans list screen: the user-visible loans plus the consolidated totals tile rendered at the top of the screen.

<details><summary>Used in the template — <code>feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/PersonalLoansListScreen.kt:181</code></summary>

```kotlin

@Composable
internal fun SummaryHero(ui: LoansListUiState) {
    HeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            AmountDisplay(
                amountText = formatMoney(ui.totalPrincipalRemaining),
```

</details>

```kotlin
sealed interface LoansListAction
```
Action sealed-hierarchy for the loans-list MVI loop.

### `feature/loans/src/commonMain/kotlin/kpt/feature/loans/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the personal-loans feature. Consumed by: - Per-screen Compose UI tests in `feature/loans/src/commonTest/` (`PersonalLoansListScreenUiTest`, `LoanDetailScreenUiTest`, `AddOrEditLoanScreenUiTest`).

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

- `const val LIST: String = "loans_list"` — The scrollable `LazyColumn` hosting the loan rows.
- `const val ROW: String = "loans_list_row"` — Per-row tag prefix. Full tag = `"${ROW}_${loan.id}"` — allowing Maestro / Compose tests to pick a specific row without needing a synthetic label.
- `const val FAB: String = "loans_list_fab"` — Extended FAB that opens the "New loan" screen.
- `const val DELETE_CONFIRM: String = "loans_list_delete_confirm"` — Confirm button in the delete-loan alert dialog.
- `const val SCAFFOLD: String = "loan_detail_scaffold"` — Root `Scaffold` — always visible once the screen is composed.
- `const val SCAFFOLD: String = "addedit_loan_scaffold"` — Root `Scaffold` — always visible once the screen is composed.

---

_13 type(s), 12 function(s)/property(ies); 25 carry KDoc at source; 0 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->
