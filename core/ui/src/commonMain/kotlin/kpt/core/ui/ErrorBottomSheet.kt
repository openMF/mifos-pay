/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.UiError
import kpt.core.designsystem.fork.component.ForkBottomSheet
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.icon.ForkIcons
/**
 * Type of error to display in the bottom sheet.
 * Determines the icon and color scheme used.
 */
enum class ErrorType {
    /** Network connectivity error - shows wifi/network icon */
    NETWORK,

    /** Server error (5xx) - shows server/error icon */
    SERVER,

    /** Client error (4xx) - shows warning icon */
    CLIENT,

    /** Validation error - shows info icon */
    VALIDATION,

    /** Generic error - shows error icon */
    GENERIC,
}

/**
 * A professional error bottom sheet that displays error information
 * with appropriate icons, colors, and action buttons.
 *
 * @param error The [UiError] containing title, message, and retry info
 * @param errorType The type of error to determine visual styling
 * @param onDismiss Called when the bottom sheet is dismissed
 * @param onRetry Called when the retry button is clicked (if [UiError.canRetry] is true)
 * @param modifier Modifier for the bottom sheet
 * @param dismissButtonText Text for the dismiss button
 * @param retryButtonText Text for the retry button
 */
@Composable
fun ErrorBottomSheet(
    error: UiError,
    errorType: ErrorType = ErrorType.GENERIC,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    dismissButtonText: String = "Close",
    retryButtonText: String = "Try Again",
) {
    ForkBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        ErrorBottomSheetContent(
            title = error.title,
            message = error.message,
            errorType = errorType,
            canRetry = error.canRetry,
            onDismiss = onDismiss,
            onRetry = onRetry,
            dismissButtonText = dismissButtonText,
            retryButtonText = retryButtonText,
        )
    }
}

/**
 * Overload for simple error messages without UiError.
 */
@Composable
fun ErrorBottomSheet(
    title: String,
    message: String,
    errorType: ErrorType = ErrorType.VALIDATION,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String = "Close",
) {
    ForkBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        ErrorBottomSheetContent(
            title = title,
            message = message,
            errorType = errorType,
            canRetry = false,
            onDismiss = onDismiss,
            onRetry = {},
            dismissButtonText = dismissButtonText,
            retryButtonText = "",
        )
    }
}

@Composable
private fun ErrorBottomSheetContent(
    title: String,
    message: String,
    errorType: ErrorType,
    canRetry: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    dismissButtonText: String,
    retryButtonText: String,
) {
    val (icon, iconBackgroundColor, iconTintColor) = getErrorVisuals(errorType)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Error icon with background
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = iconTintColor,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = title,
            style = KptTheme.typography.headlineSmall,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Message
        Text(
            text = message,
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Action buttons
        if (canRetry) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KptTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(text = dismissButtonText)
                }

                ForkButton(
                    onClick = {
                        onDismiss()
                        onRetry()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = retryButtonText)
                }
            }
        } else {
            ForkButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = dismissButtonText)
            }
        }
    }
}

@Composable
private fun getErrorVisuals(errorType: ErrorType): Triple<ImageVector, Color, Color> {
    return when (errorType) {
        ErrorType.NETWORK -> Triple(
            ForkIcons.Warning,
            KptTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            KptTheme.colorScheme.error,
        )

        ErrorType.SERVER -> Triple(
            ForkIcons.Error,
            KptTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            KptTheme.colorScheme.error,
        )

        ErrorType.CLIENT -> Triple(
            ForkIcons.Warning,
            KptTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            KptTheme.colorScheme.tertiary,
        )

        ErrorType.VALIDATION -> Triple(
            ForkIcons.Info,
            KptTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            KptTheme.colorScheme.primary,
        )

        ErrorType.GENERIC -> Triple(
            ForkIcons.Error,
            KptTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            KptTheme.colorScheme.error,
        )
    }
}
