/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.DelicateCoroutinesApi

/** Web (JS/WasmJS) implementation of `ShareUtils`. */
@OptIn(DelicateCoroutinesApi::class)
actual object ShareUtils {
    /** `shareText` on Web (JS/WasmJS). */
    actual suspend fun shareText(text: String) {
        FileKit.download(
            bytes = text.encodeToByteArray(),
            fileName = "shared_text.txt",
        )
    }

    /** `shareImage` on Web (JS/WasmJS). */
    actual suspend fun shareImage(
        title: String,
        image: ImageBitmap,
    ) {
        image.asSkiaBitmap().readPixels()?.let {
            FileKit.download(
                bytes = it,
                fileName = "$title.png",
            )
        }
    }

    /** `shareImage` on Web (JS/WasmJS). */
    actual suspend fun shareImage(title: String, byte: ByteArray) {
        FileKit.download(
            bytes = byte,
            fileName = "$title.png",
        )
    }

    /** `openUrl` on Web (JS/WasmJS). */
    actual fun openUrl(url: String) {
    }

    /** `openAppInfo` on Web (JS/WasmJS). */
    actual fun openAppInfo() {
    }

    /** `callPhone` on Web (JS/WasmJS). */
    actual fun callPhone(number: String) {
    }

    /** `sendEmail` on Web (JS/WasmJS). */
    actual fun sendEmail(to: String, subject: String?, body: String?) {
    }

    /** `sendViaSMS` on Web (JS/WasmJS). */
    actual fun sendViaSMS(number: String, message: String) {
    }

    /** `copyText` on Web (JS/WasmJS). */
    actual fun copyText(text: String) {
    }

    /** `shareApp` on Web (JS/WasmJS). */
    actual suspend fun shareApp(storeLink: String, message: String) {
        val shareContent = if (message.isNotEmpty()) {
            "$message\n$storeLink"
        } else {
            storeLink
        }
        FileKit.download(
            bytes = shareContent.encodeToByteArray(),
            fileName = "share_app.txt",
        )
    }
}
