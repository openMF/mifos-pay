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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DraftSubmitHandlerTest {

    private val formKey = "test_form"

    // ─── T1: success path ────────────────────────────────────────────────────

    @Test
    fun `submit success transitions to Submitted and does not save draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        handler.submit("payload") { "ok" }
        testScheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<String>>(handler.state.value)
        assertTrue(outbox.entries.isEmpty(), "No draft should be saved on success")
    }

    // ─── T2: network failure does NOT auto-save (user must call saveDraft) ───

    @Test
    fun `submit network failure does not auto-save draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("connect timed out") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Network, state.category)
        assertFalse(state.draftSaved, "draftSaved must be false until user confirms")
        assertNull(outbox.getPending(formKey), "Network failure must not auto-save a draft")
    }

    // ─── T3: non-network failure does NOT save draft ──────────────────────────

    @Test
    fun `submit server failure does not save draft`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        handler.submit("payload") { throw RuntimeException("HTTP 500") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Server(httpCode = 500), state.category)
        assertNull(outbox.getPending(formKey), "Server errors must not create a draft")
    }

    // ─── T4: retry after failure re-executes block ───────────────────────────

    @Test
    fun `retry after network failure re-executes the submission block`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)
        var callCount = 0

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") {
            callCount++
            throw IOException("timeout")
        }
        testScheduler.advanceUntilIdle()

        handler.retry()
        testScheduler.advanceUntilIdle()

        assertEquals(2, callCount)
    }

    // ─── T5: submit while Submitting is no-op ────────────────────────────────

    @Test
    fun `submit while already Submitting is ignored`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)
        var callCount = 0

        handler.submit("p1") {
            callCount++
            "ok"
        }
        handler.submit("p2") {
            callCount++
            "ok"
        } // should be ignored
        testScheduler.advanceUntilIdle()

        assertEquals(1, callCount, "Second submit while in-flight must be no-op")
    }

    // ─── T6: reset returns to Idle ───────────────────────────────────────────

    @Test
    fun `reset returns to Idle`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()

        handler.reset()

        assertEquals(SubmitState.Idle, handler.state.value)
    }

    // ─── T7: second network failure — no duplicate outbox rows ───────────────

    @Test
    fun `second network failure after saveDraft does not insert duplicate row`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload_v1") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()
        handler.saveDraft()
        testScheduler.advanceUntilIdle()

        handler.submit("payload_v2") { throw IOException("still offline") }
        testScheduler.advanceUntilIdle()
        handler.saveDraft()
        testScheduler.advanceUntilIdle()

        val pending = outbox.getAllPending()
        assertEquals(1, pending.size, "Idempotent upsert must not create duplicate rows")
        assertEquals("payload_v2", pending.first().payload)
    }

    // ─── T8: success after prior draft marks draft submitted ─────────────────

    @Test
    fun `success after saved draft marks the draft as SUBMITTED`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("offline") }
        testScheduler.advanceUntilIdle()
        handler.saveDraft()
        testScheduler.advanceUntilIdle()

        handler.submit("payload") { "ok" }
        testScheduler.advanceUntilIdle()

        assertIs<SubmitState.Submitted<String>>(handler.state.value)
        val entry = outbox.entries.firstOrNull { it.formKey == formKey }
        assertEquals(SubmitOutboxStatus.SUBMITTED, entry?.status)
    }

    // ─── T9: saveDraft persists payload and sets draftSaved=true ─────────────

    @Test
    fun `saveDraft saves payload to outbox and sets draftSaved true`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("my_payload") { throw IOException("no network") }
        testScheduler.advanceUntilIdle()

        handler.saveDraft()
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertTrue(state.draftSaved, "State must reflect draftSaved=true after saveDraft()")
        assertEquals("my_payload", outbox.getPending(formKey)?.payload)
    }

    // ─── T10: discardDraft resets to Idle without touching outbox ────────────

    @Test
    fun `discardDraft resets to Idle without saving to outbox`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("payload") { throw IOException("no network") }
        testScheduler.advanceUntilIdle()

        handler.discardDraft()

        assertEquals(SubmitState.Idle, handler.state.value)
        assertNull(outbox.getPending(formKey), "discardDraft must not save anything to outbox")
    }

    // ─── T11: saveDraft no-op when no prior network failure ──────────────────

    @Test
    fun `saveDraft is no-op when called without prior submit`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(outbox, formKey)

        handler.saveDraft()
        testScheduler.advanceUntilIdle()

        assertEquals(SubmitState.Idle, handler.state.value)
        assertTrue(outbox.entries.isEmpty(), "saveDraft with no payload must be a no-op")
    }

    // ─── T12: autoSaveDraft=true — network failure auto-saves immediately ────

    @Test
    fun `autoSaveDraft=true saves draft silently and sets draftSaved=true`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(
            outbox = outbox,
            formKey = formKey,
            autoSaveDraft = true,
        )

        class IOException(msg: String) : RuntimeException(msg)
        handler.submit("auto_payload") { throw IOException("no network") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Network, state.category)
        assertTrue(state.draftSaved, "autoSaveDraft=true must set draftSaved=true immediately")
        assertEquals("auto_payload", outbox.getPending(formKey)?.payload)
    }

    // ─── T13: autoSaveDraft=true — non-network failure still does NOT auto-save

    @Test
    fun `autoSaveDraft=true does not save on non-network failure`() = runTest {
        val outbox = FakeSubmitOutbox<String>()
        val handler = draftSubmitHandler<String, String>(
            outbox = outbox,
            formKey = formKey,
            autoSaveDraft = true,
        )

        handler.submit("payload") { throw RuntimeException("HTTP 500") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Server(httpCode = 500), state.category)
        assertNull(outbox.getPending(formKey), "Non-network failure must never auto-save")
    }
}
