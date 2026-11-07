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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.icon.ForkIcons
/** Dialog shown when a contact is not on the app, letting the user pick a channel to send an invite. */
@Composable
fun ContactNotFoundDialog(
    showDialog: Boolean,
    selectedOption: SharingOption,
    onOptionSelected: (SharingOption) -> Unit,
    onNotNowClick: () -> Unit,
    onSetAsDefaultClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = modifier,
            title = {
                Text(
                    text = "Set preferred ways to share",
                    style = KptTheme.typography.headlineSmall,
                    color = KptTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    Text(
                        text = "Your choice will be recommended for future sharing of any contact.",
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Start,
                    )

                    Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                    SharingOptionItem(
                        option = SharingOption.WHATSAPP,
                        title = "WhatsApp",
                        subtitle = "Share via WhatsApp",
                        isSelected = selectedOption == SharingOption.WHATSAPP,
                        onClick = { onOptionSelected(SharingOption.WHATSAPP) },
                    )

                    SharingOptionItem(
                        option = SharingOption.SMS,
                        title = "SMS",
                        subtitle = "Share via SMS",
                        isSelected = selectedOption == SharingOption.SMS,
                        onClick = { onOptionSelected(SharingOption.SMS) },
                    )
                }
            },
            confirmButton = {
                ForkButton(
                    onClick = onSetAsDefaultClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KptTheme.colorScheme.primary,
                        contentColor = KptTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Set as default",
                        style = KptTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onNotNowClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Not now",
                        style = KptTheme.typography.labelLarge,
                        color = KptTheme.colorScheme.primary,
                    )
                }
            },
            containerColor = KptTheme.colorScheme.surface,
            shape = KptTheme.shapes.large,
        )
    }
}

@Composable
private fun SharingOptionItem(
    option: SharingOption,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                KptTheme.colorScheme.primaryContainer
            } else {
                KptTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = KptTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        KptTheme.colorScheme.onPrimaryContainer
                    } else {
                        KptTheme.colorScheme.onSurface
                    },
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    style = KptTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        KptTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        KptTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                )
            },
            leadingContent = {
                Icon(
                    imageVector = when (option) {
                        SharingOption.WHATSAPP -> ForkIcons.Share
                        SharingOption.SMS -> ForkIcons.Share
                    },
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) {
                        KptTheme.colorScheme.onPrimaryContainer
                    } else {
                        KptTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isSelected) {
                            KptTheme.colorScheme.onPrimaryContainer
                        } else {
                            KptTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                    ),
                ) {
                    Icon(
                        imageVector = if (isSelected) {
                            ForkIcons.RadioButtonChecked
                        } else {
                            ForkIcons.RadioButtonUnchecked
                        },
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )
    }
}
