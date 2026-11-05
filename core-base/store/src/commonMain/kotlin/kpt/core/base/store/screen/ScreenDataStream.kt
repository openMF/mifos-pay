/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.networkStatusDebouncedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kpt.core.base.store.freshness.FreshnessBand
import kpt.core.base.store.freshness.FreshnessBands
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.infra.DecisionEngine
import kpt.core.base.store.infra.FetchedAtRepository
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Default reconnect-debounce window for [asScreenStream]. Suppresses transient
 * WiFi↔Cell handoff flicker while still feeling responsive on real reconnects.
 * Tunable per call via the `reconnectDebounceMs` parameter; pass `0L` to disable
 * auto-refresh-on-reconnect entirely.
 *
 * Mirrors `NetworkMonitorContract.DEFAULT_DEBOUNCE_MS` in `core/data/infra/`
 * (core-base intentionally doesn't depend on core/data, so the constant is
 * duplicated; both must move together if tuned).
 */
const val DEFAULT_RECONNECT_DEBOUNCE_MS: Long = 300L

/**
 * Default user-tap refresh debounce window for [ScreenDataStream.refresh] /
 * [ScreenDataStream.retry]. Within this window, rapid duplicate taps are
 * suppressed — protects the network layer from accidental thrashing when a
 * user mashes pull-to-refresh. Tunable per call via the
 * `userRefreshDebounceMs` parameter on `asScreenStream`; pass `0L` to disable.
 */
const val DEFAULT_USER_REFRESH_DEBOUNCE_MS: Long = 1_000L

/**
 * A unified data stream combining Store data + cmp-network-monitor into pre-decided [ScreenState].
 *
 * Eliminates all ViewModel boilerplate:
 * - No manual network observation (auto-refreshes on reconnect)
 * - No manual raw-state → UI state mapping (DecisionEngine handles it)
 * - No manual retry/refresh logic (built-in)
 * - No WiFi↔Cell handoff flicker (debounced at 300ms)
 * - Preserves existing content during pull-to-refresh (lastContent cache)
 * - Detects captive portals (hotel WiFi login pages)
 *
 * Usage:
 * ```
 * class MyViewModel(store: Store<Long, Data>, networkMonitor: NetworkMonitor) : ViewModel() {
 *     private val stream = store.asScreenStream(
 *         key = clientId,
 *         networkMonitor = networkMonitor,
 *         scope = viewModelScope,
 *     )
 *     val uiState = stream.state
 *         .mapContent { data, _ -> transform(data) }
 *         .emptyIfContent { it.items.isEmpty() }
 *         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScreenState.Loading)
 *     fun onRetry() = stream.retry()
 * }
 * ```
 */

/**
 * One-shot carrier for force-fresh intent between [ScreenDataStream]'s refresh entry points and the
 * `storeFlow` built in `asScreenStream`. Deliberately NOT a Flow: merging a second flow into the read
 * pipeline added an async subscription hop, and `refreshTrigger` has replay = 0, so a reconnect
 * `tryEmit` landing inside that window was dropped.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
internal class ForceFreshLatch {
    var pending: Boolean = false

    /** Read-and-reset — the force applies to exactly one read. */
    fun consume(): Boolean = pending.also { pending = false }
}

/**
 * One screen's read surface: a cold [ScreenState] flow plus the refresh/retry controls that drive it.
 *
 * This is what a repository returns and a ViewModel collects — the single seam where loading,
 * empty, error, no-network and content are decided ONCE, so no screen re-derives them. Construct it
 * via `Store.asScreenStream(…)` / `asPagingScreenStream(…)` rather than directly; the constructor is
 * internal because the flow and the trigger must be wired consistently.
 */
