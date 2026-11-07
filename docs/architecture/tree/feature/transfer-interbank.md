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

<!-- api-docs:begin module=feature/transfer-interbank sha=6a1df2f4b873c414cf7ba7f66ef15058fbcd4a7e -->
## API reference

_Generated from `feature/transfer-interbank` at tree `6a1df2f4b873` by `scripts/docs/api-docs-gen.sh`._
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

<details><summary>Used in the template — <code>feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/navigation/InterbankTransferNavigation.kt:60</code></summary>

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
Compose preview of `InterbankTransferFlowScreen`.

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
UI state of the interbank transfer flow.

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
One-shot events emitted by `InterbankTransferViewModel`.

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
Actions handled by `InterbankTransferViewModel`.

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
Koin module providing the interbank transfer view model.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/navigation/InterbankTransferNavigation.kt`

```kotlin
data class InterbankTransferRoute(
```
Navigation route of the interbank transfer flow, optionally prefilled from a scanned QR code.

```kotlin
fun NavController.navigateToInterbankTransfer(
```
Navigates to the interbank transfer flow with optional prefilled recipient details.

```kotlin
fun NavGraphBuilder.interbankTransferScreen(
```
Registers the interbank transfer destination.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/PreviewTransferScreen.kt`

```kotlin
fun PreviewTransferScreen(
```
Step showing a summary of the transfer for the user to review and confirm.

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
Compose preview of `PreviewTransferScreen`.

```kotlin
fun PreviewTransferScreenProcessingPreview()
```
Compose preview of `PreviewTransferScreen` while the transfer is processing.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/SearchRecipientScreen.kt`

```kotlin
fun SearchRecipientScreen(
```
Step for searching a recipient by phone number and choosing from the results.

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
Compose preview of `SearchRecipientScreen` with results.

```kotlin
fun SearchRecipientScreenEmptyPreview()
```
Compose preview of `SearchRecipientScreen` with no query.

```kotlin
fun SearchRecipientScreenNoResultsPreview()
```
Compose preview of `SearchRecipientScreen` with no matching recipients.

```kotlin
fun SearchRecipientScreenLoadingPreview()
```
Compose preview of `SearchRecipientScreen` while searching.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/SelectAccountScreen.kt`

```kotlin
fun SelectAccountScreen(
```
Step for choosing the account to transfer from.

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
Compose preview of `SelectAccountScreen` with accounts.

```kotlin
fun SelectAccountScreenLoadingPreview()
```
Compose preview of `SelectAccountScreen` while loading.

```kotlin
fun SelectAccountScreenErrorPreview()
```
Compose preview of `SelectAccountScreen` in the error state.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/TransferDetailsScreen.kt`

```kotlin
fun TransferDetailsScreen(
```
Step for entering the transfer amount, date and description.

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
Compose preview of `TransferDetailsScreen` with details filled in.

```kotlin
fun TransferDetailsScreenEmptyPreview()
```
Compose preview of `TransferDetailsScreen` with empty fields.

### `feature/transfer-interbank/src/commonMain/kotlin/kpt/feature/transfer/interbank/screens/TransferResultScreens.kt`

```kotlin
fun TransferSuccessScreen(
```
Result screen shown after a successful transfer, with a receipt of the transaction.

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
Result screen shown when a transfer fails, with the reason and attempted details.

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
Compose preview of `TransferSuccessScreen`.

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
Compose preview of `TransferFailedScreen`.

---

_5 type(s), 24 function(s)/property(ies); 29 carry KDoc at source; 0 authored example(s); 12 live call site(s)._
<!-- api-docs:end -->
