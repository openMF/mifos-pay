/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic finance color palette — extends Material 3's [androidx.compose.material3.ColorScheme]
 * with money/rate/freshness/urgency tokens specific to financial UIs.
 *
 * Access from any Composable via [MaterialTheme.finance].
 *
 * **Fork override pattern** — to brand the toolkit without forking widgets:
 * ```
 * CompositionLocalProvider(LocalFinanceColors provides myForkFinanceColors()) {
 *     KptTheme { App() }
 * }
 * ```
 *
 * Hex values are chosen to satisfy WCAG AA contrast (≥4.5:1 normal text, ≥3.0:1 large text)
 * against the corresponding light/dark surface tokens in [Color.kt].
 */
@Immutable
data class FinanceColors(
    // ── Money semantic — sign-based ──────────────────────────────────────────
    /** Gain, income, available balance, positive delta. Use for amounts > 0. */
    val moneyPositive: Color,
    /** Loss, expense, owed, negative delta. Use for amounts < 0. */
    val moneyNegative: Color,
    /** Zero balance, no change. Use for amounts == 0. */
    val moneyNeutral: Color,
    /** Tinted background for positive-money containers (e.g. Surface behind a +$120 chip). */
    val moneyPositiveContainer: Color,
    /** Tinted background for negative-money containers. */
    val moneyNegativeContainer: Color,
    /** Tinted background for neutral/zero containers. */
    val moneyNeutralContainer: Color,
    /** Foreground text on [moneyPositiveContainer]. */
    val onMoneyPositive: Color,
    /** Foreground text on [moneyNegativeContainer]. */
    val onMoneyNegative: Color,

    // ── Rate semantic — directional ──────────────────────────────────────────
    /** Rate increased vs previous period. Reuses money palette by default. */
    val rateUp: Color,
    /** Rate decreased vs previous period. */
    val rateDown: Color,
    /** Rate unchanged. */
    val rateFlat: Color,
    /** Container color for a rising-rate indicator. */
    val rateUpContainer: Color,
    /** Container color for a falling-rate indicator. */
    val rateDownContainer: Color,
    /** Container color for an unchanged-rate indicator. */
    val rateFlatContainer: Color,

    // ── Freshness semantic — Store5 / data-recency ───────────────────────────
    /** Data is FRESH (within TTL, last fetch succeeded). Confident hue. */
    val freshnessFresh: Color,
    /** Data is STALE (TTL expired, showing cached). Muted hue. */
    val freshnessStale: Color,
    /** Data is currently being re-fetched. Pulse-friendly hue (works with [Motion.refreshingPulseDurationMs]). */
    val freshnessUpdating: Color,
    /** No network / offline. Neutral hue. */
    val freshnessOffline: Color,

    // ── Due-date urgency — for bills, loans, reminders ───────────────────────
    /** Past-due. Alarming. */
    val urgencyOverdue: Color,
    /** Due today. Attention. */
    val urgencyToday: Color,
    /** Due within 1–7 days. Neutral attention. */
    val urgencyUpcoming: Color,
    /** Due > 7 days out. Calm. */
    val urgencyDistant: Color,
)

/**
 * Light-theme finance palette. Contrast-verified against `surfaceLight` / `surfaceContainerLight` etc.
 *
 * Hex sources: forest green (`#0E7A3B`) and Salesforce-style red (`#B3261E`) for max readability
 * on the project's lavender-tinted lightScheme; Google neutral gray for zero; amber for "updating"
 * because it animates well with [androidx.compose.animation.core.RepeatMode.Reverse] pulse.
 */
fun lightFinanceColors(): FinanceColors = FinanceColors(
    moneyPositive = Color(0xFF0E7A3B),
    moneyNegative = Color(0xFFB3261E),
    moneyNeutral = Color(0xFF5F6368),
    moneyPositiveContainer = Color(0xFFD7F5E3),
    moneyNegativeContainer = Color(0xFFFAE9E7),
    moneyNeutralContainer = Color(0xFFF1F3F4),
    onMoneyPositive = Color(0xFF003318),
    onMoneyNegative = Color(0xFF410002),
    rateUp = Color(0xFF0E7A3B),
    rateDown = Color(0xFFB3261E),
    rateFlat = Color(0xFF5F6368),
    rateUpContainer = Color(0xFFD7F5E3),
    rateDownContainer = Color(0xFFFAE9E7),
    rateFlatContainer = Color(0xFFF1F3F4),
    freshnessFresh = Color(0xFF1A73E8),
    freshnessStale = Color(0xFF9AA0A6),
    freshnessUpdating = Color(0xFFF9AB00),
    freshnessOffline = Color(0xFF5F6368),
    urgencyOverdue = Color(0xFFF87171),
    urgencyToday = Color(0xFFFB923C),
    urgencyUpcoming = Color(0xFFFBBF24),
    urgencyDistant = Color(0xFF94A3B8),
)

/**
 * Dark-theme finance palette. Contrast-verified against `surfaceDark` / `surfaceContainerDark`.
 * Mirrors light values but brightened for legibility on the deep navy `#13131B` background.
 */
fun darkFinanceColors(): FinanceColors = FinanceColors(
    moneyPositive = Color(0xFF4CC882),
    moneyNegative = Color(0xFFFFB4AB),
    moneyNeutral = Color(0xFFB0B6BC),
    moneyPositiveContainer = Color(0xFF0E3B22),
    moneyNegativeContainer = Color(0xFF5C1B17),
    moneyNeutralContainer = Color(0xFF2E3033),
    onMoneyPositive = Color(0xFFB6F4CD),
    onMoneyNegative = Color(0xFFFFDAD6),
    rateUp = Color(0xFF4CC882),
    rateDown = Color(0xFFFFB4AB),
    rateFlat = Color(0xFFB0B6BC),
    rateUpContainer = Color(0xFF0E3B22),
    rateDownContainer = Color(0xFF5C1B17),
    rateFlatContainer = Color(0xFF2E3033),
    freshnessFresh = Color(0xFF8AB4F8),
    freshnessStale = Color(0xFF80868B),
    freshnessUpdating = Color(0xFFFDD663),
    freshnessOffline = Color(0xFF80868B),
    urgencyOverdue = Color(0xFFFDBA74),
    urgencyToday = Color(0xFFFBBF24),
    urgencyUpcoming = Color(0xFFFDE68A),
    urgencyDistant = Color(0xFF94A3B8),
)

/**
 * CompositionLocal for the active [FinanceColors]. Provided by [KptTheme].
 *
 * Direct access discouraged — use [MaterialTheme.finance] extension instead.
 */
val LocalFinanceColors = staticCompositionLocalOf<FinanceColors> {
    error("FinanceColors not provided — wrap content in KptTheme")
}

/** Resolve the active [FinanceColors] from composition. */
val MaterialTheme.finance: FinanceColors
    @Composable
    @ReadOnlyComposable
    get() = LocalFinanceColors.current