class ScreenDataStream<T> internal constructor(
    /**
     * Cold Flow of ScreenState decisions. Consumer should call .stateIn() once.
     * Intentionally cold Flow (not StateFlow) to avoid double-sharing
     * when consumer applies mapContent/combineContent before stateIn.
     */
    val state: Flow<ScreenState<T>>,
    /**
     * Cold sibling Flow of pure-staleness [FreshnessSignal]s — derived from the same
     * underlying [StoreData] snapshot as [state] but decoupled from `NetworkStatus`.
     *
     * ViewModels typically convert this to `StateFlow<FreshnessSignal>` via
     * `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FreshnessSignal.initial())`
     * and pass it into `FreshnessIndicator(signal, onRefresh)` next to the card title.
     *
     * Defaults to an empty Flow for back-compat with test-only constructors that don't
     * supply a freshness stream; production code paths through `asScreenStream` always
     * supply a non-empty Flow.
     */
    val freshness: Flow<FreshnessSignal> = emptyFlow(),
    private val refreshTrigger: MutableSharedFlow<Unit>,
    /**
     * Optional observation channel for [refreshFresh] — a test can subscribe to assert that a FORCED
     * refresh was dispatched, distinctly from a policy-driven one. Kept separate from [refreshTrigger]
     * (rather than widening that flow's element type) so the public test-only factory stays
     * source-compatible. NOT part of the read pipeline: intent reaches `storeFlow` via [forceNextRead]
     * so the collector's subscription topology stays exactly as it was before force-fresh existed —
     * merging a second flow in added an async subscription hop, and `refreshTrigger` has replay = 0,
     * so a reconnect `tryEmit` landing in that window was silently dropped.
     */
    private val forceFreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    private val userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
    private val timeSource: kotlin.time.TimeSource = kotlin.time.TimeSource.Monotonic,
    /**
     * Shared with the `storeFlow` built in `asScreenStream`: set just before a trigger emit, consumed
     * by that flow's `flatMapLatest`. Carries force-fresh intent WITHOUT adding a second flow to the
     * read pipeline, so the collector's subscription topology is unchanged.
     */
    private val forceLatch: ForceFreshLatch = ForceFreshLatch(),
) {
    private var lastRefreshMark: kotlin.time.TimeMark? = null

    /**
     * Trigger a network refresh. Preserves existing content while loading.
     *
     * Rapid duplicate taps within [userRefreshDebounceMs] are suppressed — protects
     * the network layer from accidental thrashing when a user mashes pull-to-refresh.
     * The first tap fires immediately; subsequent taps inside the window are dropped
     * silently. Pass `userRefreshDebounceMs = 0L` on the builder to disable.
     *
     * Pass [forceFresh] `= true` for a USER-INITIATED refresh (pull-to-refresh, retry button): the
     * user asserted intent to burn a request, so the read bypasses the policy's cache-first shape and
     * issues `StoreReadRequest.fresh(...)`. Without it, a refresh under the [FetchPolicy.CACHE_FIRST_SWR]
     * default merely re-serves cache while revalidation stays band-gated — so on a `Fresh` band the
     * user's pull-to-refresh does nothing. [FetchPolicy.CACHE_ONLY] ignores the force and stays local.
     *
     * Debouncing applies either way — unlike [refreshFresh], which is the undebounced variant for
     * post-mutation invalidate.
     */
    fun refresh(forceFresh: Boolean = false) {
        if (userRefreshDebounceMs > 0L) {
            val mark = lastRefreshMark
            if (mark != null) {
                val elapsedMs = mark.elapsedNow().inWholeMilliseconds
                if (elapsedMs < userRefreshDebounceMs) {
                    return // suppress duplicate tap
                }
            }
            lastRefreshMark = timeSource.markNow()
        }
        forceLatch.pending = forceFresh
        if (forceFresh) forceFreshTrigger.tryEmit(Unit)
        refreshTrigger.tryEmit(Unit)
    }

    /**
     * Retry loading (semantic alias for refresh — used on error/no-network screens). Forces a fresh
     * fetch: a retry after an error must reach the network, not re-serve the cache that failed.
     */
    fun retry() = refresh(forceFresh = true)

    /**
     * Force a fresh network fetch that bypasses BOTH the read policy's own cache-first shape and
     * the SWR band gate wired around [FetchPolicy.CACHE_FIRST_SWR] — used by pull-to-refresh and
     * post-mutation invalidate paths where the caller has already asserted intent to burn a
     * request. Emits through [forceFreshTrigger], which routes `streamDataForPolicy` to
     * `StoreReadRequest.fresh(fallBackToSourceOfTruth = true)` instead of the policy's normal
     * request. (Before this was wired the trigger carried no intent, so under the
     * `CACHE_FIRST_SWR` default this method re-subscribed to `cached(refresh = false)` and issued
     * no request at all.) Distinct from [refresh] because it does NOT honor the
     * `userRefreshDebounceMs` window; the caller is responsible for its own
     * debouncing. Emits through the same [refreshTrigger] so `lastContent`
     * preservation, the freshness banner, and captive-portal detection all
     * layer in unchanged. Sibling to
     * [kpt.core.base.store.screen.refreshFresh] on `Store<Key, Output>`,
     * which exposes the same S5-5 fresh-fetch primitive at the repository
     * layer.
     */
    fun refreshFresh() {
        forceLatch.pending = true
        forceFreshTrigger.tryEmit(Unit)
        refreshTrigger.tryEmit(Unit)
    }
}

