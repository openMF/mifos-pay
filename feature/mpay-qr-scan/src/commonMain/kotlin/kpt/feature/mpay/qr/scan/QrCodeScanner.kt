/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kpt.feature.mpay.qr.scan.components.QrPermissionDenied
/** Platform camera scanner that detects the given [types] and reports each decoded value via [onScanned]. */
@Composable
expect fun QrCodeScanner(
    types: List<CodeType>,
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean = false,
    onTorchAvailabilityChanged: (Boolean) -> Unit = {},
    onScanned: (String) -> Boolean,
)

/** Wraps [QrCodeScanner] with a camera permission check, showing [permissionDeniedContent] when access is denied. */
@Composable
fun QrScannerWithPermissions(
    types: List<CodeType>,
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean = false,
    onTorchAvailabilityChanged: (Boolean) -> Unit = {},
    onScanned: (String) -> Boolean,
    onUploadQr: () -> Unit = {},
    permissionDeniedContent: @Composable (CameraPermissionState, () -> Unit) -> Unit =
        { permissionState, uploadQr ->
            QrPermissionDenied(
                onOpenSettings = permissionState::goToSettings,
                onUploadQr = uploadQr,
                modifier = modifier,
            )
        },
) {
    val permissionState = rememberCameraPermissionState()

    LaunchedEffect(Unit) {
        if (permissionState.status == CameraPermissionStatus.Unknown ||
            permissionState.status == CameraPermissionStatus.Denied
        ) {
            permissionState.requestCameraPermission()
        }
    }

    when (permissionState.status) {
        CameraPermissionStatus.Granted -> {
            QrCodeScanner(
                types = types,
                modifier = modifier.fillMaxSize(),
                isTorchEnabled = isTorchEnabled,
                onTorchAvailabilityChanged = onTorchAvailabilityChanged,
                onScanned = onScanned,
            )
        }
        CameraPermissionStatus.Denied -> {
            permissionDeniedContent(permissionState, onUploadQr)
        }
        CameraPermissionStatus.Unknown -> {
            // Show nothing while permission is being requested
            // The UI will recompose when status changes
        }
    }
}
