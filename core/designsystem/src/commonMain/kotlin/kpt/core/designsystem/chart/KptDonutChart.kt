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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.chart.DonutGeometry
import kpt.core.base.designsystem.theme.motion

/**
 * One slice of [KptDonutChart]. [value] is unitless — the chart normalizes
 * each slice to a fraction of the sum.
 */
data class DonutSlice(
    /**
     * Label for the slice, shown in the legend rather than on the arc — arc labels are unreadable below roughly 8% of
     * the total.
     */
    val label: String,
    /**
     * Slice magnitude in data units. Converted to an arc as a share of the set's total, so values need not sum to 100.
     */
    val value: Float,
    /** Slice colour, or null to take the next hue from [ChartTokens.multiSeriesColors]. */
    val color: Color? = null,
)

/**
 * Canvas-based donut chart. Renders concentric arcs around a hollow center,
 * optionally hosting a centered Composable (e.g. summary number).
 *
 * Practical input cap: any size (rendering cost is per-slice, fixed).
 * Empty input → renders nothing (no zero-divide).
 *
 * @param slices Ordered list of slices, drawn clockwise starting at 12 o'clock.
 * @param modifier Layout modifier — sized by caller.
 * @param strokeWidth Ring thickness.
 * @param centerContent Optional composable laid out in the center hole.
 * @param animationSpec How the donut animates when [slices] changes (default: motion.durationMedium2 tween).
 */
@Composable
fun KptDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 24.dp,
    centerContent: @Composable (BoxScope.() -> Unit)? = null,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = MaterialTheme.motion.durationMedium2),
) {
    val palette = ChartTokens.multiSeriesColors()
    val sweeps = remember(slices) { DonutGeometry.sweepAngles(slices.map { it.value }) }
    // Animate the per-slice sweep total. Once at-rest, sweeps[i] is the per-slice arc;
    // during transition, we scale by the animatedFraction so all arcs grow together.
    val animatedFraction by animateFloatAsState(
        targetValue = if (sweeps.any { it > 0f }) 1f else 0f,
        animationSpec = animationSpec,
        label = "kptDonutFraction",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            if (animatedFraction <= 0f) return@Canvas
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = sweeps[index] * animatedFraction
                drawArc(
                    color = slice.color ?: palette[index % palette.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt),
                )
                startAngle += sweep
            }
        }
        if (centerContent != null) centerContent()
    }
}
