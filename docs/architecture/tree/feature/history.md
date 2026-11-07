# `feature/history/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 16× `.kt`, 12× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**16 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `HistoryScreen`, `SpecificTransactionsScreen`, `TransactionDetailScreen`

ViewModels: `HistoryViewModel`, `SpecificTransactionsViewModel`, `TransactionDetailViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/history/README.md` | History Module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/history sha=c4ceede8ac259956cdb6b52bb6565928aaadd6d1 -->
## API reference

_Generated from `feature/history` at tree `c4ceede8ac25` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt`

```kotlin
fun HistoryScreen(
```
Transaction history screen with type and account filters and PDF export; `viewTransferDetail` opens a transfer.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/HistoryNavigation.kt:25</code></summary>

```kotlin
) {
    composable(HISTORY_ROUTE) {
        HistoryScreen(
            viewTransferDetail = viewTransactionDetail,
        )
    }
}
```

</details>

### `feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt`

```kotlin
class HistoryViewModel
```
ViewModel that loads, filters and exports (as PDF) the client's transaction history.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:65</code></summary>

```kotlin
    viewTransferDetail: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
```

</details>

```kotlin
data class HistoryState(
```
UI state of the history screen.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:97</code></summary>

```kotlin
@Composable
internal fun HistoryScreenContent(
    state: HistoryState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HistoryAction) -> Unit,
) {
```

</details>

```kotlin
sealed interface HistoryEvent
```
One-off events emitted by `HistoryViewModel`.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:73</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is HistoryEvent.OnTransactionDetail -> {
                state.selectedAccount?.id?.let { accountId ->
                    viewTransferDetail.invoke(accountId, event.transferId)
                }
            }
```

</details>

```kotlin
sealed interface HistoryAction
```
User actions handled by `HistoryViewModel`.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:100</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HistoryAction) -> Unit,
) {
    ForkScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
```

</details>

### `feature/history/src/commonMain/kotlin/kpt/feature/history/components/PdfGenerator.kt`

```kotlin
object HistoryHtmlTemplate
```
Builds the HTML document used to render the transaction history as a PDF.

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt:266</code></summary>

```kotlin
        mutableStateFlow.update { it.copy(isExportingPdf = true) }
        try {
            val html = HistoryHtmlTemplate.generate(
                accountNumber = state.selectedAccount?.number.orEmpty(),
                transactionType = state.selectedTransactionType.name,
                transactions = content.list,
            )
```

</details>

- `fun generate(` — Returns the HTML statement for `accountNumber` and `transactionType` listing the given `transactions`.
- `val document = createHTML().html`
- `val instant = kotlin.time.Clock.System.now()`

### `feature/history/src/commonMain/kotlin/kpt/feature/history/di/HistoryModule.kt`

```kotlin
val HistoryModule = module
```
Koin module providing the history and specific-transactions ViewModels plus the platform PDF module.

### `feature/history/src/commonMain/kotlin/kpt/feature/history/di/PdfGeneratorModule.kt`

```kotlin
expect val HistoryPdfModule: Module
```
Provides a platform-appropriate `PdfGenerator` singleton (e.g. bound to an Android `Context` via `createPdfGenerator(context)` on Android).

<details><summary>Used in the template — <code>feature/history/src/androidMain/kotlin/kpt/feature/history/di/HistoryAndroidModule.kt:58</code></summary>

```kotlin
/** Android PDF module used by the history feature. */
@OptIn(ExperimentalPdfGeneratorApi::class)
actual val HistoryPdfModule: Module
    get() = HistoryPdfAndroidModule
```

</details>

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/HistoryNavigation.kt`

```kotlin
fun NavGraphBuilder.historyNavigation(
```
Registers the history screen in the nav graph; `viewTransactionDetail` opens a transfer (two ids).

```kotlin
fun NavController.navigateToHistory(navOptions: NavOptions? = null)
```
Navigates to the history screen, optionally with `navOptions`.

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/SpecificTransactionsNavigation.kt`

```kotlin
fun NavGraphBuilder.specificTransactionsScreen(
```
Registers the specific-transactions screen in the nav graph.

```kotlin
fun NavController.navigateToSpecificTransaction(accountId: Long, transactionId: Long)
```
Navigates to the specific-transactions screen for the given account and transaction.

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/TransactionDetailNavigation.kt`

```kotlin
fun NavGraphBuilder.transactionDetailNavigation(
```
Registers the transaction detail screen in the nav graph; `navigateToReceipt` shares a transfer receipt.

```kotlin
fun NavHostController.navigateToTransactionDetail(transferId: Long)
```
Navigates to the detail screen of the transfer with `transferId`.

---

_5 type(s), 12 function(s)/property(ies); 15 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
