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

import kpt.core.base.designsystem.chart.DonutGeometry
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pure-function tests for [DonutGeometry]. Donut Composables consume
 * `sweepAngles` directly — these tests guard the slice math.
 */
class DonutGeometryTest {

    @Test
    fun emptyValuesProducesNoSweeps() {
        assertEquals(emptyList(), DonutGeometry.sweepAngles(emptyList()))
    }

    @Test
    fun positiveValuesSumToThreeSixty() {
        val sweeps = DonutGeometry.sweepAngles(listOf(1f, 2f, 3f, 4f))
        assertEquals(4, sweeps.size)
        // Floating-point tolerance — sweep math is fraction * 360 so tiny epsilon is OK.
        assertTrue(abs(sweeps.sum() - 360f) < 0.001f, "sweeps sum to ${sweeps.sum()}")
    }

    @Test
    fun equalValuesProduceEqualSweeps() {
        val sweeps = DonutGeometry.sweepAngles(listOf(10f, 10f, 10f, 10f))
        assertEquals(4, sweeps.size)
        sweeps.forEach { assertEquals(90f, it) }
    }

    @Test
    fun zeroTotalProducesZeroSweeps() {
        // All-zero input — no slice has any data. Avoid divide-by-zero.
        val sweeps = DonutGeometry.sweepAngles(listOf(0f, 0f, 0f))
        assertEquals(3, sweeps.size)
        sweeps.forEach { assertEquals(0f, it) }
    }

    @Test
    fun negativeTotalProducesZeroSweeps() {
        // Defensive — donut sweeps can't be negative; treat negative total as "no data".
        val sweeps = DonutGeometry.sweepAngles(listOf(-1f, -2f))
        assertEquals(2, sweeps.size)
        sweeps.forEach { assertEquals(0f, it) }
    }

    @Test
    fun proportionalSweepsTrackFractionOfTotal() {
        // Three slices: 1 / 1 / 2 → total 4 → 90° / 90° / 180°.
        val sweeps = DonutGeometry.sweepAngles(listOf(1f, 1f, 2f))
        assertEquals(90f, sweeps[0])
        assertEquals(90f, sweeps[1])
        assertEquals(180f, sweeps[2])
    }
}
