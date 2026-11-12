/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.finance

/**
 * Compact colored pill used to convey state at a glance — bill status, loan stage, rate
 * direction, sync state. Stays one line; no icons (use [UrgencyDot] when you want a leading
 * accent).
 */
@Composable
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier) {
    val (container, content) = resolveChipColors(intent)
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(PaddingValues(horizontal = 10.dp, vertical = 4.dp)),
        color = content,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun resolveChipColors(intent: StatusChipIntent): Pair<Color, Color> {
    val cs = MaterialTheme.colorScheme
    val f = MaterialTheme.finance
    return when (intent) {
        StatusChipIntent.Success -> cs.secondaryContainer to cs.onSecondaryContainer
        StatusChipIntent.Warning -> cs.tertiaryContainer to cs.onTertiaryContainer
        StatusChipIntent.Danger -> f.moneyNegativeContainer to f.urgencyOverdue
        StatusChipIntent.Info -> cs.primaryContainer to cs.onPrimaryContainer
        StatusChipIntent.Neutral -> cs.surfaceContainerHighest to cs.onSurfaceVariant
    }
}
