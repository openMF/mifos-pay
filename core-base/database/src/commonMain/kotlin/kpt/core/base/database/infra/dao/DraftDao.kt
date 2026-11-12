/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.infra.entity.DraftEntity

/**
 * DAO for the framework-owned `framework_submit_drafts` table.
 *
 * Consumed by [kpt.core.data.store.RoomSubmitOutbox]. The framework uses this table to
 * persist form payloads that failed to reach the server so users can resume or retry later.
 */
@Dao
interface DraftDao {

    /** Inserts a draft, returning its new row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DraftEntity): Long

    /** One draft by row id, or null if it has been submitted and pruned. */
    @Query("SELECT * FROM framework_submit_drafts WHERE id = :id")
    suspend fun getById(id: Long): DraftEntity?

    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE formKey = :formKey AND uniqueKey IS NULL AND status = 'PENDING' LIMIT 1",
    )
    /** The PENDING draft for a form, if the user has an unsent edit for it. */
    suspend fun getPendingByFormKey(formKey: String): DraftEntity?

    /** Observes the pending draft for a form, so a form screen re-opens with the user's unsent edit. */
    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE formKey = :formKey AND uniqueKey IS NULL AND status = 'PENDING' LIMIT 1",
    )
    fun observePendingByFormKey(formKey: String): Flow<DraftEntity?>

    /**
     * Multi-pending companion to [getPendingByFormKey]. Reads the PENDING draft for a specific
     * `(formKey, uniqueKey)` pair — N concurrent independent drafts can coexist under one
     * `formKey` when each carries a distinct `uniqueKey` (e.g. `loan_id`, `bill_id`, wizard step).
     */
    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE formKey = :formKey AND uniqueKey = :uniqueKey AND status = 'PENDING' LIMIT 1",
    )
    suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): DraftEntity?

    /** Streaming variant of [getPendingByUniqueKey]. */
    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE formKey = :formKey AND uniqueKey = :uniqueKey AND status = 'PENDING' LIMIT 1",
    )
    fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<DraftEntity?>

    /**
     * Observes ALL non-terminal drafts (PENDING / RETRYING / FAILED) for a `formKey`, ordered
     * newest-first by `createdAtMs`. Includes singleton drafts (uniqueKey IS NULL) and all
     * multi-pending rows. Used by feature UIs that need to display "N drafts pending sync".
     */
    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE formKey = :formKey AND status IN ('PENDING','RETRYING','FAILED') " +
            "ORDER BY createdAtMs DESC",
    )
    fun observeAllByFormKey(formKey: String): Flow<List<DraftEntity>>

    /** Every draft still awaiting submission — what the syncer drains on reconnect. */
    @Query("SELECT * FROM framework_submit_drafts WHERE status = 'PENDING'")
    suspend fun getAllPending(): List<DraftEntity>

    /**
     * Observes ALL non-terminal drafts (PENDING / RETRYING / FAILED) across EVERY formKey,
     * newest-first. Cross-form data source for the template-level **Sync & Drafts** screen
     * (Settings) — a live preview of every draft + syncable record the app is holding, so the
     * user can resume/discard drafts and inspect pending/failed syncs in one place.
     */
    @Query(
        "SELECT * FROM framework_submit_drafts " +
            "WHERE status IN ('PENDING','RETRYING','FAILED') " +
            "ORDER BY updatedAtMs DESC",
    )
    fun observeAll(): Flow<List<DraftEntity>>

    @Query(
        "UPDATE framework_submit_drafts SET status = 'RETRYING', updatedAtMs = :nowMs, " +
            "attemptCount = attemptCount + 1 WHERE id = :id",
    )
    /** Marks a draft as being retried now, bumping its attempt count. */
    suspend fun markRetrying(id: Long, nowMs: Long)

    /** Marks a draft submitted. It stays until pruned, so the outcome remains visible to the user. */
    @Query("UPDATE framework_submit_drafts SET status = 'SUBMITTED', updatedAtMs = :nowMs WHERE id = :id")
    suspend fun markSubmitted(id: Long, nowMs: Long)

    /**
     * Re-queues a draft for sync: transitions any state (typically FAILED) back to PENDING and
     * clears [DraftEntity.errorMessage], so [kpt.core.base.store.submit.OfflineSubmitSyncer] —
     * which drains PENDING via [getAllPending] (RETRYING is excluded) — re-attempts it on the
     * next online transition. The MANUAL counterpart to the automatic reconnect retry, surfaced
     * by the template-level **Sync & Drafts** screen's per-row Retry action.
     */
    @Query(
        "UPDATE framework_submit_drafts SET status = 'PENDING', errorMessage = NULL, " +
            "updatedAtMs = :nowMs WHERE id = :id",
    )
    suspend fun requeue(id: Long, nowMs: Long)

    @Query(
        "UPDATE framework_submit_drafts SET status = 'FAILED', " +
            "updatedAtMs = :nowMs, errorMessage = :error WHERE id = :id",
    )
    /** Marks a draft failed, recording [error] so the user can be told WHY rather than just that it failed. */
    suspend fun markFailed(id: Long, nowMs: Long, error: String?)

    /** Replaces a draft's payload — the user edited an unsent draft rather than creating another. */
    @Query("UPDATE framework_submit_drafts SET payloadJson = :payloadJson, updatedAtMs = :nowMs WHERE id = :id")
    suspend fun updatePayload(id: Long, payloadJson: String, nowMs: Long)

    /**
     * Deletes every draft for one form, regardless of status. Called once a submit is confirmed, and by the resume
     * prompt when the user chooses Discard.
     */
    @Query("DELETE FROM framework_submit_drafts WHERE formKey = :formKey")
    suspend fun deleteByFormKey(formKey: String)

    /**
     * Deletes a single `(formKey, uniqueKey)` row. Use after a multi-pending draft
     * successfully submits (paired with [getPendingByUniqueKey]). Does NOT affect the
     * singleton draft (uniqueKey IS NULL) under the same `formKey`.
     */
    @Query("DELETE FROM framework_submit_drafts WHERE formKey = :formKey AND uniqueKey = :uniqueKey")
    suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String)

    /**
     * Permanently deletes a single draft row by surrogate [id]. Cross-form counterpart to
     * [deleteByFormKey] / [deleteByUniqueKey] — the template-level **Sync & Drafts** screen's
     * per-row Discard acts on an [id] it already holds, regardless of formKey/uniqueKey.
     */
    @Query("DELETE FROM framework_submit_drafts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Deletes every draft. Called on logout: a draft is one user's unsent data. */
    @Query("DELETE FROM framework_submit_drafts")
    suspend fun deleteAll()

    /**
     * Deletes SUBMITTED and FAILED rows older than [thresholdMs] (epoch millis).
     * PENDING drafts are never pruned here — the user may still want to resume them.
     * Call on app start via [kpt.core.base.store.infra.StoreCacheManager.pruneExpiredDrafts].
     */
    @Query(
        "DELETE FROM framework_submit_drafts " +
            "WHERE createdAtMs < :thresholdMs AND status IN ('SUBMITTED', 'FAILED')",
    )
    suspend fun deleteOlderThan(thresholdMs: Long)
}
