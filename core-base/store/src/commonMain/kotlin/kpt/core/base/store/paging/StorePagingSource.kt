/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.paging

import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kpt.core.base.store.screen.DataOrigin
import kpt.core.base.store.screen.toDataOrigin
import kpt.core.base.store.screen.toThrowable
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Key for paginated Store requests.
 *
 * Use this as the Store key type when the data source supports pagination.
 * The Store will cache each page independently.
 *
 * @param page Zero-based page index.
 * @param pageSize Number of items per page.
 * @param query Optional search/filter query.
 */
data class PageKey(
    val page: Int,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val query: String? = null,
) {
    /** Shared paging defaults. */
    companion object {
        /**
         * Rows fetched per page when a caller does not specify. Matched to a typical screenful so the first page fills
         * the viewport without a second round-trip.
         */
        const val DEFAULT_PAGE_SIZE = 20

        /** First page with default size. */
        fun first(pageSize: Int = DEFAULT_PAGE_SIZE, query: String? = null): PageKey =
            PageKey(page = 0, pageSize = pageSize, query = query)
    }

    /** Next page key. */
    fun next(): PageKey = copy(page = page + 1)

    /** Offset for SQL LIMIT/OFFSET queries. */
    val offset: Int get() = page * pageSize
}

/**
 * Triggers a Store refresh for a specific page and suspends until data arrives.
 *
 * Use this inside a PagingSource.load() implementation to let Store handle
 * caching while Paging handles the pagination UX.
 *
 * Usage in core/domain (where paging transformation lives):
 * ```
 * class ClientPagingSource(
 *     private val store: Store<PageKey, List<Client>>,
 * ) : PagingSource<Int, Client>() {
 *
 *     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Client> {
 *         val page = params.key ?: 0
 *         val pageKey = PageKey(page = page, pageSize = params.loadSize)
 *         return store.loadPage(pageKey)
 *     }
 * }
 * ```
 *
 * **Important:** Store's `stream()` returns an infinite Flow (never completes).
 * This function uses `filterNot` + `first` to get the first Data or Error emission
 * and then cancel the flow. Do NOT use `collect { return@collect }` -- `return@collect`
 * only returns from the lambda, it does not cancel the flow.
 *
 * @param key The page key to load.
 * @param refresh When true, always hit the network even if the page is cached
 *   (use for pull-to-refresh). When false (default), serve from cache if available
 *   and only hit the network for uncached pages — the right behavior for
 *   infinite-scroll load-more.
 * @return Success with items and pagination keys, or Error.
 */
suspend fun <Value : Any> Store<PageKey, List<Value>>.loadPage(
    key: PageKey,
    refresh: Boolean = false,
): StorePageResult<Value> {
    // refresh=true (pull-to-refresh, explicit retry): use fresh(key) so we WAIT for
    // the fetcher to return. cached(refresh=true) would emit the SoT value FIRST,
    // .first() would short-circuit there, and the network call would never be observed —
    // which would (a) make pull-to-refresh hide instantly, and (b) prevent
    // lastFetchedAt from ever updating.
    //
    // refresh=false (cache-first load-more / initial page): cached(refresh=false) serves
    // SoT instantly when available; falls through to the fetcher only when SoT is empty.
    val request = if (refresh) {
        StoreReadRequest.fresh(key)
    } else {
        StoreReadRequest.cached(key, refresh = false)
    }
    val response = stream(request)
        .filterNot {
            it is StoreReadResponse.Loading ||
                it is StoreReadResponse.NoNewData ||
                it is StoreReadResponse.Initial
        }
        .first()

    return when (response) {
        is StoreReadResponse.Data -> {
            val items = response.value
            StorePageResult.Success(
                items = items,
                prevKey = if (key.page > 0) key.page - 1 else null,
                nextKey = if (items.size >= key.pageSize) key.page + 1 else null,
                fromNetwork = response.origin.toDataOrigin() == DataOrigin.NETWORK,
            )
        }

        is StoreReadResponse.Error -> StorePageResult.Error(response.toThrowable())

        else -> StorePageResult.Error(RuntimeException("Unexpected store response: $response"))
    }
}

/**
 * Result of a paginated Store load, matching PagingSource.LoadResult shape.
 *
 * Consumer apps map this to `PagingSource.LoadResult` in their domain layer:
 * ```
 * when (result) {
 *     is StorePageResult.Success -> LoadResult.Page(result.items, result.prevKey, result.nextKey)
 *     is StorePageResult.Error -> LoadResult.Error(result.error)
 * }
 * ```
 */
sealed class StorePageResult<out T> {
    /**
     * @param fromNetwork true when this page came from the network fetcher; false when
     *   it was served from memory cache or SoT (Room). Used by [PagingScreenStream]
     *   to update its `lastFetchedAt` timestamp only on real network successes —
     *   otherwise the freshness banner would say "Updated just now" every time
     *   you re-open a screen that simply re-reads from cache.
     */
    data class Success<T>(
        /** The rows in this page. */
        val items: List<T>,
        /** Cursor for the previous page, or null at the start of the list. */
        val prevKey: Int?,
        /** Cursor for the next page, or null once the list is exhausted — this is what ends infinite scroll. */
        val nextKey: Int?,
        val fromNetwork: Boolean = false,
    ) : StorePageResult<T>()

    /**
     * A page that failed to load. The footer maps [error] to copy via its `ErrorCategory`, so an offline page-load
     * reads as offline rather than as a server fault.
     *
     * @property error the failure that ended this page load. Transient: the page is retried, not skipped.
     */
    data class Error(val error: Throwable) : StorePageResult<Nothing>()
}
