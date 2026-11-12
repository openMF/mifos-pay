/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.submit.SubmitHandler
import kpt.core.base.store.submit.SubmitState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ScreenStateMutationExtensionsTest {

    // ─── canInteract ─────────────────────────────────────────────────────────

    @Test
    fun canInteract_contentAndIdle_returnsTrue() {
        assertTrue(
            ScreenState.Content("data")
                .canInteract(SubmitState.Idle),
        )
    }

    @Test
    fun canInteract_contentAndSubmitting_returnsFalse() {
        assertFalse(
            ScreenState.Content("data")
                .canInteract(SubmitState.Submitting()),
        )
    }

    @Test
    fun canInteract_contentAndFailed_returnsTrue() {
        assertFalse(
            ScreenState.Content("data")
                .canInteract(SubmitState.Submitting()),
        )
    }

    @Test
    fun canInteract_loading_returnsFalse() {
        assertFalse(ScreenState.Loading.canInteract(SubmitState.Idle))
    }

    @Test
    fun canInteract_error_returnsFalse() {
        assertFalse(
            ScreenState.Error(RuntimeException("e"))
                .canInteract(SubmitState.Idle),
        )
    }

    @Test
    fun canInteract_noNetwork_returnsFalse() {
        assertFalse(ScreenState.NoNetwork().canInteract(SubmitState.Idle))
    }

    // ─── submitWhenContent ───────────────────────────────────────────────────

    @Test
    fun submitWhenContent_loading_noOp() = runTest {
        val handler = submitHandler<String>()
        handler.submitWhenContent(ScreenState.Loading) { "result" }
        assertEquals(SubmitState.Idle, handler.state.value)
    }

    @Test
    fun submitWhenContent_error_noOp() = runTest {
        val handler = submitHandler<String>()
        handler.submitWhenContent(ScreenState.Error(RuntimeException("e"))) { "result" }
        assertEquals(SubmitState.Idle, handler.state.value)
    }

    @Test
    fun submitWhenContent_noNetwork_noOp() = runTest {
        val handler = submitHandler<String>()
        handler.submitWhenContent(ScreenState.NoNetwork()) { "result" }
        assertEquals(SubmitState.Idle, handler.state.value)
    }

    @Test
    fun submitWhenContent_content_submitsWithExtractedData() = runTest {
        val handler = submitHandler<String>()
        handler.submitWhenContent(
            ScreenState.Content("hello"),
        ) { data -> data.uppercase() }
        testScheduler.advanceUntilIdle()
        val state = assertIs<SubmitState.Submitted<String>>(handler.state.value)
        assertEquals("HELLO", state.result)
    }

    @Test
    fun submitWhenContent_alreadySubmitting_noOp() = runTest {
        val handler = submitHandler<String>()
        handler.submit {
            kotlinx.coroutines.delay(10_000)
            "first"
        }
        assertIs<SubmitState.Submitting>(handler.state.value)

        var secondCalled = false
        handler.submitWhenContent(
            ScreenState.Content("data"),
        ) {
            secondCalled = true
            "second"
        }

        assertFalse(secondCalled)
        handler.reset()
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private fun <R> TestScope.submitHandler(): SubmitHandler<R> = SubmitHandler(this)
}
