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

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.core.view.drawToBitmap
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Android implementation of ScreenshotUtils for taking screenshots of Compose screens.
 *
 * This implementation provides platform-specific screenshot functionality for Android,
 * including the ability to exclude specific UI elements and generate context-aware filenames.
 */
actual object ScreenshotUtils {

    /**
     * Provider function to retrieve the current [Activity].
     * This must be set before using screenshot functionality.
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
     * This is required to initialize before calling any screenshot methods.
     *
     * @param provider A lambda that returns the current [Activity].
     */
    fun setActivityProvider(provider: () -> Activity) {
        activityProvider = provider
    }

    /**
     * Takes a screenshot and immediately shares it using the platform's sharing mechanism.
     * This overload allows passing context-specific details for better file naming.
     *
     * @param excludeModifiers List of modifiers that identify UI elements to exclude from the screenshot
     * @param contextDetails Context-specific details to include in the filename
     * @param onError Callback when screenshot or sharing fails
     */
    actual suspend fun takeScreenshotAndShare(
        excludeModifiers: List<Modifier>,
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    ) {
        try {
            withContext(Dispatchers.Main) {
                val activity = activityProvider.invoke()
                val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

                val bitmap = rootView.drawToBitmap()
                val canvas = Canvas(bitmap)
                val paint = Paint().apply {
                    color = android.graphics.Color.TRANSPARENT
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }

                // Exclude bottom area if modifiers are provided
                if (excludeModifiers.isNotEmpty()) {
                    val screenHeight = bitmap.height
                    val screenWidth = bitmap.width
                    val excludeAreaHeight = (screenHeight * 0.15f).toInt()

                    val excludeRect = RectF(
                        0f,
                        (screenHeight - excludeAreaHeight).toFloat(),
                        screenWidth.toFloat(),
                        screenHeight.toFloat(),
                    )

                    canvas.drawRect(excludeRect, paint)
                }

                // Compress and share
                withContext(Dispatchers.IO) {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                    val bytes = outputStream.toByteArray()

                    val fileName = contextDetails.generateFileName()
                    val shareFile = ShareFileModel(
                        mime = MimeType.IMAGE,
                        fileName = fileName,
                        bytes = bytes,
                    )

                    withContext(Dispatchers.Main) {
                        ShareUtils.shareFile(shareFile)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to share screenshot: ${e.message}" }
            onError("Failed to share screenshot: ${e.message}")
        }
    }
}
