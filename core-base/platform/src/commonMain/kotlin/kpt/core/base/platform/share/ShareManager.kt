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
import kpt.core.base.platform.model.MimeType

/**
 * Hands content to the platform share chooser.
 *
 * ## Why this is not part of `IntentManager` or `UrlLauncher`
 * Sharing RAISES A CHOOSER and gives the content to another app; opening a URL sends the user
 * straight to a handler; a system intent asks the OS for a specific screen or document. Three
 * different outcomes, so three interfaces — a caller that wants one of them cannot accidentally
 * reach for another.
 *
 * Every method suspends because the share sheet is presented and awaited on most targets.
 */
interface ShareManager {

    /** Share plain text. */
    suspend fun shareText(text: String)

    /** Share a URL as a link rather than as text, so receivers render a preview. */
    suspend fun shareUrl(url: String)

    /** Share a file by uri. [mimeType] decides which apps the chooser offers. */
    suspend fun shareFile(fileUri: String, mimeType: MimeType)

    /** Share a file alongside a message — one chooser, both payloads. */
    suspend fun shareFile(fileUri: String, mimeType: MimeType, extraText: String)

    /** Share an in-memory image. Encoded to PNG on the way out; [title] names the file. */
    suspend fun shareImage(title: String, image: ImageBitmap)
}
