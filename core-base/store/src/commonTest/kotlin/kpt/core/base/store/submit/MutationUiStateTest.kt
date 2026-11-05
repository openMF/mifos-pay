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

import kpt.core.base.store.screen.ScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MutationUiStateTest {

    // ─── defaults ────────────────────────────────────────────────────────────

    @Test
    fun defaults_screenIsLoading_submitIsIdle() {
        val state = MutationUiState<String, Unit>()
        assertEquals(ScreenState.Loading, state.screen)
        assertEquals(SubmitState.Idle, state.submit)
    }

    // ─── canInteract ─────────────────────────────────────────────────────────

    @Test
    fun canInteract_contentAndIdle_returnsTrue() {
        val state = MutationUiState(
            screen = ScreenState.Content("data"),
            submit = SubmitState.Idle,
        )
        assertTrue(state.canInteract)
    }

    @Test
    fun canInteract_contentAndSubmitting_returnsFalse() {
        val state = MutationUiState(
            screen = ScreenState.Content("data"),
            submit = SubmitState.Submitting(),
        )
        assertFalse(state.canInteract)
    }

    @Test
    fun canInteract_loading_returnsFalse() {
        assertFalse(MutationUiState<String, Unit>(screen = ScreenState.Loading).canInteract)
    }

    @Test
    fun canInteract_error_returnsFalse() {
        assertFalse(
            MutationUiState<String, Unit>(
                screen = ScreenState.Error(RuntimeException("e")),
            ).canInteract,
        )
    }

    // ─── isSubmitting ─────────────────────────────────────────────────────────

    @Test
    fun isSubmitting_submitting_returnsTrue() {
        val state = MutationUiState<String, Unit>(submit = SubmitState.Submitting())
        assertTrue(state.isSubmitting)
    }

    @Test
    fun isSubmitting_idle_returnsFalse() {
        assertFalse(MutationUiState<String, Unit>().isSubmitting)
    }

    // ─── dataOrNull ───────────────────────────────────────────────────────────

    @Test
    fun dataOrNull_content_returnsData() {
        val state = MutationUiState(
            screen = ScreenState.Content("hello"),
            submit = SubmitState.Idle,
        )
        assertEquals("hello", state.dataOrNull)
    }

    @Test
    fun dataOrNull_loading_returnsNull() {
        assertNull(MutationUiState<String, Unit>().dataOrNull)
    }
}
