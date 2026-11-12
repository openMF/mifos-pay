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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.store.error.OfflineException
import kpt.core.base.store.infra.DecisionEngine
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.DataOrigin
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.ScreenStreamContext
import kpt.core.base.store.screen.StoreData
import org.koin.mp.KoinPlatform
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Paginated variant of [kpt.core.base.store.screen.ScreenDataStream].
 * Manages page loading, appending, error surfacing, and unified state for infinite lists.
 *
 * Usage:
 * ```
 * val pagingStream = clientStore.asPagingScreenStream(
 *     networkMonitor = networkMonitor,
 *     scope = viewModelScope,
 *     pageSize = 20,
 * )
 * val uiState = pagingStream.state  // Flow<ScreenState<List<Client>>>
 * fun loadMore() = pagingStream.loadNextPage()
 * ```
 *
 * Scroll and UI position retention is a Compose concern — `rememberLazyListState()`
 * (which uses `rememberSaveable` internally with `LazyListState.Saver`) restores
 * the list position across tab-switch and config-change automatically. This paging
 * class carries no durable cursor of its own.
 */
@OptIn(ExperimentalTime::class)
class PagingScreenStream<T : Any> internal constructor(
    /** The page-aware screen state, already accumulated across every loaded page. */
    val state: Flow<ScreenState<List<T>>>,
    /** Whether another page exists — what the footer keys on to offer 'load more' versus end-of-list. */
    val hasMore: StateFlow<Boolean>,
    /** Whether a page load is in flight, so the footer shows progress without a second state source. */
    val isLoadingMore: StateFlow<Boolean>,
    /** The most recent load-more error, or null. Cleared on next loadNextPage/refresh. */
    val loadMoreError: StateFlow<Throwable?>,
    private val scope: CoroutineScope,
    private val store: Store<PageKey, List<T>>,
    private val pageSize: Int,
    private val query: String?,
    private val items: MutableStateFlow<List<T>>,
    private val hasMoreMutable: MutableStateFlow<Boolean>,
    private val isLoadingMoreMutable: MutableStateFlow<Boolean>,
    private val isInitialLoading: MutableStateFlow<Boolean>,
    private val error: MutableStateFlow<Throwable?>,
    private val currentPage: MutableStateFlow<Int>,
    /** Wall-clock instant of last successful page load. Drives DataFreshnessIndicator timestamp. */
    private val lastFetchedAt: MutableStateFlow<Instant?>,
    private val networkMonitor: NetworkMonitor,
    private val cacheKey: String,
    private val fetchedAtRepository: FetchedAtRepository,
    private val fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
) {
    /** Load the next page. No-op if already loading or no more pages. Cache-first. */
    fun loadNextPage() {
        if (isLoadingMoreMutable.value || !hasMoreMutable.value) return
        scope.launch {
            isLoadingMoreMutable.value = true
            error.value = null
            val nextPage = currentPage.value + 1
            val pageKey = PageKey(page = nextPage, pageSize = pageSize, query = query)
            // refresh=false (cache-first): if this page is in SoT, return it instantly.
            // Pull-to-refresh uses the dedicated refresh() path.
            when (val result = store.loadPage(pageKey, refresh = false)) {
                is StorePageResult.Success -> {
                    items.update { it + result.items }
                    currentPage.value = nextPage
                    hasMoreMutable.value = result.nextKey != null
                    // Only update on actual network success — cache hits keep the previous
                    // timestamp so DataFreshnessIndicator shows the real age of the data,
                    // not the moment we re-read from SoT.
                    if (result.fromNetwork) {
                        val now = Clock.System.now()
                        lastFetchedAt.value = now
                        fetchedAtRepository.write(cacheKey, now)
                    }
                }
                is StorePageResult.Error -> {
                    // Re-type as OfflineException when offline so categorize() routes
                    // through Network category and LoadMoreFooter shows the no-network
                    // treatment instantly — instead of "Failed to load more" after the
                    // fetcher's executeWithRetry burns ~3s on a hopeless retry.
                    error.value = retypeIfOffline(result.error)
                }
            }
            isLoadingMoreMutable.value = false
        }
    }

    /**
     * Refresh from page 0. Forces network for the first page.
     *
     * Keeps existing items in place during the fetch so the UI shows
     * `Content(UPDATING)` (data + refreshing) instead of bouncing through
     * `Loading` (data-less). This keeps the pull-to-refresh spinner visible
     * for the full duration of the fetch and prevents content flicker.
     * Items are replaced with the new page-0 data when the fetch completes.
     */
    fun refresh() {
        scope.launch {
            currentPage.value = 0
            // Don't clear items — preserves the visible list during refresh so
            // DecisionEngine emits Content(UPDATING) and the pull-to-refresh
            // spinner stays on screen until the fetch completes.
            hasMoreMutable.value = true
            error.value = null
            isInitialLoading.value = true
            loadInitialPage(refresh = true)
        }
    }

    /**
     * Retries the failed page load. An alias for refresh, so a footer retry and a pull-to-refresh do the same thing.
     */
    fun retry() = refresh()

    internal fun loadInitialPage(refresh: Boolean = false) {
        scope.launch {
            // CACHE_ONLY: read from local SoT only — never hit the network.
            if (fetchPolicy == FetchPolicy.CACHE_ONLY) {
                val pageKey = PageKey.first(pageSize = pageSize, query = query)
                when (val result = store.loadPage(pageKey, refresh = false)) {
                    is StorePageResult.Success -> {
                        items.value = result.items
                        hasMoreMutable.value = result.nextKey != null
                    }
                    is StorePageResult.Error -> error.value = result.error
                }
                isInitialLoading.value = false
                return@launch
            }
            // Online-first policies always force a fresh fetch:
            //   - NETWORK_ONLY: stale data would be misleading; skip cache.
            // Store5's fresh(fallBackToSourceOfTruth = true) honours this intent at the
            // single-key layer (see streamDataForPolicy); for paging, the same forceRefresh
            // signal is the closest analogue — failure to fetch still surfaces existing
            // items from `items` MutableStateFlow if the previous page load primed them.
            val forceRefresh = refresh ||
                fetchPolicy == FetchPolicy.NETWORK_ONLY
            // Offline pre-check for forced-network: if we're offline,
            // don't even call store.loadPage — `fresh(key)` would dispatch to the
            // fetcher whose `executeWithRetry` may block indefinitely waiting for
            // reconnect. Fail fast with OfflineException so DecisionEngine renders
            // the no-network treatment and the pull-to-refresh spinner can hide.
            if (forceRefresh && !isOnlineNow()) {
                error.value = OfflineException()
                isInitialLoading.value = false
                return@launch
            }
            val pageKey = PageKey.first(pageSize = pageSize, query = query)
            when (val result = store.loadPage(pageKey, refresh = forceRefresh)) {
                is StorePageResult.Success -> {
                    items.value = result.items
                    hasMoreMutable.value = result.nextKey != null
                    // Network-only fetchedAt update — see loadNextPage for rationale.
                    if (result.fromNetwork) {
                        val now = Clock.System.now()
                        lastFetchedAt.value = now
                        fetchedAtRepository.write(cacheKey, now)
                    }
                }
                is StorePageResult.Error -> {
                    // Same offline-retype as loadNextPage — fixes the warm-reopen bug
                    // where the user navigates back to a paged screen after disabling
                    // internet and sees "failed to fetch" briefly while the fetcher
                    // burns retry budget. With this, DecisionEngine sees a Network-
                    // class error and renders NoNetwork immediately.
                    error.value = retypeIfOffline(result.error)
                }
            }
            isInitialLoading.value = false
        }
    }

    /**
     * If the device is offline, replace [original] with an [OfflineException] so the
     * UI stack routes the error through the no-network treatment. If we're online,
     * the original error is preserved untouched (caller still sees the real cause).
     */
    private suspend fun retypeIfOffline(original: Throwable): Throwable =
        if (isOnlineNow()) original else OfflineException()

    /** Sample current network state synchronously from the StateFlow. */
    private suspend fun isOnlineNow(): Boolean =
        networkMonitor.networkStatus.first() is NetworkStatus.Available
}