/**
 * Test-only constructor for [ScreenDataStream]. Lets feature-module unit tests
 * stitch a [state] flow + an externally-observable [refreshTrigger] into a real
 * [ScreenDataStream] instance, without granting feature modules access to the
 * primary [internal] constructor.
 *
 * The framework's own tests use [FakeScreenDataStream] (a higher-level fake);
 * cross-module consumers should reach for this when they need bespoke routing
 * (per-key fakes, multi-stream dashboards) that doesn't fit the FakeScreenDataStream
 * single-buffer shape. Returned instance is fully usable production-side — there
 * is no internal "test mode" flag.
 *
 * Marked `ExperimentalScreenDataStreamTestingApi` so feature modules opt-in
 * explicitly, signalling test-only intent in the call site.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is for unit tests of ScreenDataStream consumers only. " +
        "Do not call from production code.",
)
/**
 * Opt-in marker for the testing-only [ScreenDataStream] constructors.
 *
 * Those entry points let a test supply an arbitrary state flow, which bypasses the Store that
 * normally decides state — useful in a unit test, wrong in production, where it would silently
 * detach a screen from its cache and freshness rules. Warning rather than error so a test can opt in
 * locally without a module-wide flag.
 */
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalScreenDataStreamTestingApi

/**
 * Construct a [ScreenDataStream] from an arbitrary [state] flow and a caller-owned
 * [refreshTrigger]. The trigger is what [ScreenDataStream.refresh] / .retry() will
 * emit to — tests can subscribe to it to assert refresh dispatch behavior without
 * touching the framework's internals.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@ExperimentalScreenDataStreamTestingApi
fun <T> screenDataStreamForTesting(
    state: Flow<ScreenState<T>>,
    refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    forceFreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    // tests don't want debounce by default
    userRefreshDebounceMs: Long = 0L,
    timeSource: kotlin.time.TimeSource = kotlin.time.TimeSource.Monotonic,
    freshness: Flow<FreshnessSignal> = emptyFlow(),
): ScreenDataStream<T> = ScreenDataStream(
    state = state,
    freshness = freshness,
    refreshTrigger = refreshTrigger,
    forceFreshTrigger = forceFreshTrigger,
    userRefreshDebounceMs = userRefreshDebounceMs,
    timeSource = timeSource,
)

/**
 * Creates a [ScreenDataStream] from this Store, fusing network state via cmp-network-monitor.
 *
 * Features:
 * - Auto-refreshes when network reconnects (offline→online, debounced 300ms by default)
 * - Preserves last known content during refresh (no flicker to Loading)
 * - DecisionEngine maps all StoreData + NetworkStatus combinations to ScreenState
 * - Handles captive portal detection
 * - Single refresh/retry entry point with 1s user-tap debounce
 *
 * **Concurrent-subscriber behavior:** N subscribers to the same Store key with the
 * same FetchPolicy each get their own [ScreenDataStream] instance with its own
 * refresh trigger. Whether the underlying network fetch is shared depends on the
 * Store's configuration: a Store backed by a [org.mobilenativefoundation.store.store5.SourceOfTruth]
 * (e.g. Room) OR an explicit `cachePolicy(MemoryPolicy)` will dedupe concurrent
 * reads through the cache layer. A plain `StoreBuilder.from(fetcher)` (no SoT,
 * no cachePolicy) fires the fetcher once per subscriber. Configure each app
 * Store deliberately based on the dedup requirement.
 *
 * @param key Store key to stream data for.
 * @param networkMonitor cmp-network-monitor's NetworkMonitor (injected via Koin).
 * @param scope CoroutineScope (typically viewModelScope) for auto-refresh coroutine.
 * @param isEmpty Optional predicate for "no data has arrived yet from Store".
 *   NOT for "empty list" detection — use [emptyIfContent] for that.
 * @param reconnectDebounceMs Window suppressing transient WiFi↔Cell flicker on
 *   reconnect refresh; pass `0L` to disable auto-refresh-on-reconnect entirely.
 *   Defaults to 300ms (sensible for most apps). Forks with slow networks may
 *   pass 1-2s; high-frequency dashboards may pass 100ms.
 * @param userRefreshDebounceMs Window suppressing rapid duplicate user-tap refresh
 *   requests; pass `0L` to disable. Defaults to 1s. Protects against pull-to-refresh
 *   spam.
 */
