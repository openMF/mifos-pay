/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra.impl

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import kpt.core.base.store.submit.SubmitOutboxStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Serializable
private data class Payload(val amount: Int, val note: String = "")

/**
 * Locks [RoomSubmitOutbox] — the durable queue that survives process death and makes an offline
 * submit recoverable. Untested until now, despite being the thing standing between a user's unsent
 * form and silent data loss.
 *
 * Two behaviours carry the most risk and are pinned hardest: the **idempotent upsert** (a re-save
 * under the same formKey must update in place, never queue a duplicate submission) and the
 * **uniqueKey** path (N independent drafts under one formKey, e.g. per-bill edits).
 */
class RoomSubmitOutboxTest {

    private fun outbox(dao: FakeDraftDao = FakeDraftDao()) =
        RoomSubmitOutbox(dao, serializer<Payload>())

    @Test
    fun saveThenGetPendingRoundTripsThePayload() = runTest {
        val ob = outbox()
        ob.save("bill/edit", Payload(amount = 42, note = "rent"))

        val entry = ob.getPending("bill/edit")
        assertEquals(Payload(42, "rent"), entry?.payload)
        assertEquals(SubmitOutboxStatus.PENDING, entry?.status)
    }

    @Test
    fun resavingSameFormKeyUpdatesInPlaceAndNeverDuplicates() = runTest {
        // The idempotent-upsert contract. If this regressed, every keystroke-triggered autosave would
        // enqueue another row and the user's single edit would submit N times on reconnect.
        val dao = FakeDraftDao()
        val ob = outbox(dao)

        val first = ob.save("bill/edit", Payload(1))
        val second = ob.save("bill/edit", Payload(2))

        assertEquals(first, second, "re-saving one form must reuse the existing row id")
        assertEquals(1, dao.rows.value.size, "a duplicate row means a duplicate submission")
        assertEquals(Payload(2), ob.getPending("bill/edit")?.payload, "latest payload wins")
    }

    @Test
    fun uniqueKeyGivesIndependentDraftsUnderOneFormKey() = runTest {
        val ob = outbox()
        ob.saveByUniqueKey("bill/edit", "bill-1", Payload(10))
        ob.saveByUniqueKey("bill/edit", "bill-2", Payload(20))

        assertEquals(Payload(10), ob.getPendingByUniqueKey("bill/edit", "bill-1")?.payload)
        assertEquals(Payload(20), ob.getPendingByUniqueKey("bill/edit", "bill-2")?.payload)
        assertEquals(2, ob.getAllPending().size)
    }

    @Test
    fun uniqueKeyDraftsDoNotCollideWithThePlainFormKeyDraft() = runTest {
        // The plain save() path filters on `uniqueKey IS NULL`; a keyed draft must not be picked up
        // as "the" pending draft for the form, or one bill's edit would overwrite another's.
        val ob = outbox()
        ob.save("bill/edit", Payload(1))
        ob.saveByUniqueKey("bill/edit", "bill-9", Payload(9))

        assertEquals(Payload(1), ob.getPending("bill/edit")?.payload)
        assertEquals(Payload(9), ob.getPendingByUniqueKey("bill/edit", "bill-9")?.payload)
    }

    @Test
    fun markSubmittedRemovesItFromPending() = runTest {
        val ob = outbox()
        val id = ob.save("f", Payload(1))
        ob.markSubmitted(id)

        assertNull(ob.getPending("f"), "a submitted draft is done and must not be retried")
        assertTrue(ob.getAllPending().isEmpty())
    }

    @Test
    fun markFailedRecordsTheReasonForTheResumeUi() = runTest {
        val ob = outbox()
        val dao = FakeDraftDao()
        val ob2 = outbox(dao)
        val id = ob2.save("f", Payload(1))
        ob2.markFailed(id, "402 payment required")

        val row = dao.getById(id)
        assertEquals("FAILED", row?.status)
        assertEquals("402 payment required", row?.errorMessage)
        assertTrue(ob.getAllPending().isEmpty())
    }

    @Test
    fun markRetryingIncrementsAttemptCount() = runTest {
        // attemptCount is what OfflineSubmitSyncer's RetryPolicy cap reads. If the outbox stopped
        // incrementing it, a permanently-failing payload would retry forever on every reconnect.
        val dao = FakeDraftDao()
        val ob = outbox(dao)
        val id = ob.save("f", Payload(1))
        assertEquals(0, dao.getById(id)?.attemptCount)

        ob.markRetrying(id)
        ob.markRetrying(id)

        assertEquals(2, dao.getById(id)?.attemptCount)
    }

    @Test
    fun observePendingEmitsTheCurrentDraft() = runTest {
        val ob = outbox()
        ob.save("f", Payload(7))
        assertEquals(Payload(7), ob.observePending("f").first()?.payload)
    }

    @Test
    fun observeAllByFormKeyReturnsEveryDraftForThatForm() = runTest {
        val ob = outbox()
        ob.saveByUniqueKey("f", "a", Payload(1))
        ob.saveByUniqueKey("f", "b", Payload(2))
        ob.save("other", Payload(3))

        assertEquals(2, ob.observeAllByFormKey("f").first().size)
    }

    @Test
    fun deleteByFormKeyLeavesOtherFormsIntact() = runTest {
        val ob = outbox()
        ob.save("keep", Payload(1))
        ob.save("drop", Payload(2))

        ob.deleteByFormKey("drop")

        assertNotEquals(null, ob.getPending("keep"))
        assertNull(ob.getPending("drop"))
    }

    @Test
    fun deleteAllEmptiesTheOutbox() = runTest {
        val ob = outbox()
        ob.save("a", Payload(1))
        ob.saveByUniqueKey("b", "k", Payload(2))

        ob.deleteAll()

        assertTrue(ob.getAllPending().isEmpty())
    }

    @Test
    fun aScopedOutboxReplaysOnlyItsOwnFormsRows() = runTest {
        // One table, many forms: an unscoped outbox hands its syncer every form's pending rows.
        val shared = FakeDraftDao()
        val notes = RoomSubmitOutbox(shared, String.serializer(), ownsFormKey = { it == "note" })
        val creates = RoomSubmitOutbox(shared, String.serializer(), ownsFormKey = { it.startsWith("create.") })
        notes.save("note", "a note")
        creates.save("create.group", "a group")

        assertEquals(listOf("a note"), notes.getAllPending().map { it.payload })
        assertEquals(listOf("a group"), creates.getAllPending().map { it.payload })
    }
}
