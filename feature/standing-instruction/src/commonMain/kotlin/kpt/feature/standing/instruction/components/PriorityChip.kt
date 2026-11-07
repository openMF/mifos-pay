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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.model.standinginstruction.StandingInstruction
/** Chip showing a standing instruction's priority option, colored per priority. */
@Composable
fun PriorityChip(
    priority: StandingInstruction.Option?,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, contentColor) = when (priority?.id) {
        1L -> KptTheme.colorScheme.error to KptTheme.colorScheme.surfaceContainerLowest
        2L -> KptTheme.colorScheme.errorContainer to KptTheme.colorScheme.surfaceContainerLowest
        3L -> KptTheme.colorScheme.secondary to KptTheme.colorScheme.surfaceContainerLowest
        4L -> KptTheme.colorScheme.tertiary to KptTheme.colorScheme.surfaceContainerLowest
        else -> KptTheme.colorScheme.outline to KptTheme.colorScheme.surfaceContainerLowest
    }

    Surface(
        modifier = modifier,
        shape = KptTheme.shapes.extraSmall,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Text(
            text = priority?.value ?: "",
            modifier = Modifier.padding(KptTheme.spacing.xs),
            style = KptTheme.typography.bodySmall,
            maxLines = 1,
        )
    }
}
