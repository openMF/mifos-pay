# `feature/invoices/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 15 tracked files — 7× `.kt`, 5× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `InvoiceDetailScreen`, `InvoiceScreen`

ViewModels: `InvoiceDetailViewModel`, `InvoicesViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/invoices/README.md` | Invoice Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/invoices sha=f54b4085be35b0f3a9eeb157ca55833b5ce4eaeb -->
## API reference

_Generated from `feature/invoices` at tree `f54b4085be35` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/InvoiceScreen.kt`

```kotlin
fun InvoiceScreen(
```
_No KDoc at source._

### `feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/InvoicesViewModel.kt`

```kotlin
class InvoicesViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/InvoiceScreen.kt:42</code></summary>

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
_No KDoc at source._

```kotlin
sealed interface InvoiceAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/InvoiceScreen.kt:68</code></summary>

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/InvoiceScreen.kt:48</code></summary>

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

### `feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/di/InvoicesModule.kt`

```kotlin
val InvoicesModule = module
```
_No KDoc at source._

### `feature/invoices/src/commonMain/kotlin/org/mifospay/feature/invoices/navigation/InvoiceNavigation.kt`

```kotlin
fun NavController.navigateToInvoiceDetail(invoiceId: Long)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.invoiceDetailScreen(
```
_No KDoc at source._

---

_4 type(s), 4 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
