# `feature/history/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 16× `.kt`, 12× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**16 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `HistoryScreen`, `SpecificTransactionsScreen`, `TransactionDetailScreen`

ViewModels: `HistoryViewModel`, `SpecificTransactionsViewModel`, `TransactionDetailViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/history/README.md` | History Module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/history sha=376c3cec73abe3bec2b57be08442ec654afb1d7b -->
## API reference

_Generated from `feature/history` at tree `376c3cec73ab` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt`

```kotlin
fun HistoryScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/HistoryNavigation.kt:23</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:64</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:96</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:72</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryScreen.kt:99</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HistoryAction) -> Unit,
) {
    MifosScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
```

</details>

### `feature/history/src/commonMain/kotlin/kpt/feature/history/components/PdfGenerator.kt`

```kotlin
object HistoryHtmlTemplate
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/history/src/commonMain/kotlin/kpt/feature/history/HistoryViewModel.kt:264</code></summary>

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

- `fun generate(`
- `val document = createHTML().html`
- `val instant = kotlin.time.Clock.System.now()`

### `feature/history/src/commonMain/kotlin/kpt/feature/history/di/HistoryModule.kt`

```kotlin
val HistoryModule = module
```
_No KDoc at source._

### `feature/history/src/commonMain/kotlin/kpt/feature/history/di/PdfGeneratorModule.kt`

```kotlin
expect val HistoryPdfModule: Module
```
Provides a platform-appropriate `PdfGenerator` singleton (e.g. bound to an Android `Context` via `createPdfGenerator(context)` on Android).

<details><summary>Used in the template — <code>feature/history/src/androidMain/kotlin/kpt/feature/history/di/HistoryAndroidModule.kt:57</code></summary>

```kotlin

@OptIn(ExperimentalPdfGeneratorApi::class)
actual val HistoryPdfModule: Module
    get() = HistoryPdfAndroidModule
```

</details>

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/HistoryNavigation.kt`

```kotlin
fun NavGraphBuilder.historyNavigation(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToHistory(navOptions: NavOptions? = null)
```
_No KDoc at source._

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/SpecificTransactionsNavigation.kt`

```kotlin
fun NavGraphBuilder.specificTransactionsScreen(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSpecificTransaction(accountId: Long, transactionId: Long)
```
_No KDoc at source._

### `feature/history/src/commonMain/kotlin/kpt/feature/history/navigation/TransactionDetailNavigation.kt`

```kotlin
fun NavGraphBuilder.transactionDetailNavigation(
```
_No KDoc at source._

```kotlin
fun NavHostController.navigateToTransactionDetail(transferId: Long)
```
_No KDoc at source._

---

_5 type(s), 12 function(s)/property(ies); 1 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
