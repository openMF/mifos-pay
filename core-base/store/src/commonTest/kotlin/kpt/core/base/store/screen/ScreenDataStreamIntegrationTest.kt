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

import app.cash.turbine.test
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.fixtures.FakeNetworkMonitor
import kpt.core.base.store.infra.FakeFetchedAtRepository
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * End-to-end integration tests for [asScreenStream].
 *
 * Uses real in-memory Store5 + [FakeNetworkMonitor] + [FakeFetchedAtRepository] to verify
 * that the full pipeline (Fetcher → DecisionEngine → ScreenState) produces the correct
 * state transitions without mocking any framework internals.
 */
class ScreenDataStreamIntegrationTest {

    private val onlineInfo = NetworkInfo(type = NetworkType.WiFi, isMetered = false)

    // Real-time budget for every Turbine block below. These tests drive a REAL in-memory Store5
    // whose internal coroutines are not fully virtual-time-bound, so a heavily-loaded CI runner can
    // exceed Turbine's 3s default while the logic under test is perfectly correct — the
    // 2026-09-02 `offline_with_empty_store_emits_Empty_offlineFirst` failure on a runner executing
    // 3054 Gradle tasks, green on four prior runs of the identical code. Widening the wait budget
    // removes the scheduling flake WITHOUT relaxing a single assertion.
    //
    // Raised again 2026-09-05: 30s was still not enough under the Kover job, where coverage
    // INSTRUMENTATION multiplies the cost of every Store5 coroutine hop
    // (`reconnect_triggers_refresh_after_offline`, 3119 tasks). The tell was that the identical
    // commit passed `desktopTest` on all three Desktop runners and failed only under Kover.
    //
    // This budget exists to bound a genuine HANG, not to assert performance: a healthy run
    // satisfies it in milliseconds, so a generous ceiling costs nothing on success and still fails
    // loudly — with a real "no more events" diagnostic — if a state never arrives. [runTestTimeout]
    // sits above it so Turbine's diagnostic wins the race against runTest's bare timeout.
    //
    // 2026-09-10 — the budget was NOT the cause, and raising it a third time would not have helped.
    // `offline_with_empty_store_emits_Empty_offlineFirst` failed again under Kover, but the whole
    // `:core-base:store:desktopTest` task ran ~18s end to end: it asserted the WRONG STATE, it did
    // not time out. Root cause was in production code, not scheduling — DecisionEngine's
    // offline-first rule required `error == null`, while Store5's `cached(refresh = false)` DOES
    // invoke the fetcher when nothing is cached, so offline cold start reliably produced a
    // connection failure and the screen showed Empty or a blocking NoNetwork depending on which
    // emission won. Fixed in DecisionEngine.decideNoData and pinned deterministically by
    // DecisionEngineTest ("…NETWORK error + CACHE_FIRST_SWR = Empty — doomed fetch ignored").
    // Leave these budgets alone: they are a hang ceiling, and the next failure here is signal, not
    // a reason to widen them again.
    private val turbineTimeout = 4.minutes

    /** Must exceed [turbineTimeout] so Turbine reports WHICH state never arrived. */
    private val runTestTimeout = 5.minutes

    // ─── T1: online → Content(FRESH) ─────────────────────────────────────────

