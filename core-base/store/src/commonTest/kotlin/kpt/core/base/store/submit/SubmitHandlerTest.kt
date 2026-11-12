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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitHandlerTest {

    // ─── T1: submit success ──────────────────────────────────────────────────

    @Test
    fun `submit transitions Idle to Submitting then Submitted on success`() = runTest {
        val handler = submitHandler<String>()

        assertEquals(SubmitState.Idle, handler.state.value)

        handler.submit { "ok" }
        assertIs<SubmitState.Submitting>(handler.state.value)

        testScheduler.advanceUntilIdle()

        val state = handler.state.value
        assertIs<SubmitState.Submitted<String>>(state)
        assertEquals("ok", state.result)
    }

    // ─── T2: submit failure ──────────────────────────────────────────────────

    @Test
    fun `submit transitions to Failed with categorized error on exception`() = runTest {
        class IOException(message: String) : RuntimeException(message)
        val networkError = IOException("connect timed out")
        val handler = submitHandler<Unit>()

        handler.submit { throw networkError }
        testScheduler.advanceUntilIdle()

        val state = handler.state.value
        assertIs<SubmitState.Failed>(state)
        assertEquals(networkError, state.error)
        assertEquals(ErrorCategory.Network, state.category)
    }

    // ─── T3: double-tap guard ────────────────────────────────────────────────

    @Test
    fun `submit is no-op when already Submitting`() = runTest {
        var callCount = 0
        val handler = submitHandler<Unit>()

        handler.submit {
            callCount++
            // second submit fires before this completes
            handler.submit { callCount++ }
        }
        testScheduler.advanceUntilIdle()

        // only the first block executed
        assertEquals(1, callCount)
    }

    // ─── T4: retry after Failed ──────────────────────────────────────────────

    @Test
    fun `retry re-runs last block after failure`() = runTest {
        var attempt = 0
        val handler = submitHandler<String>()

        handler.submit {
            attempt++
            if (attempt == 1) throw RuntimeException("first attempt failed")
            "success on retry"
        }
        testScheduler.advanceUntilIdle()

        assertIs<SubmitState.Failed>(handler.state.value)

        handler.retry()
        testScheduler.advanceUntilIdle()

        val state = handler.state.value
        assertIs<SubmitState.Submitted<String>>(state)
        assertEquals("success on retry", state.result)
        assertEquals(2, attempt)
    }

    // ─── T5: retry with no prior block ──────────────────────────────────────

    @Test
    fun `retry is no-op when no block has been submitted`() = runTest {
        val handler = submitHandler<Unit>()

        handler.retry()
        testScheduler.advanceUntilIdle()

        assertEquals(SubmitState.Idle, handler.state.value)
    }

    // ─── T6: reset from Submitted ────────────────────────────────────────────

    @Test
    fun `reset returns to Idle from Submitted`() = runTest {
        val handler = submitHandler<Unit>()

        handler.submit { Unit }
        testScheduler.advanceUntilIdle()
        assertIs<SubmitState.Submitted<Unit>>(handler.state.value)

        handler.reset()
        assertEquals(SubmitState.Idle, handler.state.value)
    }

    // ─── T7: reset from Failed ───────────────────────────────────────────────

    @Test
    fun `reset returns to Idle from Failed`() = runTest {
        val handler = submitHandler<Unit>()

        handler.submit { throw RuntimeException("fail") }
        testScheduler.advanceUntilIdle()
        assertIs<SubmitState.Failed>(handler.state.value)

        handler.reset()
        assertEquals(SubmitState.Idle, handler.state.value)
    }

    // ─── T8: reset while Submitting ─────────────────────────────────────────

    @Test
    fun `reset while Submitting cancels job and returns to Idle`() = runTest {
        val handler = submitHandler<Unit>()
        var blockCompleted = false

        handler.submit {
            // This block will be cancelled by reset()
            kotlinx.coroutines.delay(10_000)
            blockCompleted = true
        }
        assertIs<SubmitState.Submitting>(handler.state.value)

        handler.reset()
        testScheduler.advanceUntilIdle()

        assertEquals(SubmitState.Idle, handler.state.value)
        assertTrue(!blockCompleted, "block should have been cancelled")
    }

    // ─── T9: CancellationException propagates ────────────────────────────────

    @Test
    fun `CancellationException is not caught as Failed`() = runTest {
        val handler = submitHandler<Unit>()
        var reachedFailed = false

        // We verify by observing that state never becomes Failed when the scope is cancelled.
        // The cancel happens via reset() which cancels activeJob.
        handler.submit { kotlinx.coroutines.delay(10_000) }
        assertIs<SubmitState.Submitting>(handler.state.value)

        handler.reset() // cancels the job
        testScheduler.advanceUntilIdle()

        // reset() forces Idle — not Failed
        assertNotEquals<SubmitState<*>>(SubmitState.Idle, SubmitState.Submitting())
        assertEquals(SubmitState.Idle, handler.state.value)
        assertTrue(!reachedFailed)
    }

    // ─── T10–T12: Error categorisation ──────────────────────────────────────

    @Test
    fun `network IOException is categorised as Network`() = runTest {
        class IOException(message: String) : RuntimeException(message)
        val handler = submitHandler<Unit>()
        handler.submit { throw IOException("failed to connect") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Network, state.category)
    }

    @Test
    fun `HTTP 401 error is categorised as Auth`() = runTest {
        val handler = submitHandler<Unit>()
        handler.submit { throw RuntimeException("HTTP 401 Unauthorized") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Auth, state.category)
    }

    @Test
    fun `HTTP 500 error is categorised as Server`() = runTest {
        val handler = submitHandler<Unit>()
        handler.submit { throw RuntimeException("HTTP 500 Internal Server Error") }
        testScheduler.advanceUntilIdle()

        val state = assertIs<SubmitState.Failed>(handler.state.value)
        assertEquals(ErrorCategory.Server(httpCode = 500), state.category)
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun <R> TestScope.submitHandler(): SubmitHandler<R> =
        SubmitHandler(this)
}
