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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.DefaultEmptyContent
import kpt.core.base.ui.screen.DefaultErrorContent
import kpt.core.base.ui.screen.DefaultLoadingContent
import kpt.core.base.ui.screen.DefaultNoNetworkContent
import kpt.core.base.ui.screen.ScreenContent

/**
 * Renders a vertical stack of cards where each card carries its OWN [ScreenState] —
 * so a slow card can still spin while a fast one shows content, and a single failed
 * fetch only ruins one card instead of blanking the whole dashboard.
 *
 * Each card is wrapped in [ScreenContent], so the per-card loading / empty / no-network
 * / error / content rendering is identical to a standalone screen — same defaults,
 * same captive-portal handling, same retry CTA. Pass [onDismiss] to additionally render
 * an "X" close icon next to retry, letting users hide a single failing card
 * (typical use case: a non-critical optional card in a multi-card dashboard).
 *
 * Pair with [aggregateDashboardProgress] + [DashboardProgressBar] for a top-of-screen
 * "X of Y loaded" indicator above the grid.
 *
 * The per-card content lambda receives the `(index, data, freshness)` tuple so consumers
 * can drive per-index dispatch (e.g., a different child composable per card slot).
 *
 * TODO(testing): UI tests for this composable land once Compose UI test infrastructure
 * (compose.uiTest in commonTest) is wired into core-base/ui — see Phase 13 of the
 * core-base-store-coverage epic.
 *
 * @param states List of per-card [ScreenState] — order defines the visual order of cards.
 * @param onRetry Invoked with the card's index when the user taps "Try again" on its
 *   error / no-network state.
 * @param modifier Modifier applied to the wrapping [Column].
 * @param onDismiss Optional — when non-null, each card's error / no-network state shows an
 *   "X" icon button alongside retry; the lambda receives the card index.
 * @param loading Optional per-card loading slot (receives the card index). Defaults to the
 *   full-size [DefaultLoadingContent]. Provide a COMPACT card-sized composable for dense
 *   dashboards where the branded full-screen skeleton would overflow the card.
 * @param empty Optional per-card empty slot (receives the card index). Defaults to [DefaultEmptyContent].
 * @param noNetwork Optional per-card offline slot (receives index + `isCaptivePortal`). Defaults to
 *   [DefaultNoNetworkContent] wired to this card's retry/dismiss.
 * @param error Optional per-card error slot (receives index + the [Throwable]). Defaults to
 *   [DefaultErrorContent] wired to this card's retry/dismiss.
 * @param content Per-card content composable invoked when the card's state is
 *   [ScreenState.Content]. Receives the card index, its payload, and freshness.
 */
@Composable
fun <T> IndependentCardLayout(
    states: List<ScreenState<T>>,
    onRetry: (Int) -> Unit,
    modifier: Modifier = Modifier,
    refreshingIndicator: (@Composable () -> Unit)? = null,
    onDismiss: ((Int) -> Unit)? = null,
    cardChrome: (@Composable (index: Int, card: @Composable () -> Unit) -> Unit)? = null,
    loading: (@Composable (index: Int) -> Unit)? = null,
    empty: (@Composable (index: Int) -> Unit)? = null,
    noNetwork: (@Composable (index: Int, isCaptivePortal: Boolean) -> Unit)? = null,
    error: (@Composable (index: Int, error: Throwable) -> Unit)? = null,
    content: @Composable (index: Int, data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    val spacing = KptTheme.spacing
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        states.forEachIndexed { index, state ->
            val cardDismiss: (() -> Unit)? = onDismiss?.let { dismiss -> { dismiss(index) } }
            // The per-card state body: ScreenContent drives loading/empty/no-network/error/content.
            val cardBody: @Composable () -> Unit = {
                ScreenContent(
                    state = state,
                    onRetry = { onRetry(index) },
                    refreshingIndicator = refreshingIndicator,
                    onDismiss = cardDismiss,
                    // Per-card slots: use the consumer's compact slot when provided, else the
                    // full-size defaults wired to THIS card's retry/dismiss (identical to prior behavior).
                    loading = if (loading != null) {
                        { loading(index) }
                    } else {
                        { DefaultLoadingContent() }
                    },
                    empty = if (empty != null) {
                        { empty(index) }
                    } else {
                        { DefaultEmptyContent() }
                    },
                    noNetwork = if (noNetwork != null) {
                        { captive -> noNetwork(index, captive) }
                    } else {
                        { captive ->
                            DefaultNoNetworkContent(
                                onRetry = { onRetry(index) },
                                isCaptivePortal = captive,
                                onDismiss = cardDismiss,
                            )
                        }
                    },
                    error = if (error != null) {
                        { throwable -> error(index, throwable) }
                    } else {
                        { throwable ->
                            DefaultErrorContent(
                                throwable,
                                onRetry = { onRetry(index) },
                                onDismiss = cardDismiss,
                            )
                        }
                    },
                ) { data, freshness ->
                    content(index, data, freshness)
                }
            }
            // Persistent per-card frame (title / accent / clickable) rendered ONCE around every
            // state via cardChrome; without it the card body renders bare (prior behavior).
            if (cardChrome != null) cardChrome(index, cardBody) else cardBody()
        }
    }
}

