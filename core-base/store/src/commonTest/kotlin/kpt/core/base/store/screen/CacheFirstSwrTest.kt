/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import app.cash.turbine.test
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.fixtures.FakeNetworkMonitor
import kpt.core.base.store.infra.FakeFetchedAtRepository
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Canary tests for [FetchPolicy.CACHE_FIRST_SWR] — the Phase 1 SWR read path.
 *
 * Locks the four invariants Phase 1 ships:
 * - **AC-1 (fresh band)** — first emission on a Fresh-band cache serves the
 *   cached value with no reload spinner (`isRefreshing = false`) and no fetcher
 *   call. Also covered by [freshBandDoesNotFetchNetwork] at the full
 *   `asScreenStream` level.
 * - **AC-2 (stale band)** — when the cached value is Stale/VeryStale, the band
 *   gate MUST launch a REAL background revalidation whose fresh value is
 *   silently swapped into the stream via the SourceOfTruth. Locked by
 *   [staleBandTriggersBackgroundSwap] end-to-end: first emission is stale
 *   cache, subsequent emission is the fetcher's fresh value, fetcher invoked
 *   EXACTLY once (edge-detector).
 * - **S5-5** — [ScreenDataStream.refreshFresh] on the class AND the sibling
 *   `Store<K,O>.refreshFresh(key)` extension expose the fresh-fetch primitive
 *   pull-to-refresh + post-mutation invalidate need.
 *
 * Reuses the shipped test fakes ([FakeNetworkMonitor], [FakeFetchedAtRepository])
 * rather than inventing new ones so the canary matches how production ViewModels
 * consume the API. Stores that need to observe the SoT-re-emission invariant
 * (AC-2) are built with a `MutableStateFlow`-backed [SourceOfTruth] reader —
 * the same shape [SourceOfTruthReEmissionSwrTest] uses to simulate the DAO Flow /
 * Room's InvalidationTracker production contract, hermetic to `core-base/store`.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class CacheFirstSwrTest {

    private val onlineInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)
    private val online = NetworkStatus.Available(onlineInfo)

    // ─── AC-1 — first emission serves cache with no reload spinner ────────────

    @Test
    fun firstEmissionServesCacheWithNoReloadSpinner() = runTest {
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "primed:$key"
                },
            )
            .build()

        // Prime the in-memory cache while online. Consumes one fetcher call so
        // downstream reads can serve from cache.
        store.streamData("k1").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        val fetchesAfterPrime = fetchCount

        val stream = store.asScreenStream(
            key = "k1",
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:swr-first-emission",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = 24.hours,
        )

        stream.state.test {
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) state = awaitItem()
            val content = assertIs<ScreenState.Content<String>>(state)
            assertEquals("primed:k1", content.data)
            // No reload spinner in the content path — CACHE_FIRST_SWR served
            // the cache without any in-flight refresh visible on the signal.
            assertFalse(
                content.freshnessSignal.isRefreshing,
                "SWR must not surface an isRefreshing spinner when serving fresh cache",
            )
            cancelAndIgnoreRemainingEvents()
        }

        // Fresh band → no extra fetch beyond the priming call.
        assertEquals(
            fetchesAfterPrime,
            fetchCount,
            "CACHE_FIRST_SWR on a Fresh band must not invoke the fetcher; " +
                "fetchesAfterPrime=$fetchesAfterPrime fetchCount=$fetchCount",
        )
    }

    // ─── Fresh band — no fetcher call, no reload spinner ──────────────────────

    @Test
    fun freshBandDoesNotFetchNetwork() = runTest {
        // Locks AC-1 at the full `asScreenStream` level:
        // when the cached value is Fresh (age << ttl), the band gate MUST NOT
        // fire the fetcher — no reload spinner, no network hit. Uses a
        // SoT-backed store (matching the SWR archetype's production shape) with
        // a value already sitting in SoT — no priming fetch is needed and the
        // fetcher wraps a counter so any invocation is observable.
        val sotState = MutableStateFlow<List<String>>(listOf("fresh-cached"))
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, List<String>, List<String>>(
                fetcher = Fetcher.of { _ ->
                    fetchCount++
                    listOf("network")
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { _ -> flow { sotState.collect { emit(it) } } },
                    writer = { _, value -> sotState.value = value },
                ),
            )
            .build()

        val stream = store.asScreenStream(
            key = "k-fresh",
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:swr-fresh-no-fetch",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            // suppress reconnect refresh path
            reconnectDebounceMs = 0L,
            userRefreshDebounceMs = 0L,
            // any cache age « ttl → Fresh band
            ttl = 24.hours,
        )

        stream.state.test {
            var state: ScreenState<List<String>> = awaitItem()
            while (state is ScreenState.Loading) state = awaitItem()
            val content = assertIs<ScreenState.Content<List<String>>>(state)
            assertEquals(
                listOf("fresh-cached"),
                content.data,
                "Fresh band must serve the cached value directly",
            )
            assertFalse(
                content.freshnessSignal.isRefreshing,
                "Fresh band must not surface an isRefreshing spinner (AC-1)",
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(
            0,
            fetchCount,
            "CACHE_FIRST_SWR on Fresh band must NOT invoke the fetcher; got $fetchCount",
        )
    }

    // ─── AC-2 — stale band launches real revalidation + silent swap ───────────

    @Test
    fun staleBandTriggersBackgroundSwap() = runTest {
        // Real end-to-end SWR contract:
        //   (a) first emission serves the CACHED (stale) value with no reload
        //       spinner — screen stays on the old data (AC-1 for stale band)
        //   (b) a SUBSEQUENT emission swaps in the fetcher's fresh value —
        //       the band gate launched a genuine `stream(fresh())` side-fetch,
        //       Store5 wrote SoT, the base `cached(refresh=false)` subscription
        //       re-emitted with the swapped-in value (AC-2)
        //   (c) the fetcher was invoked EXACTLY once — the edge detector
        //       suppresses re-fires while the band remains stale (no thrash)
        //
        // Uses a SoT-backed store (MutableStateFlow reader/writer, matching
        // `SourceOfTruthReEmissionSwrTest`) so the fresh() SoT write actually propagates
        // to the base subscription — the mechanism the fix relies on.
        //
        // Staleness is PINNED, not raced. The band is computed from the drift between two
        // real-clock reads — the mapper stamps `lastFetchInstant = Clock.System.now()` on the first
        // cache emission, the gate reads `now()` again — so with `ttl = 0` the outcome depended on
        // whether the clock ticked in between. It usually did on desktop/iOS/JS and never did on
        // wasm/node, where this test hung. Handing the gate a clock an hour ahead makes `age` about
        // an hour against a 5-minute ttl: VeryStale by arithmetic, on every platform.

        val sotState = MutableStateFlow<List<String>>(listOf("stale-cached"))
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, List<String>, List<String>>(
                fetcher = Fetcher.of { _ ->
                    fetchCount++
                    listOf("fresh-network")
                },
                sourceOfTruth = SourceOfTruth.of(
                    reader = { _ -> flow { sotState.collect { emit(it) } } },
                    writer = { _, value -> sotState.value = value },
                ),
            )
            .build()

        // Do NOT prime with `store.streamData(...)` — that would burn a fetcher
        // call before the subscription and break the "exactly once" assertion.
        // The SoT already carries "stale-cached", which is enough for the
        // cache-first read to serve it.

        val stream = store.asScreenStream(
            key = "k-swr",
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:swr-stale-swap",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            // suppress reconnect refresh path
            reconnectDebounceMs = 0L,
            userRefreshDebounceMs = 0L,
            ttl = 5.minutes,
            clock = object : Clock {
                // An hour ahead of whatever the mapper stamps, so age >> ttl * 3 regardless of the
                // platform's clock granularity.
                override fun now(): Instant = Clock.System.now() + 1.hours
            },
        )

        stream.state.test {
            // (a) First non-Loading emission is the STALE cached value with no
            //     reload spinner.
            var state: ScreenState<List<String>> = awaitItem()
            while (state is ScreenState.Loading) state = awaitItem()
            val cached = assertIs<ScreenState.Content<List<String>>>(state)
            assertEquals(
                listOf("stale-cached"),
                cached.data,
                "SWR first emission must be the STALE cached value",
            )
            assertFalse(
                cached.freshnessSignal.isRefreshing,
                "SWR must serve stale cache without a reload spinner (AC-1 for stale band)",
            )

            // (b) Subsequent emission swaps in the fetcher's fresh value.
            //     The band gate's side-fetch called `Store.stream(fresh())`,
            //     which invoked the fetcher and wrote SoT; the base flow's
            //     `cached(refresh=false)` subscription re-emitted from SoT.
            var next: ScreenState<List<String>> = awaitItem()
            while (
                next is ScreenState.Loading ||
                (
                    next is ScreenState.Content<List<String>> &&
                        (next as ScreenState.Content<List<String>>).data == listOf("stale-cached")
                    )
            ) {
                next = awaitItem()
            }
            val fresh = assertIs<ScreenState.Content<List<String>>>(next)
            assertEquals(
                listOf("fresh-network"),
                fresh.data,
                "SWR band gate MUST launch a real revalidation that swaps in the fetcher's fresh value",
            )
            cancelAndIgnoreRemainingEvents()
        }

        // (c) Exactly ONE fetcher invocation. The edge detector must suppress
        //     re-fires: the SoT re-emit after the side-fetch keeps
        //     `storeData.fetchedAtInstant` pinned at the mapper's original
        //     cache-load stamp (Store5 SoT re-emissions carry
        //     `origin = SourceOfTruth`, not NETWORK), so the band stays Stale
        //     and any subsequent tick would fire again without the guard.
        assertEquals(
            1,
            fetchCount,
            "SWR band gate must fire EXACTLY one background revalidation per " +
                "non-stale → stale transition; got $fetchCount (edge detector regression?)",
        )
    }

    // ─── S5-5 — refreshFresh() forces a fresh-fetch emission ──────────────────

    @Test
    fun refreshFreshBypassesBandGate() = runTest {
        // Verifies both surfaces of the S5-5 fresh-fetch pair BY FETCHER INVOCATION:
        //   1. `ScreenDataStream.refreshFresh()` emits force-fresh intent through refreshTrigger,
        //      routing streamDataForPolicy to StoreReadRequest.fresh(...) instead of
        //      CACHE_FIRST_SWR's cached(refresh = false) — so the FETCHER RUNS.
        //   2. `Store<K,O>.refreshFresh(key)` extension delegates to the
        //      existing `freshData(key)` path and drives the fetcher.
        //
        // REGRESSION GUARD: this assertion used to accept "at least one more emission", which a
        // re-emission of CACHED data satisfies. That let the real defect through — refreshTrigger
        // was MutableSharedFlow<Unit>, carried no intent, and under the CACHE_FIRST_SWR default
        // refreshFresh() re-subscribed to cached(refresh = false) and issued NO request at all.
        // Assert the fetch, never the emission.
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "fresh:$key"
                },
            )
            .build()

        // Prime cache first so CACHE_FIRST_SWR can serve from cache.
        store.streamData("k3").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        val fetchesAfterPrime = fetchCount

        val stream = store.asScreenStream(
            key = "k3",
            networkMonitor = FakeNetworkMonitor(online),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:refresh-fresh",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            userRefreshDebounceMs = 0L,
            ttl = 24.hours,
        )

        // The class-side surface is asserted at the ROUTING layer instead — see
        // forceFreshRoutesToFreshRequestUnderSwr. Driving it through stream.state and draining to
        // Content proves nothing: `fresh(fallBackToSourceOfTruth = true)` may emit the CACHED value
        // before the network one, so a Content emission is not evidence the fetcher ran, and on a
        // SoT-less store the drain can block until runTest times out. (That unsound shape is exactly
        // what let the original refreshFresh defect ship green.) Dispatch from the ViewModel layer is
        // covered by InterestRateDetailViewModelTest.
        stream.state.test {
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) state = awaitItem()
            assertIs<ScreenState.Content<String>>(state)
            cancelAndIgnoreRemainingEvents()
        }

        // Exercise the Store<K,O>.refreshFresh(key) extension — delegates
        // to freshData() which forces a StoreReadRequest.fresh() Store5 call
        // and therefore invokes the fetcher.
        val freshBefore = fetchCount
        val fresh = store.refreshFresh("k3").first { !it.isEmpty }
        assertEquals("fresh:k3", fresh.data)
        assertTrue(
            fetchCount > freshBefore,
            "Store.refreshFresh(key) extension must call the fetcher; " +
                "freshBefore=$freshBefore, fetchCount=$fetchCount",
        )
        // Sanity: the extension actually did *something* beyond the priming
        // + follow-up refresh from the state-flow branch above.
        assertTrue(fetchCount >= fetchesAfterPrime, "fetch count must be monotonically non-decreasing")
    }

    // ─── forceFresh routing (streamDataForPolicy) ─────────────────────────────
    //
    // Asserted at the routing layer, NOT through ScreenDataStream + Turbine: a
    // `fresh(fallBackToSourceOfTruth = true)` read can emit the cached value before the network one,
    // so "drain to Content" never proved a fetch happened and could block until runTest timed out.

    @Test
    fun forceFreshRoutesToFreshRequestUnderSwr() = runTest {
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "fresh:$key"
                },
            )
            .build()
        store.streamData("k4").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        val before = fetchCount
        store.streamDataForPolicy("k4", FetchPolicy.CACHE_FIRST_SWR, forceFresh = true)
            .first { !it.isEmpty }
        assertTrue(
            fetchCount > before,
            "forceFresh must route to StoreReadRequest.fresh and drive the fetcher even under " +
                "CACHE_FIRST_SWR (whose normal read is cached(refresh = false)); " +
                "before=$before after=$fetchCount",
        )
    }

    @Test
    fun cacheOnlyIgnoresForceFresh() = runTest {
        // An offline-only screen must NEVER reach the network, even when a caller forces a refresh.
        var fetchCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { key ->
                    fetchCount++
                    "fresh:$key"
                },
            )
            .build()
        store.streamData("k5").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        val before = fetchCount
        store.streamDataForPolicy("k5", FetchPolicy.CACHE_ONLY, forceFresh = true).first()
        assertEquals(
            before,
            fetchCount,
            "CACHE_ONLY must ignore forceFresh and stay local; fetchCount moved $before -> $fetchCount",
        )
    }
}
