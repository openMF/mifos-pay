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

/** Chip showing a standing instruction's type (fixed or dues), colored per type. */
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

/** Standing instruction types supported by the wallet. */
enum class StandingInstructionType {
    /** Transfers a fixed amount. */
    FIXED,

    /** Transfers the dues amount. */
    DUES,
    ;

    /** Lookup helpers for [StandingInstructionType]. */
    companion object {
        /** Returns the type whose name matches [value] ignoring case, or null if there is none. */
        fun fromString(value: String?): StandingInstructionType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
