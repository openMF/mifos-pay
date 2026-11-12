/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.chart

/**
 * Pure-function path geometry for sparkline / area chart composables.
 *
 * Extracted from the `Canvas { drawPath() }` block so the math is unit-testable
 * without a Compose test rule. The composable consumes the returned `(x, y)` pairs
 * and emits exactly one `lineTo` per point.
 *
 * **Y-axis convention**: Compose Canvas places `y = 0` at the top. We flip the
 * normalized value so the series maximum maps to `y = 0` (top of the canvas)
 * and the series minimum maps to `y = height` (bottom). That matches what a
 * human reads as "line going up = value going up".
 *
 * **Degenerate-input contracts**:
 *  - Empty list → empty result.
 *  - Single value → `x = 0`, `y = height / 2`.
 *  - All-equal values (zero range) → all y's at the canvas midline. No division by zero.
 */
object SparklineGeometry {

    /**
     * Normalize [values] to canvas coordinates fitting `[0, width] × [0, height]`.
     *
     * @return ordered list of `(x, y)` pairs — one per input value.
     */
    fun normalize(values: List<Double>, width: Float, height: Float): List<Pair<Float, Float>> {
        val result: List<Pair<Float, Float>> = when {
            values.isEmpty() -> emptyList()
            values.size == 1 -> listOf(0f to height / 2f)
            else -> {
                val min = values.min()
                val max = values.max()
                val range = max - min
                val stepX = width / (values.size - 1)
                values.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = if (range == 0.0) {
                        height / 2f
                    } else {
                        val normalized = ((value - min) / range).toFloat()
                        height - normalized * height
                    }
                    x to y
                }
            }
        }
        return result
    }
}
