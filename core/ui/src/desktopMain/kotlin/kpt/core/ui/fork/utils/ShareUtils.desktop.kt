/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.fork.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write

/**
 * JVM-specific implementation of [ShareUtils] for desktop platforms.
 *
 * This object simulates file sharing by prompting the user with a "Save As" dialog
 * using [FileKit.openFileSaver]. It allows the user to save the content locally,
 * which is the most suitable alternative to "sharing" in desktop environments.
 */
actual object ShareUtils {

    /**
     * Prompts the user to save the given text content as a file on their system.
     *
     * This method uses [FileKit.openFileSaver] to open a native "Save As" dialog,
     * and writes the provided text to the selected file location.
     *
     * @param text The plain text content the user will save to disk.
     */
    actual suspend fun shareText(text: String) {
        val newFile = FileKit.openFileSaver(
            suggestedName = "text.txt",
        )
        newFile?.write(text.encodeToByteArray())
    }

    /**
     * Prompts the user to save a binary file (e.g., image, PDF) to their local system.
     *
     * This is used as a desktop-friendly alternative to file sharing, using
     * [FileKit.openFileSaver] to let the user choose the destination file path.
     *
     * @param file The file to be "shared", including its filename and byte content.
     */
    actual suspend fun shareFile(file: ShareFileModel) {
        val newFile = FileKit.openFileSaver(
            suggestedName = file.fileName,
        )
        newFile?.write(file.bytes)
    }
}
