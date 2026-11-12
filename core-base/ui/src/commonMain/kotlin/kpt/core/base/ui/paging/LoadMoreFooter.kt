/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.paging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.progress.KptProgress
import kpt.core.base.designsystem.component.progress.ProgressSize
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.base.store.paging.PagingScreenStream

/**
 * Footer for paginated lists driven by a [PagingScreenStream]. Place as the last
 * item in a LazyColumn. Shows loading indicator, error with retry, or end-of-list.
 *
 * @param pagingStream The paging stream to observe.
 * @param onLoadMore Optional auto-trigger called during composition when the footer
 *   becomes visible AND more pages are available AND no load is in flight.
 * @param loadingMessage Custom message during page load.
 * @param endMessage Custom message when all pages loaded.
 */
@Composable
fun <T : Any> LoadMoreFooter(
    pagingStream: PagingScreenStream<T>,
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
    loadingMessage: String = "Loading more...",
    endMessage: String = "You're all caught up",
) {
    val isLoadingMore by pagingStream.isLoadingMore.collectAsState()
    val hasMore by pagingStream.hasMore.collectAsState()
    val loadMoreError by pagingStream.loadMoreError.collectAsState()

    // Auto-trigger load when this footer becomes visible
    if (hasMore && !isLoadingMore && loadMoreError == null) {
        onLoadMore?.invoke()
    }

    LoadMoreFooter(
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        loadMoreError = loadMoreError,
        onRetry = pagingStream::loadNextPage,
        modifier = modifier,
        loadingMessage = loadingMessage,
        endMessage = endMessage,
    )
}

/**
 * Footer overload taking raw state values. Lets screens that drive paging without
 * exposing [PagingScreenStream] directly still use the canonical footer rendering.
 *
 * @param isLoadingMore Whether a page load is currently in flight.
 * @param hasMore Whether more pages are available.
 * @param loadMoreError The most recent load-more error, or null.
 * @param onRetry Called when the user taps "Tap to retry".
 * @param loadingMessage Custom message during page load.
 * @param endMessage Custom message when all pages loaded.
 */
@Composable
fun LoadMoreFooter(
    isLoadingMore: Boolean,
    hasMore: Boolean,
    loadMoreError: Throwable?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingMessage: String = "Loading more...",
    endMessage: String = "You're all caught up",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoadingMore -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    KptProgress(variant = KptProgress.Circular(size = ProgressSize.Sm))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = loadingMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            loadMoreError != null -> {
                // Category-aware copy: typed errors get typed UI. An OfflineException
                // (from PagingScreenStream's offline guard) categorizes as Network and
                // shows "No internet" + cloud-off icon — instead of the generic
                // "Failed to load more" treatment.
                val copy = remember(loadMoreError) { loadMoreFooterCopy(loadMoreError) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = copy.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = copy.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    TextButton(onClick = onRetry) {
                        Text(copy.retryText)
                    }
                }
            }

            !hasMore -> {
                Text(
                    text = endMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/** Footer copy + icon selected by error category. Pure function — directly testable. */
internal data class LoadMoreFooterCopy(
    val label: String,
    val retryText: String,
    val icon: ImageVector,
)

/**
 * Maps a paging failure to footer copy via its `ErrorCategory`, so an offline page-load reads
 * "No internet" rather than a server message.
 *
 * A pure function for testability: the mapping is asserted without composing the footer.
 */
internal fun loadMoreFooterCopy(error: Throwable): LoadMoreFooterCopy =
    when (categorize(error)) {
        ErrorCategory.Network,
        ErrorCategory.Timeout.Connect,
        ErrorCategory.Timeout.Read,
        -> LoadMoreFooterCopy(
            label = "No internet",
            retryText = "Tap to retry",
            icon = Icons.Default.CloudOff,
        )
        ErrorCategory.Auth -> LoadMoreFooterCopy(
            label = "Session expired",
            retryText = "Sign in again",
            icon = Icons.Default.Warning,
        )
        is ErrorCategory.Server -> LoadMoreFooterCopy(
            label = "Server unavailable",
            retryText = "Tap to retry",
            icon = Icons.Default.Warning,
        )
        ErrorCategory.RateLimit -> LoadMoreFooterCopy(
            label = "Too many requests",
            retryText = "Tap to retry",
            icon = Icons.Default.Warning,
        )
        ErrorCategory.QuotaExceeded -> LoadMoreFooterCopy(
            label = "Quota exceeded",
            retryText = "Tap to retry",
            icon = Icons.Default.Warning,
        )
        is ErrorCategory.ClientError,
        ErrorCategory.Generic,
        -> LoadMoreFooterCopy(
            label = "Failed to load more",
            retryText = "Tap to retry",
            icon = Icons.Default.Warning,
        )
    }