@Suppress("CyclomaticComplexMethod", "LongParameterList")
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    key: Key,
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKey: String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    // Phase-5 T10 FLIP (AC-21c) — app-wide default is now CACHE_FIRST_SWR.
    // Previous value `NETWORK_WITH_CACHE` retired; call sites that need
    // network-first pin `FetchPolicy.NETWORK_ONLY` explicitly (see e.g.
    // `feature/currency-rates/.../CurrencyRatesViewModel.kt` — the only
    // production site that pins NETWORK_ONLY today, unaffected by this flip).
    fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
    reconnectDebounceMs: Long = DEFAULT_RECONNECT_DEBOUNCE_MS,
    userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
    ttl: Duration = 24.hours,
    // The SWR band gate's time source. Defaulted, so no production call site changes.
    //
    // It exists because the band is computed from the DRIFT between two real-clock reads: the mapper
    // stamps `lastFetchInstant = Clock.System.now()` on the first cache emission, and this gate then
    // reads `now()` again. With a small `ttl` the whole outcome hinges on whether the clock happened
    // to tick in between — `age == 0` lands on `age <= ttl` -> Fresh and no revalidation fires. That
    // made `CacheFirstSwrTest.staleBandTriggersBackgroundSwap` pass on desktop/iOS/JS by luck and
    // hang on wasm/node, whose clock does not tick in that window. A test can now pin the instant
    // instead of hoping for drift.
    clock: Clock = Clock.System,
): ScreenDataStream<Output> {
    // Single debounced NetworkStatus StateFlow shared by reconnect trigger, screen state,
    // and freshness combine. When reconnectDebounceMs > 0, transient WiFi↔Cell handoffs
    // (< 300ms Unavailable) are suppressed — no NoNetwork flash during handoff.
    // When 0, raw networkStatus is used directly (caller opted out of debounce).
    val networkStatusFlow: StateFlow<NetworkStatus> = if (reconnectDebounceMs > 0L) {
        networkMonitor.networkStatusDebouncedState(scope, reconnectDebounceMs)
    } else {
        networkMonitor.networkStatus
    }

    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceFreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLatch = ForceFreshLatch()

    // Auto-refresh when network reconnects. Uses the same debounced status flow so a brief
    // handoff that doesn't reach Unavailable→Available never triggers a spurious fetch.
    // Skipped for CACHE_ONLY — no network requests are ever made.
    // Skipped when reconnectDebounceMs <= 0 — consumer opted out of auto-refresh.
    if (fetchPolicy != FetchPolicy.CACHE_ONLY && reconnectDebounceMs > 0L) {
        scope.launch {
            networkStatusFlow
                .map { it is NetworkStatus.Available }
                .distinctUntilChanged()
                .filter { it }
                .drop(1) // Skip initial seed — don't double-load on start
                .collect { refreshTrigger.tryEmit(Unit) }
        }
    }

    // Periodic background refresh — FetchPolicy.PERIODIC only.
    // The ticker fires through the same refreshTrigger as user-tap / reconnect refresh,
    // so lastContent preservation + debounce + freshness banner all work uniformly.
    // foregroundOnly: parsed but currently informational; the foreground gate is
    // a follow-up phase (needs LocalAppForegroundFlow threading through core-base/ui).
    if (fetchPolicy is FetchPolicy.PERIODIC) {
        scope.launch {
            while (isActive) {
                delay(fetchPolicy.intervalMillis)
                refreshTrigger.tryEmit(Unit)
            }
        }
    }

    // Seed persisted timestamp so warm-reopen shows real "Updated 5m ago".
    // Held in a holder so the map step below can read the latest value.
    var persistedFetchedAt: kotlin.time.Instant? = null
    scope.launch { persistedFetchedAt = fetchedAtRepository.read(cacheKey) }

    // Track last known content to preserve during refresh
    var lastContent: StoreData<Output>? = null

    // Policy-driven refreshes (user tap, reconnect, PERIODIC ticker) carry `false`; refreshFresh()
    // carries `true`. Merging keeps ONE ordered trigger stream while leaving refreshTrigger's public
    // `MutableSharedFlow<Unit>` shape untouched.
    val storeFlow: Flow<StoreData<Output>> = refreshTrigger
        .onStart { emit(Unit) } // Initial load on subscription
        .flatMapLatest {
            streamDataForPolicy(
                key = key,
                policy = fetchPolicy,
                isEmpty = isEmpty,
                forceFresh = forceLatch.consume(),
            )
        }
        .map { storeData ->
            // Persistence: when Store5 hands us NETWORK-origin data with a fresh
            // timestamp, write it through. When we get CACHE/empty with null
            // timestamp, fall back to the persisted value so the staleness banner
            // can show the real age across ViewModel destruction.
            val enriched = when {
                storeData.origin == DataOrigin.NETWORK && storeData.fetchedAtInstant != null -> {
                    fetchedAtRepository.write(cacheKey, storeData.fetchedAtInstant)
                    storeData
                }
                storeData.fetchedAtInstant == null && persistedFetchedAt != null ->
                    storeData.copy(fetchedAtInstant = persistedFetchedAt)
                else -> storeData
            }
            // Offline-local (CACHE_ONLY) has NO network fetcher, so there is no
            // "refresh in flight" during which to preserve the previous content — every
            // SoT emission is authoritative. An empty read is a genuine deletion and MUST
            // surface as empty (so DecisionEngine renders Empty), not a stale-content flash.
            if (fetchPolicy == FetchPolicy.CACHE_ONLY) {
                lastContent = enriched
                return@map enriched
            }
            val cached = lastContent
            if (!enriched.isEmpty) {
                lastContent = enriched
                enriched
            } else if (enriched.isEmpty && cached != null && enriched.error == null) {
                // Refresh in progress — preserve last content with isRefreshing=true.
                cached.copy(isRefreshing = true)
            } else if (enriched.isEmpty && cached != null && enriched.error != null) {
                // Empty + error — preserve last content, attach error so
                // DecisionEngine routes to Content (has data + error → STALE) instead of
                // NoNetwork, and the sibling freshness Flow emits VeryStale + lastError.
                cached.copy(isRefreshing = false, error = enriched.error)
            } else {
                enriched
            }
        }

    // CACHE_FIRST_SWR: on each cache emission, consult FreshnessBands and fire a
    // background revalidation iff band ∈ {Stale, VeryStale}. Fresh/Initial are
    // pass-through — the cache stays authoritative, no reload spinner.
    //
    // The revalidation is a SIDE-FETCH — an independent Store5 subscription to
    // `stream(StoreReadRequest.fresh(key, fallBackToSourceOfTruth = true))`.
    // That path drives the fetcher AND writes the SourceOfTruth; the base
    // storeFlow (built on `cached(key, refresh = false)`, see
    // `streamDataForPolicy`) continuously observes the SoT and re-emits the
    // swapped-in fresh value automatically. See `SourceOfTruthReEmissionSwrTest` for the
    // invariant this fix relies on: a SoT write DOES re-fan-out through an
    // already-open `cached(refresh = false)` subscription.
    //
    // We intentionally do NOT re-use `refreshTrigger` for this — that trigger
    // re-runs `flatMapLatest { streamDataForPolicy(CACHE_FIRST_SWR) }` which is
    // still `cached(refresh = false)` (no network). That was the Phase 1 bug:
    // the band gate emitted but no fetcher ever ran. The trigger is retained for
    // `refresh()` / `retry()` / periodic ticker / reconnect refresh — all of
    // which are legitimate re-subscription events; SWR revalidation is not.
    //
    // Anti-thrash: track the previously-observed band; only fire on the edge
    // (non-stale → stale). Without this guard, the SoT re-emit that follows the
    // side-fetch keeps `storeData.fetchedAtInstant` pinned at the mapper's
    // initial cache-load stamp (Store5 SoT emissions carry
    // `origin = SourceOfTruth`, not NETWORK, so `mapToStoreDataNoFallback`
    // does not re-stamp fetchedAt) → band remains Stale → without the edge
    // detector, every re-emit would fire another fetch, thrashing the network.
    if (fetchPolicy is FetchPolicy.CACHE_FIRST_SWR) {
        scope.launch {
            var lastBand: FreshnessBand? = null
            storeFlow.collect { storeData ->
                val band = FreshnessBands.bandFor(
                    now = clock.now(),
                    lastSyncedAt = storeData.fetchedAtInstant,
                    ttl = ttl,
                    lastError = storeData.error,
                )
                val isStale = band == FreshnessBand.Stale || band == FreshnessBand.VeryStale
                val wasStale = lastBand == FreshnessBand.Stale || lastBand == FreshnessBand.VeryStale
                // Gate the SWR revalidation on ONLINE: never fire a doomed background fetch
                // while offline (or on a falsely-'Available' no-plan / captive-portal link). This
                // side-fetch is already decoupled from rendering, so offline it only wastes a
                // guaranteed-to-fail network attempt (and can leave a spurious error stamp). The
                // reconnect trigger re-emits storeFlow, so the gate re-fires once the network is back.
                val isOnline = networkStatusFlow.value is NetworkStatus.Available
                if (isOnline && isStale && !wasStale) {
                    scope.launch {
                        try {
                            this@asScreenStream
                                .stream(
                                    StoreReadRequest.fresh(
                                        key = key,
                                        fallBackToSourceOfTruth = true,
                                    ),
                                )
                                .mapToStoreDataNoFallback(isEmpty)
                                .collect { fresh ->
                                    // Best-effort propagate the NETWORK stamp to
                                    // the persistence layer so warm-reopen +
                                    // future FreshnessBands checks see the fresh
                                    // fetch time. The base flow will re-emit
                                    // with the swapped-in value from the SoT
                                    // write regardless.
                                    if (fresh.origin == DataOrigin.NETWORK &&
                                        fresh.fetchedAtInstant != null
                                    ) {
                                        fetchedAtRepository.write(cacheKey, fresh.fetchedAtInstant)
                                        persistedFetchedAt = fresh.fetchedAtInstant
                                    }
                                }
                        } catch (t: Throwable) {
                            // SWR revalidation failure MUST NOT crash the
                            // consumer's screen state pipeline. The base flow
                            // continues serving the stale cache; on the next
                            // reconnect / user-tap refresh, the error surfaces
                            // via the standard mapping path.
                        }
                    }
                }
                lastBand = band
            }
        }
    }

    // Combine with FULL debounced NetworkStatus for captive portal detection.
    // .onStart {} immediately emits NoNetwork when offline so that newly-created ViewModels
    // (e.g., detail pages navigated to while offline) show NoNetwork instead of Loading.
    // Without this, combine() cannot fire until storeFlow emits its first value (Store5
    // round-trip), leaving the stateIn initialValue = Loading visible for a brief flash.
    val screenStateFlow: Flow<ScreenState<Output>> = combine(
        storeFlow,
        networkStatusFlow,
    ) { storeData, status ->
        DecisionEngine.decide(storeData, status, fetchPolicy)
    }.onStart {
        val current = networkStatusFlow.value
        // Pre-emit NoNetwork so an offline screen shows it instead of a Loading flash — EXCEPT for the
        // offline-first [FetchPolicy.CACHE_FIRST_SWR], which lets the DecisionEngine decide from the
        // first storeFlow emission (Content for cached data, Empty for genuinely-no-data) so it never
        // flashes the blocking NoNetwork state offline. Matches the DecisionEngine offline-empty rule.
        if (current !is NetworkStatus.Available && fetchPolicy != FetchPolicy.CACHE_FIRST_SWR) {
            emit(ScreenState.NoNetwork(isCaptivePortal = current is NetworkStatus.CaptivePortal))
        }
    }

    // Sibling Flow of pure-staleness signals — derived from the same storeFlow snapshot
    // but decoupled from NetworkStatus. Uses the same debounced flow for consistency.
    val freshnessFlow: Flow<FreshnessSignal> = combine(
        storeFlow,
        networkStatusFlow,
    ) { storeData, status ->
        DecisionEngine.decideFreshness(storeData = storeData, networkStatus = status, ttl = ttl)
    }.distinctUntilChanged()

    return ScreenDataStream(
        state = screenStateFlow,
        freshness = freshnessFlow,
        refreshTrigger = refreshTrigger,
        forceFreshTrigger = forceFreshTrigger,
        forceLatch = forceLatch,
        userRefreshDebounceMs = userRefreshDebounceMs,
    )
}

