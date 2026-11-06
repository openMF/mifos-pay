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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kpt.feature.mpay.qr.scan.components.QrImportScreen
/**
 * Scan mode for web platform.
 */
private enum class WebScanMode {
    /** Using live camera (mobile browsers) */
    CAMERA,

    /** Using file picker (desktop browsers or fallback) */
    FILE_PICKER,
}

@Composable
actual fun QrCodeScanner(
    types: List<CodeType>,
    modifier: Modifier,
    isTorchEnabled: Boolean,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    onScanned: (String) -> Boolean,
) {
    val platformType = remember { WebPlatformDetection.detectPlatformType() }
    var scanMode by remember {
        mutableStateOf(
            when (platformType) {
                WebPlatformType.MOBILE_WITH_CAMERA -> WebScanMode.CAMERA
                WebPlatformType.DESKTOP_WITH_CAMERA,
                WebPlatformType.NO_CAMERA,
                -> WebScanMode.FILE_PICKER
            },
        )
    }

    var isProcessing by remember { mutableStateOf(false) }
    var imagePreviewBytes by remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
    ) { file ->
        if (file != null) {
            scope.launch {
                isProcessing = true
                imagePreviewBytes = file.readBytes()
                val result = decodeQrFromFile(file)
                isProcessing = false
                if (result != null) {
                    onScanned(result)
                } else {
                    imagePreviewBytes = null
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // Torch is not available on web
        onTorchAvailabilityChanged(false)
    }

    when (scanMode) {
        WebScanMode.CAMERA -> {
            WebCameraScanner(
                onScanned = onScanned,
                onFallbackToFilePicker = {
                    scanMode = WebScanMode.FILE_PICKER
                    imagePicker.launch()
                },
                modifier = modifier,
            )
        }

        WebScanMode.FILE_PICKER -> {
            QrImportScreen(
                isProcessing = isProcessing,
                onSelectImage = { imagePicker.launch() },
                modifier = modifier,
                imagePreviewBytes = imagePreviewBytes,
            )
        }
    }
}
