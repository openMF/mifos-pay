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

import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Decodes a QR code from an image file on Android using ZXing, returning the decoded text or null if none is found. */
actual suspend fun decodeQrFromFile(file: PlatformFile): String? = withContext(Dispatchers.IO) {
    try {
        val bytes = file.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        if (bitmap != null) {
            val image = InputImage.fromBitmap(bitmap, 0)
            val scanner = BarcodeScanning.getClient()
            val barcodes = scanner.process(image).await()
            barcodes.firstOrNull()?.rawValue
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
