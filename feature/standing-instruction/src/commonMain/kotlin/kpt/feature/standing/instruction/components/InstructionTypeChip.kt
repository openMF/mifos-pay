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

@Composable
fun InstructionTypeChip(
    type: String?,
    modifier: Modifier = Modifier,
) {
    val typeEnum = StandingInstructionType.fromString(type)

    val (backgroundColor, contentColor) = when (typeEnum) {
        StandingInstructionType.FIXED ->
            KptTheme.colorScheme.primaryContainer to
                KptTheme.colorScheme.onPrimaryContainer

        StandingInstructionType.DUES ->
            KptTheme.colorScheme.tertiaryContainer to
                KptTheme.colorScheme.onTertiaryContainer

        else -> KptTheme.colorScheme.surfaceVariant to KptTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = KptTheme.shapes.extraSmall,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Text(
            text = typeEnum?.name ?: "Not Available",
            modifier = Modifier.padding(KptTheme.spacing.xs),
            color = if (typeEnum == null) {
                KptTheme.colorScheme.error
            } else {
                KptTheme.colorScheme.onBackground
            },
            style = KptTheme.typography.bodySmall,
        )
    }
}

enum class StandingInstructionType {
    FIXED,
    DUES,
    ;

    companion object {
        fun fromString(value: String?): StandingInstructionType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
