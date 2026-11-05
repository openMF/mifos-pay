/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.database.infra.entity.DraftEntity
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.store.submit.SubmitOutboxEntry
import kpt.core.base.store.submit.SubmitOutboxStatus

/**
 * Room-backed [SubmitOutbox] that persists form payloads across process death.
 *
 * Serializes payloads to JSON via [kotlinx.serialization] and stores them in the
 * `framework_submit_drafts` table in [kpt.core.database.AppDatabase].
 *
 * Wire one instance per form type in your DI module:
 * ```kotlin
 * single<SubmitOutbox<LoanApplicationPayload>> {
 *     RoomSubmitOutbox(get<AppDatabase>().draftDao, LoanApplicationPayload.serializer())
 * }
 * ```
 *
 * @param P Payload type. Must be annotated with `@Serializable`.
 * @param dao The Room DAO for the `framework_submit_drafts` table.
 * @param serializer Kotlinx serializer for [P]. Obtain via `P.serializer()`.
 * @param ownsFormKey Which `formKey`s this outbox's [getAllPending] returns. The table is shared by
 *   every form, and each form's `OfflineSubmitSyncer` replays [getAllPending] with ITS serializer —
 *   so an app with two outboxes must scope them, or a row of one form that happens to decode as
 *   the other's payload is replayed as the wrong thing. Default = all rows (one-outbox apps).
 */
class RoomSubmitOutbox<P>(
    private val dao: DraftDao,
    private val serializer: KSerializer<P>,
    private val ownsFormKey: (String) -> Boolean = { true },
) : SubmitOutbox<P> {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun save(formKey: String, payload: P): Long = run {
        val nowMs = currentTimeMillis()
        val existing = dao.getPendingByFormKey(formKey)
        if (existing != null) {
            dao.updatePayload(existing.id, json.encodeToString(serializer, payload), nowMs)
            existing.id
        } else {
            val entity = DraftEntity(
                formKey = formKey,
                uniqueKey = null,
                payloadJson = json.encodeToString(serializer, payload),
                status = SubmitOutboxStatus.PENDING.name,
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            )
            dao.insert(entity)
        }
    }

    override suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long =
        run {
            val nowMs = currentTimeMillis()
            val existing = dao.getPendingByUniqueKey(formKey, uniqueKey)
            if (existing != null) {
                dao.updatePayload(existing.id, json.encodeToString(serializer, payload), nowMs)
                existing.id
            } else {
                val entity = DraftEntity(
                    formKey = formKey,
                    uniqueKey = uniqueKey,
                    payloadJson = json.encodeToString(serializer, payload),
                    status = SubmitOutboxStatus.PENDING.name,
                    createdAtMs = nowMs,
                    updatedAtMs = nowMs,
                )
                dao.insert(entity)
            }
        }

    override suspend fun getPending(formKey: String): SubmitOutboxEntry<P>? =
        dao.getPendingByFormKey(formKey)?.toEntry()

    override suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>? =
        dao.getPendingByUniqueKey(formKey, uniqueKey)?.toEntry()

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> =
        dao.observePendingByFormKey(formKey).map { it?.toEntry() }

    override fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<SubmitOutboxEntry<P>?> =
        dao.observePendingByUniqueKey(formKey, uniqueKey).map { it?.toEntry() }

    override fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>> =
        dao.observeAllByFormKey(formKey).map { rows -> rows.mapNotNull { it.toEntry() } }

    override fun observePendingIds(): Flow<Set<Long>> = dao.observeAll().map { rows ->
        rows.filter { it.status == SubmitOutboxStatus.PENDING.name && ownsFormKey(it.formKey) }.map { it.id }.toSet()
    }

    override suspend fun getAllPending(): List<SubmitOutboxEntry<P>> =
        dao.getAllPending().filter { ownsFormKey(it.formKey) }.mapNotNull { it.toEntry() }

    override suspend fun markRetrying(id: Long) =
        dao.markRetrying(id, currentTimeMillis())

    override suspend fun markSubmitted(id: Long) =
        dao.markSubmitted(id, currentTimeMillis())

    override suspend fun markFailed(id: Long, error: String?) =
        dao.markFailed(id, currentTimeMillis(), error)

    override suspend fun requeue(id: Long, error: String?) =
        dao.requeue(id, currentTimeMillis())

    override suspend fun deleteByFormKey(formKey: String) =
        dao.deleteByFormKey(formKey)

    override suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String) =
        dao.deleteByUniqueKey(formKey, uniqueKey)

    override suspend fun deleteAll() = dao.deleteAll()

    private fun DraftEntity.toEntry(): SubmitOutboxEntry<P>? = runCatching {
        SubmitOutboxEntry(
            id = id,
            formKey = formKey,
            payload = json.decodeFromString(serializer, payloadJson),
            status = SubmitOutboxStatus.valueOf(status),
            createdAtMs = createdAtMs,
            uniqueKey = uniqueKey,
            errorMessage = errorMessage,
            attemptCount = attemptCount,
        )
    }.getOrNull()
}

private fun currentTimeMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
