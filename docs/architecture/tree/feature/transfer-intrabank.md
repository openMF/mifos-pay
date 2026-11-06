# `feature/transfer-intrabank/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 28 tracked files — 17× `.kt`, 8× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**17 Kotlin files** — 4 screen(s), 3 ViewModel(s).

Screens: `IntraBankHubScreen`, `SelectPayeeScreen`, `TransferConfirmScreen`, `TransferSuccessScreen`

ViewModels: `IntraBankHubViewModel`, `SelectScreenViewModel`, `TransferConfirmViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/transfer-intrabank/README.md` | Send Money |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/transfer-intrabank sha=344141e1e2bd0851da2cfff441e3e0e9a2f2a9b2 -->
## API reference

_Generated from `feature/transfer-intrabank` at tree `344141e1e2bd` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/confirm/TransferConfirmScreen.kt`

```kotlin
fun ClientCard(
```
_No KDoc at source._

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
_No KDoc at source._

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt`

```kotlin
fun IntraBankHubScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/navigation/IntraBankHubNavigation.kt:44</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/di/IntraBankModule.kt:18</code></summary>

```kotlin
import org.koin.dsl.module
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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:183</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:124</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:185</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:233</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:80</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:118</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:246</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:257</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:217</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:165</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/hub/IntraBankHubScreen.kt:198</code></summary>

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
_No KDoc at source._

```kotlin
fun NavController.navigateToIntraBankHub(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.intraBankHubScreen(
```
_No KDoc at source._

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectScreenNavigation.kt:46</code></summary>

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
_No KDoc at source._

```kotlin
fun NavController.navigateToSelectAccountScreen(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.selectAccountScreenDestination(
```
_No KDoc at source._

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectScreenViewModel.kt`

```kotlin
class SelectScreenViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/di/IntraBankModule.kt:19</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:125</code></summary>

```kotlin
@Composable
private fun SelectAccountScreen(
    state: SelectScreenState,
    modifier: Modifier = Modifier,
    onAction: (SelectScreenAction) -> Unit,
) {
    MifosGradientBackground {
```

</details>

```kotlin
sealed interface SelectScreenEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:97</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/selectScreen/SelectPayeeScreen.kt:127</code></summary>

```kotlin
    state: SelectScreenState,
    modifier: Modifier = Modifier,
    onAction: (SelectScreenAction) -> Unit,
) {
    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
```

</details>

### `feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/success/TransferSuccessNavigation.kt`

```kotlin
data class TransferSuccessRoute(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.transferSuccessScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateTransferSuccess(
```
_No KDoc at source._

---

_16 type(s), 17 function(s)/property(ies); 4 carry KDoc at source; 0 authored example(s); 20 live call site(s)._
<!-- api-docs:end -->
