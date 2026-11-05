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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Locks the wasmJs invalidation-bridge wiring for [RoomSubmitOutbox] — the framework-owned
 * `DraftSubmitHandler` backing store used by every offline-resilient form.
 *
 * [FakeDraftDao] returns **cold snapshot** flows that never self-re-emit, modelling Room 3
 * alpha05 on wasmJs (the `InvalidationTracker` does not fan out to a live collector after a
 * write). These assertions can only pass because the outbox now:
 *  - exposes `observePending*` / `observeAllByFormKey` as plain Room `Flow`s, and
 *  - wraps every write (`save`, `saveByUniqueKey`, `mark*`, `delete*`) in
 *    a plain DAO write.
 *
 * On the pre-fix code — raw `dao.observePending*` reads + raw writes — the draft-status badge
 * on web would never advance past its first emission. Regression guard for that defect class.
 */
class RoomSubmitOutboxReactiveInvalidationTest {

    private val dao = FakeDraftDao()
    private val outbox = RoomSubmitOutbox(dao, String.serializer())

    @Test
    fun observePendingReEmitsAcrossSaveThenSubmit() = runTest {
        outbox.observePending(FORM).test {
            assertNull(awaitItem())
            outbox.save(FORM, "draft-1")
            assertEquals("draft-1", awaitItem()?.payload)
            val id = outbox.getPending(FORM)!!.id
            outbox.markSubmitted(id)
            assertNull(awaitItem()) // no longer PENDING → filtered out
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeAllByFormKeyReEmitsOnSaveThenDelete() = runTest {
        outbox.observeAllByFormKey(FORM).test {
            assertEquals(emptyList(), awaitItem().map { it.payload })
            outbox.save(FORM, "d1")
            assertEquals(listOf("d1"), awaitItem().map { it.payload })
            outbox.deleteByFormKey(FORM)
            assertEquals(emptyList(), awaitItem().map { it.payload })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observePendingByUniqueKeyReEmitsAcrossSaveThenDelete() = runTest {
        outbox.observePendingByUniqueKey(FORM, UNIQUE).test {
            assertNull(awaitItem())
            outbox.saveByUniqueKey(FORM, UNIQUE, "draft-u1")
            assertEquals("draft-u1", awaitItem()?.payload)
            outbox.deleteByUniqueKey(FORM, UNIQUE)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val FORM = "loan_application"
        const val UNIQUE = "loan_42"
    }
}
