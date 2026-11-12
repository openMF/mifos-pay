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

/**
 * WasmJS implementation of QR decoding.
 *
 * Currently returns null as html5-qrcode interop is not fully
 * compatible with WasmJS. The file picker UI is still available.
 */
actual suspend fun decodeQrFromFile(file: PlatformFile): String? {
    // WasmJS QR decoding not yet implemented due to JS interop limitations
    // The file picker UI allows selecting images, but decoding requires
    // additional native WasmJS-compatible QR decoding implementation
    return null
}
