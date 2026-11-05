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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.chart.SparklineGeometry

/**
 * Minimal Canvas-based sparkline — a one-pass connected polyline whose y-axis is
 * normalized to the series's own `[min, max]` range. No external chart dependency.
 *
 * Drawn purely with [Canvas] + [Path.lineTo]; the geometry computation lives in
 * [SparklineGeometry] for unit-testability.
 *
 * Caps practical input at ~365 points (1 year daily) — caller decimates larger
 * series before passing in. Renders identically on Android / iOS / Desktop / Web.
 *
 * @param values Ordered time series (oldest → newest). Empty list renders nothing.
 * @param modifier Layout modifier — caller supplies `Modifier.size(...)` or `.fillMaxWidth().height(...)`.
 * @param color Stroke color. Defaults to `colorScheme.primary`; caller can pass `MaterialTheme.finance.rateUp` etc.
 * @param strokeWidth Stroke thickness; defaults to `ChartTokens.defaultStrokeWidth`.
 * @param markerAtEnd If true, draws a filled circle at the final point to emphasize "current" value.
 */
@Composable
fun KptSparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ChartTokens.defaultStrokeWidth,
    markerAtEnd: Boolean = false,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val points = SparklineGeometry.normalize(
            values = values,
            width = size.width,
            height = size.height,
        )
        if (points.isEmpty()) return@Canvas

        val path = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(1).forEach { (x, y) -> lineTo(x, y) }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx()),
        )

        if (markerAtEnd) {
            val (lastX, lastY) = points.last()
            drawCircle(
                color = color,
                radius = strokeWidth.toPx() * 1.75f,
                center = Offset(lastX, lastY),
            )
        }
    }
}
