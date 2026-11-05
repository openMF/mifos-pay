/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState

/**
 * [ScreenContent] wrapped in a Material 3 [PullToRefreshBox] — a self-contained pull-to-refresh
 * screen for the common case where a screen owns its own refresh gesture rather than delegating
 * to the app-level `KptScaffold` / `KptPullToRefreshState`.
 *
 * Closes the doc↔source gap in the `CORE_STORE.md` wrapper table (the pull-to-refresh row) with a
 * concrete, dual-input (Store5-optional, D2) composable: it accepts BOTH a live [ScreenDataStream]
 * (the stream overload) AND a plain [ScreenState] value (the value overload), so a static screen
 * passes a value now and upgrades to a Store5 stream later with ZERO call-site change.
 *
 * The refreshing spinner is driven by the content's [FreshnessSignal.isRefreshing] — when the
 * Store5 stream reports an in-flight refresh, the M3 indicator shows automatically. `onRefresh`
 * is invoked when the user pulls; the stream overload defaults it to `stream.refresh()`.
 *
 * Every one of [ScreenContent]'s state slots — [loading], [empty], [noNetwork], [error],
 * [onDismiss] — is forwarded, matching the sibling `PagingScreenContent`. A screen that brands one
 * of them (an app-specific empty card, say) keeps the pull gesture; it used to have to choose,
 * because these parameters did not exist here and the inner `ScreenContent` always rendered the
 * framework defaults.
 *
 * For infinite-scroll paginated lists prefer `PagingScreenContent` (it coordinates load-more with
 * refresh); for dashboards whose refresh is owned by `KptScaffold`, keep the app-level gesture and
 * use plain `ScreenContent`. This wrapper is for a single-source screen that owns its own refresh.
 */
// STORE5-COMPLETENESS: refreshable-stream — stream overload
/**
 * Direct [ScreenDataStream] overload — the ViewModel exposes the repository-built stream and
 * Compose collects `stream.state` lifecycle-aware here, wiring the pull gesture to
 * `stream.refresh()`. Mirrors [ScreenContent]'s stream overload.
 *
 * ```kotlin
 * RefreshableScreenContent(stream = viewModel.rates) { rates, _ -> RatesList(rates) }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RefreshableScreenContent(
    stream: ScreenDataStream<T>,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = stream::refresh,
    onDismiss: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRefresh, isCaptivePortal = captive, onDismiss = onDismiss)
    },
    error: @Composable (Throwable) -> Unit = {
        DefaultErrorContent(it, onRefresh, onDismiss = onDismiss)
    },
    content: @Composable (data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    val state by stream.state.collectAsStateWithLifecycle(ScreenState.Loading)
    RefreshableScreenContent(
        state = state,
        onRefresh = onRefresh,
        modifier = modifier,
        onDismiss = onDismiss,
        loading = loading,
        empty = empty,
        noNetwork = noNetwork,
        error = error,
        content = content,
    )
}

// STORE5-COMPLETENESS: refreshable-value — non-stream (plain ScreenState) overload
/**
 * Non-stream (Store5-optional) overload — a caller with a plain [ScreenState] value (static
 * content, a test fixture, a projected/combined payload the ViewModel owns) renders through the
 * SAME [PullToRefreshBox] chrome without being forced to construct a [ScreenDataStream]. The
 * refreshing indicator tracks [ScreenState.Content.freshnessSignal]`.isRefreshing`.
 *
 * ```kotlin
 * RefreshableScreenContent(state = uiState, onRefresh = viewModel::onRefresh) { data, _ -> … }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RefreshableScreenContent(
    state: ScreenState<T>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultLoadingContent() },
    empty: @Composable () -> Unit = { DefaultEmptyContent() },
    noNetwork: @Composable (isCaptivePortal: Boolean) -> Unit = { captive ->
        DefaultNoNetworkContent(onRefresh, isCaptivePortal = captive, onDismiss = onDismiss)
    },
    error: @Composable (Throwable) -> Unit = {
        DefaultErrorContent(it, onRefresh, onDismiss = onDismiss)
    },
    content: @Composable (data: T, freshnessSignal: FreshnessSignal) -> Unit,
) {
    val isRefreshing = (state as? ScreenState.Content)?.freshnessSignal?.isRefreshing == true
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        // ScreenContent owns the loading / empty / no-network / error / content rendering, and
        // every one of its slots is forwarded so a screen that brands one is not forced to give
        // up the pull gesture to do it. The in-content refreshing banner is the single exception:
        // it is suppressed because PullToRefreshBox already shows the M3 pull indicator, and two
        // progress affordances for one fetch is worse than none.
        ScreenContent(
            state = state,
            onRetry = onRefresh,
            refreshingIndicator = null,
            onDismiss = onDismiss,
            loading = loading,
            empty = empty,
            noNetwork = noNetwork,
            error = error,
            content = content,
        )
    }
}
