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
 * WasmJS implementation of QrCodeScanner.
 *
 * Uses file-based QR import only due to WasmJS interop limitations
 * with the html5-qrcode library.
 */
@Composable
actual fun QrCodeScanner(
    types: List<CodeType>,
    modifier: Modifier,
    isTorchEnabled: Boolean,
    onTorchAvailabilityChanged: (Boolean) -> Unit,
    onScanned: (String) -> Boolean,
) {
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

    // Header is shown by QrScanTopBar in parent, use dark theme to match scanner
    QrImportScreen(
        isProcessing = isProcessing,
        onSelectImage = { imagePicker.launch() },
        modifier = modifier,
        imagePreviewBytes = imagePreviewBytes,
        showHeader = false,
        useDarkTheme = true,
    )
}
