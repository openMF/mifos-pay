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
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.icon.MifosIcons
/**
 * Type of information to display in the bottom sheet.
 * Determines the icon and color scheme used.
 */
enum class InfoType {
    /** General information - shows info icon with primary color */
    INFO,

    /** Warning information - shows warning icon with tertiary/amber color */
    WARNING,

    /** Success information - shows check icon with green color */
    SUCCESS,

    /** Action required - shows action icon with secondary color */
    ACTION_REQUIRED,
}

/**
 * A professional info bottom sheet that displays informational content
 * with appropriate icons, colors, and action buttons.
 *
 * Similar to [ErrorBottomSheet] but for informational/warning scenarios
 * rather than errors.
 *
 * @param title The title to display
 * @param message The message/description to display
 * @param infoType The type of info to determine visual styling
 * @param onDismiss Called when the bottom sheet is dismissed
 * @param modifier Modifier for the bottom sheet
 * @param primaryActionText Text for the primary action button (optional)
 * @param onPrimaryAction Called when the primary action button is clicked
 * @param secondaryActionText Text for the secondary/dismiss button
 * @param onSecondaryAction Called when the secondary action button is clicked (defaults to onDismiss)
 */
@Composable
fun InfoBottomSheet(
    title: String,
    message: String,
    infoType: InfoType = InfoType.INFO,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String = "Cancel",
    onSecondaryAction: (() -> Unit)? = null,
) {
    MifosBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        InfoBottomSheetContent(
            title = title,
            message = message,
            infoType = infoType,
            primaryActionText = primaryActionText,
            onPrimaryAction = onPrimaryAction,
            secondaryActionText = secondaryActionText,
            onSecondaryAction = onSecondaryAction ?: onDismiss,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun InfoBottomSheetContent(
    title: String,
    message: String,
    infoType: InfoType,
    primaryActionText: String?,
    onPrimaryAction: (() -> Unit)?,
    secondaryActionText: String,
    onSecondaryAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (icon, iconBackgroundColor, iconTintColor) = getInfoVisuals(infoType)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Info icon with background
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
        if (primaryActionText != null && onPrimaryAction != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onSecondaryAction()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KptTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(text = secondaryActionText)
                }

                MifosButton(
                    onClick = {
                        onDismiss()
                        onPrimaryAction()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = primaryActionText)
                }
            }
        } else {
            // Single dismiss button
            MifosButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = secondaryActionText)
            }
        }
    }
}

@Composable
private fun getInfoVisuals(infoType: InfoType): Triple<ImageVector, Color, Color> {
    return when (infoType) {
        InfoType.INFO -> Triple(
            MifosIcons.Info,
            KptTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            KptTheme.colorScheme.primary,
        )

        InfoType.WARNING -> Triple(
            MifosIcons.Warning,
            KptTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            KptTheme.colorScheme.tertiary,
        )

        InfoType.SUCCESS -> Triple(
            MifosIcons.CheckCircle,
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
        )

        InfoType.ACTION_REQUIRED -> Triple(
            MifosIcons.SendRightTilted,
            KptTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            KptTheme.colorScheme.secondary,
        )
    }
}
