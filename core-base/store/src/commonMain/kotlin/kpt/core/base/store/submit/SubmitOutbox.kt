/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Durable outbox for form payloads that failed to reach the server.
 *
 * Persist a payload on network failure → the user can resume later from any session.
 * The concrete implementation ([kpt.core.data.store.RoomSubmitOutbox]) serializes
 * `P` to JSON and writes it into the `framework_submit_drafts` Room table.
 *
 * Consumers rarely call this directly — [DraftSubmitHandler] wraps it for out-of-box use.
 *
 * @param P Serializable payload type that represents a complete form submission.
 */
interface SubmitOutbox<P> {

    /**
     * Save [payload] as the singleton PENDING draft under [formKey].
     *
     * If a PENDING draft already exists for [formKey] with `uniqueKey IS NULL`, this method
     * updates it in-place and returns its existing id (idempotent upsert — no duplicate rows).
     *
     * Use [saveByUniqueKey] when a screen needs N concurrent independent drafts under one
     * `formKey` (e.g. Portfolio Tracker per-holding, Bill Reminders per-bill).
     *
     * @return The surrogate id of the upserted row, useful for subsequent status updates.
     */
    suspend fun save(formKey: String, payload: P): Long

    /**
     * Multi-pending companion to [save]. Saves [payload] as a PENDING draft scoped on
     * `(formKey, uniqueKey)`. N concurrent drafts can coexist under one [formKey] as long as
     * each carries a distinct [uniqueKey] (e.g. `loan_id`, `bill_id`, wizard `"step_N"`).
     *
     * If a PENDING draft already exists for the exact `(formKey, uniqueKey)` pair, this method
     * updates it in-place — preserves single-pending-per-(formKey, uniqueKey) semantics.
     */
    suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long

    /** Retrieve the current singleton PENDING entry for [formKey], or `null` if none. */
    suspend fun getPending(formKey: String): SubmitOutboxEntry<P>?

    /** Retrieve the PENDING entry for a specific `(formKey, uniqueKey)` pair, or `null` if none. */
    suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>?

    /** Hot flow that emits the current singleton PENDING entry whenever it changes. */
    fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?>

    /** Streaming variant of [getPendingByUniqueKey]. */
    fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<SubmitOutboxEntry<P>?>

    /**
     * Observes ALL non-terminal drafts (PENDING / RETRYING / FAILED) for [formKey], ordered
     * newest-first. Used by feature UIs that need to display "N drafts pending sync" across
     * multiple uniqueKeys (e.g. Portfolio: "3 holdings pending").
     */
    fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>>

    /** All entries currently in PENDING status (excludes RETRYING) — used by [OfflineSubmitSyncer]. */
    suspend fun getAllPending(): List<SubmitOutboxEntry<P>>

    /**
     * Ids of this outbox's PENDING entries, re-emitted on change — lets the syncer send a draft the
     * moment it becomes pending while online (a Retry tapped in Sync & Drafts), instead of only on
     * the next reconnect. Default = never emits: such an outbox syncs on reconnects only.
     */
    fun observePendingIds(): Flow<Set<Long>> = emptyFlow()

    /** Claim this entry for retry — transitions PENDING → RETRYING; prevents concurrent UI double-submit. */
    suspend fun markRetrying(id: Long)

    /** Transition the entry identified by [id] to SUBMITTED. */
    suspend fun markSubmitted(id: Long)

    /** Transition the entry identified by [id] to FAILED with an optional [error] message. */
    suspend fun markFailed(id: Long, error: String?)

    /**
     * Put the entry back to PENDING after a failed attempt that is still within the retry cap, so
     * the syncer tries it again. Its `attemptCount` (bumped by [markRetrying]) is kept.
     * Default = [markFailed], the pre-retry behaviour, for implementations with no requeue.
     */
    suspend fun requeue(id: Long, error: String?) = markFailed(id, error)

    /** Delete all drafts for [formKey] (e.g. on screen close or after successful submit). */
    suspend fun deleteByFormKey(formKey: String)

    /**
     * Delete a single `(formKey, uniqueKey)` row. Use after a multi-pending draft successfully
     * submits. Does NOT affect the singleton draft (uniqueKey IS NULL) under the same [formKey].
     */
    suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String)

    /** Delete every draft — called from [StoreCacheManager.clearAll] on logout. */
    suspend fun deleteAll()
}

/**
 * A single outbox record as seen by the framework.
 *
 * @param id        Surrogate row id from the database.
 * @param formKey   Consumer-defined screen/form identifier.
 * @param uniqueKey Sub-identifier within [formKey]. `null` = singleton draft (legacy [save]);
 *                  non-null = one of N concurrent drafts under one form type (via
 *                  [SubmitOutbox.saveByUniqueKey]). Surfaced here so [SubmitOutbox.getAllPending]
 *                  consumers (notably [OfflineSubmitSyncer]) can re-route the retry per uniqueKey.
 * @param payload   Decoded payload (already deserialized by the concrete [SubmitOutbox]).
 * @param status    Current lifecycle state.
 * @param createdAtMs Epoch millis when the draft was first saved.
 * @param errorMessage Last failure reason, if any.
 * @param idempotencyKey Optional client-generated UUID for server-side de-duplication of
 *                  retries (see [kpt.core.base.store.submit.IdempotencyKey]).
 *                  **Phase 06 caveat:** the field is currently in-memory only — the Room
 *                  schema migration to v11 that persists this column is deferred. The
 *                  field is honoured by [BatchSubmitHandler] but ignored by every Room-
 *                  backed [SubmitOutbox] for now.
 */
data class SubmitOutboxEntry<out P>(
    val id: Long,
    val formKey: String,
    val payload: P,
    val status: SubmitOutboxStatus,
    val createdAtMs: Long,
    val uniqueKey: String? = null,
    val errorMessage: String? = null,
    val idempotencyKey: String? = null,
    /** Submit attempts made so far (0 = never attempted). Drives [RetryPolicy] backoff + cap. */
    val attemptCount: Int = 0,
)

/**
 * Lifecycle states for a [SubmitOutboxEntry].
 */
enum class SubmitOutboxStatus {
    /** Saved locally, not yet sent to the server. */
    PENDING,

    /** Claimed by [OfflineSubmitSyncer] — in-flight retry; prevents concurrent UI double-submit. */
    RETRYING,

    /** Successfully delivered to the server. Rows in this state may be pruned. */
    SUBMITTED,

    /** Retry attempt failed — [SubmitOutboxEntry.errorMessage] holds the reason. */
    FAILED,
}
