# `feature/mpay-qr/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 26 tracked files — 12× `.kt`, 10× `.xml`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**12 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `MpayQrScreen`

ViewModels: `MpayQrViewModel`

**Consumes:** `core/store/repository`, `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/mpay-qr/README.md` | :feature:mpay-qr module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/mpay-qr sha=2cff5c941a582f7814ec95dd4fd7b52c286a6f08 -->
## API reference

_Generated from `feature/mpay-qr` at tree `2cff5c941a58` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrViewModel.kt`

```kotlin
class MpayQrViewModel(
```
ViewModel of the QR screen; builds intra-bank and inter-bank QR codes for the selected account.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrScreen.kt:93</code></summary>

```kotlin
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MpayQrViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val currencyList by viewModel.currencyList.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
```

</details>

```kotlin
data class MpayQrState(
```
Persisted UI state of the QR screen.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrScreen.kt:139</code></summary>

```kotlin
@Composable
private fun MpayQrDialogs(
    dialogState: MpayQrState.DialogState?,
    showAmountDialog: @Composable () -> Unit,
    onAction: (MpayQrAction) -> Unit,
) {
    when (dialogState) {
```

</details>

```kotlin
sealed interface MpayQrEvent
```
One-off events of the QR screen.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrScreen.kt:105</code></summary>

```kotlin
    EventsEffect(viewModel) { event ->
        when (event) {
            MpayQrEvent.OnNavigateBack -> navigateBack.invoke()
            MpayQrEvent.QrDownloaded -> snackbarHostState.showSnackbar(downloadedMessage)
            is MpayQrEvent.ShowSnackbar -> {
                clipboardManager.setText(AnnotatedString(event.message))
                snackbarHostState.showSnackbar(copiedMessage)
```

</details>

```kotlin
sealed interface MpayQrAction
```
User and internal actions of the QR screen.

<details><summary>Used in the template — <code>feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/MpayQrScreen.kt:141</code></summary>

```kotlin
    dialogState: MpayQrState.DialogState?,
    showAmountDialog: @Composable () -> Unit,
    onAction: (MpayQrAction) -> Unit,
) {
    when (dialogState) {
        is MpayQrState.DialogState.ShowSetAmountDialog -> showAmountDialog.invoke()
```

</details>

### `feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/components/QrCodeCard.kt`

```kotlin
enum class QrType
```
QR type identifier for visual distinction.

<details><summary>Used in the template — <code>feature/beneficiary/src/commonMain/kotlin/kpt/feature/beneficiary/addupdatebeneficiary/AddEditBeneficiaryViewModel.kt:89</code></summary>

```kotlin
                        officeId = beneficiary.officeId,
                        addEditType = addEditType,
                        sourceQrType = addEditType.sourceQrType,
                        sourceQrData = sourceQrData,
                    )
                } else {
                    // Empty form for manual add
```

</details>

### `feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/di/MpayQrModule.kt`

```kotlin
val MpayQrModule = module
```
Koin module providing the `MpayQrViewModel` factory.

### `feature/mpay-qr/src/commonMain/kotlin/kpt/feature/mpay/qr/navigation/MpayQrNavigation.kt`

```kotlin
fun NavGraphBuilder.mpayQrScreen(
```
Registers the Mpay QR screen destination in the navigation graph.

```kotlin
fun NavController.navigateToMpayQrScreen(navOptions: NavOptions? = null)
```
Navigates to the Mpay QR screen.

---

_5 type(s), 3 function(s)/property(ies); 8 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->
