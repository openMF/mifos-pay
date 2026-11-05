/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("ktlint:standard:class-naming")
// The policy objects keep their SCREAMING_CASE names: they read as enum constants at every call site
// (`FetchPolicy.CACHE_FIRST_SWR`) and are public API forks already reference, so a rename would break them.

package kpt.core.base.store.screen

/**
 * Controls whether a screen stream reads from cache, hits the network, or both.
 *
 * Pass to [ScreenDataStream.asScreenStream], [LoadOnceStream.asLoadOnceStream], or
 * [PagingScreenStream] to override that entry point's default.
 *
 * **Defaults differ per entry point — there is no single global default:**
 * | Entry point                                             | Default              |
 * |---------------------------------------------------------|----------------------|
 * | `asScreenStream` (all overloads)                        | [CACHE_FIRST_SWR]    |
 * | `asLoadOnceStream`                                      | [NETWORK_WITH_CACHE] |
 * | [PagingScreenStream] / `asPagingScreenStream`           | [NETWORK_WITH_CACHE] |
 * | `StoreFactory.createScreenWithMutation`                 | [NETWORK_WITH_CACHE] |
 *
 * **Choosing a policy:**
 * | Scenario                                                                    | Policy                |
 * |-----------------------------------------------------------------------------|-----------------------|
 * | Offline-first screen — cached value now, revalidate only when stale         | [CACHE_FIRST_SWR]     |
 * | Show cached data immediately, always refresh in background                  | [NETWORK_WITH_CACHE]  |
 * | Always-fresh data required (e.g. payment confirmation)                      | [NETWORK_ONLY]        |
 * | Offline-only view or explicit "load from cache"                             | [CACHE_ONLY]          |
 * | Ambient surface that silently re-fetches on a cadence (FX rates, tickers)  | [PERIODIC]            |
 *
 * Modelled as a sealed interface so the vocabulary is open to additional variants
 * without breaking the source-compatible `FetchPolicy.NETWORK_WITH_CACHE`-style
 * access pattern.
 */
sealed interface FetchPolicy {

    /**
     * Emit cached data immediately (if present), then trigger a background network fetch
     * and emit refreshed data when it arrives.
     *
     * The default for `asLoadOnceStream`, [PagingScreenStream] and
     * `StoreFactory.createScreenWithMutation` — but NOT for `asScreenStream`, which defaults
     * to [CACHE_FIRST_SWR]. The user sees content quickly while the data is silently refreshed
     * in the background. Unlike [CACHE_FIRST_SWR] this refetches on EVERY subscription, not
     * only when the freshness band is stale. The staleness banner from
     * [DataFreshnessIndicator] is shown when the data is older than the configured TTL.
     */
    data object NETWORK_WITH_CACHE : FetchPolicy

    /**
     * Skip the local cache entirely and always fetch from the network.
     *
     * Use for screens where stale data would be misleading or harmful (e.g. payment status,
     * balance after a transaction). If the network fails the stream emits
     * [ScreenState.NoNetwork] or [ScreenState.Error] instead of showing stale data.
     */
    data object NETWORK_ONLY : FetchPolicy

    /**
     * Read only from the local cache; never perform a network request.
     *
     * Use for explicitly offline screens or when the calling code knows connectivity is
     * unavailable and wants to avoid error flicker. If the cache is empty the stream emits
     * [ScreenState.Empty].
     *
     * **DataFreshness behaviour:** the stream emits [kpt.core.base.store.screen.DataFreshness.STALE]
     * when the cached data is older than the TTL. [DataFreshness.UPDATING] is never emitted
     * in CACHE_ONLY mode, so no refreshing banner shows. Feature screens handle stale presentation
     * inside their `content { data, freshness -> }` lambda by checking `freshness == DataFreshness.STALE`.
     */
    data object CACHE_ONLY : FetchPolicy

    /**
     * Network-with-cache on the read side, plus a periodic background refresh.
     *
     * Use for ambient surfaces that should silently re-fetch on a cadence — live FX
     * rates, market tickers, dashboard tiles. The read semantic is identical to
     * [NETWORK_WITH_CACHE] (cached value emitted immediately, network refresh
     * layered in); the periodic refresh is fired by [ScreenDataStream] internally
     * via the same refresh-trigger pipeline as the reconnect refresh and the
     * user-tap refresh — so it goes through the user-tap debounce window, the
     * staleness banner, and the lastContent preservation logic for free.
     *
     * **Tuning notes:**
     * - [intervalMillis] must be positive. Values shorter than ~5s on mobile
     *   risk battery drain; values shorter than the
     *   [DEFAULT_USER_REFRESH_DEBOUNCE_MS] (1s) will be coalesced by the
     *   user-tap debounce anyway.
     * - [foregroundOnly] is wired as a flag today but the foreground-aware
     *   gating is deferred to a follow-up phase (needs `LocalAppForegroundFlow`
     *   threading through core-base/ui). The ticker fires unconditionally for
     *   now; the flag is parsed and held so callers can declare intent without
     *   waiting for the runtime gate.
     *
     * @property intervalMillis Refresh cadence in milliseconds. Must be `> 0`.
     * @property foregroundOnly If true (default), refresh only when the app is
     *   in the foreground. Currently informational — see "Tuning notes".
     */
    data class PERIODIC(
        val intervalMillis: Long,
        val foregroundOnly: Boolean = true,
    ) : FetchPolicy {
        init {
            require(intervalMillis > 0L) { "intervalMillis must be positive (got $intervalMillis)" }
        }
    }

    /**
     * Cache-first stale-while-revalidate: emit the cached value immediately
     * (via [org.mobilenativefoundation.store.store5.StoreReadRequest.cached] with
     * `refresh = false`), then consult
     * [kpt.core.base.store.freshness.FreshnessBands.bandFor] on each cache
     * emission and fire the background refresh **only** when the band is
     * [kpt.core.base.store.freshness.FreshnessBand.Stale] or
     * [kpt.core.base.store.freshness.FreshnessBand.VeryStale]. Pair with
     * [ScreenDataStream.refreshFresh] for the sibling `refresh = true`
     * fresh-fetch path (pull-to-refresh, post-mutation) — the pair together
     * satisfies Store5 S5-5 (single source of truth for freshness).
     *
     * This is the `asScreenStream` DEFAULT (ScreenDataStream + ScreenStreamContext overloads)
     * — no per-call-site opt-in needed. Opt OUT explicitly when a screen must refetch on every
     * subscription: `asScreenStream(fetchPolicy = NETWORK_WITH_CACHE)`.
     *
     * Being the default is what makes an offline screen with an empty store render the screen's
     * own [ScreenState.Empty] rather than a blocking [ScreenState.NoNetwork]: the `.onStart`
     * NoNetwork pre-emit in [ScreenDataStream] is deliberately skipped for this policy, so the
     * DecisionEngine decides from the first store emission instead.
     */
    data object CACHE_FIRST_SWR : FetchPolicy
}
