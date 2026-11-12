/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kpt.core.base.designsystem.component.progress.KptProgress
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.freshness.humanizeDuration
import kpt.core.base.ui.generated.resources.Res
import kpt.core.base.ui.generated.resources.dashboard_freshness_loading
import kpt.core.base.ui.generated.resources.dashboard_freshness_updated
import kpt.core.base.ui.generated.resources.dashboard_freshness_updated_refreshing
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Top-of-dashboard **freshness** strip: shows *when* the data was last loaded — "Updated 5m ago" —
 * NOT a "2 of 4 loaded" count. A load count conveys transient progress; staleness is what the user
 * needs to trust the numbers, so we surface the age of the stalest card ([DashboardProgressState.oldestFetchedAt]).
 *
 *  - Data present → "Updated {humanizeDuration(now − oldestFetchedAt)}", with "· refreshing…" when a
 *    background refresh is in flight ([DashboardProgressState.isAnyLoading]).
 *  - No data yet, still loading → an indeterminate bar + "Loading…".
 *  - Nothing to show (empty dashboard, no loading) → hides itself.
 *
 * @param state Aggregate state from [aggregateDashboardProgress].
 * @param modifier Modifier applied to the wrapping layout.
 * @param now Injectable clock read — defaults to [Clock.System]; overridden in tests/previews for
 *   deterministic age text.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DashboardProgressBar(
    state: DashboardProgressState,
    modifier: Modifier = Modifier,
    now: Instant = Clock.System.now(),
) {
    val spacing = KptTheme.spacing
    val fetchedAt = state.oldestFetchedAt

    when {
        // Data has loaded → show its age (staleness), optionally flagged as refreshing.
        fetchedAt != null -> {
            val ageText = humanizeDuration(now - fetchedAt)
            val label = if (state.isAnyLoading) {
                stringResource(Res.string.dashboard_freshness_updated_refreshing, ageText)
            } else {
                stringResource(Res.string.dashboard_freshness_updated, ageText)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = label
                        liveRegion = LiveRegionMode.Polite
                    },
            )
        }
        // Nothing loaded yet but the first fetch is in flight → subtle indeterminate progress.
        state.isAnyLoading && state.total > 0 -> {
            val loadingLabel = stringResource(Res.string.dashboard_freshness_loading)
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = loadingLabel
                        liveRegion = LiveRegionMode.Polite
                    },
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                KptProgress(
                    variant = KptProgress.Linear(progress = null),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = loadingLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Empty dashboard / all-error with no timestamp → nothing to surface.
        else -> return
    }
}
