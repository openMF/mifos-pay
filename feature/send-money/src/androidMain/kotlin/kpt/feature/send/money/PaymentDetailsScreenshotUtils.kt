/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.core.view.drawToBitmap
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kpt.core.ui.fork.utils.MimeType
import kpt.core.ui.fork.utils.ScreenshotContextDetails
import kpt.core.ui.fork.utils.ShareFileModel
import kpt.core.ui.fork.utils.ShareUtils
import java.io.ByteArrayOutputStream

// TODO Capture the whole content of this page
// currently this captures only visible screen between topbar and share screenshot button
/**
 * Specialized screenshot utility for PaymentDetailsScreen.
 *
 * This utility is designed specifically for taking screenshots of the payment details screen
 * while excluding the top bar and bottom button areas. It captures only the scrollable content
 * area for clean, professional screenshots.
 */
object PaymentDetailsScreenshotUtils {

    private const val CONTENT_PADDING_DP = 16
    private const val COMPRESSION_QUALITY = 90
    private const val TEST_TAG_PAYMENT_DETAILS_CONTENT = "payment-details-content"

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
     * Takes a screenshot of the PaymentDetailsScreen and shares it.
     *
     * @param contextDetails Payment context details for better file naming
     * @param onError Callback when screenshot or sharing fails
     */
    suspend fun takePaymentDetailsScreenshotAndShare(
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    ) {
        try {
            Logger.d("Taking payment details screenshot and sharing...")
            val screenshotBytes = takePaymentDetailsScreenshotSync()

            withContext(Dispatchers.Main) {
                val fileName = contextDetails.generateFileName()
                val shareFile = ShareFileModel(
                    mime = MimeType.IMAGE,
                    fileName = fileName,
                    bytes = screenshotBytes,
                )
                ShareUtils.shareFile(shareFile)
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to take payment details screenshot: ${e.message}" }
            onError("Failed to take screenshot: ${e.message}")
        }
    }

    /**
     * Takes a screenshot of the PaymentDetailsScreen synchronously and returns the bytes.
     *
     * @return Byte array of the compressed screenshot
     */
    private suspend fun takePaymentDetailsScreenshotSync(): ByteArray {
        return withContext(Dispatchers.Main) {
            val activity = activityProvider.invoke()
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

            val screenshot = capturePaymentDetailsScreenshot(rootView)
            compressScreenshot(screenshot)
        }
    }

    /**
     * Captures a screenshot of the PaymentDetailsScreen while excluding the top bar and bottom button areas.
     *
     * @param rootView The root view to capture
     * @return Bitmap of the screenshot with only the content area
     */
    private fun capturePaymentDetailsScreenshot(rootView: ViewGroup): Bitmap {
        val bitmap = rootView.drawToBitmap()
        val contentArea = findContentArea(rootView)

        return if (contentArea != null && contentArea.width() > 0 && contentArea.height() > 0) {
            captureContentAreaScreenshot(bitmap, contentArea, rootView.resources.displayMetrics.density)
        } else {
            captureFallbackScreenshot(bitmap)
        }
    }

    /**
     * Captures screenshot using the identified content area with proper padding.
     */
    private fun captureContentAreaScreenshot(
        bitmap: Bitmap,
        contentArea: android.graphics.Rect,
        density: Float,
    ): Bitmap {
        val paddingPx = (CONTENT_PADDING_DP * density).toInt()

        val paddedLeft = maxOf(0, contentArea.left - paddingPx)
        val paddedTop = maxOf(0, contentArea.top - paddingPx)
        val paddedRight = minOf(bitmap.width, contentArea.right + paddingPx)
        val paddedBottom = minOf(bitmap.height, contentArea.bottom + paddingPx)

        val paddedWidth = paddedRight - paddedLeft
        val paddedHeight = paddedBottom - paddedTop

        val contentBitmap = Bitmap.createBitmap(
            bitmap,
            paddedLeft,
            paddedTop,
            paddedWidth,
            paddedHeight,
        )
        bitmap.recycle()
        return contentBitmap
    }

    /**
     * Captures screenshot using fallback method when content area is not found.
     * Excludes top bar and bottom button areas using estimated positions.
     */
    private fun captureFallbackScreenshot(bitmap: Bitmap): Bitmap {
        val screenHeight = bitmap.height
        val screenWidth = bitmap.width

        // Estimate top bar height (status bar + app bar)
        val topBarHeight = (screenHeight * 0.12f).toInt()

        // Estimate bottom button area height
        val bottomButtonAreaHeight = (screenHeight * 0.15f).toInt()

        // Calculate the area to capture (between top bar and bottom button)
        val captureTop = topBarHeight
        val captureHeight = screenHeight - topBarHeight - bottomButtonAreaHeight

        Logger.d("Fallback screenshot: excluding top ${topBarHeight}px and bottom ${bottomButtonAreaHeight}px")

        if (captureHeight > 0) {
            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                captureTop,
                screenWidth,
                captureHeight,
            )
            bitmap.recycle()
            return croppedBitmap
        } else {
            Logger.w("Invalid capture dimensions, returning original bitmap")
            return bitmap
        }
    }

    /**
     * Compresses the screenshot bitmap to reduce file size.
     *
     * @param bitmap The screenshot bitmap to compress
     * @return Compressed image as byte array
     */
    private suspend fun compressScreenshot(bitmap: Bitmap): ByteArray {
        return withContext(Dispatchers.IO) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()

            bitmap.recycle()
            bytes
        }
    }

    /**
     * Finds the content area using the testTag.
     *
     * @param rootView The root view to search in
     * @return Rect representing the content area bounds, or null if not found
     */
    private fun findContentArea(rootView: ViewGroup): android.graphics.Rect? {
        return findViewWithTestTag(rootView, TEST_TAG_PAYMENT_DETAILS_CONTENT)?.let { contentView ->
            val location = IntArray(2)
            contentView.getLocationInWindow(location)

            var viewWidth = contentView.width
            var viewHeight = contentView.height

            if (viewWidth <= 0 || viewHeight <= 0) {
                Logger.w("Content view has invalid dimensions: ${viewWidth}x$viewHeight")
                return null
            }

            android.graphics.Rect(
                location[0],
                location[1],
                location[0] + viewWidth,
                location[1] + viewHeight,
            )
        }
    }

    /**
     * Recursively searches for a view with the specified test tag.
     *
     * @param root The root view to search in
     * @param testTag The test tag to search for
     * @return The view with the matching test tag, or null if not found
     */
    private fun findViewWithTestTag(root: View, testTag: String): View? {
        if (root.tag == testTag) {
            return root
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                val result = findViewWithTestTag(child, testTag)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }
}
