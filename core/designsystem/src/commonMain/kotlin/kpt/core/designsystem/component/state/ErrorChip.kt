/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * M3 AssistChip styled for error reporting. Slightly more prominent than [InlineErrorPill]
 * — adds an icon and uses chip semantics (clickable by default if [onClick] is non-null).
 *
 * Use for filter-bar-level / toolbar-level error reporting, or anywhere an M3 chip would
 * fit naturally (alongside FilterChips, etc.).
 */
@Composable
fun ErrorChip(message: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    AssistChip(
        onClick = onClick ?: {},
        enabled = onClick != null,
        label = { Text(message) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            labelColor = MaterialTheme.colorScheme.onErrorContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = modifier,
    )
}
