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

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Original source: https://github.com/kalinjul/EasyQRScan/blob/main/scanner/src/androidMain/kotlin/Codetype%2BtoFormat.kt
 */
fun List<CodeType>.toFormat(): Int = map {
    when (it) {
        CodeType.Codabar -> Barcode.FORMAT_CODABAR
        CodeType.Code39 -> Barcode.FORMAT_CODE_39
        CodeType.Code93 -> Barcode.FORMAT_CODE_93
        CodeType.Code128 -> Barcode.FORMAT_CODE_128
        CodeType.EAN8 -> Barcode.FORMAT_EAN_8
        CodeType.EAN13 -> Barcode.FORMAT_EAN_13
        CodeType.ITF -> Barcode.FORMAT_ITF
        CodeType.UPCE -> Barcode.FORMAT_UPC_E
        CodeType.Aztec -> Barcode.FORMAT_AZTEC
        CodeType.DataMatrix -> Barcode.FORMAT_DATA_MATRIX
        CodeType.PDF417 -> Barcode.FORMAT_PDF417
        CodeType.QR -> Barcode.FORMAT_QR_CODE
    }
}.fold(0) { acc, next ->
    acc + next
}
