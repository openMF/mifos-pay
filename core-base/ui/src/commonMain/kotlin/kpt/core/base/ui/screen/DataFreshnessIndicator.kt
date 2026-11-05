/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.store.freshness.FreshnessSignal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Compact updating banner shown above content when a background refresh is in flight.
 *
 * - `signal.isRefreshing = false` → hidden (no banner)
 * - `signal.isRefreshing = true`  → `primaryContainer` background, `Sync` icon, linear progress on top,
 *   "Refreshing · Last updated Xm ago"
 *
 * Stale/offline UI is owned by feature screens via the per-card [FreshnessIndicator]
 * (Material 3 `RichTooltip` info icon). Network connectivity lives in `ConnectivityBanner`.
 *
 * @param signal Per-card freshness signal. Only [FreshnessSignal.isRefreshing] is read here;
 *   the band + lastSyncedAt feed the per-card `FreshnessIndicator` info icon.
 * @param updatingLabel Custom updating message. Null uses "Refreshing…" with timestamp.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DataFreshnessIndicator(
    signal: FreshnessSignal,
    modifier: Modifier = Modifier,
    updatingLabel: String? = null,
) {
    AnimatedVisibility(
        visible = signal.isRefreshing,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier,
    ) {
        UpdatingBanner(fetchedAt = signal.lastSyncedAt, customLabel = updatingLabel)
    }
}

/**
 * Default refreshing banner used as the initial value of [ScreenContent]'s
 * `refreshingIndicator` slot. Pass `null` for the slot to suppress all in-flight
 * progress UI.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DefaultRefreshingBanner(fetchedAt: Instant? = null) {
    UpdatingBanner(fetchedAt = fetchedAt, customLabel = null)
}

/**
 * Visible-state representation derived from [FreshnessSignal.isRefreshing].
 * Internal so [DataFreshnessIndicatorStateTest] can exercise [deriveDisplayState] directly.
 */
internal enum class DisplayState {
    Hidden,
    Updating,
}

/** Pure helper — directly testable. */
internal fun deriveDisplayState(signal: FreshnessSignal): DisplayState =
    if (signal.isRefreshing) DisplayState.Updating else DisplayState.Hidden

@OptIn(ExperimentalTime::class)
@Composable
private fun UpdatingBanner(
    fetchedAt: Instant?,
    customLabel: String?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = customLabel ?: buildUpdatingText(fetchedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/**
 * Builds the refreshing label, appending how stale the showing data is when [fetchedAt] is known.
 *
 * A null timestamp means nothing has been fetched yet, so there is no age to report — "Refreshing…"
 * alone, never "Last updated never".
 */
@OptIn(ExperimentalTime::class)
internal fun buildUpdatingText(fetchedAt: Instant?): String {
    if (fetchedAt == null) return "Refreshing…"
    val age = kotlin.time.Clock.System.now() - fetchedAt
    return "Refreshing · Last updated ${formatDurationAgo(age)}"
}

/**
 * Formats an age as a coarse relative string ("just now", "5m ago", "2h ago", "3d ago").
 *
 * Deliberately coarse: a freshness hint that ticks by the second draws attention to itself instead
 * of to the data.
 */
internal fun formatDurationAgo(duration: kotlin.time.Duration): String {
    val seconds = duration.inWholeSeconds
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
