/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme

@Composable
internal fun FrequencyChip(
    option: String?,
    interval: Long?,
    modifier: Modifier = Modifier,
) {
    val isInvalid = option == null || interval == null
    val displayText = if (isInvalid) "Not Available" else "$interval $option"

    Surface(
        modifier = modifier,
        shape = KptTheme.shapes.extraSmall,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = KptTheme.colorScheme.outline,
        ),
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(KptTheme.spacing.xs),
            color = if (isInvalid) {
                KptTheme.colorScheme.error
            } else {
                KptTheme.colorScheme.onBackground
            },
            style = KptTheme.typography.bodyMedium,
        )
    }
}
