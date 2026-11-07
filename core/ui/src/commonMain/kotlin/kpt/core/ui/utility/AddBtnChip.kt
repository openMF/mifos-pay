/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utility

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
/** Chip with text and an add button, used to prompt adding a card. */
@Composable
fun AddCardChip(
    text: String,
    btnText: String,
    onAddBtn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier,
        onClick = onAddBtn,
        label = {
            Text(
                text = text,
                style = KptTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = MifosIcons.Add,
                contentDescription = btnText,
                modifier = Modifier.size(16.dp),
                tint = KptTheme.colorScheme.onPrimary,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = KptTheme.colorScheme.primary,
        ),
    )
}
