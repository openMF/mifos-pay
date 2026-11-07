# `feature/receipt/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 11 tracked files — 4× `.kt`, 3× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**4 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ReceiptScreen`

ViewModels: `ReceiptViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/receipt/README.md` | Download & View Receipt Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/receipt sha=727587b709e8ac85d37f14b6c7309fe853d6b46c -->
## API reference

_Generated from `feature/receipt` at tree `727587b709e8` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/ReceiptViewModel.kt`

```kotlin
class ReceiptViewModel(
```
Reads OFF the shared `TransferDetail` Store5 vertical — the SAME store `TransactionDetailViewModel` (`feature/history`) already consumes via `AccountRepository.getAccountTransferStream`.

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/ReceiptScreen.kt:61</code></summary>

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
One-shot events emitted by `ReceiptViewModel`.

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/ReceiptScreen.kt:68</code></summary>

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
Actions dispatched to `ReceiptViewModel`.

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/ReceiptScreen.kt:82</code></summary>

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
UI state of the receipt screen.

<details><summary>Used in the template — <code>feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/ReceiptScreen.kt:70</code></summary>

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

### `feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/di/ReceiptModule.kt`

```kotlin
val ReceiptModule = module
```
Koin module providing the `kpt.feature.receipt.ReceiptViewModel` for the receipt feature.

### `feature/receipt/src/commonMain/kotlin/kpt/feature/receipt/navigation/ReceiptNavigation.kt`

```kotlin
fun NavGraphBuilder.receiptScreen(
```
Reads `transferId` (see `navigateToReceipt`) via `SavedStateHandle` inside `ReceiptViewModel` — same nav-arg contract `TransactionDetailNavigation.kt` already uses (D13 verdict — `sub-plans/RECEIPT_DATA_SOURCE.md`).

```kotlin
fun NavController.navigateToReceipt(transferId: Long)
```
Navigates to the receipt screen for the transfer identified by `transferId`.

---

_4 type(s), 3 function(s)/property(ies); 7 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
