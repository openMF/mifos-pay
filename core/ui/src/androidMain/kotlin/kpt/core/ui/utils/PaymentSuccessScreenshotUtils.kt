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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.ViewGroup
import androidx.core.view.drawToBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.compressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kpt.core.ui.utils.fork.toInt
import java.io.ByteArrayOutputStream

// TODO enhance content presentation image shared in the screenshot utils
/**
 * Specialized screenshot utility for PaymentSuccessScreen.
 *
 * This utility is designed specifically for taking screenshots of the payment success screen
 * while excluding the advertisement banner and action buttons. It captures only the content
 * above the advertisement banner for clean, professional screenshots.
 */
object PaymentSuccessScreenshotUtils {

    private const val CONTENT_PADDING_DP = 48
    private const val EXTRA_BOTTOM_PADDING_DP = 16
    private const val BUTTON_AREA_HEIGHT_RATIO = 0.25f
    private const val BANNER_START_RATIO = 0.60f
    private const val TOP_PADDING_RATIO = 0.03f
    private const val COMPRESSION_QUALITY = 90
    private const val MAX_WIDTH = 1080
    private const val MAX_HEIGHT = 1920
    private const val TEST_TAG_PAYMENT_SUCCESS_CONTENT = "payment-success-content"

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
     * Takes a screenshot of the PaymentSuccessScreen with context details and immediately shares it.
     *
     * @param contextDetails Payment context details for better file naming
     * @param onError Callback when screenshot or sharing fails
     */
    suspend fun takePaymentSuccessScreenshotAndShare(
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    ) {
        try {
            val screenshotBytes = takePaymentSuccessScreenshotSync()
            val fileName = contextDetails.generateFileName()
            val shareFile = ShareFileModel(
                mime = MimeType.IMAGE,
                fileName = fileName,
                bytes = screenshotBytes,
            )

            ShareUtils.shareFile(shareFile)
        } catch (e: Exception) {
            onError("Failed to share payment success screenshot: ${e.message}")
        }
    }

    /**
     * Takes a screenshot of the PaymentSuccessScreen synchronously and returns the bytes.
     *
     * @return Byte array of the compressed screenshot
     */
    private suspend fun takePaymentSuccessScreenshotSync(): ByteArray {
        return withContext(Dispatchers.Main) {
            val activity = activityProvider.invoke()
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

            val screenshot = capturePaymentSuccessScreenshot(rootView)
            compressScreenshot(screenshot)
        }
    }

    /**
     * Captures a screenshot of the PaymentSuccessScreen while excluding the advertisement banner and action buttons.
     *
     * @param rootView The root view to capture
     * @return Bitmap of the screenshot with only content above the advertisement banner
     */
    private fun capturePaymentSuccessScreenshot(rootView: ViewGroup): Bitmap {
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
        val extraBottomPaddingPx = (EXTRA_BOTTOM_PADDING_DP * density).toInt()

        val paddedLeft = maxOf(0, contentArea.left - paddingPx)
        val paddedTop = maxOf(0, contentArea.top - paddingPx)
        val paddedRight = minOf(bitmap.width, contentArea.right + paddingPx)
        val paddedBottom = minOf(bitmap.height, contentArea.bottom + paddingPx + extraBottomPaddingPx)

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
     */
    private fun captureFallbackScreenshot(bitmap: Bitmap): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        val screenHeight = bitmap.height
        val screenWidth = bitmap.width

        val buttonAreaHeight = (screenHeight * BUTTON_AREA_HEIGHT_RATIO).toInt()
        val bannerStartY = (screenHeight * BANNER_START_RATIO).toFloat()
        val topPadding = (screenHeight * TOP_PADDING_RATIO).toInt()

        val excludeBottomRect = RectF(
            0f,
            (screenHeight - buttonAreaHeight).toFloat(),
            screenWidth.toFloat(),
            screenHeight.toFloat(),
        )
        canvas.drawRect(excludeBottomRect, paint)

        val excludeBannerRect = RectF(
            0f,
            bannerStartY,
            screenWidth.toFloat(),
            screenHeight.toFloat(),
        )
        canvas.drawRect(excludeBannerRect, paint)

        val excludeTopRect = RectF(
            0f,
            0f,
            screenWidth.toFloat(),
            topPadding.toFloat(),
        )
        canvas.drawRect(excludeTopRect, paint)

        return bitmap
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val originalBytes = outputStream.toByteArray()

            FileKit.compressImage(
                bytes = originalBytes,
                quality = COMPRESSION_QUALITY,
                maxWidth = MAX_WIDTH,
                maxHeight = MAX_HEIGHT,
                imageFormat = ImageFormat.PNG,
            )
        }
    }

    /**
     * Finds the content area above the advertisement banner using the testTag.
     *
     * @param rootView The root view to search in
     * @return Rect representing the content area bounds, or null if not found
     */
    private fun findContentArea(rootView: ViewGroup): android.graphics.Rect? {
        return findViewWithTestTag(rootView, TEST_TAG_PAYMENT_SUCCESS_CONTENT)?.let { contentView ->
            val location = IntArray(2)
            contentView.getLocationInWindow(location)

            var viewWidth = contentView.width
            var viewHeight = contentView.height

            if (viewWidth <= 0 || viewHeight <= 0) {
                contentView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                )
                viewWidth = contentView.measuredWidth
                viewHeight = contentView.measuredHeight
            }

            if (contentView is ViewGroup) {
                contentView.layout(0, 0, viewWidth, viewHeight)
            }

            if (viewWidth > 0 && viewHeight > 0) {
                android.graphics.Rect(
                    location[0],
                    location[1],
                    location[0] + viewWidth,
                    location[1] + viewHeight,
                )
            } else {
                null
            }
        }
    }

    /**
     * Recursively searches for a view with the specified test tag.
     *
     * @param viewGroup The view group to search in
     * @param testTag The test tag to search for
     * @return The view with the test tag, or null if not found
     */
    private fun findViewWithTestTag(viewGroup: ViewGroup, testTag: String): android.view.View? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            if (child.tag == testTag) {
                return child
            }

            if (child is ViewGroup) {
                val found = findViewWithTestTag(child, testTag)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }
}
