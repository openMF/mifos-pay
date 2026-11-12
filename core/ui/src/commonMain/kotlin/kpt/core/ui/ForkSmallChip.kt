/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kpt.core.base.designsystem.theme.KptTheme

/** Small non-interactive chip showing a [label]. */
@Composable
fun ForkSmallChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = KptTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    onClick: () -> Unit = {},
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = KptTheme.shapes.extraSmall,
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Text(
            text = label,
            style = KptTheme.typography.bodySmall,
            modifier = Modifier.padding(KptTheme.spacing.xs),
        )
    }
}
