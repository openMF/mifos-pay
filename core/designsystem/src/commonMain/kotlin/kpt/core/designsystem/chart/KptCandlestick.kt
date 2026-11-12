/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.core.designsystem.chart

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.finance

/**
 * One open-high-low-close bar for [KptCandlestick]. All values are in the
 * same unit (price, rate, etc.) and rendered against a shared y-axis covering
 * the series' min/max.
 */
data class Candle(
    /** Opening price. */
    val open: Float,
    /**
     * Highest price within the period. Sets the top of the wick; must be ≥ both [open] and [close] or the candle
     * renders inverted.
     */
    val high: Float,
    /** Lowest price within the period. Sets the bottom of the wick. */
    val low: Float,
    /** Closing price. Compared against [open] to decide the up/down colour. */
    val close: Float,
) {
    /** True when the candle closed at or above its open (rendered with `upColor`). */
    val isUp: Boolean get() = close >= open
}

/**
 * Classic OHLC candlestick chart for price-like time series. Up candles
 * (close ≥ open) use [upColor]; down candles use [downColor]. Default
 * up/down colors resolve from `MaterialTheme.finance` so they brand-shift
 * with theme without code edits.
 *
 * Practical input cap: ~90 candles (3 months daily). For longer ranges
 * caller buckets to weekly/monthly candles.
 *
 * Empty input → renders nothing.
 *
 * @param candles Ordered OHLC bars, drawn left-to-right.
 * @param modifier Layout modifier — sized by caller.
 * @param upColor Color for candles that closed at or above their open.
 * @param downColor Color for candles that closed below their open.
 * @param wickWidth Stroke width for the high-low wick line.
 */
@Composable
fun KptCandlestick(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    upColor: Color = MaterialTheme.finance.moneyPositive,
    downColor: Color = MaterialTheme.finance.moneyNegative,
    wickWidth: Dp = 1.dp,
) {
    Canvas(modifier = modifier) {
        if (candles.isEmpty()) return@Canvas

        val priceMin = candles.minOf { it.low }
        val priceMax = candles.maxOf { it.high }
        val priceRange = (priceMax - priceMin).takeIf { it > 0f } ?: 1f

        val candleSpacing = size.width / candles.size
        val candleBodyWidth = candleSpacing * 0.7f
        val wickStrokePx = wickWidth.toPx()

        candles.forEachIndexed { index, candle ->
            val centerX = (index + 0.5f) * candleSpacing
            fun yFor(price: Float) = size.height - ((price - priceMin) / priceRange) * size.height

            val color = if (candle.isUp) upColor else downColor

            // Wick (high ↔ low).
            drawLine(
                color = color,
                start = Offset(centerX, yFor(candle.high)),
                end = Offset(centerX, yFor(candle.low)),
                strokeWidth = wickStrokePx,
            )

            // Body (open ↔ close).
            val bodyTop = yFor(maxOf(candle.open, candle.close))
            val bodyBottom = yFor(minOf(candle.open, candle.close))
            val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(wickStrokePx)
            drawRect(
                color = color,
                topLeft = Offset(centerX - candleBodyWidth / 2f, bodyTop),
                size = Size(candleBodyWidth, bodyHeight),
            )
        }
    }
}
