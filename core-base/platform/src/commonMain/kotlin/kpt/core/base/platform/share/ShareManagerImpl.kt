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
import com.mobilebytelabs.kmptoolkit.share.ExperimentalShareApi
import com.mobilebytelabs.kmptoolkit.share.Share
import com.mobilebytelabs.kmptoolkit.share.SharePayload
import com.mobilebytelabs.kmptoolkit.share.file
import com.mobilebytelabs.kmptoolkit.share.text
import com.mobilebytelabs.kmptoolkit.share.url
import kpt.core.base.platform.model.MimeType

/**
 * The one [ShareManager], for every target.
 *
 * `cmp-share` carries the per-target `actual`s. The only platform-split piece is
 * [encodeImageAsPng], because turning an [ImageBitmap] into bytes needs the platform's own bitmap
 * codec — there is no toolkit module for it.
 */
@OptIn(ExperimentalShareApi::class)
class ShareManagerImpl : ShareManager {

    override suspend fun shareText(text: String) {
        Share.text(text)
    }

    override suspend fun shareUrl(url: String) {
        Share.url(url)
    }

    override suspend fun shareFile(fileUri: String, mimeType: MimeType) {
        Share.file(fileUri, mimeType.value)
    }

    override suspend fun shareFile(fileUri: String, mimeType: MimeType, extraText: String) {
        Share.share(
            SharePayload.Multi(
                items = listOf(
                    SharePayload.Text(extraText),
                    SharePayload.File(fileUri, mimeType.value),
                ),
            ),
        )
    }

    override suspend fun shareImage(title: String, image: ImageBitmap) {
        // No bytes means the platform could not encode it; sharing an empty payload would raise a
        // chooser that fails after the user has already picked a target.
        val bytes = encodeImageAsPng(image) ?: return
        Share.share(
            SharePayload.Image(
                bytes = bytes,
                mimeType = "image/png",
                filename = "$title.png",
            ),
        )
    }
}

/** Platform bitmap encoding — the one piece of [ShareManagerImpl] the toolkit cannot supply. */
internal expect fun encodeImageAsPng(image: ImageBitmap): ByteArray?
