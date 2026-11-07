# `feature/invoices/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 15 tracked files — 7× `.kt`, 5× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `InvoiceDetailScreen`, `InvoiceScreen`

ViewModels: `InvoiceDetailViewModel`, `InvoicesViewModel`

**Consumes:** `core/store/repository`

### Docs in the tree

| Path | |
|---|---|
| `feature/invoices/README.md` | Invoice Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/invoices sha=46135e63699e5e9d65a65686611b4c3f9b99e9aa -->
## API reference

_Generated from `feature/invoices` at tree `46135e63699e` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoiceScreen.kt`

```kotlin
fun InvoiceScreen(
```
Screen listing the client's invoices; selecting one calls `navigateToInvoiceDetailScreen` with its id.

### `feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoicesViewModel.kt`

```kotlin
class InvoicesViewModel(
```
ViewModel that loads the current client's invoices and emits navigation to invoice detail.

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoiceScreen.kt:42</code></summary>

```kotlin
    navigateToInvoiceDetailScreen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoicesViewModel = koinViewModel(),
) {
    val invoiceUiState by viewModel.invoiceUiState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
```

</details>

```kotlin
data class InvoiceState(
```
Persisted UI state of the invoices screen.

```kotlin
sealed interface InvoiceAction
```
User actions on the invoices screen.

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoiceScreen.kt:68</code></summary>

```kotlin
    state: ScreenState<List<Invoice>>,
    onRetry: () -> Unit,
    onAction: (InvoiceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
```

</details>

```kotlin
sealed interface InvoiceEvent
```
One-off events emitted by the invoices screen.

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/InvoiceScreen.kt:48</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is InvoiceEvent.NavigateToInvoiceDetail -> {
                navigateToInvoiceDetailScreen(event.invoiceId)
            }
        }
    }
```

</details>

### `feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/di/InvoicesModule.kt`

```kotlin
val InvoicesModule = module
```
Koin module providing the invoice list and invoice detail view models.

### `feature/invoices/src/commonMain/kotlin/kpt/feature/invoices/navigation/InvoiceNavigation.kt`

```kotlin
fun NavController.navigateToInvoiceDetail(invoiceId: Long)
```
Navigates to the detail screen of the invoice with the given `invoiceId`.

```kotlin
fun NavGraphBuilder.invoiceDetailScreen(
```
Registers the invoice detail destination, which takes the invoice id as a long argument.

---

_4 type(s), 4 function(s)/property(ies); 8 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
