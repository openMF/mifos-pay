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

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLDivElement
import kotlin.random.Random

/**
 * Decodes a QR code from a file using html5-qrcode library.
 *
 * @param file The image file to decode
 * @return The decoded QR code string, or null if decoding failed
 */
actual suspend fun decodeQrFromFile(file: PlatformFile): String? {
    return try {
        // Create a temporary scanner element
        val scannerId = "qr-file-scanner-${Random.nextInt(10000)}"
        val scannerDiv = document.createElement("div") as HTMLDivElement
        scannerDiv.id = scannerId
        scannerDiv.style.display = "none"
        document.body?.appendChild(scannerDiv)

        try {
            val html5Qrcode = Html5Qrcode(scannerId)

            // Get the underlying JavaScript File object
            val jsFile = file.asDynamic().file ?: file.asDynamic()

            // Scan the file
            val result = html5Qrcode.scanFile(jsFile, false).await()

            // Clean up
            try {
                html5Qrcode.clear().await()
            } catch (_: Exception) {
                // Ignore cleanup errors
            }

            result
        } finally {
            // Remove the temporary element
            document.getElementById(scannerId)?.remove()
        }
    } catch (e: Exception) {
        console.log("QR decode error: ${e.message}")
        null
    }
}

private external val console: Console

private external interface Console {
    fun log(message: String)
}
