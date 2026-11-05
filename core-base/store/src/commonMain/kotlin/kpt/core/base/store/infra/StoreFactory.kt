/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.store.combine.ScreenWithMutationStream
import kpt.core.base.store.combine.internal.ScreenWithMutationStreamImpl
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.asScreenStream
import kpt.core.base.store.submit.SubmitHandler
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MemoryPolicy
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import org.mobilenativefoundation.store.store5.Validator

/**
 * Factory for creating [Store] and [MutableStore] instances with sensible defaults.
 *
 * Wraps Store 5's builder API into two concise factory methods:
 * - [createStore] for read-only stores (network fetch + local cache)
 * - [createMutableStore] for read-write stores (adds write-back + offline sync)
 */
object StoreFactory {

    /**
     * Creates a read-only [Store] where the fetcher output type matches
     * the source of truth input type (no conversion needed).
     *
     * **[DefaultValidator] wiring note:** If you pass a [DefaultValidator] as [validator],
     * you **must** call [DefaultValidator.markFresh] inside the [fetcher] block after a
     * successful network response. Without this call the TTL never starts and cached data
     * is treated as always valid regardless of age:
     * ```kotlin
     * val validator = DefaultValidator.withTtl<MyData>()
     * createStore(
     *     fetcher = Fetcher.of { key ->
     *         val data = api.fetch(key)
     *         validator.markFresh()   // ← required
     *         data
     *     },
     *     sourceOfTruth = mySourceOfTruth,
     *     validator = validator,
     * )
     * ```
     *
     * @param Key The type used to identify data (e.g., a user ID or query params).
     * @param Input The type produced by the fetcher and consumed by the source of truth.
     * @param Output The domain type exposed to consumers.
     * @param fetcher Network data source that fetches fresh data for a given key.
     * @param sourceOfTruth Local persistence layer (reader emits cached data, writer persists).
     * @param validator Optional cache validity check (e.g., TTL-based expiration).
     * @param memoryPolicy Optional in-memory cache eviction policy (max items, expiration).
     * @return A configured [Store] ready for streaming reads.
     */
    fun <Key : Any, Input : Any, Output : Any> createStore(
        fetcher: Fetcher<Key, Input>,
        sourceOfTruth: SourceOfTruth<Key, Input, Output>,
        validator: Validator<Output>? = null,
        memoryPolicy: MemoryPolicy<Key, Output>? = null,
    ): Store<Key, Output> {
        var builder = StoreBuilder.from(
            fetcher = fetcher,
            sourceOfTruth = sourceOfTruth,
        )
        if (validator != null) {
            builder = builder.validator(validator)
        }
        if (memoryPolicy != null) {
            builder = builder.cachePolicy(memoryPolicy)
        }
        return builder.build()
    }

    /**
     * Creates a read-only [Store] backed only by a [Fetcher] (no local persistence).
     *
     * Data is cached in-memory only. Useful for transient data that doesn't
     * need to survive process death.
     *
     * @param Key The type used to identify data.
     * @param Output The type produced by the fetcher and exposed to consumers.
     * @param fetcher Network data source.
     * @return A configured [Store] with in-memory caching only.
     */
    fun <Key : Any, Output : Any> createMemoryStore(
        fetcher: Fetcher<Key, Output>,
    ): Store<Key, Output> {
        return StoreBuilder.from(fetcher = fetcher).build()
    }

    /**
     * Creates a read-only [Store] backed only by a [SourceOfTruth] (no network fetcher).
     *
     * Suitable for purely offline/local-only data flows where there is no remote data
     * source — Room DAO flows, file-backed stores, or in-process caches that are
     * populated through a separate write path (e.g., [createMutableStore]).
     *
     * @param Key The type used to identify data (e.g., a primary key or query params).
     * @param Output The domain type exposed to consumers.
     * @param sourceOfTruth Local persistence layer whose reader emits cached data.
     * @return A configured [Store] that streams exclusively from local storage.
     */
    fun <Key : Any, Output : Any> createOfflineStore(
        sourceOfTruth: SourceOfTruth<Key, Output, Output>,
    ): Store<Key, Output> = StoreBuilder
        .from(
            fetcher = Fetcher.ofFlow<Key, Output> { _ -> emptyFlow() },
            sourceOfTruth = sourceOfTruth,
        )
        .build()

