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
 * Decodes a QR code from an image file.
 * Platform-specific implementations use native QR decoding libraries.
 *
 * @param file The platform file containing the QR code image
 * @return The decoded QR code string, or null if no QR code was found
 */
expect suspend fun decodeQrFromFile(file: PlatformFile): String?
