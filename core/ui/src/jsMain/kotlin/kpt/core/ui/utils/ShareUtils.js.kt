/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download

/**
 * Provides utility functions for sharing content on JS and WASM platforms.
 *
 * This implementation uses [FileKit.download] to trigger file downloads
 * in web environments (JS), as native share dialogs are not supported
 * on these platforms.
 */
actual object ShareUtils {

    /**
     * Shares plain text content by triggering a file download.
     *
     * The text is saved to a file named `text.txt` and offered to the user
     * as a downloadable file in the browser.
     *
     * @param text The plain text content to be shared.
     */
    actual suspend fun shareText(text: String) {
        FileKit.download(bytes = text.encodeToByteArray(), fileName = "text.txt")
    }

    /**
     * Shares a file by triggering a download of the file's byte content.
     *
     * This method creates a download link in the browser for the given
     * [ShareFileModel.bytes], using the provided [ShareFileModel.fileName]
     * as the download file name.
     *
     * @param file The [ShareFileModel] containing file name and content to be downloaded.
     */
    actual suspend fun shareFile(file: ShareFileModel) {
        FileKit.download(bytes = file.bytes, fileName = file.fileName)
    }
}
