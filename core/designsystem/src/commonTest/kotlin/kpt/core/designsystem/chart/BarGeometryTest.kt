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

import kpt.core.base.designsystem.chart.BarGeometry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pure-function tests for [BarGeometry]. Bar charts consume normalized heights
 * directly — these tests guard the max-normalization math.
 */
class BarGeometryTest {

    @Test
    fun emptyValuesProducesNoHeights() {
        assertEquals(emptyList(), BarGeometry.normalizedHeights(emptyList()))
    }

    @Test
    fun tallestBarFillsFullHeight() {
        val heights = BarGeometry.normalizedHeights(listOf(2f, 4f, 1f, 3f))
        // 4f is the max → maps to 1.0.
        assertEquals(1f, heights[1])
    }

    @Test
    fun proportionalToMax() {
        val heights = BarGeometry.normalizedHeights(listOf(10f, 20f, 30f, 40f))
        assertEquals(0.25f, heights[0])
        assertEquals(0.50f, heights[1])
        assertEquals(0.75f, heights[2])
        assertEquals(1.00f, heights[3])
    }

    @Test
    fun allZeroValuesProduceZeroHeights() {
        val heights = BarGeometry.normalizedHeights(listOf(0f, 0f, 0f))
        assertEquals(3, heights.size)
        heights.forEach { assertEquals(0f, it) }
    }

    @Test
    fun negativeValuesClampToZero() {
        // Defensive — bars can't grow downward; render as empty rather than crash.
        val heights = BarGeometry.normalizedHeights(listOf(-5f, 10f, -2f))
        assertEquals(0f, heights[0])
        assertEquals(1f, heights[1])
        assertEquals(0f, heights[2])
    }

    @Test
    fun heightsStayWithinUnitInterval() {
        // Defensive — fractions must be in [0, 1] no matter the input shape.
        val heights = BarGeometry.normalizedHeights(listOf(1f, 1_000_000f, 0.001f))
        heights.forEach { assertTrue(it in 0f..1f, "fraction $it out of [0, 1]") }
    }
}
