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
 * Pure-function math for bar chart composables. Normalizes each bar to a fraction
 * in `[0f, 1f]` against the series' max.
 *
 * **Degenerate-input contracts**:
 *  - Empty list → empty result.
 *  - All-zero or negative max → every fraction is `0f` (no division by zero).
 *  - Negative values clamp to `0f` (bars can't grow downward).
 */
object BarGeometry {

    /**
     * Per-bar height as a fraction `[0, 1]` of the canvas height.
     */
    fun normalizedHeights(values: List<Float>): List<Float> {
        val max = values.maxOrNull() ?: 0f
        return when {
            values.isEmpty() -> emptyList()
            max <= 0f -> List(values.size) { 0f }
            else -> values.map { (it / max).coerceIn(0f, 1f) }
        }
    }
}
