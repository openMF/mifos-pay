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

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Original source: https://github.com/kalinjul/EasyQRScan
 *
 * Analyzes camera frames to detect and decode QR codes using Google ML Kit.
 *
 * Uses [Barcode.getDisplayValue] first (user-friendly decoded value), falling back to
 * [Barcode.getRawValue] (raw string from QR). This ensures consistent cross-platform
 * behavior with iOS's AVMetadataMachineReadableCodeObject.stringValue.
 */
class BarcodeAnalyzer(
    formats: Int = Barcode.FORMAT_QR_CODE,
    private val onScanned: (String) -> Boolean,
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(formats)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes?.firstOrNull()?.let { barcode ->
                    // Use displayValue first (user-friendly decoded value),
                    // fall back to rawValue (raw string from QR)
                    val scannedData = barcode.displayValue ?: barcode.rawValue

                    scannedData?.trim()?.let { data ->
                        Log.d("BarcodeAnalyzer", "QR detected: ${data.take(50)}...")
                        if (onScanned(data)) {
                            scanner.close()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeAnalyzer", "ML Kit scanning failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
