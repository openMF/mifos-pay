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

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

/** Android `Bitmap.compress(PNG)` — no Skia on Android. */
internal actual fun encodeImageAsPng(image: ImageBitmap): ByteArray? {
    val stream = ByteArrayOutputStream()
    val ok = image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
    return if (ok) stream.toByteArray() else null
}
