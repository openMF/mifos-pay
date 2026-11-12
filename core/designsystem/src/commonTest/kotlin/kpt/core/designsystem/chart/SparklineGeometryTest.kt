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

import kpt.core.base.designsystem.chart.SparklineGeometry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pure-function tests for [SparklineGeometry]. By keeping the path math out of the
 * `Canvas { drawPath() }` block we can exercise normalization + degenerate-input
 * handling without spinning up a Compose test rule.
 *
 * The geometry is what's visually load-bearing — once these tests pass, the Canvas
 * call site reduces to a couple of `lineTo` invocations whose correctness is
 * mechanical.
 */
class SparklineGeometryTest {

    @Test
    fun emptyValuesProducesNoPoints() {
        assertEquals(emptyList(), SparklineGeometry.normalize(values = emptyList(), width = 100f, height = 40f))
    }

    @Test
    fun singleValueRendersAsHorizontalMidpoint() {
        // A 1-point series must NOT divide-by-zero on range.
        val pts = SparklineGeometry.normalize(values = listOf(2.5), width = 100f, height = 40f)
        assertEquals(1, pts.size)
        assertEquals(0f, pts[0].first)
        // Y centered when range is 0 — value sits at mid-height.
        assertEquals(20f, pts[0].second)
    }

    @Test
    fun flatSeriesRendersAsHorizontalMidline() {
        // All-equal values would otherwise yield NaN from (v - min) / (max - min).
        val pts = SparklineGeometry.normalize(
            values = listOf(5.0, 5.0, 5.0, 5.0),
            width = 90f,
            height = 60f,
        )
        assertEquals(4, pts.size)
        pts.forEach { (_, y) -> assertEquals(30f, y) }
    }

    @Test
    fun monotonicallyIncreasingMapsMinToBottomMaxToTop() {
        // Y axis flipped — Compose Canvas y=0 is top, so MAX must map to y=0 and MIN to y=height.
        val pts = SparklineGeometry.normalize(
            values = listOf(1.0, 2.0, 3.0, 4.0),
            width = 30f,
            height = 100f,
        )
        assertEquals(4, pts.size)
        // First point — minimum value — bottom of canvas.
        assertEquals(0f, pts[0].first)
        assertEquals(100f, pts[0].second)
        // Last point — maximum value — top of canvas.
        assertEquals(30f, pts[3].first)
        assertEquals(0f, pts[3].second)
    }

    @Test
    fun xCoordinatesAreEvenlyDistributedAcrossWidth() {
        val pts = SparklineGeometry.normalize(
            values = listOf(1.0, 2.0, 3.0, 4.0, 5.0),
            width = 100f,
            height = 50f,
        )
        // 5 points → step = 100 / (5-1) = 25
        assertEquals(0f, pts[0].first)
        assertEquals(25f, pts[1].first)
        assertEquals(50f, pts[2].first)
        assertEquals(75f, pts[3].first)
        assertEquals(100f, pts[4].first)
    }

    @Test
    fun yCoordinatesAreClampedToCanvasBounds() {
        val pts = SparklineGeometry.normalize(
            values = listOf(10.0, 50.0, 90.0),
            width = 80f,
            height = 40f,
        )
        pts.forEach { (_, y) ->
            assertTrue(y in 0f..40f, "y=$y out of [0, 40]")
        }
    }
}
