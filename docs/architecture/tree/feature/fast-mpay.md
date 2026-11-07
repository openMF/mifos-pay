# `feature/fast-mpay/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 12 tracked files — 9× `.kt`, 2× `.xml`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `FastMpayScreen`

ViewModels: `FastMpayViewModel`

**Consumes:** `core/store/fork`, `core/store/repository`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/fast-mpay sha=a55ed2bb24433e0c6394c46d0f3098cfca0941ae -->
## API reference

_Generated from `feature/fast-mpay` at tree `a55ed2bb2443` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayProcessor.kt`

```kotlin
class FastMpayProcessor(
```
Central processor for QR code data. Receives `QrCodeData` and determines the appropriate navigation target based on `QrCodeData.type`.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayViewModel.kt:36</code></summary>

```kotlin
class FastMpayViewModel(
    savedStateHandle: SavedStateHandle,
    private val processor: FastMpayProcessor,
) : BaseViewModel<FastMpayState, FastMpayEvent, FastMpayAction>(
    initialState = FastMpayState,
) {
```

</details>

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt`

```kotlin
fun FastMpayScreen(
```
Fast MPay processing screen. This screen acts as a routing hub - it receives encoded QR data via navigation, decodes and processes it using `FastMpayProcessor`, and immediately navigates to the appropriate destination based on the QR type.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/navigation/FastMpayNavigation.kt:77</code></summary>

```kotlin
        ),
    ) {
        FastMpayScreen(
            onNavigateToAddBeneficiary = onNavigateToAddBeneficiary,
            onNavigateToMakeTransfer = onNavigateToMakeTransfer,
            onNavigateToInterbankTransfer = onNavigateToInterbankTransfer,
            onNavigateToIntraBankTransfer = onNavigateToIntraBankTransfer,
```

</details>

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayViewModel.kt`

```kotlin
class FastMpayViewModel(
```
ViewModel for fast MPay QR code processing. Receives encoded QR data from navigation, decodes it, processes it using `FastMpayProcessor`, and emits the navigation result.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayScreen.kt:70</code></summary>

```kotlin
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FastMpayViewModel = koinViewModel(),
) {
    val result by viewModel.resultFlow.collectAsStateWithLifecycle()
    val error by viewModel.errorFlow.collectAsStateWithLifecycle()
```

</details>

```kotlin
data object FastMpayState
```
Empty UI state; the Fast Mpay screen holds no state of its own.

```kotlin
sealed interface FastMpayEvent
```
One-off events of Fast Mpay (none defined).

```kotlin
sealed interface FastMpayAction
```
User actions handled by the Fast Mpay ViewModel.

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/di/FastMpayModule.kt`

```kotlin
val FastMpayModule = module
```
Koin module providing the Fast Mpay QR processor and ViewModel.

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/model/QrProcessResult.kt`

```kotlin
sealed interface QrProcessResult
```
Sealed interface representing the result of processing a QR code. Used by `FastMpayProcessor` to determine navigation target based on `QrCodeData.type`.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/FastMpayProcessor.kt:83</code></summary>

```kotlin
     * @return [QrProcessResult] indicating where to navigate.
     */
    suspend fun processQrCode(qrData: QrCodeData, scope: CoroutineScope): QrProcessResult {
        return when (qrData.type) {
            QrCodeType.INTRA_BANK -> {
                processIntraBankQr(qrData, scope)
            }
```

</details>

### `feature/fast-mpay/src/commonMain/kotlin/kpt/feature/fastmpay/navigation/FastMpayNavigation.kt`

```kotlin
fun NavController.navigateToFastMpay(
```
Navigate to Fast MPay processing screen.

```kotlin
fun NavGraphBuilder.fastMpayScreen(
```
Registers the Fast MPay screen in the navigation graph.

---

_6 type(s), 4 function(s)/property(ies); 10 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
