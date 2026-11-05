/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kpt.core.base.store.paging.PagingScreenStream
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState

/**
 * Data class representing the pull-to-refresh state and behavior.
 *
 * @param isRefreshing Indicates whether the content is currently being
 *    refreshed.
 * @param onRefresh Callback triggered when a pull-to-refresh gesture is
 *    performed.
 */
data class KptPullToRefreshState(
    /**
     * Whether the gesture is available at all. Separate from [isRefreshing] so a screen can disable pull-to-refresh
     * (e.g. while offline) without faking an in-flight refresh.
     */
    val isEnabled: Boolean,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

/**
 * Remembers and returns a [KptPullToRefreshState] instance.
 *
 * @param isRefreshing Whether the refresh animation should be shown.
 *    *(Default: `false`)*
 * @param onRefresh Callback to execute on pull-to-refresh. *(Default:
 *    empty lambda)*
 */
@Composable
fun rememberKptPullToRefreshState(
    isEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = { },
) = remember(isEnabled, isRefreshing, onRefresh) {
    KptPullToRefreshState(
        isEnabled = isEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )
}

/**
 * Bridge: derive a [KptPullToRefreshState] from a [PagingScreenStream] so
 * `KptScaffold` / `KptRootScaffold` can drive pull-to-refresh on paginated
 * screens without the consumer wiring isRefreshing/onRefresh manually.
 *
 * `isRefreshing` is true whenever the stream's current state is
 * `ScreenState.Content` with freshness == UPDATING — i.e. while a
 * refresh-triggered fetch is in flight. Pull gesture invokes
 * [PagingScreenStream.refresh].
 *
 * Usage:
 * ```kotlin
 * KptScaffold(
 *     pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
 *     ...
 * ) { ... }
 * // and disable PagingScreenContent's built-in pull-to-refresh:
 * PagingScreenContent(
 *     pagingStream = viewModel.pagingStream,
 *     onRetry = viewModel::onRetry,
 *     enablePullToRefresh = false,   // scaffold owns the gesture
 * ) { items(coins) { ... } }
 * ```
 */
@Composable
fun rememberKptPullToRefreshState(
    pagingStream: PagingScreenStream<*>,
    isEnabled: Boolean = true,
): KptPullToRefreshState {
    val state by pagingStream.state.collectAsState(ScreenState.Loading)
    val isRefreshing = (state as? ScreenState.Content<*>)?.freshnessSignal?.isRefreshing == true
    return remember(isEnabled, isRefreshing) {
        KptPullToRefreshState(
            isEnabled = isEnabled,
            isRefreshing = isRefreshing,
            onRefresh = pagingStream::refresh,
        )
    }
}

/**
 * Bridge for non-paginated single-key screens driven by [ScreenDataStream].
 * Use with `KptScaffold` for detail pages that benefit from pull-to-refresh.
 *
 * Usage:
 * ```kotlin
 * KptScaffold(
 *     pullToRefreshState = rememberKptPullToRefreshState(
 *         stream = viewModel.stream,
 *         currentState = uiState,  // your collectAsStateWithLifecycle value
 *     ),
 *     ...
 * ) { ... }
 * ```
 */
@Composable
fun <T> rememberKptPullToRefreshState(
    stream: ScreenDataStream<T>,
    currentState: ScreenState<T>,
    isEnabled: Boolean = true,
): KptPullToRefreshState {
    val isRefreshing = (currentState as? ScreenState.Content<*>)?.freshnessSignal?.isRefreshing == true
    return remember(isEnabled, isRefreshing) {
        KptPullToRefreshState(
            isEnabled = isEnabled,
            isRefreshing = isRefreshing,
            onRefresh = stream::refresh,
        )
    }
}
