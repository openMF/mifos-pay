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

import androidx.compose.ui.Modifier

/**
 * Platform-specific utilities for taking screenshots of Compose screens.
 *
 * This expect declaration should be implemented for each platform to handle
 * the specifics of screenshot functionality.
 */
expect object ScreenshotUtils {

    /**
     * Takes a screenshot and immediately shares it using the platform's sharing mechanism.
     * This overload allows passing context-specific details for better file naming.
     *
     * @param excludeModifiers List of modifiers that identify UI elements to exclude from the screenshot
     * @param contextDetails Context-specific details to include in the filename
     * @param onError Callback when screenshot or sharing fails
     */
    suspend fun takeScreenshotAndShare(
        excludeModifiers: List<Modifier> = emptyList(),
        contextDetails: ScreenshotContextDetails,
        onError: (String) -> Unit,
    )
}

/**
 * Context details for screenshots to enable better file naming.
 *
 * @property screenType The type of screen being captured
 * @property amount Optional amount information for payment-related screens
 * @property recipientName Optional recipient name for payment-related screens
 * @property timestamp Optional timestamp for the screenshot
 * @property customSuffix Optional custom suffix to append to the filename
 */
data class ScreenshotContextDetails(
    val screenType: String,
    val amount: String? = null,
    val recipientName: String? = null,
    val timestamp: String? = null,
    val customSuffix: String? = null,
) {
    /**
     * Generates a meaningful filename based on the context details.
     *
     * @return A sanitized filename suitable for file systems
     */
    fun generateFileName(): String {
        val sanitizedScreenType = screenType.replace(" ", "_").lowercase()
        val sanitizedAmount = amount?.replace(" ", "_")?.replace(",", "")?.replace(".", "_")?.replace("₹", "INR")
        val sanitizedRecipient = recipientName?.replace(" ", "_")?.replace(",", "")?.replace(".", "")
        val sanitizedTimestamp = timestamp?.replace(" ", "_")?.replace(",", "")?.replace(":", "_")?.replace("am", "AM")?.replace("pm", "PM")

        val fileName = buildString {
            append(sanitizedScreenType)
            if (sanitizedAmount != null) append("_$sanitizedAmount")
            if (sanitizedRecipient != null) append("_to_$sanitizedRecipient")
            if (sanitizedTimestamp != null) append("_$sanitizedTimestamp")
            if (customSuffix != null) append("_$customSuffix")
            append(".png")
        }

        return fileName
    }
}
