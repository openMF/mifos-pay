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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.finance

/**
 * Compact rate-change indicator — directional icon + percentage / delta text, both colored
 * from [MaterialTheme.finance] (rateUp / rateDown / rateFlat). Renders inside a tinted
 * container so it reads as a single visual unit.
 *
 * Pass [delta] already-formatted (e.g. "+1.25%", "-$0.12", "—").
 */
@Composable
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier) {
    val (container, content, icon) = resolveRateColors(direction)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(PaddingValues(horizontal = 8.dp, vertical = 3.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = delta,
            color = content,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

private data class RateColors(
    val container: Color,
    val content: Color,
    val icon: ImageVector,
)

@Composable
private fun resolveRateColors(direction: RateDirection): RateColors {
    val f = MaterialTheme.finance
    return when (direction) {
        RateDirection.Up -> RateColors(f.rateUpContainer, f.rateUp, Icons.Default.TrendingUp)
        RateDirection.Down -> RateColors(f.rateDownContainer, f.rateDown, Icons.Default.TrendingDown)
        RateDirection.Flat -> RateColors(
            f.rateFlatContainer,
            f.rateFlat,
            Icons.AutoMirrored.Filled.TrendingFlat,
        )
    }
}

private operator fun RateColors.component1() = container
private operator fun RateColors.component2() = content
private operator fun RateColors.component3() = icon
