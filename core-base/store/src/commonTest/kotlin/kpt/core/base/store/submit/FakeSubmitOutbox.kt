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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [SubmitOutbox] for unit tests. Never write to disk or Room.
 *
 * Usage:
 * ```kotlin
 * val outbox = FakeSubmitOutbox<MyPayload>()
 * val handler = testScope.draftSubmitHandler(outbox, "test_form")
 * ```
 */
class FakeSubmitOutbox<P> : SubmitOutbox<P> {

    private val _entries = MutableStateFlow<List<SubmitOutboxEntry<P>>>(emptyList())
    private var nextId = 1L

    /** Current snapshot of all entries — useful for assertions. */
    val entries: List<SubmitOutboxEntry<P>> get() = _entries.value

    override suspend fun save(formKey: String, payload: P): Long {
        val existing = _entries.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }
        if (existing != null) {
            _entries.value = _entries.value.map {
                if (it.id == existing.id) it.copy(payload = payload) else it
            }
            return existing.id
        }
        val id = nextId++
        _entries.value = _entries.value + SubmitOutboxEntry(
            id = id,
            formKey = formKey,
            payload = payload,
            status = SubmitOutboxStatus.PENDING,
            createdAtMs = 0L,
            uniqueKey = null,
        )
        return id
    }

    override suspend fun saveByUniqueKey(formKey: String, uniqueKey: String, payload: P): Long {
        val existing = _entries.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == SubmitOutboxStatus.PENDING
        }
        if (existing != null) {
            _entries.value = _entries.value.map {
                if (it.id == existing.id) it.copy(payload = payload) else it
            }
            return existing.id
        }
        val id = nextId++
        _entries.value = _entries.value + SubmitOutboxEntry(
            id = id,
            formKey = formKey,
            payload = payload,
            status = SubmitOutboxStatus.PENDING,
            createdAtMs = 0L,
            uniqueKey = uniqueKey,
        )
        return id
    }

    override suspend fun getPending(formKey: String): SubmitOutboxEntry<P>? =
        _entries.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
        }

    override suspend fun getPendingByUniqueKey(formKey: String, uniqueKey: String): SubmitOutboxEntry<P>? =
        _entries.value.firstOrNull {
            it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == SubmitOutboxStatus.PENDING
        }

    override fun observePending(formKey: String): Flow<SubmitOutboxEntry<P>?> =
        _entries.map { list ->
            list.firstOrNull {
                it.formKey == formKey && it.uniqueKey == null && it.status == SubmitOutboxStatus.PENDING
            }
        }

    override fun observePendingByUniqueKey(formKey: String, uniqueKey: String): Flow<SubmitOutboxEntry<P>?> =
        _entries.map { list ->
            list.firstOrNull {
                it.formKey == formKey && it.uniqueKey == uniqueKey && it.status == SubmitOutboxStatus.PENDING
            }
        }

    override fun observeAllByFormKey(formKey: String): Flow<List<SubmitOutboxEntry<P>>> =
        _entries.map { list ->
            list.filter {
                it.formKey == formKey && it.status in NON_TERMINAL_STATES
            }.sortedByDescending { it.createdAtMs }
        }

    override suspend fun getAllPending(): List<SubmitOutboxEntry<P>> =
        _entries.value.filter { it.status == SubmitOutboxStatus.PENDING }

    // Mirrors production: RoomSubmitOutbox's DAO does
    // `SET status = 'RETRYING', attemptCount = attemptCount + 1`. The fake must increment too, or
    // OfflineSubmitSyncer's RetryPolicy cap can never be exercised in tests.
    override suspend fun markRetrying(id: Long) {
        _entries.value = _entries.value.map {
            if (it.id == id) {
                it.copy(status = SubmitOutboxStatus.RETRYING, attemptCount = it.attemptCount + 1)
            } else {
                it
            }
        }
    }

    override fun observePendingIds(): kotlinx.coroutines.flow.Flow<Set<Long>> =
        _entries.map { rows -> rows.filter { it.status == SubmitOutboxStatus.PENDING }.map { it.id }.toSet() }

    // Mirrors `DraftDao.requeue`: back to PENDING, attemptCount kept.
    override suspend fun requeue(id: Long, error: String?) {
        _entries.value = _entries.value.map {
            if (it.id == id) it.copy(status = SubmitOutboxStatus.PENDING, errorMessage = null) else it
        }
    }

    /** Test seam — force an entry's attemptCount to simulate prior failed cycles. */
    fun seedAttemptCount(id: Long, attempts: Int) {
        _entries.value = _entries.value.map {
            if (it.id == id) it.copy(attemptCount = attempts) else it
        }
    }

    override suspend fun markSubmitted(id: Long) = updateStatus(id, SubmitOutboxStatus.SUBMITTED)

    override suspend fun markFailed(id: Long, error: String?) {
        _entries.value = _entries.value.map {
            if (it.id == id) it.copy(status = SubmitOutboxStatus.FAILED, errorMessage = error) else it
        }
    }

    override suspend fun deleteByFormKey(formKey: String) {
        _entries.value = _entries.value.filter { it.formKey != formKey }
    }

    override suspend fun deleteByUniqueKey(formKey: String, uniqueKey: String) {
        _entries.value = _entries.value.filter { !(it.formKey == formKey && it.uniqueKey == uniqueKey) }
    }

    override suspend fun deleteAll() {
        _entries.value = emptyList()
    }

    private fun updateStatus(id: Long, status: SubmitOutboxStatus) {
        _entries.value = _entries.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
    }

    private companion object {
        val NON_TERMINAL_STATES = setOf(
            SubmitOutboxStatus.PENDING,
            SubmitOutboxStatus.RETRYING,
            SubmitOutboxStatus.FAILED,
        )
    }
}
