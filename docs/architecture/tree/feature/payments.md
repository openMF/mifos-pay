# `feature/payments/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 21 tracked files — 11× `.xml`, 7× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 3 screen(s), 1 ViewModel(s).

Screens: `PaymentsScreen`, `RequestScreen`, `SelectTransferTypeScreen`

ViewModels: `TransferViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/payments/README.md` | Payments Feature |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/payments sha=28fc85666e29240dd77350d96a2dd85f02a46348 -->
## API reference

_Generated from `feature/payments` at tree `28fc85666e29` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/PaymentNavigation.kt`

```kotlin
fun NavController.navigateToPayments(navOptions: NavOptions) = navigate(PAYMENTS_ROUTE, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.paymentsScreen(
```
_No KDoc at source._

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/PaymentsScreen.kt`

```kotlin
fun PaymentsRoute(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/PaymentNavigation.kt:26</code></summary>

```kotlin
) {
    composable(route = PAYMENTS_ROUTE) {
        PaymentsRoute(tabContents = tabContents)
    }
}
```

</details>

```kotlin
enum class PaymentsScreenContents
```
_No KDoc at source._

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/RequestScreen.kt`

```kotlin
fun RequestScreen(
```
_No KDoc at source._

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/TransferViewModel.kt`

```kotlin
class TransferViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/RequestScreen.kt:51</code></summary>

```kotlin
    showQr: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransferViewModel = koinViewModel(),
) {
    val clipboard = LocalClipboardManager.current
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class TransferState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/RequestScreen.kt:77</code></summary>

```kotlin
@Composable
private fun RequestScreenContent(
    state: TransferState,
    onAction: (TransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
```

</details>

```kotlin
sealed interface TransferEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/RequestScreen.kt:58</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            is TransferEvent.OnShowQR -> showQr.invoke()

            is TransferEvent.OnCopyTextToClipboard -> {
                clipboard.setText(AnnotatedString(event.text))
            }
```

</details>

```kotlin
sealed interface TransferAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/RequestScreen.kt:78</code></summary>

```kotlin
private fun RequestScreenContent(
    state: TransferState,
    onAction: (TransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
```

</details>

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/di/PaymentsModule.kt`

```kotlin
val PaymentsModule = module
```
_No KDoc at source._

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/selectTransferType/SelectTransferTypeNavigation.kt`

```kotlin
data object SelectTransferTypeRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSelectTransferType(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.selectTransferTypeScreen(
```
_No KDoc at source._

### `feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/selectTransferType/SelectTransferTypeScreen.kt`

```kotlin
fun SelectTransferTypeScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/org/mifospay/feature/payments/selectTransferType/SelectTransferTypeNavigation.kt:29</code></summary>

```kotlin
) {
    composable<SelectTransferTypeRoute> {
        SelectTransferTypeScreen(
            onIntraBankTransferClick = onIntraBankTransferClick,
            onInterBankTransferClick = onInterBankTransferClick,
        )
    }
```

</details>

```kotlin
fun SelectTransferTypeScreenPreview()
```
_No KDoc at source._

---

_6 type(s), 9 function(s)/property(ies); 0 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
