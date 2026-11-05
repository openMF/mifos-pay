/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.chart

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.chart.SparklineGeometry

/**
 * Filled area chart for the detail-screen Hero. Same geometry contract as
 * [KptSparkline] but adds a gradient fill under the line via
 * [ChartTokens.areaFillBrush].
 *
 * Reuses [SparklineGeometry] so degenerate-input handling (empty / single /
 * flat) is consistent with the sparkline.
 *
 * @param values Ordered time series.
 * @param modifier Layout modifier.
 * @param lineColor Stroke color along the top of the area.
 * @param fillColor Color used for the area fill (gradient fades to transparent at the bottom).
 *                  Defaults to [lineColor] so single-color call sites stay simple.
 * @param strokeWidth Stroke thickness for the line.
 */
@Composable
fun KptAreaChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = lineColor,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val points = SparklineGeometry.normalize(
            values = values,
            width = size.width,
            height = size.height,
        )
        if (points.isEmpty()) return@Canvas

        // Filled area: top line + drop to bottom-right + sweep to bottom-left.
        val areaPath = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { (x, y) -> lineTo(x, y) }
            lineTo(points.last().first, size.height)
            lineTo(points.first().first, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = ChartTokens.areaFillBrush(fillColor),
        )

        // Top line.
        val linePath = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { (x, y) -> lineTo(x, y) }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx()),
        )
    }
}
