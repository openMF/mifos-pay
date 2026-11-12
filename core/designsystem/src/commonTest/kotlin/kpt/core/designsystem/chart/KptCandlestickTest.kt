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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [Candle.isUp] direction classification — drives the up/down color
 * pick inside [KptCandlestick]. Pure-Kotlin; no Canvas / Compose test rule
 * needed.
 */
class KptCandlestickTest {

    @Test
    fun closeAboveOpenIsUp() {
        val candle = Candle(open = 100f, high = 110f, low = 95f, close = 108f)
        assertTrue(candle.isUp)
    }

    @Test
    fun closeBelowOpenIsDown() {
        val candle = Candle(open = 100f, high = 110f, low = 90f, close = 92f)
        assertFalse(candle.isUp)
    }

    @Test
    fun closeEqualToOpenIsUp() {
        // Doji candle — convention: tie goes to "up" so the chart shows a neutral-positive bar
        // rather than flickering between colors on noisy series.
        val candle = Candle(open = 100f, high = 102f, low = 98f, close = 100f)
        assertTrue(candle.isUp)
    }

    @Test
    fun zeroValuedCandleIsUp() {
        // Defensive — all-zero (no-data placeholder) shouldn't crash; reads as flat-up.
        val candle = Candle(open = 0f, high = 0f, low = 0f, close = 0f)
        assertTrue(candle.isUp)
    }
}
