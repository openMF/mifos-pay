/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation

import kpt.core.base.store.mutation.conflict.ConflictReport
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.MutableStore

/**
 * The single WRITE door for every mutation in the app.
 *
 * A repository never calls a `Dao` write directly; it routes upsert / delete / command through this
 * gateway, which reaches the network and write-throughs to the local source of truth (Room) so every
 * read — a Store stream OR a Dao reactive query on the same table — stays coherent.
 *
 * The gateway COMPOSES the existing Store5 write machinery (`MutableStore.write` + `Updater` +
 * `Bookkeeper`) and the command outbox; it does not replace them. Room remains the durable source of
 * truth; the gateway is the single write SoT on top of it.
 *
 * @see MutationPolicy for the two execution models (optimistic vs online-required).
 * @see MutationResult for the exhaustive outcome type — no mutation fails silently.
 */
interface MutationGateway {

    /**
     * Upsert [value] under [key] through [store].
     *
     * - [MutationPolicy.Optimistic]: the local write lands first (UI updates instantly); the network
     *   sync is queued via the store's `Updater` + `Bookkeeper` and retried on reconnect. Server-wins
     *   reconciliation is applied when the read store next fetches. A keyed optimistic write cannot be
     *   rolled back or conflict-recorded synchronously here (its network leg is async) — route a
     *   mutation that must await the server and record a conflict / roll back through [command].
     * - [MutationPolicy.OnlineRequired]: the network call is awaited first; on success the real server
     *   record is ingested by its server key. Offline → [MutationResult.Blocked].
     */
    suspend fun <K : Any, V : Any> upsert(
        store: MutableStore<K, V>,
        key: K,
        value: V,
        policy: MutationPolicy = MutationPolicy.Optimistic,
    ): MutationResult<V>

    /**
     * Delete the entity for [key] through [store], syncing the delete to the network via [deleteEndpoint].
     *
     * Store5's `MutableStore` has no network-DELETE path (its `Updater` is write-only), so the caller
     * supplies [deleteEndpoint] — the suspend network DELETE — and the store's [bookkeeper], into which
     * an Optimistic offline/failed delete records a pending-delete tombstone (retried on reconnect by
     * the feature's `SyncOrchestrator`). Optimistic delete clears the local row immediately; an
     * OnlineRequired delete awaits the network first and clears local only on success (offline →
     * [MutationResult.Blocked], never tombstoned).
     */
    suspend fun <K : Any, V : Any> delete(
        store: MutableStore<K, V>,
        key: K,
        deleteEndpoint: suspend (K) -> Unit,
        bookkeeper: Bookkeeper<K>,
        policy: MutationPolicy = MutationPolicy.Optimistic,
    ): MutationResult<Unit>

    /**
     * Run an RPC / command / form mutation described by [spec] (approve, submit, pay, …) — an operation
     * that is not a plain keyed value write. Composes the durable submit outbox for offline retry.
     */
    suspend fun <P : Any, R : Any> command(
        spec: CommandSpec<P, R>,
        policy: MutationPolicy = MutationPolicy.Optimistic,
    ): MutationResult<R>

    /**
     * Local-only mutation for a CACHE_ONLY feature (no `MutableStore` / `Updater`): runs [mutate] as a
     * write-through to Room inside a plain DAO write, so the table's reactive readers (Store
     * streams AND Dao the Room `Flow` queries) re-emit. This is the single write door for local-only features
     * — the repository never calls a `Dao` write in its own body; it hands the write to the gateway.
     * Always `Applied(synced = true)` (a local commit has no network leg) or `Failed` on a DB error.
     */
    suspend fun localMutation(table: String, mutate: suspend () -> Unit): MutationResult<Unit>
}

/**
 * How a mutation reaches the network relative to the local write.
 *
 * Two policies only — "optimistic" already means queue-and-retry; there is no separate queue-only mode.
 */
sealed interface MutationPolicy {
    /** Room-first: apply locally, queue the network sync, retry on reconnect, roll back on permanent reject. */
    data object Optimistic : MutationPolicy

    /** Network-first: await the server, ingest the real server record; offline → [MutationResult.Blocked]. */
    data object OnlineRequired : MutationPolicy
}

/**
 * The exhaustive outcome of a mutation. The caller (ViewModel) must handle every arm, so an offline
 * write or a conflict can never be silently swallowed.
 */
sealed interface MutationResult<out T> {
    /**
     * Applied locally. [synced] is false when the network sync was queued (optimistic offline).
     *
     * @property value the value as it now stands locally.
     * @property synced false when the mutation is queued rather than accepted by
     *   the server — the caller must not report it as confirmed.
     */
    data class Applied<out T>(val value: T, val synced: Boolean) : MutationResult<T>

    /**
     * An [MutationPolicy.OnlineRequired] mutation could not reach the network — nothing was written.
     *
     * @property reason which precondition refused it.
     */
    data class Blocked(val reason: BlockReason) : MutationResult<Nothing>

    /**
     * The server record diverged from the optimistic local write; recorded in the conflict inbox.
     *
     * @property conflictId addresses the inbox row, so the conflict can be resolved later.
     * @property server the server's value, so both sides can be shown to the user.
     */
    data class Conflicted<out T>(val conflictId: String, val server: T) : MutationResult<T>

    /**
     * The mutation failed permanently. [rolledBack] is true when the optimistic local write was undone.
     *
     * @property cause the underlying failure.
     * @property rolledBack whether local state was restored; false means local and
     *   server have diverged and need reconciling.
     */
    data class Failed(val cause: Throwable, val rolledBack: Boolean) : MutationResult<Nothing>
}

/** Why an [MutationPolicy.OnlineRequired] mutation was [MutationResult.Blocked]. */
enum class BlockReason {
    /** No network connection. */
    OFFLINE,

    /** The user is not authenticated for this operation. */
    UNAUTHENTICATED,

    /** A server-side precondition (e.g. insufficient funds, stale version) failed before any write. */
    PRECONDITION_FAILED,
}

/**
 * Describes a command / RPC mutation for [MutationGateway.command].
 *
 * @param payload      the request payload sent to [endpoint].
 * @param endpoint     the suspend network call producing the server result `R`.
 * @param localApply   optional optimistic local write applied BEFORE [endpoint] (Optimistic policy only);
 *                     omitted for OnlineRequired commands that must not touch local state until success.
 * @param rollback     undoes [localApply] on a permanent reject.
 * @param resultKeyOf  extracts the server key from the result `R` so it can be ingested under the real
 *                     server key (the temp-key → server-key remap for OnlineRequired).
 */
class CommandSpec<P : Any, R : Any>(
    val payload: P,
    val endpoint: suspend (P) -> R,
    val localApply: (suspend (R?) -> Unit)? = null,
    val rollback: (suspend () -> Unit)? = null,
    val resultKeyOf: ((R) -> Any)? = null,
    /** Detect a conflict between the local payload and the server result; non-null → recorded + Conflicted. */
    val conflictOf: ((P, R) -> ConflictReport?)? = null,
)
