/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.core.designsystem.chart

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.chart.BarGeometry
import kpt.core.base.designsystem.theme.motion
import kpt.core.designsystem.theme.spacing

/**
 * One bar of [KptBarChart]. [value] is unitless — the chart normalizes each
 * bar to a fraction of the running max.
 */
data class BarDatum(
    /** Category label shown under the bar. */
    val label: String,
    /**
     * Bar height in data units. The chart scales the axis to the largest value in the set, so units need not be
     * normalised.
     */
    val value: Float,
    /** Bar colour, or null to take the next hue from [ChartTokens.multiSeriesColors]. */
    val color: Color? = null,
)

/**
 * Vertical bar chart with rounded tops and optional axis labels.
 *
 * Practical input cap: ~24 bars (2 years monthly). For longer series, caller
 * decimates or buckets.
 *
 * Empty input → renders nothing.
 *
 * @param data Ordered bars, drawn left-to-right.
 * @param modifier Layout modifier — sized by caller (chart fills width; one weight unit of
 *                 height when labels shown).
 * @param showLabels Whether to render [BarDatum.label] beneath each bar.
 * @param barCornerRadius Corner radius of bar tops/bottoms.
 * @param animationSpec How the max-value normalization animates on data change.
 */
@Composable
fun KptBarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    barCornerRadius: Dp = 4.dp,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = MaterialTheme.motion.durationMedium2),
) {
    val palette = ChartTokens.multiSeriesColors()
    val fractions = remember(data) { BarGeometry.normalizedHeights(data.map { it.value }) }
    val animatedFraction by animateFloatAsState(
        targetValue = if (data.isEmpty()) 0f else 1f,
        animationSpec = animationSpec,
        label = "kptBarFraction",
    )
    val labelStyle = ChartTokens.axisLabelStyle()
    val sp = MaterialTheme.spacing

    Column(modifier = modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (data.isEmpty()) return@Canvas
            // N bars laid out as N "slots"; each bar occupies the middle 50% of its slot.
            val slotWidth = size.width / data.size
            val barWidth = slotWidth * 0.5f
            val cornerPx = barCornerRadius.toPx()
            data.forEachIndexed { index, bar ->
                val barHeight = fractions[index] * animatedFraction * size.height
                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                drawRoundRect(
                    color = bar.color ?: palette[index % palette.size],
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                )
            }
        }
        if (showLabels && data.isNotEmpty()) {
            Spacer(Modifier.height(sp.xs))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                data.forEach { Text(it.label, style = labelStyle) }
            }
        }
    }
}
