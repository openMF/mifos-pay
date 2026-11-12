/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image as SkiaImage

/** Compose-MP non-Android PNG encoding via Skia. Covers JVM Desktop / iOS / JS / wasmJs. */
internal actual fun encodeImageAsPng(image: ImageBitmap): ByteArray? {
    val skiaBitmap = image.asSkiaBitmap()
    return SkiaImage.makeFromBitmap(skiaBitmap)
        .encodeToData(EncodedImageFormat.PNG)
        ?.bytes
}
