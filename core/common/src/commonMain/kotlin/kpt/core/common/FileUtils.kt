/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Platform-neutral contract for writing downloaded bytes (e.g. receipts) to disk. */
interface FileUtils {
    /** Writes [inputStream] bytes to [filePath]; returns true on success. */
    suspend fun writeInputStreamDataToFile(inputStream: ByteArray, filePath: String): Boolean

    /** Holds the shared logger for [FileUtils] implementations. */
    companion object {
        /** Logger tagged FileUtils. */
        val logger = Logger.withTag("FileUtils")
    }
}

/** Creates the platform-specific [FileUtils] implementation. */
expect fun createPlatformFileUtils(): FileUtils

/** Default [FileUtils] used on platforms without a dedicated implementation. */
class CommonFileUtils : FileUtils {
    override suspend fun writeInputStreamDataToFile(
        inputStream: ByteArray,
        filePath: String,
    ): Boolean =
        withContext(Dispatchers.Default) {
            try {
                true
            } catch (e: Exception) {
                FileUtils.logger.e { "Error writing file: ${e.message}" }
                false
            }
        }
}