    /**
     * Creates a [MutableStore] for a LOCAL-ONLY entity — every mutation flows through
     * `store.write` / `store.clear` (→ the [sourceOfTruth] writer/delete, i.e. Room), but there is
     * NO network. The [Updater] is a no-op "success" and the [Bookkeeper] never records anything, so
     * `MutationGateway.upsert`/`delete` (Optimistic) persist locally and report `synced = true`
     * without any remote call.
     *
     * This is the WRITE half for `OFFLINE_LOCAL_ONLY` features that route every mutation through the
     * store (the single write door — no repo-level DAO writes), pairing with a [createOfflineStore]
     * read store over the same table. The caller's SoT writer/delete SHOULD wrap the DAO call in
     * a plain DAO write so wasmJs read collectors re-emit (Room 3 alpha05 invalidation gap).
     *
     * @param Key per-item key (e.g. the row id wrapped in a value class).
     * @param Output the domain type persisted + exposed.
     * @param sourceOfTruth local persistence (Room) — its writer/delete are the ONLY DAO callers.
     */
    @OptIn(ExperimentalStoreApi::class)
    fun <Key : Any, Output : Any> createOfflineMutableStore(
        sourceOfTruth: SourceOfTruth<Key, Output, Output>,
    ): MutableStore<Key, Output> {
        val identity = Converter.Builder<Output, Output, Output>()
            .fromNetworkToLocal { it }
            .fromOutputToLocal { it }
            .build()
        val noopUpdater = Updater.by<Key, Output, Output>(
            post = { _, value -> UpdaterResult.Success.Typed(value) },
        )
        return StoreBuilder
            .from(
                fetcher = Fetcher.ofFlow<Key, Output> { _ -> emptyFlow() },
                sourceOfTruth = sourceOfTruth,
                converter = identity,
            )
            .toMutableStoreBuilder(identity)
            .build(updater = noopUpdater, bookkeeper = NoopBookkeeper())
    }

    /** No-network [Bookkeeper] for [createOfflineMutableStore] — local writes never fail-to-sync. */
    private class NoopBookkeeper<Key : Any> : Bookkeeper<Key> {
        override suspend fun getLastFailedSync(key: Key): Long? = null
        override suspend fun setLastFailedSync(key: Key, timestamp: Long): Boolean = true
        override suspend fun clear(key: Key): Boolean = true
        override suspend fun clearAll(): Boolean = true
    }

    /**
     * Creates a [MutableStore] that supports reads, writes, and offline sync.
     *
     * Uses a [Converter] to transform between network, local, and output types.
     * When a write fails (e.g., no network), the [Bookkeeper] records the failure
     * for retry on connectivity restore.
     *
     * @param Key The type used to identify data.
     * @param Network The raw type returned by the network layer.
     * @param Local The type persisted in local storage.
     * @param Output The domain type exposed to consumers.
     * @param fetcher Network data source for reads.
     * @param sourceOfTruth Local persistence layer.
     * @param converter Transforms between Network, Local, and Output types.
     * @param updater Writes data back to the server (e.g., POST/PUT API call).
     * @param bookkeeper Tracks unsynced local changes for retry on reconnection.
     * @param validator Optional cache validity check.
     * **Conflict resolution is NOT a parameter here.** Store5's [Updater] is typed
     * `Updater<Key, Output, *>` — the server response type is star-projected, so the factory
     * cannot reach the server value a [ConflictStrategy] needs. Resolution therefore belongs
     * inside the caller's own `Updater`, which DOES have both sides:
     * `conflictStrategy.resolve(server = serverEcho, client = value)` — see
     * `provideCloudTodoStore` for the canonical shape. This factory previously accepted a
     * `conflictStrategy` parameter that was `@Suppress("UNUSED_PARAMETER")` and had zero runtime
     * effect: passing `ServerWins()` and passing nothing behaved identically, which read as
     * configuration but was decoration.
     * @return A configured [MutableStore] ready for reads and writes.
     */
    @OptIn(ExperimentalStoreApi::class)
    fun <Key : Any, Network : Any, Local : Any, Output : Any> createMutableStore(
        fetcher: Fetcher<Key, Network>,
        sourceOfTruth: SourceOfTruth<Key, Local, Output>,
        converter: Converter<Network, Local, Output>,
        updater: Updater<Key, Output, *>,
        bookkeeper: Bookkeeper<Key>,
        validator: Validator<Output>? = null,
    ): MutableStore<Key, Output> {
        var builder = StoreBuilder.from(
            fetcher = fetcher,
            sourceOfTruth = sourceOfTruth,
            converter = converter,
        )
        if (validator != null) {
            builder = builder.validator(validator)
        }
        return builder
            .toMutableStoreBuilder(converter)
            .build(
                updater = updater,
                bookkeeper = bookkeeper,
            )
    }

