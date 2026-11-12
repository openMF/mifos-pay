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

/** Barcode symbologies the scanner can be configured to detect. */
enum class CodeType {
    /** Codabar linear barcode. */
    Codabar,

    /** Code 39 linear barcode. */
    Code39,

    /** Code 93 linear barcode. */
    Code93,

    /** Code 128 linear barcode. */
    Code128,

    /** EAN-8 retail barcode. */
    EAN8,

    /** EAN-13 retail barcode. */
    EAN13,

    /** Interleaved 2 of 5 barcode. */
    ITF,

    /** UPC-E retail barcode. */
    UPCE,

    /** Aztec 2D code. */
    Aztec,

    /** Data Matrix 2D code. */
    DataMatrix,

    /** PDF417 stacked 2D barcode. */
    PDF417,

    /** QR code, the format used for wallet payments. */
    QR,
}
