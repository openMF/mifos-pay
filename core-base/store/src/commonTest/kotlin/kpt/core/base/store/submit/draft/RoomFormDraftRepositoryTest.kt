/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit.draft

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kpt.core.base.store.infra.impl.FakeDraftDao
import kpt.core.base.store.submit.SubmitOutboxStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomFormDraftRepositoryTest {

    @Serializable
    private data class GroupForm(val name: String, val officeId: Int)

    private val dao = FakeDraftDao()
    private val drafts = RoomFormDraftRepository(dao, listOf("create.group"))
    private val slot = formDraftSlot(null)

    private fun draft(name: String) = FormDraft(
        form = "create.group",
        title = name,
        payload = FormDraftJson.encode(GroupForm.serializer(), GroupForm(name, 1)),
        state = "{}",
    )

    @Test
    fun anOfflineDraftIsPendingSoItReplays() = runTest {
        drafts.save(slot, draft("A"))
        assertEquals(SubmitOutboxStatus.PENDING, drafts.observe("create.group", slot).first()?.status)
        assertEquals(1, dao.getAllPending().size)
    }

    @Test
    fun aRejectedDraftIsFailedWithTheReasonAndIsNotReplayed() = runTest {
        drafts.save(slot, draft("A"), failure = "Insufficient privileges")
        val saved = drafts.observe("create.group", slot).first()
        assertEquals(SubmitOutboxStatus.FAILED, saved?.status)
        assertEquals("Insufficient privileges", saved?.errorMessage)
        assertEquals(emptyList(), dao.getAllPending(), "resending what was just rejected would only fail again")
    }

    @Test
    fun savingOverAFailedDraftReplacesItRatherThanAddingASecond() = runTest {
        drafts.save(slot, draft("first"), failure = "rejected")
        drafts.save(slot, draft("second"))
        assertEquals(listOf("second"), drafts.observeAll().first().map { it.draft.title })
    }

    @Test
    fun deleteEmptiesTheSlot() = runTest {
        drafts.save(slot, draft("A"))
        drafts.delete("create.group", slot)
        assertNull(drafts.observe("create.group", slot).first())
    }

    @Test
    fun slotsKeepOwnersOfDifferentKindsApart() {
        assertEquals("new", formDraftSlot(null))
        assertEquals("client:7", formDraftSlot(7, "client"))
        assertEquals("group:7", formDraftSlot(7, "group"))
    }

    @Test
    fun aDraftPayloadDecodesWithTheFormsOwnSerializer() {
        with(FormDraftJson) {
            assertEquals(GroupForm("A", 1), draft("A").payloadAs(GroupForm.serializer()))
        }
    }

    @Test
    fun aNetworkFailureCountsAsOfflineSoTheDraftIsKeptAutomatically() {
        // The monitor had not seen the drop yet: the gateway SENT, and the request failed on the network.
        val networkFailure = kpt.core.base.store.mutation.MutationResult.Failed(
            ConnectFailure("Unable to resolve host apis.mifos.community"),
            rolledBack = false,
        )
        assertEquals(true, networkFailure.isOffline)
        assertEquals(
            kpt.core.base.store.mutation.MutationResult.Blocked(kpt.core.base.store.mutation.BlockReason.OFFLINE),
            networkFailure.offlineWhenUnsent(),
        )
        val refused = kpt.core.base.store.mutation.MutationResult.Failed(
            IllegalStateException("403 Forbidden"),
            rolledBack = false,
        )
        assertEquals(false, refused.isOffline)
        assertEquals(refused, refused.offlineWhenUnsent())
    }

    /** A connection failure by name, as the network classifier recognises it — portable to every target. */
    private class ConnectFailure(message: String) : Exception(message)
}