/**
 * Overload accepting a Flow<Key> for dynamic keys (e.g., selected client from DataStore).
 * Re-streams from Store when key changes. Resets lastContent on key change.
 *
 * @param cacheKeyFor Computes the persistence key for each Key emission. Lets the
 *   `framework_fetched_at` table store one timestamp per key — e.g. one per
 *   selected client — instead of overwriting on every key change.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
@Suppress("CyclomaticComplexMethod", "LongParameterList")
fun <Key : Any, Output : Any> Store<Key, Output>.asScreenStream(
    keyFlow: Flow<Key>,
    networkMonitor: NetworkMonitor,
    fetchedAtRepository: FetchedAtRepository,
    cacheKeyFor: (Key) -> String,
    scope: CoroutineScope,
    isEmpty: (Output) -> Boolean = { false },
    // Phase-5 T10 FLIP (AC-21c) — matches the single-key overload above.
    fetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
    reconnectDebounceMs: Long = DEFAULT_RECONNECT_DEBOUNCE_MS,
    userRefreshDebounceMs: Long = DEFAULT_USER_REFRESH_DEBOUNCE_MS,
    ttl: Duration = 24.hours,
    // The SWR band gate's time source. Defaulted, so no production call site changes.
    //
    // It exists because the band is computed from the DRIFT between two real-clock reads: the mapper
    // stamps `lastFetchInstant = Clock.System.now()` on the first cache emission, and this gate then
    // reads `now()` again. With a small `ttl` the whole outcome hinges on whether the clock happened
    // to tick in between — `age == 0` lands on `age <= ttl` -> Fresh and no revalidation fires. That
    // made `CacheFirstSwrTest.staleBandTriggersBackgroundSwap` pass on desktop/iOS/JS by luck and
    // hang on wasm/node, whose clock does not tick in that window. A test can now pin the instant
    // instead of hoping for drift.
    clock: Clock = Clock.System,
): ScreenDataStream<Output> {
    val networkStatusFlow: StateFlow<NetworkStatus> = if (reconnectDebounceMs > 0L) {
        networkMonitor.networkStatusDebouncedState(scope, reconnectDebounceMs)
    } else {
        networkMonitor.networkStatus
    }

    val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceFreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLatch = ForceFreshLatch()

    if (fetchPolicy != FetchPolicy.CACHE_ONLY && reconnectDebounceMs > 0L) {
        scope.launch {
            networkStatusFlow
                .map { it is NetworkStatus.Available }
                .distinctUntilChanged()
                .filter { it }
                .drop(1)
                .collect { refreshTrigger.tryEmit(Unit) }
        }
    }

    // Periodic background refresh — see single-key overload for rationale.
    if (fetchPolicy is FetchPolicy.PERIODIC) {
        scope.launch {
            while (isActive) {
                delay(fetchPolicy.intervalMillis)
                refreshTrigger.tryEmit(Unit)
            }
        }
    }

    var lastContent: StoreData<Output>? = null
    var currentCacheKey: String? = null
    var persistedFetchedAt: kotlin.time.Instant? = null
    // Track the most-recently observed dynamic key so the CACHE_FIRST_SWR band
    // gate below can target its side-fetch at the right key when the key flow
    // emits mid-stream.
    var lastObservedKey: Key? = null

    val storeFlow: Flow<StoreData<Output>> = combine(
        keyFlow,
        refreshTrigger.onStart { emit(Unit) },
    ) { key, _ -> key }
        .flatMapLatest { key ->
            // NOTE: do NOT null `lastContent` on key change — carry previous content forward
            // as a stale fallback so input-type stores (Rate History period/currency picker)
            // keep showing data while the new key's fetch is in-flight or fails. The
            // sibling `freshness` Flow surfaces a VeryStale band with `lastError` populated
            // when carry-forward is active, letting UI render an inline `RefreshStateChip`
            // ("No network · Showing previous data ↺") rather than a full-screen NoNetwork.
            // `lastContent` is updated to the new key's data once a successful fetch arrives.
            currentCacheKey = cacheKeyFor(key)
            lastObservedKey = key
            // Re-seed persistedFetchedAt for the new key.
            persistedFetchedAt = fetchedAtRepository.read(currentCacheKey)
            streamDataForPolicy(
                key = key,
                policy = fetchPolicy,
                isEmpty = isEmpty,
                forceFresh = forceLatch.consume(),
            )
        }
        .map { storeData ->
            val key = currentCacheKey ?: return@map storeData
            val enriched = when {
                storeData.origin == DataOrigin.NETWORK && storeData.fetchedAtInstant != null -> {
                    fetchedAtRepository.write(key, storeData.fetchedAtInstant)
                    storeData
                }
                storeData.fetchedAtInstant == null && persistedFetchedAt != null ->
                    storeData.copy(fetchedAtInstant = persistedFetchedAt)
                else -> storeData
            }
            // Offline-local (CACHE_ONLY): no network refresh to carry content across — every
            // SoT emission is authoritative, so an empty read surfaces as empty (→ Empty),
            // never a stale-content carry-forward. See the single-key overload.
            if (fetchPolicy == FetchPolicy.CACHE_ONLY) {
                lastContent = enriched
                return@map enriched
            }
            val carry = lastContent
            if (!enriched.isEmpty) {
                lastContent = enriched
                enriched
            } else if (carry != null && enriched.error == null) {
                // Empty mid-refresh — preserve last content with isRefreshing=true.
                carry.copy(isRefreshing = true)
            } else if (carry != null && enriched.error != null) {
                // Empty + error mid-key-change — preserve last content, attach error so
                // DecisionEngine routes to Content (has data + error → STALE) instead of
                // NoNetwork, and the sibling freshness Flow emits VeryStale + lastError.
                carry.copy(isRefreshing = false, error = enriched.error)
            } else {
                enriched
            }
        }

    // CACHE_FIRST_SWR band-gated background revalidation — see single-key
    // overload for full rationale. Difference from single-key: `key` is dynamic
    // (last value from keyFlow) so the side-fetch closes over the currently
    // observed cache key via the shared `currentCacheKey`/`lastObservedKey`
    // holders — set inside the flatMapLatest block above. If `keyFlow` swaps
    // mid-fetch, the fresh() call still targets the key that was stale at fire
    // time; flatMapLatest will cancel the base-flow branch for the old key on
    // the next keyFlow emission, and a new stale-band check will fire fresh()
    // for the new key on its own edge.
    if (fetchPolicy is FetchPolicy.CACHE_FIRST_SWR) {
        scope.launch {
            var lastBand: FreshnessBand? = null
            storeFlow.collect { storeData ->
                val band = FreshnessBands.bandFor(
                    now = clock.now(),
                    lastSyncedAt = storeData.fetchedAtInstant,
                    ttl = ttl,
                    lastError = storeData.error,
                )
                val isStale = band == FreshnessBand.Stale || band == FreshnessBand.VeryStale
                val wasStale = lastBand == FreshnessBand.Stale || lastBand == FreshnessBand.VeryStale
                // Gate the SWR revalidation on ONLINE: never fire a doomed background fetch
                // while offline (or on a falsely-'Available' no-plan / captive-portal link). This
                // side-fetch is already decoupled from rendering, so offline it only wastes a
                // guaranteed-to-fail network attempt (and can leave a spurious error stamp). The
                // reconnect trigger re-emits storeFlow, so the gate re-fires once the network is back.
                val isOnline = networkStatusFlow.value is NetworkStatus.Available
                if (isOnline && isStale && !wasStale) {
                    val revalidateKey = lastObservedKey
                    val revalidateCacheKey = currentCacheKey
                    if (revalidateKey != null && revalidateCacheKey != null) {
                        scope.launch {
                            try {
                                this@asScreenStream
                                    .stream(
                                        StoreReadRequest.fresh(
                                            key = revalidateKey,
                                            fallBackToSourceOfTruth = true,
                                        ),
                                    )
                                    .mapToStoreDataNoFallback(isEmpty)
                                    .collect { fresh ->
                                        if (fresh.origin == DataOrigin.NETWORK &&
                                            fresh.fetchedAtInstant != null
                                        ) {
                                            fetchedAtRepository.write(
                                                revalidateCacheKey,
                                                fresh.fetchedAtInstant,
                                            )
                                            persistedFetchedAt = fresh.fetchedAtInstant
                                        }
                                    }
                            } catch (t: Throwable) {
                                // Silent: SWR revalidation failures never crash
                                // the consumer's screen state pipeline.
                            }
                        }
                    }
                }
                lastBand = band
            }
        }
    }

    val screenStateFlow: Flow<ScreenState<Output>> = combine(
        storeFlow,
        networkStatusFlow,
    ) { storeData, status ->
        DecisionEngine.decide(storeData, status, fetchPolicy)
    }.onStart {
        val current = networkStatusFlow.value
        // Pre-emit NoNetwork so an offline screen shows it instead of a Loading flash — EXCEPT for the
        // offline-first [FetchPolicy.CACHE_FIRST_SWR], which lets the DecisionEngine decide from the
        // first storeFlow emission (Content for cached data, Empty for genuinely-no-data) so it never
        // flashes the blocking NoNetwork state offline. Matches the DecisionEngine offline-empty rule.
        if (current !is NetworkStatus.Available && fetchPolicy != FetchPolicy.CACHE_FIRST_SWR) {
            emit(ScreenState.NoNetwork(isCaptivePortal = current is NetworkStatus.CaptivePortal))
        }
    }

    // Sibling Flow of pure-staleness signals — see single-key overload for rationale.
    val freshnessFlow: Flow<FreshnessSignal> = combine(
        storeFlow,
        networkStatusFlow,
    ) { storeData, status ->
        DecisionEngine.decideFreshness(storeData = storeData, networkStatus = status, ttl = ttl)
    }.distinctUntilChanged()

    return ScreenDataStream(
        state = screenStateFlow,
        freshness = freshnessFlow,
        refreshTrigger = refreshTrigger,
        forceFreshTrigger = forceFreshTrigger,
        forceLatch = forceLatch,
        userRefreshDebounceMs = userRefreshDebounceMs,
    )
}
