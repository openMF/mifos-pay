# `feature/receipt/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 11 tracked files — 4× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**4 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ReceiptScreen`

ViewModels: `ReceiptViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/receipt/README.md` | Download & View Receipt Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/receipt sha=dd48380f311de5096a57ec47d0d70200defbeb19 -->
## API reference

_Generated from `feature/receipt` at tree `dd48380f311d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/ReceiptViewModel.kt`

```kotlin
class ReceiptViewModel(
```
Reads OFF the shared `TransferDetail` Store5 vertical — the SAME store `TransactionDetailViewModel` (`feature/history`) already consumes via `AccountRepository.getAccountTransferStream`.

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/ReceiptScreen.kt:62</code></summary>

```kotlin
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptViewModel = koinViewModel(),
) {
    val receiptUiState by viewModel.receiptUiState.collectAsState()
    val scope = rememberCoroutineScope()
```

</details>

```kotlin
sealed interface ReceiptEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/ReceiptScreen.kt:69</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            ReceiptEvent.OnNavigateBack -> onBackClick()
            ReceiptEvent.OnShareReceipt -> {
                val text = (receiptUiState as? ReceiptUiState.Success)?.let {
                    formatReceiptShareText(it.transferDetail)
                }
```

</details>

```kotlin
sealed interface ReceiptAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/ReceiptScreen.kt:83</code></summary>

```kotlin
    ReceiptScreen(
        uiState = receiptUiState,
        onBackClick = { viewModel.trySendAction(ReceiptAction.NavigateBack) },
        onShareClick = { viewModel.trySendAction(ReceiptAction.ShareReceipt) },
        modifier = modifier,
    )
}
```

</details>

```kotlin
sealed interface ReceiptUiState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/ReceiptScreen.kt:71</code></summary>

```kotlin
            ReceiptEvent.OnNavigateBack -> onBackClick()
            ReceiptEvent.OnShareReceipt -> {
                val text = (receiptUiState as? ReceiptUiState.Success)?.let {
                    formatReceiptShareText(it.transferDetail)
                }
                if (text != null) {
                    scope.launch { ShareUtils.shareText(text) }
```

</details>

### `feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/di/ReceiptModule.kt`

```kotlin
val ReceiptModule = module
```
_No KDoc at source._

### `feature/receipt/src/commonMain/kotlin/org/mifospay/feature/receipt/navigation/ReceiptNavigation.kt`

```kotlin
fun NavGraphBuilder.receiptScreen(
```
Reads `transferId` (see `navigateToReceipt`) via `SavedStateHandle` inside `ReceiptViewModel` — same nav-arg contract `TransactionDetailNavigation.kt` already uses (D13 verdict — `sub-plans/RECEIPT_DATA_SOURCE.md`).

```kotlin
fun NavController.navigateToReceipt(transferId: Long)
```
_No KDoc at source._

---

_4 type(s), 3 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