    @Test
    fun online_emits_Content_FRESH_after_successful_fetch() = runTest(timeout = runTestTimeout) {
        val store = StoreBuilder
            .from<String, String>(fetcher = Fetcher.of { _ -> "hello" })
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))
        val stream = store.asScreenStream(
            key = "k1",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:online-fresh",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // May emit Loading first (depends on Store5 in-memory-only path)
            val first = awaitItem()
            val content = if (first is ScreenState.Content) first else awaitItem()
            assertIs<ScreenState.Content<String>>(content)
            // Phase A of data-freshness-redesign (2026-06-17): Content path no longer
            // encodes STALE on the band — staleness lives in FreshnessSignal.band
            // computed by decideFreshness() sibling. For a live in-memory store with
            // network, either isRefreshing=true (request in-flight) or false (FRESH).
            // Both are valid initial emissions; either way the Content path is healthy.
            // Assert the value actually reaches the screen. The original assertion here was
            // `assertTrue(true, "…signal=$signal")` — it could not fail, and merely interpolated the
            // signal into a message that nothing read. Freshness band / isRefreshing are genuinely
            // timing-dependent against a live in-memory store, which is why they are pinned in
            // DecisionEngineTest against a deterministic clock rather than asserted here.
            assertEquals("hello", content.data, "the fetched value must reach the screen")
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T2: offline, no cache, CACHE_FIRST_SWR default → Empty (offline-first) ──

    @Test
    fun offline_with_empty_store_emits_Empty_offlineFirst() = runTest(timeout = runTestTimeout) {
        // asScreenStream's default policy is CACHE_FIRST_SWR (offline-first): offline with no cached
        // data surfaces the screen's own Empty state, NOT a blocking full-screen NoNetwork
        // (DecisionEngine offline-first-empty rule; asserted deterministically in DecisionEngineTest
        // "…NETWORK error + CACHE_FIRST_SWR = Empty — doomed fetch ignored").
        //
        // The fetcher throws an IOException-NAMED error because that is what an offline fetch really
        // produces, and `categorize()` keys Network off the class-name chain, not the message. The
        // old fixture threw a bare RuntimeException("no network"), which categorizes as Generic, so
        // `isExpectedWhenOffline()` rejected it and DecisionEngine fell through to NoNetwork — the
        // test then depended on whether the empty emission or the doomed fetch won the race: Empty
        // on a fast machine, NoNetwork under Kover's instrumentation. The comment here used to claim
        // "offline CACHE_FIRST_SWR skips the network leg, so the fetcher is never invoked and error
        // stays null", which is precisely the assumption DecisionEngine documents as FALSE —
        // Store5's `cached(key, refresh = false)` DOES invoke the fetcher when nothing is cached.
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { _ -> throw FakeIOException("connection refused") },
            )
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Unavailable)
        val stream = store.asScreenStream(
            key = "k2",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:offline-nodata",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // Settle the debounced NetworkStatus + Store5 round-trip before deciding.
            advanceUntilIdle()
            // Drain until we see a non-Loading state
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) {
                state = awaitItem()
            }
            assertIs<ScreenState.Empty>(state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T3: CACHE_ONLY skips network even when online ────────────────────────

    @Test
    fun cacheOnly_online_store_emits_without_network_call() = runTest(timeout = runTestTimeout) {
        var fetchCallCount = 0
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { _ ->
                    fetchCallCount++
                    "from-network"
                },
            )
            .build()

        val network = FakeNetworkMonitor(NetworkStatus.Available(onlineInfo))

        // Prime the in-memory cache via a normal read first
        store.streamData("k3").test(timeout = turbineTimeout) {
            awaitItem() // consume
            cancelAndIgnoreRemainingEvents()
        }
        val callsAfterPrime = fetchCallCount

        val stream = store.asScreenStream(
            key = "k3",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:cache-only",
            scope = backgroundScope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

        stream.state.test(timeout = turbineTimeout) {
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) {
                state = awaitItem()
            }
            // CACHE_ONLY must not trigger new network fetches
            assertTrue(
                fetchCallCount == callsAfterPrime,
                "CACHE_ONLY must not call the fetcher; fetchCallCount=$fetchCallCount",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── T4: reconnect triggers state refresh ────────────────────────────────

    @Test
    fun reconnect_triggers_refresh_after_offline() = runTest(timeout = runTestTimeout) {
        // The fetcher is connectivity-aware because a real one is: while the device is offline it
        // MUST fail. The old fixture returned "refreshed" unconditionally, so the doomed offline
        // fetch that Store5 issues on a cold cache (`cached(refresh = false)` invokes the fetcher
        // when nothing is cached) SUCCEEDED — and the offline half of this test then asserted Empty
        // against a stream that had legitimately reached Content. Fast machines emitted Empty first
        // and passed; under Kover the fetch landed first and the assertion saw Content. Modelling
        // the fetcher honestly makes both halves deterministic without touching either assertion.
        var reachable = false
        val store = StoreBuilder
            .from<String, String>(
                fetcher = Fetcher.of { _ ->
                    if (!reachable) throw FakeIOException("connection refused")
                    "refreshed"
                },
            )
            .build()
        val network = FakeNetworkMonitor(NetworkStatus.Unavailable)

        val stream = store.asScreenStream(
            key = "k4",
            networkMonitor = network,
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test:reconnect",
            scope = backgroundScope,
        )

        stream.state.test(timeout = turbineTimeout) {
            // Offline-first (CACHE_FIRST_SWR default): offline + empty + no error surfaces Empty, not
            // a blocking NoNetwork (see T2 + DecisionEngineTest). The reconnect below re-runs the
            // decision and fetches, moving the screen to Content.
            var state: ScreenState<String> = awaitItem()
            while (state is ScreenState.Loading) {
                state = awaitItem()
            }
            assertIs<ScreenState.Empty>(state)

            // Simulate reconnect — the fetcher becomes reachable at the same instant the monitor
            // reports Available, so the refresh that the reconnect triggers can actually succeed.
            reachable = true
            network.setStatus(NetworkStatus.Available(onlineInfo))
            advanceUntilIdle()

            // After reconnect + debounce window, a refresh is triggered.
            //
            // Drain until Content, bounded by turbine's `timeout` (30s) — NOT by an iteration
            // count. The previous `repeat(5)` contradicted this comment's own stated intent: how
            // many intermediate states (Loading / Refreshing / freshness re-emissions) precede
            // Content is timing-dependent, so a slower or more contended runner can legitimately
            // emit more than five before the fetch lands and exhaust the budget while the stream
            // is still healthy. That made this a CI-only failure — it passes on a fast dev machine.
            // If Content genuinely never arrives, awaitItem() now fails on the turbine timeout
            // with a real "no more events" diagnostic instead of a bare AssertionError.
            var next: ScreenState<String> = awaitItem()
            while (next !is ScreenState.Content) {
                next = awaitItem()
            }
            assertEquals(
                "refreshed",
                next.data,
                "reconnect must refresh from the network, not resurface stale cache",
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * An offline transport failure. The NAME is what matters: `categorize()` resolves
 * [ErrorCategory.Network] from the class-name chain (it looks for "IOException"), never from the
 * message — so a bare `RuntimeException("no network")` is Generic and does NOT get the offline-first
 * treatment. Declared file-private, matching DecisionEngineTest and PagingScreenStreamDecisionParityTest.
 */
private class FakeIOException(message: String) : Exception(message)
