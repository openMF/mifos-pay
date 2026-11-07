# `feature/accounts/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 10× `.kt`, 8× `.xml`, 6× `.png`, 1× `.gitignore`, 1× `.md`

**10 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `AccountsScreen`, `AddEditSavingAccountScreen`, `SavingAccountDetailScreen`

ViewModels: `AccountViewModel`, `AddEditSavingViewModel`, `SavingAccountDetailViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/accounts/README.md` | Accounts |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/accounts sha=5b9766c77f1f015cc662ee2dadd8bb4474a97273 -->
## API reference

_Generated from `feature/accounts` at tree `5b9766c77f1f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt`

```kotlin
class AccountViewModel(
```
Loads the user's savings accounts and beneficiaries, and handles default-account, beneficiary and navigation actions.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:103</code></summary>

```kotlin
    onApplyForLoanClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
```

</details>

```kotlin
data class AccountState(
```
UI state of the accounts screen.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:156</code></summary>

```kotlin
internal fun AccountsScreenContent(
    defaultAccountId: Long?,
    state: AccountState.ViewState,
    onAction: (AccountAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
```

</details>

```kotlin
sealed interface AccountEvent
```
One-off events emitted by the accounts screen.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:113</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }

            is AccountEvent.OnNavigateToAccountDetail -> {
```

</details>

```kotlin
sealed interface AccountAction
```
Actions the accounts screen can send to its view model.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt:140</code></summary>

```kotlin
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AccountAction.DismissDialog) }
        },
    )

    AccountsScreenContent(
```

</details>

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountsScreen.kt`

```kotlin
fun AccountsScreen(
```
Screen listing the user's savings accounts and beneficiaries with add, edit and detail actions.

```kotlin
enum class SavingAccountStatus(
```
Savings account lifecycle statuses, each mapped to a status predicate and label.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailScreen.kt:475</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    val activeStatuses = SavingAccountStatus.entries.filter { it.isActive(status) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
```

</details>

```kotlin
val SavingAccountStatus.color: Color
```
Chip container color used to display this status.

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/di/AccountsModule.kt`

```kotlin
val AccountsModule = module
```
Koin module providing the accounts feature view models and its Json instance.

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingNavigation.kt`

```kotlin
data class SavingAccountAddEditArgs(
```
Navigation arguments of the add/edit savings account screen.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/AddEditSavingViewModel.kt:64</code></summary>

```kotlin
    initialState = savedStateHandle.getSerialized(ADD_EDIT_SAVING_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val type = SavingAccountAddEditArgs(savedStateHandle).savingsAddEditType

        AESState(
            clientId = clientId,
            type = type,
```

</details>

```kotlin
fun NavGraphBuilder.addEditSavingAccountScreen(
```
Registers the add/edit savings account destination.

```kotlin
fun NavController.navigateToSavingAccountAddEdit(
```
Navigates to the add/edit savings account screen for `addEditType`.

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/SavingsAddEditType.kt`

```kotlin
sealed class SavingsAddEditType
```
Whether the savings account form creates a new account or edits an existing one.

<details><summary>Used in the template — <code>feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/AccountViewModel.kt:170</code></summary>

```kotlin
        when (action) {
            is AccountAction.CreateSavingsAccount -> {
                sendEvent(OnAddEditSavingsAccount(SavingsAddEditType.AddItem))
            }

            is AccountAction.EditSavingsAccount -> {
                sendEvent(OnAddEditSavingsAccount(SavingsAddEditType.EditItem(action.accountId)))
```

</details>

### `feature/accounts/src/commonMain/kotlin/kpt/feature/accounts/savingsaccount/details/SavingAccountDetailNavigation.kt`

```kotlin
fun NavGraphBuilder.savingAccountDetailRoute(
```
Registers the savings account detail destination.

```kotlin
fun NavController.navigateToSavingAccountDetails(accountId: Long)
```
Navigates to the details of the savings account with `accountId`.

---

_7 type(s), 7 function(s)/property(ies); 14 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