/**
 * Creates a [PagingScreenStream] with network-fused state via cmp-network-monitor.
 *
 * @param fetchedAtRepository Persists last-network-fetch timestamps so the staleness
 *   banner shows real "Updated 5m ago" across ViewModel destruction and (with a
 *   Room-backed impl) app restart. Required — there is no default. Production apps
 *   wire `RoomFetchedAtRepository` (`core/data`); tests use `FakeFetchedAtRepository`.
 * @param cacheKey Identifies this Store in the [fetchedAtRepository]. Convention:
 *   `"<feature>:<storeName>"` (e.g. `"crypto:coinMarkets"`).
 */
@Suppress("LongParameterList")
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKey: String,
    scope: CoroutineScope,
    pageSize: Int = PageKey.DEFAULT_PAGE_SIZE,
    query: String? = null,
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
): PagingScreenStream<Value> {
    val items = MutableStateFlow<List<Value>>(emptyList())
    val hasMore = MutableStateFlow(true)
    val isLoadingMore = MutableStateFlow(false)
    val isInitialLoading = MutableStateFlow(true)
    val error = MutableStateFlow<Throwable?>(null)
    val currentPage = MutableStateFlow(0)
    val lastFetchedAt = MutableStateFlow<Instant?>(null)
    // Seed lastFetchedAt from persistence so warm-reopen (new ViewModel for the
    // same Store) shows the real age of cached data. Launches in scope so the
    // I/O doesn't block stream construction; UI shows null timestamp briefly.
    scope.launch { lastFetchedAt.value = fetchedAtRepository.read(cacheKey) }

    val screenState: Flow<ScreenState<List<Value>>> = combine(
        items,
        networkMonitor.networkStatus,
        isInitialLoading,
        error,
        lastFetchedAt,
    ) { itemList, status, loading, err, fetchedAt ->
        // Definitive Empty: paging knows the difference between "load hasn't started"
        // (isInitialLoading=true) and "load completed with zero items" (isInitialLoading=false,
        // err=null). DecisionEngine treats these the same (Loading), so we special-case
        // the post-load empty here before delegating.
        if (itemList.isEmpty() && !loading && err == null) {
            return@combine ScreenState.Empty
        }
        // Wrap paging state into a StoreData<List<Value>> and let DecisionEngine handle
        // every other transition (Loading / NoNetwork / CaptivePortal / Error /
        // Content+freshness). Single state machine for both single-key (asScreenStream)
        // and paged (this) flows — eliminates drift between two separate `when` blocks.
        // Note: DecisionEngine never reads .data when isEmpty=true, so emptyList() is a
        // safe sentinel here (avoids the EMPTY_SENTINEL `as List` cast which fails at
        // runtime — outer collection type isn't erased).
        // fetchedAtInstant flows through to DataFreshnessIndicator so paged screens
        // show "Updated Xs ago" timestamps just like single-key screens do.
        val storeData = StoreData(
            data = itemList,
            origin = DataOrigin.NETWORK,
            isRefreshing = loading,
            fetchedAt = null,
            fetchedAtInstant = fetchedAt,
            error = err,
            isEmpty = itemList.isEmpty(),
        )
        DecisionEngine.decide(storeData, status)
    }

    return PagingScreenStream(
        state = screenState,
        hasMore = hasMore.asStateFlow(),
        isLoadingMore = isLoadingMore.asStateFlow(),
        loadMoreError = error.asStateFlow(),
        scope = scope,
        store = this,
        pageSize = pageSize,
        query = query,
        items = items,
        hasMoreMutable = hasMore,
        isLoadingMoreMutable = isLoadingMore,
        isInitialLoading = isInitialLoading,
        error = error,
        currentPage = currentPage,
        lastFetchedAt = lastFetchedAt,
        networkMonitor = networkMonitor,
        cacheKey = cacheKey,
        fetchedAtRepository = fetchedAtRepository,
        fetchPolicy = fetchPolicy,
    ).also { it.loadInitialPage() }
}

/**
 * [asPagingScreenStream] overload taking a bundled [ScreenStreamContext] instead of the two infra
 * deps — so a paginated repository reads `store.asPagingScreenStream(screen, cacheKey, scope, …)`
 * with `screen` its one injected [ScreenStreamContext]. Delegates to the primary overload.
 */
fun <Value : Any> Store<PageKey, List<Value>>.asPagingScreenStream(
    context: ScreenStreamContext = KoinPlatform.getKoin().get(),
    cacheKey: String,
    scope: CoroutineScope,
    pageSize: Int = PageKey.DEFAULT_PAGE_SIZE,
    query: String? = null,
    fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
): PagingScreenStream<Value> = asPagingScreenStream(
    networkMonitor = context.networkMonitor,
    fetchedAtRepository = context.fetchedAtRepository,
    cacheKey = cacheKey,
    scope = scope,
    pageSize = pageSize,
    query = query,
    fetchPolicy = fetchPolicy,
)
