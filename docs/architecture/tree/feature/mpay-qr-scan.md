# `feature/mpay-qr-scan/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 55 tracked files — 51× `.kt`, 2× `.xml`, 1× `.md`, 1× `.kts`

**51 Kotlin files** — 2 screen(s), 1 ViewModel(s).

Screens: `QrImportScreen`, `ScanQrCodeScreen`

ViewModels: `ScanQrViewModel`

**Consumes:** `core/store/util`

### Docs in the tree

| Path | |
|---|---|
| `feature/mpay-qr-scan/README.md` | :feature:mpay-qr-scan module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/mpay-qr-scan sha=91d9ef6779e6d2a37bc51a2806351e411203f515 -->
## API reference

_Generated from `feature/mpay-qr-scan` at tree `91d9ef6779e6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/CameraPermissionState.kt`

```kotlin
interface CameraPermissionState
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/CameraPermissionState.android.kt:29</code></summary>

```kotlin
@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val accPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val context = LocalContext.current
    val wrapper = remember(accPermissionState) {
```

</details>

- `val status: CameraPermissionStatus`
- `fun requestCameraPermission()`
- `fun goToSettings()`

```kotlin
expect fun rememberCameraPermissionState(): CameraPermissionState
```
_No KDoc at source._

```kotlin
enum class CameraPermissionStatus
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/CameraPermissionState.android.kt:48</code></summary>

```kotlin
    private val context: Context,
) : CameraPermissionState {
    override val status: CameraPermissionStatus
        get() = permissionState.status.toCameraPermissionStatus()

    override fun requestCameraPermission() {
        permissionState.launchPermissionRequest()
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/CodeType.kt`

```kotlin
enum class CodeType
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.android.kt:18</code></summary>

```kotlin
@Composable
actual fun QrCodeScanner(
    types: List<CodeType>,
    modifier: Modifier,
    isTorchEnabled: Boolean,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    onScanned: (String) -> Boolean,
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ImagePicker.kt`

```kotlin
expect suspend fun decodeQrFromFile(file: PlatformFile): String?
```
Decodes a QR code from an image file. Platform-specific implementations use native QR decoding libraries.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.kt`

```kotlin
expect fun QrCodeScanner(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.android.kt:17</code></summary>

```kotlin

@Composable
actual fun QrCodeScanner(
    types: List<CodeType>,
    modifier: Modifier,
    isTorchEnabled: Boolean,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
```

</details>

```kotlin
fun QrScannerWithPermissions(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:174</code></summary>

```kotlin
        // Camera preview / QR Scanner (full screen)
        // On web desktop, this shows file picker UI; on mobile/native, shows camera
        QrScannerWithPermissions(
            types = listOf(CodeType.QR),
            modifier = Modifier.fillMaxSize(),
            isTorchEnabled = isTorchEnabled,
            onTorchAvailabilityChanged = { available ->
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt`

```kotlin
fun ScanQrCodeScreenContent(
```
_No KDoc at source._

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrViewModel.kt`

```kotlin
class ScanQrViewModel(
```
ViewModel for QR code scanning screen.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:59</code></summary>

```kotlin
    navigateToAddBeneficiaryScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanQrViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val isTorchEnabled by viewModel.isTorchEnabled.collectAsStateWithLifecycle()
```

</details>

```kotlin
data object ScanQrState
```
_No KDoc at source._

```kotlin
sealed interface ScanQrEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:74</code></summary>

```kotlin
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ScanQrEvent.OnNavigateToIntraBankTransfer -> {
                    navigateToIntraBankTransfer.invoke(event.qrData)
                }

                is ScanQrEvent.OnNavigateToInterbankTransfer -> {
```

</details>

```kotlin
sealed interface ScanQrAction
```
_No KDoc at source._

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/WebPlatform.kt`

```kotlin
expect object WebPlatform
```
Platform detection for QR scanner UI behavior.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/WebPlatform.android.kt:12</code></summary>

```kotlin
package kpt.feature.mpay.qr.scan

actual object WebPlatform {
    actual val isWeb: Boolean = false
    actual val isWebDesktop: Boolean = false
}
```

</details>

- `val isWeb: Boolean` — Returns true if running on web platform (JS or WasmJS). Used to determine whether to show camera scanner UI or file picker UI.
- `val isWebDesktop: Boolean` — Returns true if running on web platform on a desktop browser. Desktop browsers should show file picker only, not camera scanner.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrActionButton.kt`

```kotlin
fun QrActionButton(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:203</code></summary>

```kotlin
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    QrActionButton(
                        icon = MifosIcons.PhotoLibrary,
                        label = stringResource(Res.string.feature_qr_upload_qr),
                        onClick = { imagePicker.launch() },
                    )
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrHelpDialog.kt`

```kotlin
fun QrHelpDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:246</code></summary>

```kotlin
        // Help dialog
        if (showHelpDialog) {
            QrHelpDialog(
                onDismiss = onDismissHelpDialog,
            )
        }
    }
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrImportScreen.kt`

```kotlin
fun QrImportScreen(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/desktopMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.desktop.kt:60</code></summary>

```kotlin
    }

    QrImportScreen(
        isProcessing = isProcessing,
        onSelectImage = { imagePicker.launch() },
        modifier = modifier,
        imagePreviewBytes = imagePreviewBytes,
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrPermissionDenied.kt`

```kotlin
fun QrPermissionDenied(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.kt:36</code></summary>

```kotlin
    permissionDeniedContent: @Composable (CameraPermissionState, () -> Unit) -> Unit =
        { permissionState, uploadQr ->
            QrPermissionDenied(
                onOpenSettings = permissionState::goToSettings,
                onUploadQr = uploadQr,
                modifier = modifier,
            )
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrProcessingOverlay.kt`

```kotlin
fun QrProcessingOverlay(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:230</code></summary>

```kotlin
        // Processing overlay
        if (isProcessingImage) {
            QrProcessingOverlay(
                imageBytes = selectedImageBytes,
                modifier = Modifier.fillMaxSize(),
            )
        }
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrScanFooter.kt`

```kotlin
fun QrScanFooter(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:217</code></summary>

```kotlin
                }

                QrScanFooter()
            }
        }

        // Top bar - always show for consistent navigation
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrScanTopBar.kt`

```kotlin
fun QrScanTopBar(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:222</code></summary>

```kotlin

        // Top bar - always show for consistent navigation
        QrScanTopBar(
            onCloseClick = navigateBack,
            onHelpClick = onShowHelpDialog,
            modifier = Modifier.align(Alignment.TopCenter),
        )
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrViewfinder.kt`

```kotlin
fun QrViewfinder(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:188</code></summary>

```kotlin
        if (!isWebDesktop) {
            // Viewfinder overlay with animation
            QrViewfinder(
                modifier = Modifier.fillMaxSize(),
            )

            // Action buttons and footer (bottom aligned)
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/di/MpayQrScanModule.kt`

```kotlin
val MpayQrScanModule = module
```
_No KDoc at source._

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/navigation/MpayQrScanNavigation.kt`

```kotlin
fun NavController.navigateToScanQr(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.scanQrScreen(
```
_No KDoc at source._

---

_8 type(s), 21 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 16 live call site(s)._
<!-- api-docs:end -->