// STORE5-COMPLETENESS: card-stream — stream overload
/**
 * Direct [ScreenDataStream] overload of [IndependentCardLayout] — one Store5 read stream per
 * independent card. Each `stream.state` is collected lifecycle-aware here and folded into the
 * `List<ScreenState<T>>` the value overload above renders, so a fast card shows content while a
 * slow one still spins and a single failed fetch only ruins its own card. Retry defaults to the
 * card's own `stream.retry()`.
 *
 * Mirrors [ScreenContent]'s stream overload so a dashboard binds a list of live Store5 streams OR
 * a list of plain [ScreenState] values (the base `states:` overload) with zero call-site
 * divergence (the D2 dual-input contract). The per-card `(index, data, freshness)` content lambda
 * is unchanged across both shapes.
 *
 * ```kotlin
 * IndependentCardLayout(
 *     streams = listOf(viewModel.loansStream, viewModel.billsStream, viewModel.ratesStream),
 * ) { index, data, _ -> DashboardCard(index, data) }
 * ```
 *
 * @param streams One [ScreenDataStream] per card — order defines the visual order of cards.
 * @param onRetry Invoked with the card's index; defaults to that card's `stream.retry()`.
 */
@Composable
fun <T> IndependentCardLayout(
    streams: List<ScreenDataStream<T>>,
    modifier: Modifier = Modifier,
    onRetry: (Int) -> Unit = { index -> streams[index].retry() },
    refreshingIndicator: (@Composable () -> Unit)? = null,
    onDismiss: ((Int) -> Unit)? = null,
    cardChrome: (@Composable (index: Int, card: @Composable () -> Unit) -> Unit)? = null,
    loading: (@Composable (index: Int) -> Unit)? = null,
    empty: (@Composable (index: Int) -> Unit)? = null,
    noNetwork: (@Composable (index: Int, isCaptivePortal: Boolean) -> Unit)? = null,
    error: (@Composable (index: Int, error: Throwable) -> Unit)? = null,
    content: @Composable (index: Int, data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    // `map` is a Kotlin inline fun, so the @Composable collectAsStateWithLifecycle call inherits
    // this composable's calling context (same inline-lambda idiom the value overload uses for
    // `states.forEachIndexed`). One collector per card stream.
    val states = streams.map { stream ->
        stream.state.collectAsStateWithLifecycle(ScreenState.Loading).value
    }
    IndependentCardLayout(
        states = states,
        onRetry = onRetry,
        modifier = modifier,
        refreshingIndicator = refreshingIndicator,
        onDismiss = onDismiss,
        cardChrome = cardChrome,
        loading = loading,
        empty = empty,
        noNetwork = noNetwork,
        error = error,
        content = content,
    )
}
