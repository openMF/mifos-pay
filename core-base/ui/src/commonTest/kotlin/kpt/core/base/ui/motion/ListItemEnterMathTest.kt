/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure-function tests for [ListItemEnterMath]. The Composable
 * `Modifier.kptListItemEnter` consumes these helpers — these tests guard
 * the stagger / cap / snap-past-N contract.
 */
class ListItemEnterMathTest {

    @Test
    fun itemsWithinTotalAnimatedShouldAnimate() {
        repeat(20) { i ->
            assertTrue(
                ListItemEnterMath.shouldAnimate(index = i, totalAnimated = 20),
                "index $i within [0, 20) must animate",
            )
        }
    }

    @Test
    fun itemsBeyondTotalAnimatedSnapIn() {
        assertFalse(ListItemEnterMath.shouldAnimate(index = 20, totalAnimated = 20))
        assertFalse(ListItemEnterMath.shouldAnimate(index = 100, totalAnimated = 20))
        assertFalse(ListItemEnterMath.shouldAnimate(index = 1000, totalAnimated = 20))
    }

    @Test
    fun negativeIndicesNeverAnimate() {
        assertFalse(ListItemEnterMath.shouldAnimate(index = -1, totalAnimated = 20))
        assertFalse(ListItemEnterMath.shouldAnimate(index = -100, totalAnimated = 20))
    }

    @Test
    fun firstItemHasZeroStaggerDelay() {
        assertEquals(0L, ListItemEnterMath.staggerDelayMs(index = 0, totalAnimated = 20, staggerMs = 30))
    }

    @Test
    fun staggerDelayGrowsLinearlyUntilCap() {
        assertEquals(30L, ListItemEnterMath.staggerDelayMs(index = 1, totalAnimated = 20, staggerMs = 30))
        assertEquals(150L, ListItemEnterMath.staggerDelayMs(index = 5, totalAnimated = 20, staggerMs = 30))
        assertEquals(300L, ListItemEnterMath.staggerDelayMs(index = 10, totalAnimated = 20, staggerMs = 30))
    }

    @Test
    fun staggerDelayClampsAt600Ms() {
        assertEquals(600L, ListItemEnterMath.staggerDelayMs(index = 20, totalAnimated = 1000, staggerMs = 30))
        assertEquals(600L, ListItemEnterMath.staggerDelayMs(index = 100, totalAnimated = 1000, staggerMs = 30))
    }

    @Test
    fun nonAnimatedItemsReturnZeroDelay() {
        assertEquals(0L, ListItemEnterMath.staggerDelayMs(index = 25, totalAnimated = 20, staggerMs = 30))
    }
}
