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

<!-- api-docs:begin module=feature/payments sha=a255c07276ccd7b6defa97d384a04f86e99e8539 -->
## API reference

_Generated from `feature/payments` at tree `a255c07276cc` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/PaymentNavigation.kt`

```kotlin
fun NavController.navigateToPayments(navOptions: NavOptions) = navigate(PAYMENTS_ROUTE, navOptions)
```
Navigates to the payments screen.

```kotlin
fun NavGraphBuilder.paymentsScreen(
```
Registers the payments screen destination, rendering the given `tabContents`.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/PaymentsScreen.kt`

```kotlin
fun PaymentsRoute(
```
Route-level composable of the payments screen, hosting the given `tabContents`.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/PaymentNavigation.kt:28</code></summary>

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
Tabs available on the payments screen.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/RequestScreen.kt`

```kotlin
fun RequestScreen(
```
Screen for requesting money, showing the client details and a button to show the QR code.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/TransferViewModel.kt`

```kotlin
class TransferViewModel(
```
ViewModel holding the client details used to send and request money.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/RequestScreen.kt:51</code></summary>

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
Serializable UI state of the transfer screens, holding the client contact details.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/RequestScreen.kt:77</code></summary>

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
One-shot events emitted by `TransferViewModel`.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/RequestScreen.kt:58</code></summary>

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
Actions dispatched to `TransferViewModel`.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/RequestScreen.kt:78</code></summary>

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

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/di/PaymentsModule.kt`

```kotlin
val PaymentsModule = module
```
Koin module providing the `TransferViewModel`.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/selectTransferType/SelectTransferTypeNavigation.kt`

```kotlin
data object SelectTransferTypeRoute
```
Type-safe navigation route of the select-transfer-type screen.

```kotlin
fun NavController.navigateToSelectTransferType(navOptions: NavOptions? = null) =
```
Navigates to the select-transfer-type screen.

```kotlin
fun NavGraphBuilder.selectTransferTypeScreen(
```
Registers the select-transfer-type destination.

### `feature/payments/src/commonMain/kotlin/kpt/feature/payments/selectTransferType/SelectTransferTypeScreen.kt`

```kotlin
fun SelectTransferTypeScreen(
```
Screen where the user chooses between an intra-bank and an inter-bank transfer.

<details><summary>Used in the template — <code>feature/payments/src/commonMain/kotlin/kpt/feature/payments/selectTransferType/SelectTransferTypeNavigation.kt:32</code></summary>

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
Preview of `SelectTransferTypeScreen`.

---

_6 type(s), 9 function(s)/property(ies); 15 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
