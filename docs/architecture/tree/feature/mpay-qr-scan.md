# `feature/mpay-qr-scan/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 55 tracked files — 51× `.kt`, 2× `.xml`, 1× `.md`, 1× `.kts`

**51 Kotlin files** — 2 screen(s), 1 ViewModel(s).

Screens: `QrImportScreen`, `ScanQrCodeScreen`

ViewModels: `ScanQrViewModel`

**Consumes:** `core/store/fork`

### Docs in the tree

| Path | |
|---|---|
| `feature/mpay-qr-scan/README.md` | :feature:mpay-qr-scan module |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/mpay-qr-scan sha=c928db830c012a6591cf6c739a78b6d1361aa41a -->
## API reference

_Generated from `feature/mpay-qr-scan` at tree `c928db830c01` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/CameraPermissionState.kt`

```kotlin
interface CameraPermissionState
```
Observable camera permission state plus the actions to request the permission or open system settings.

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

- `val status: CameraPermissionStatus` — Current camera permission status.
- `fun requestCameraPermission()` — Asks the user for camera permission.
- `fun goToSettings()` — Opens the system settings screen so the user can grant camera permission manually.

```kotlin
expect fun rememberCameraPermissionState(): CameraPermissionState
```
Remembers a platform-specific `CameraPermissionState` for the current composition.

```kotlin
enum class CameraPermissionStatus
```
Camera permission status values reported by `CameraPermissionState`.

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
Barcode symbologies the scanner can be configured to detect.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.android.kt:22</code></summary>

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
Platform camera scanner that detects the given `types` and reports each decoded value via `onScanned`.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.android.kt:21</code></summary>

```kotlin
 */
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
Wraps `QrCodeScanner` with a camera permission check, showing `permissionDeniedContent` when access is denied.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:175</code></summary>

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
Stateless content of the scan-QR screen: camera viewfinder, torch and help controls, and gallery image import.

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
Empty UI state; the screen has no state beyond the view model's flows.

```kotlin
sealed interface ScanQrEvent
```
One-shot events emitted by `ScanQrViewModel`.

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
Actions handled by `ScanQrViewModel`.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/WebPlatform.kt`

```kotlin
expect object WebPlatform
```
Platform detection for QR scanner UI behavior.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/androidMain/kotlin/kpt/feature/mpay/qr/scan/WebPlatform.android.kt:13</code></summary>

```kotlin

/** Android actual of [WebPlatform]; never a web environment. */
actual object WebPlatform {
    /** Always false on Android. */
    actual val isWeb: Boolean = false

    /** Always false on Android. */
```

</details>

- `val isWeb: Boolean` — Returns true if running on web platform (JS or WasmJS). Used to determine whether to show camera scanner UI or file picker UI.
- `val isWebDesktop: Boolean` — Returns true if running on web platform on a desktop browser. Desktop browsers should show file picker only, not camera scanner.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrActionButton.kt`

```kotlin
fun QrActionButton(
```
Icon-over-label action button used on the scanner screen.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:204</code></summary>

```kotlin
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    QrActionButton(
                        icon = ForkIcons.PhotoLibrary,
                        label = stringResource(Res.string.feature_qr_upload_qr),
                        onClick = { imagePicker.launch() },
                    )
```

</details>

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/components/QrHelpDialog.kt`

```kotlin
fun QrHelpDialog(
```
Dialog explaining how to scan a QR code; closed through `onDismiss`.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:247</code></summary>

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
Screen for importing a QR code from an image, with preview, drag state and processing indicator.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/desktopMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.desktop.kt:61</code></summary>

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
Content shown when camera permission is denied, offering to open settings or upload a QR image instead.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/QrCodeScanner.kt:38</code></summary>

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
Overlay showing the selected image preview and a progress indicator while a QR image is decoded.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:231</code></summary>

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
Footer caption displayed under the scanner viewfinder.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:218</code></summary>

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
Top bar of the scanner with close and help buttons.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:223</code></summary>

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
Animated scan frame with a moving scan line drawn over the camera preview.

<details><summary>Used in the template — <code>feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/ScanQrCodeScreen.kt:189</code></summary>

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
Koin module providing the QR scan feature's view model.

### `feature/mpay-qr-scan/src/commonMain/kotlin/kpt/feature/mpay/qr/scan/navigation/MpayQrScanNavigation.kt`

```kotlin
fun NavController.navigateToScanQr(navOptions: NavOptions? = null) =
```
Navigates to the scan-QR screen.

```kotlin
fun NavGraphBuilder.scanQrScreen(
```
Registers the scan-QR destination, wiring navigation callbacks for intra-bank, inter-bank and add-beneficiary results.

---

_8 type(s), 21 function(s)/property(ies); 29 carry KDoc at source; 0 authored example(s); 16 live call site(s)._
<!-- api-docs:end -->
