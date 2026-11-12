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
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreImage.CIContext
import platform.CoreImage.CIDetector
import platform.CoreImage.CIDetectorAccuracy
import platform.CoreImage.CIDetectorAccuracyHigh
import platform.CoreImage.CIDetectorTypeQRCode
import platform.CoreImage.CIImage
import platform.CoreImage.CIQRCodeFeature

/** Decodes a QR code from an image file on iOS, returning the decoded text or null if none is found. */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun decodeQrFromFile(file: PlatformFile): String? {
    return try {
        val nsUrl = file.nsUrl
        val ciImage = CIImage(contentsOfURL = nsUrl) ?: return null

        val context = CIContext()
        val detector = CIDetector.detectorOfType(
            type = CIDetectorTypeQRCode,
            context = context,
            options = mapOf(CIDetectorAccuracy to CIDetectorAccuracyHigh),
        ) ?: return null

        val features = detector.featuresInImage(ciImage)
        val qrFeature = features.firstOrNull() as? CIQRCodeFeature

        qrFeature?.messageString
    } catch (e: Exception) {
        null
    }
}
