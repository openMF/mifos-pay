# `feature/transfer-interbank/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 14 tracked files — 9× `.kt`, 2× `.md`, 1× `.gitignore`, 1× `.kts`, 1× `.xml`

**9 Kotlin files** — 5 screen(s), 1 ViewModel(s).

Screens: `InterbankTransferFlowScreen`, `PreviewTransferScreen`, `SearchRecipientScreen`, `SelectAccountScreen`, `TransferDetailsScreen`

ViewModels: `InterbankTransferViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/transfer-interbank/IMPLEMENTATION_SUMMARY.md` | Interbank Transfer Implementation Summary |
| `feature/transfer-interbank/README.md` | Interbank Transfer Module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/transfer-interbank sha=7cbfb56b55971d087f6a60f4ed0e23fbd5d9fd33 -->
## API reference

_Generated from `feature/transfer-interbank` at tree `7cbfb56b5597` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt`

```kotlin
fun InterbankTransferFlowScreen(
```
Main orchestrator screen for the interbank transfer flow Manages navigation between all steps of the transfer process

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/navigation/InterbankTransferNavigation.kt:51</code></summary>

```kotlin
    composable<InterbankTransferRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<InterbankTransferRoute>()
        InterbankTransferFlowScreen(
            onBackClick = onBackClick,
            onTransferSuccess = onTransferSuccess,
            initialPhoneNumber = route.phoneNumber,
            initialAmount = route.amount,
```

</details>

```kotlin
fun InterbankTransferFlowScreenPreview()
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferViewModel.kt`

```kotlin
class InterbankTransferViewModel(
```
ViewModel for managing interbank transfer flow Handles all stages: account selection, recipient search, transfer details, preview, and confirmation

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:54</code></summary>

```kotlin
    initialPhoneNumber: String? = null,
    initialAmount: String? = null,
    viewModel: InterbankTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf(initialPhoneNumber ?: "") }
```

</details>

```kotlin
data class InterbankTransferState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:78</code></summary>

```kotlin

    when (state.currentStep) {
        InterbankTransferState.Step.SelectAccount -> {
            SelectAccountScreen(
                accounts = state.fromAccounts,
                isLoading = state.loadingState is InterbankTransferState.LoadingState.Loading,
                error = (state.loadingState as? InterbankTransferState.LoadingState.Error)?.message,
```

</details>

```kotlin
sealed interface InterbankTransferEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:70</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            InterbankTransferEvent.OnNavigateBack -> onBackClick()
            else -> {
                // Other steps don't require navigation
            }
        }
```

</details>

```kotlin
sealed interface InterbankTransferAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:63</code></summary>

```kotlin
        initialAmount?.let { amount ->
            if (amount.isNotBlank()) {
                viewModel.trySendAction(InterbankTransferAction.UpdateAmount(amount))
            }
        }
    }
```

</details>

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/di/InterbankTransferModule.kt`

```kotlin
val interbankTransferModule = module
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/navigation/InterbankTransferNavigation.kt`

```kotlin
data class InterbankTransferRoute(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToInterbankTransfer(
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.interbankTransferScreen(
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/PreviewTransferScreen.kt`

```kotlin
fun PreviewTransferScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:155</code></summary>

```kotlin

        InterbankTransferState.Step.PreviewTransfer -> {
            PreviewTransferScreen(
                amount = state.transferAmount,
                transferDate = state.transferDate,
                transferDescription = state.transferDescription,
                fromAccountName = state.selectedFromAccount?.name ?: "Unknown",
```

</details>

```kotlin
fun PreviewTransferScreenPreview()
```
_No KDoc at source._

```kotlin
fun PreviewTransferScreenProcessingPreview()
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/SearchRecipientScreen.kt`

```kotlin
fun SearchRecipientScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:100</code></summary>

```kotlin
            val searchError = (searchState as? InterbankTransferState.SearchRecipientState.Error)?.message

            SearchRecipientScreen(
                searchQuery = searchQuery,
                onSearchQueryChanged = { query ->
                    searchQuery = query
                },
```

</details>

```kotlin
fun SearchRecipientScreenPreview()
```
_No KDoc at source._

```kotlin
fun SearchRecipientScreenEmptyPreview()
```
_No KDoc at source._

```kotlin
fun SearchRecipientScreenNoResultsPreview()
```
_No KDoc at source._

```kotlin
fun SearchRecipientScreenLoadingPreview()
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/SelectAccountScreen.kt`

```kotlin
fun SelectAccountScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:79</code></summary>

```kotlin
    when (state.currentStep) {
        InterbankTransferState.Step.SelectAccount -> {
            SelectAccountScreen(
                accounts = state.fromAccounts,
                isLoading = state.loadingState is InterbankTransferState.LoadingState.Loading,
                error = (state.loadingState as? InterbankTransferState.LoadingState.Error)?.message,
                onAccountSelected = { account ->
```

</details>

```kotlin
fun SelectAccountScreenPreview()
```
_No KDoc at source._

```kotlin
fun SelectAccountScreenLoadingPreview()
```
_No KDoc at source._

```kotlin
fun SelectAccountScreenErrorPreview()
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/TransferDetailsScreen.kt`

```kotlin
fun TransferDetailsScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:126</code></summary>

```kotlin

        InterbankTransferState.Step.TransferDetails -> {
            TransferDetailsScreen(
                fromAccount = state.selectedFromAccount,
                recipient = state.selectedParticipantInfo,
                amount = state.transferAmount,
                onAmountChanged = { amount ->
```

</details>

```kotlin
fun TransferDetailsScreenPreview()
```
_No KDoc at source._

```kotlin
fun TransferDetailsScreenEmptyPreview()
```
_No KDoc at source._

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/TransferResultScreens.kt`

```kotlin
fun TransferSuccessScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:180</code></summary>

```kotlin

        InterbankTransferState.Step.TransferSuccess -> {
            TransferSuccessScreen(
                recipientName = "${state.selectedParticipantInfo?.firstName ?: ""} ${state.selectedParticipantInfo?.lastName ?: ""}".trim().ifEmpty { "Recipient" },
                amount = state.transferAmount,
                transactionReference = state.transferResponse ?: "N/A",
                fromAccount = state.selectedFromAccount?.number ?: "N/A",
```

</details>

```kotlin
fun TransferFailedScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/InterbankTransferFlowScreen.kt:197</code></summary>

```kotlin

        InterbankTransferState.Step.TransferFailed -> {
            TransferFailedScreen(
                errorMessage = state.errorMessage ?: "Unknown error occurred",
                errorTitle = "Transfer Failed",
                attemptedAmount = CurrencyFormatter.format(
                    state.transferAmount.toDoubleOrNull() ?: 0.0,
```

</details>

```kotlin
fun TransferSuccessScreenPreview()
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/transfer-intrabank/src/commonMain/kotlin/kpt/feature/transfer/intrabank/success/TransferSuccessScreen.kt:299</code></summary>

```kotlin
@Preview
@Composable
private fun TransferSuccessScreenPreview() {
    KptTheme {
        TransferSuccessScreen(
            transferResult = TransferResult(
                transactionId = "TXN-20250217-001",
```

</details>

```kotlin
fun TransferFailedScreenPreview()
```
_No KDoc at source._

---

_5 type(s), 24 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 12 live call site(s)._
<!-- api-docs:end -->
