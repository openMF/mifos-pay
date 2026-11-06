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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File

/**
 * Actual implementation of [ShareUtils] for Android platform.
 *
 * This utility enables sharing of text and files (PDF, image, text) through Android's
 * native `Intent`-based sharing system.
 */
actual object ShareUtils {

    /**
     * Provider function to retrieve the current [Activity].
     * This must be set before using [shareText] or [shareFile].
     */
    private var activityProvider: () -> Activity = {
        throw IllegalArgumentException(
            "You need to implement the 'activityProvider' to provide the required Activity. " +
                "Just make sure to set a valid activity using " +
                "the 'setActivityProvider()' method.",
        )
    }

    /**
     * Sets the activity provider function to be used internally for context retrieval.
     *
     * This is required to initialize before calling any sharing methods.
     *
     * @param provider A lambda that returns the current [Activity].
     */
    fun setActivityProvider(provider: () -> Activity) {
        activityProvider = provider
    }

    /**
     * Shares plain text content using an Android share sheet (`Intent.ACTION_SEND`).
     *
     * @param text The text content to share.
     */
    actual suspend fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val intentChooser = Intent.createChooser(intent, null)
        activityProvider.invoke().startActivity(intentChooser)
    }

    /**
     * Shares a file (e.g. PDF, text, image) using Android's file sharing mechanism.
     *
     * If the file is an image, it is compressed before sharing.
     * The file is temporarily saved to internal cache and shared using a `FileProvider`.
     *
     * @param file A [ShareFileModel] containing file metadata and binary content.
     */
    @OptIn(ExperimentalResourceApi::class)
    actual suspend fun shareFile(file: ShareFileModel) {
        val context = activityProvider.invoke().application.baseContext

        try {
            withContext(Dispatchers.IO) {
                val compressedBytes = if (file.mime == MimeType.IMAGE) {
                    compressImage(file.bytes)
                } else {
                    file.bytes
                }

                val savedFile = saveFile(file.fileName, compressedBytes, context = context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    savedFile,
                )

                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        flags += Intent.FLAG_ACTIVITY_NEW_TASK
                        flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
                        setDataAndType(uri, file.mime.toAndroidMimeType())
                    }
                    val chooser = Intent.createChooser(intent, null)
                    activityProvider.invoke().startActivity(chooser)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(e) { "Failed to share file: ${e.message}" }
        }
    }

    /**
     * Saves the provided byte array as a temporary file in the internal cache directory.
     *
     * @param name The name of the file to be saved.
     * @param data Byte array representing the file content.
     * @param context Android [Context] used to access the cache directory.
     * @return The saved [File] object.
     */
    private fun saveFile(name: String, data: ByteArray, context: Context): File {
        val cache = context.cacheDir
        val savedFile = File(cache, name)
        savedFile.writeBytes(data)
        return savedFile
    }

    /**
     * Maps [MimeType] to a corresponding Android MIME type string.
     *
     * @return Android-compatible MIME type string.
     */
    private fun MimeType.toAndroidMimeType(): String = when (this) {
        MimeType.PDF -> "application/pdf"
        MimeType.TEXT -> "text/plain"
        MimeType.IMAGE -> "image/*"
    }

    /**
     * Compresses an image file using [FileKit] logic.
     *
     * @param imageBytes The original image byte array.
     * @return A compressed image as a byte array.
     */
    private suspend fun compressImage(imageBytes: ByteArray): ByteArray {
        return FileKit.compressImage(
            bytes = imageBytes,
            // Compression quality (0–100)
            quality = 100,
            // Max width in pixels
            maxWidth = 1024,
            // Max height in pixels
            maxHeight = 1024,
            // Image format (e.g., PNG or JPEG)
            imageFormat = ImageFormat.PNG,
        )
    }
}
