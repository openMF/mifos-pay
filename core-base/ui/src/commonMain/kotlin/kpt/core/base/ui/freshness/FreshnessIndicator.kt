/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.freshness

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessSignal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Per-card freshness indicator: a small Material 3 info / clock / warning icon
 * anchored to a [TooltipBox]. Tapping the icon opens a [RichTooltip] (or
 * [PlainTooltip] when the band is `Fresh`) summarizing the staleness and offering
 * a "Refresh now" / "Retry" action when [onRefresh] is provided.
 *
 * The composable returns nothing when [signal].band is [FreshnessBand.Initial] —
 * no data has been synced yet, so showing an indicator would be misleading.
 *
 * Pure-staleness driven: never reads `NetworkStatus`. Network connectivity is
 * the orthogonal `ConnectivityBanner`'s responsibility.
 *
 * @param signal pure-staleness signal from `ViewModel.freshness.collectAsStateWithLifecycle()`
 * @param onRefresh optional refresh trigger; the tooltip action button is hidden
 *   when `null` (read-only / informational mode)
 * @param modifier propagates to the surrounding `IconButton`
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FreshnessIndicator(
    signal: FreshnessSignal,
    onRefresh: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (signal.band == FreshnessBand.Initial) return

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    val visual = freshnessVisualFor(
        band = signal.band,
        errorCategory = signal.lastError?.let { categorize(it) },
    )

    val now = Clock.System.now()
    val age = signal.lastSyncedAt?.let { now - it }
    val ageText = age?.let { humanizeDuration(it) } ?: "unknown"

    val tooltipTitle = freshnessTooltipTitle(
        band = signal.band,
        ageText = ageText,
        errorCategory = signal.lastError?.let { categorize(it) },
    )
    val tooltipBody = freshnessTooltipBody(
        band = signal.band,
        errorCategory = signal.lastError?.let { categorize(it) },
    )
    val actionLabel = if (signal.band == FreshnessBand.VeryStale) "Retry" else "Refresh now"

    val iconTint: Color = when (visual.tint) {
        FreshnessTint.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        FreshnessTint.Tertiary -> MaterialTheme.colorScheme.tertiary
        FreshnessTint.Error -> MaterialTheme.colorScheme.error
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            if (signal.band == FreshnessBand.Fresh) {
                PlainTooltip { Text(tooltipTitle) }
            } else {
                RichTooltip(
                    title = { Text(tooltipTitle) },
                    action = onRefresh?.let { refresh ->
                        {
                            TextButton(onClick = refresh) { Text(actionLabel) }
                        }
                    },
                ) {
                    Text(tooltipBody)
                }
            }
        },
        state = tooltipState,
    ) {
        IconButton(
            onClick = { scope.launch { tooltipState.show() } },
            modifier = modifier.testTag(FRESHNESS_INDICATOR_TEST_TAG),
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = "Data freshness: ${signal.band.name}",
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Test tag for the [FreshnessIndicator] root icon button. */
const val FRESHNESS_INDICATOR_TEST_TAG: String = "freshness_indicator"

/** Theme-agnostic tint slot — the composable resolves to a `MaterialTheme.colorScheme` color. */
internal enum class FreshnessTint { Neutral, Tertiary, Error }

/** The resolved (icon, tint) pair for one freshness band — the output of the mapping below. */
internal data class FreshnessVisual(
    val icon: ImageVector,
    val tint: FreshnessTint,
)

/**
 * Pure mapping from `(band, errorCategory)` to the (icon, tint) pair shown by
 * [FreshnessIndicator]. Extracted as a top-level function so it is unit-testable
 * without standing up a Compose UI test environment.
 *
 * `Initial` is mapped here for completeness even though [FreshnessIndicator]
 * returns early on it (avoids accidental "info icon for never-synced data").
 */
internal fun freshnessVisualFor(
    band: FreshnessBand,
    errorCategory: ErrorCategory?,
): FreshnessVisual = when (band) {
    FreshnessBand.Initial -> FreshnessVisual(Icons.Outlined.Info, FreshnessTint.Neutral)
    FreshnessBand.Fresh -> FreshnessVisual(Icons.Outlined.Info, FreshnessTint.Neutral)
    FreshnessBand.Stale -> FreshnessVisual(Icons.Outlined.Schedule, FreshnessTint.Tertiary)
    FreshnessBand.VeryStale -> FreshnessVisual(
        icon = Icons.Outlined.Warning,
        tint = if (errorCategory != null) FreshnessTint.Error else FreshnessTint.Tertiary,
    )
}

/**
 * Pure tooltip-title builder. `Fresh` / `Stale` show "Updated $ageText"; `VeryStale`
 * prefixes the categorised error short-message when the error is non-null.
 */
internal fun freshnessTooltipTitle(
    band: FreshnessBand,
    ageText: String,
    errorCategory: ErrorCategory?,
): String = when (band) {
    FreshnessBand.Initial -> ""
    FreshnessBand.Fresh, FreshnessBand.Stale -> "Updated $ageText"
    FreshnessBand.VeryStale -> if (errorCategory != null) {
        "${errorCategory.toShortMessage()} · $ageText"
    } else {
        "Updated $ageText"
    }
}

/** Pure body-text builder used inside [RichTooltip]. */
internal fun freshnessTooltipBody(
    band: FreshnessBand,
    errorCategory: ErrorCategory?,
): String = when (band) {
    FreshnessBand.Initial -> ""
    FreshnessBand.Fresh -> ""
    FreshnessBand.Stale -> "Data may be outdated. Pull down or tap refresh to update."
    FreshnessBand.VeryStale -> errorCategory?.toLongMessage()
        ?: "Data is significantly out of date. Try refreshing."
}
