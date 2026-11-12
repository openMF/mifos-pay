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
 * Pure-function math for donut chart composables. Extracted from the Composable
 * so sweep-angle correctness can be tested without a Compose test rule.
 *
 * **Degenerate-input contracts**:
 *  - Empty list → empty result.
 *  - All-zero or negative total → every sweep is `0f` (no division by zero).
 *  - Otherwise sweeps sum to exactly `360f` for finite positive inputs.
 */
object DonutGeometry {

    /**
     * Per-slice sweep angle in degrees, in the same order as [values].
     */
    fun sweepAngles(values: List<Float>): List<Float> {
        val total = values.sum()
        return when {
            values.isEmpty() -> emptyList()
            total <= 0f -> List(values.size) { 0f }
            else -> values.map { (it / total) * 360f }
        }
    }
}