    /**
     * Creates a [ScreenWithMutationStream] — a fused read + write + sync seam for
     * screens that both display data and submit mutations against the same domain object
     * (edit forms, settings panels, in-place record updates).
     *
     * Wires the read pipeline ([Store.asScreenStream]) and the write pipeline
     * ([SubmitHandler]) into a single hot state flow ([ScreenWithMutationStream.state])
     * so the consuming ViewModel exposes one `StateFlow<CombinedState<R, W>>` instead
     * of three separate flows.
     *
     * **Outbox / sync wiring (optional):** pass [pendingCountFlow] to surface a
     * "X pending" badge, and [syncingFlow] to surface a "Syncing..." indicator
     * (typically wired to `outbox.observeAllByFormKey(formKey).map { it.size }` and
     * the offline syncer's status flow respectively). Both default to constant flows
     * so screens that don't need the outbox indicator get a clean
     * `outboxPending = 0, isSyncing = false`.
     *
     * @param Key Store key type.
     * @param R Read-side domain payload type.
     * @param W Write-side payload type submitted by the screen.
     * @param store the read-side Store.
     * @param key Store key to stream data for.
     * @param networkMonitor cmp-network-monitor's NetworkMonitor (injected via Koin).
     * @param fetchedAtRepository Persists last-network-fetch timestamps; required for
     *   the read pipeline's freshness banner.
     * @param cacheKey Identifies this Store in the [fetchedAtRepository].
     * @param submitHandler Caller-provided write handler typed at [W]. Created via
     *   `viewModelScope.submitHandler<W>()` at the call site.
     * @param submitBlock The actual API call. Receives the payload [W] and returns
     *   the saved/updated payload (typically the same value passed in) on success.
     *   Throwing transitions [submitHandler] to `Failed`.
     * @param scope CoroutineScope (typically `viewModelScope`).
     * @param fetchPolicy Read-side fetch policy. Defaults to
     *   [FetchPolicy.NETWORK_WITH_CACHE].
     * @param pendingCountFlow Optional outbox-pending-count flow; default `flowOf(0)`.
     * @param syncingFlow Optional background-sync indicator flow; default `flowOf(false)`.
     */
    fun <Key : Any, R : Any, W : Any> createScreenWithMutation(
        store: Store<Key, R>,
        key: Key,
        networkMonitor: NetworkMonitor,
        fetchedAtRepository: FetchedAtRepository,
        cacheKey: String,
        submitHandler: SubmitHandler<W>,
        submitBlock: suspend (W) -> W,
        scope: CoroutineScope,
        fetchPolicy: FetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
        pendingCountFlow: Flow<Int> = flowOf(0),
        syncingFlow: Flow<Boolean> = flowOf(false),
    ): ScreenWithMutationStream<R, W> {
        val readStream = store.asScreenStream(
            key = key,
            networkMonitor = networkMonitor,
            fetchedAtRepository = fetchedAtRepository,
            cacheKey = cacheKey,
            scope = scope,
            fetchPolicy = fetchPolicy,
        )
        return ScreenWithMutationStreamImpl(
            readStream = readStream.state,
            submitHandler = submitHandler,
            submitBlock = submitBlock,
            pendingCountFlow = pendingCountFlow,
            syncingFlow = syncingFlow,
            scope = scope,
            onRefresh = { readStream.refresh() },
        )
    }
}
