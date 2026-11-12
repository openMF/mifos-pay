/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.infra

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.database.infra.entity.DraftEntity

/**
 * In-memory fake of [DraftDao] whose reactive reads are backed by a [MutableStateFlow], so a live
 * collector re-emits after every write — what a real Room DAO `Flow` does.
 *
 * These reads were COLD until 2026-09-17, modelling a wasmJs invalidation gap so that re-emission
 * could only come from the `RoomChangeBus`/`daoFlow`/`notifyingWrite` bridge. That bridge is gone
 * (Room 3.1.0-alpha01 measured re-emitting correctly on js and wasmJs — see
 * `core/database/src/{js,wasmJs}Test/.../WebInvalidationProbeTest.kt`), so a cold fake would now
 * assert the absence of a mechanism production depends on.
 */
internal class FakeDraftDao : DraftDao {

    private val rows = MutableStateFlow<List<DraftEntity>>(emptyList())
    private var nextId = 1L

    private val nonTerminal = setOf("PENDING", "RETRYING", "FAILED")

    private val current: List<DraftEntity> get() = rows.value

    override suspend fun insert(entity: DraftEntity): Long {
        val id = nextId++
        rows.update { it + entity.copy(id = id) }
        return id
    }

    override suspend fun getById(id: Long): DraftEntity? = current.firstOrNull { it.id == id }

    override suspend fun getPendingByFormKey(formKey: String): DraftEntity? =
        current.firstOrNull { it.formKey == formKey && it.uniqueKey == null && it.status == "PENDING" }

    override fun observePendingByFormKey(formKey: String): Flow<DraftEntity?> =
        rows.map { list ->
            list.firstOrNull { it.formKey == formKey && it.uniqueKey == null && it.status == "PENDING" }
        }

    override suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): DraftEntity? =
        current.firstOrNull { it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == "PENDING" }

    override fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<DraftEntity?> =
        rows.map { list ->
            list.firstOrNull { it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == "PENDING" }
        }

    override fun observeAllByFormKey(formKey: String): Flow<List<DraftEntity>> =
        rows.map { list ->
            list.filter { it.formKey == formKey && it.status in nonTerminal }
                .sortedByDescending { it.createdAtMs }
        }

    override fun observeAll(): Flow<List<DraftEntity>> =
        rows.map { list ->
            list.filter { it.status in nonTerminal }
                .sortedByDescending { it.updatedAtMs }
        }

    override suspend fun getAllPending(): List<DraftEntity> = current.filter { it.status == "PENDING" }

    override suspend fun markRetrying(id: Long, nowMs: Long) =
        updateRow(id) { it.copy(status = "RETRYING", updatedAtMs = nowMs) }

    override suspend fun requeue(id: Long, nowMs: Long) =
        updateRow(id) { it.copy(status = "PENDING", errorMessage = null, updatedAtMs = nowMs) }

    override suspend fun markSubmitted(id: Long, nowMs: Long) =
        updateRow(id) { it.copy(status = "SUBMITTED", updatedAtMs = nowMs) }

    override suspend fun markFailed(id: Long, nowMs: Long, error: String?) =
        updateRow(id) { it.copy(status = "FAILED", updatedAtMs = nowMs, errorMessage = error) }

    override suspend fun updatePayload(id: Long, payloadJson: String, nowMs: Long) =
        updateRow(id) { it.copy(payloadJson = payloadJson, updatedAtMs = nowMs) }

    override suspend fun deleteByFormKey(formKey: String) {
        rows.update { list -> list.filterNot { it.formKey == formKey } }
    }

    override suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String) {
        rows.update { list -> list.filterNot { it.formKey == formKey && it.uniqueKey == uniqueKey } }
    }

    override suspend fun deleteById(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteAll() {
        rows.update { emptyList() }
    }

    override suspend fun deleteOlderThan(thresholdMs: Long) {
        rows.update { list ->
            list.filterNot { it.createdAtMs < thresholdMs && (it.status == "SUBMITTED" || it.status == "FAILED") }
        }
    }

    private fun updateRow(id: Long, block: (DraftEntity) -> DraftEntity) {
        rows.update { list -> list.map { if (it.id == id) block(it) else it } }
    }
}
