# `feature/transfer-intrabank/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 28 tracked files — 17× `.kt`, 8× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**17 Kotlin files** — 4 screen(s), 3 ViewModel(s).

Screens: `IntraBankHubScreen`, `SelectPayeeScreen`, `TransferConfirmScreen`, `TransferSuccessScreen`

ViewModels: `IntraBankHubViewModel`, `SelectScreenViewModel`, `TransferConfirmViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/transfer-intrabank/README.md` | Send Money |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/transfer-intrabank sha=ebe505c8eec60d924a6e7f54ffefeeb416325651 -->
## API reference

_Generated from `feature/transfer-intrabank` at tree `ebe505c8eec6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt`

```kotlin
fun ClientCard(
```
Card showing a client's name and account number with an edit action.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmViewModel.kt`

```kotlin
data class TransferResult(
```
Transfer result data containing all the details of a successful transfer.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt:139</code></summary>

```kotlin
internal fun TransferConfirmScreen(
    navigateBack: () -> Unit,
    onTransferSuccess: (TransferResult) -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
    entryStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewModel: TransferConfirmViewModel = koinViewModel(),
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/di/IntraBankModule.kt`

```kotlin
val IntraBankModule = module
```
Koin module providing the intra-bank transfer view models.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt`

```kotlin
fun IntraBankHubScreen(
```
Intra-bank transfer hub with quick actions, recent payees and saved beneficiaries.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/navigation/IntraBankHubNavigation.kt:47</code></summary>

```kotlin
) {
    composable<IntraBankHubRoute> {
        IntraBankHubScreen(
            navigateToSelectAccountScreen = navigateToSelectAccountScreen,
            navigateBack = navigateBack,
            navigateToBeneficiary = navigateToBeneficiary,
            navigateToTransferConfirm = navigateToTransferConfirm,
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubViewModel.kt`

```kotlin
class IntraBankHubViewModel(
```
ViewModel for the intra-bank hub; loads recent payees, beneficiaries and the transfer template and handles hub actions.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/di/IntraBankModule.kt:19</code></summary>

```kotlin
/** Koin module providing the intra-bank transfer view models. */
val IntraBankModule = module {
    viewModelOf(::IntraBankHubViewModel)
    viewModelOf(::SelectScreenViewModel)
    viewModelOf(::TransferConfirmViewModel)
}
```

</details>

```kotlin
enum class PayeeTab
```
Tabs of the payee list on the hub.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:184</code></summary>

```kotlin
            // Content based on selected tab
            when (state.selectedTab) {
                PayeeTab.Recents -> {
                    when (state.recentPayeesState) {
                        RecentPayeesState.Loading -> {
                            item {
                                Box(
```

</details>

```kotlin
data class IntraBankHubState(
```
UI state of the intra-bank hub.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:125</code></summary>

```kotlin
@Composable
private fun IntraBankHubScreenContent(
    state: IntraBankHubState,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onAction: (IntraBankHubAction) -> Unit,
) {
```

</details>

```kotlin
enum class RecentPayeesState
```
Loading state of the recent payees list.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:186</code></summary>

```kotlin
                PayeeTab.Recents -> {
                    when (state.recentPayeesState) {
                        RecentPayeesState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
                                    contentAlignment = Alignment.Center,
```

</details>

```kotlin
enum class BeneficiariesState
```
Loading state of the beneficiaries list.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:234</code></summary>

```kotlin
                PayeeTab.Beneficiaries -> {
                    when (state.beneficiariesState) {
                        BeneficiariesState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
                                    contentAlignment = Alignment.Center,
```

</details>

```kotlin
sealed interface IntraBankHubEvent
```
One-shot navigation events emitted by `IntraBankHubViewModel`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:81</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            IntraBankHubEvent.NavigateToSearchAccountSelection -> navigateToSelectAccountScreen()

            IntraBankHubEvent.NavigateBack -> navigateBack()

            IntraBankHubEvent.NavigateToBeneficiary -> navigateToBeneficiary()
```

</details>

```kotlin
sealed interface IntraBankHubAction
```
Actions handled by `IntraBankHubViewModel`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:119</code></summary>

```kotlin

    LaunchedEffect(Unit) {
        viewModel.trySendAction(IntraBankHubAction.RefreshBeneficiaries)
    }
}

@Composable
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/BeneficiaryCard.kt`

```kotlin
fun BeneficiaryCard(
```
Card showing a saved beneficiary with a pay button.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:247</code></summary>

```kotlin
                        BeneficiariesState.Success -> {
                            items(state.beneficiaries, key = { it.id }) { beneficiary ->
                                BeneficiaryCard(
                                    beneficiary = beneficiary,
                                    onPayClick = {
                                        onAction(IntraBankHubAction.OnPayBeneficiary(beneficiary))
                                    },
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/EmptyBeneficiariesState.kt`

```kotlin
fun EmptyBeneficiariesState(
```
Placeholder shown when there are no beneficiaries, with an optional add-payee action.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:258</code></summary>

```kotlin
                        BeneficiariesState.Empty -> {
                            item {
                                EmptyBeneficiariesState(
                                    onAddPayeeClick = {
                                        onAction(IntraBankHubAction.OnAddPayeeClicked)
                                    },
                                )
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/EmptyRecentsState.kt`

```kotlin
fun EmptyRecentsState(
```
Placeholder shown when there are no recent payees, with an optional add-payee action.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:218</code></summary>

```kotlin
                        RecentPayeesState.Empty -> {
                            item {
                                EmptyRecentsState(
                                    onAddPayeeClick = {
                                        onAction(IntraBankHubAction.OnAddPayeeClicked)
                                    },
                                )
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/QuickActionsGrid.kt`

```kotlin
fun QuickActionsGrid(
```
Grid of quick actions on the hub: add payee, history, scan QR and request money.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:166</code></summary>

```kotlin

            item {
                QuickActionsGrid(
                    onAddPayeeClick = { onAction(IntraBankHubAction.OnAddPayeeClicked) },
                    onHistoryClick = { onAction(IntraBankHubAction.OnHistoryClicked) },
                    onScanQrClick = { onAction(IntraBankHubAction.OnScanQrClicked) },
                    onRequestMoneyClick = { onAction(IntraBankHubAction.OnRequestMoneyClicked) },
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/components/RecentPayeeCard.kt`

```kotlin
fun RecentPayeeCard(
```
Card showing a recent payee with a pay button.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:199</code></summary>

```kotlin
                        RecentPayeesState.Success -> {
                            items(state.recentPayees, key = { it.clientId }) { payee ->
                                RecentPayeeCard(
                                    payee = payee,
                                    onPayClick = {
                                        onAction(IntraBankHubAction.OnPayRecentPayee(payee))
                                    },
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/navigation/IntraBankHubNavigation.kt`

```kotlin
data object IntraBankHubRoute
```
Navigation route of the intra-bank hub.

```kotlin
fun NavController.navigateToIntraBankHub(navOptions: NavOptions? = null)
```
Navigates to the intra-bank hub.

```kotlin
fun NavGraphBuilder.intraBankHubScreen(
```
Registers the intra-bank hub destination and its navigation callbacks.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/navigation/TransferConfirmNavigation.kt`

```kotlin
data class TransferConfirmRoute(
```
Type-safe route for the intra-bank transfer confirmation screen. Carries the source/destination account snapshot and the post-success destination the caller wants to return to (`home` or `payments`).

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmViewModel.kt:100</code></summary>

```kotlin
) : BaseViewModel<TransferConfirmState, TransferConfirmEvent, TransferConfirmAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<TransferConfirmRoute>()
        TransferConfirmState(
            toOfficeId = route.toOfficeId,
            toClientId = route.toClientId,
            toAccountType = route.toAccountTypeId,
```

</details>

```kotlin
fun NavController.navigateToTransferConfirm(
```
Push the transfer-confirm screen with a fully-populated `TransferConfirmRoute`.

```kotlin
fun NavGraphBuilder.transferConfirmScreen(
```
Registers the transfer-confirm destination. Hoists the destination's own `SavedStateHandle` into `TransferConfirmScreen` so the `INTRA_BANK_TRANSFER_VERIFICATION_KEY` round-trip channel works.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt`

```kotlin
fun SelectPayeeScreen(
```
Screen for choosing the destination account by number or name and proceeding to the transfer.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectScreenNavigation.kt:52</code></summary>

```kotlin
    composable<SelectAccountRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<SelectAccountRoute>()
        SelectPayeeScreen(
            navigateToTransferConfirm = { toOfficeId, toClientId, toAccountTypeId, toAccountId, amount, accountName, accountNo ->
                navigateToTransferConfirm(toOfficeId, toClientId, toAccountTypeId, toAccountId, amount, accountName, accountNo, route.returnDestination)
            },
            navigateBack = navigateBack,
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectScreenNavigation.kt`

```kotlin
data class SelectAccountRoute(
```
Navigation route of the select-account screen.

```kotlin
fun NavController.navigateToSelectAccountScreen(
```
Navigates to the select-account screen.

```kotlin
fun NavGraphBuilder.selectAccountScreenDestination(
```
Registers the select-account destination and its navigation callbacks.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectScreenViewModel.kt`

```kotlin
class SelectScreenViewModel(
```
ViewModel for choosing a destination account; loads the destination options and filters them by the entered text.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/di/IntraBankModule.kt:20</code></summary>

```kotlin
val IntraBankModule = module {
    viewModelOf(::IntraBankHubViewModel)
    viewModelOf(::SelectScreenViewModel)
    viewModelOf(::TransferConfirmViewModel)
}
```

</details>

```kotlin
data class SelectScreenState(
```
UI state of the select-account screen.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:126</code></summary>

```kotlin
@Composable
private fun SelectAccountScreen(
    state: SelectScreenState,
    modifier: Modifier = Modifier,
    onAction: (SelectScreenAction) -> Unit,
) {
    ForkGradientBackground {
```

</details>

```kotlin
sealed interface SelectScreenEvent
```
One-shot events emitted by `SelectScreenViewModel`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:98</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is SelectScreenEvent.NavigateToTransferScreen -> {
                navigateToTransferConfirm(
                    state.selectedAccount?.officeId,
                    state.selectedAccount?.clientId,
                    state.selectedAccount?.accountType?.id ?: 0,
```

</details>

```kotlin
sealed interface SelectScreenAction
```
Actions handled by `SelectScreenViewModel`.

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:128</code></summary>

```kotlin
    state: SelectScreenState,
    modifier: Modifier = Modifier,
    onAction: (SelectScreenAction) -> Unit,
) {
    ForkGradientBackground {
        ForkScaffold(
            modifier = modifier,
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/success/TransferSuccessNavigation.kt`

```kotlin
data class TransferSuccessRoute(
```
Navigation route of the transfer success screen.

```kotlin
fun NavGraphBuilder.transferSuccessScreen(
```
Registers the transfer success destination.

```kotlin
fun NavController.navigateTransferSuccess(
```
Navigates to the transfer success screen for `transferResult`.

---

_16 type(s), 17 function(s)/property(ies); 33 carry KDoc at source; 0 authored example(s); 20 live call site(s)._
<!-- api-docs:end -->
