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

import kpt.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubmitStateExtensionsTest {

    private val idle: SubmitState<String> = SubmitState.Idle
    private val submitting: SubmitState<String> = SubmitState.Submitting()
    private val submitted: SubmitState<String> = SubmitState.Submitted("result")
    private val failed: SubmitState<String> = SubmitState.Failed(
        error = RuntimeException("oops"),
        category = ErrorCategory.Generic,
    )

    // ─── isSubmitting ────────────────────────────────────────────────────────

    @Test
    fun `isSubmitting is true only for Submitting`() {
        assertFalse(idle.isSubmitting)
        assertTrue(submitting.isSubmitting)
        assertFalse(submitted.isSubmitting)
        assertFalse(failed.isSubmitting)
    }

    // ─── isIdle ──────────────────────────────────────────────────────────────

    @Test
    fun `isIdle is true only for Idle`() {
        assertTrue(idle.isIdle)
        assertFalse(submitting.isIdle)
        assertFalse(submitted.isIdle)
        assertFalse(failed.isIdle)
    }

    // ─── isSubmitted ─────────────────────────────────────────────────────────

    @Test
    fun `isSubmitted is true only for Submitted`() {
        assertFalse(idle.isSubmitted)
        assertFalse(submitting.isSubmitted)
        assertTrue(submitted.isSubmitted)
        assertFalse(failed.isSubmitted)
    }

    // ─── isFailed ────────────────────────────────────────────────────────────

    @Test
    fun `isFailed is true only for Failed`() {
        assertFalse(idle.isFailed)
        assertFalse(submitting.isFailed)
        assertFalse(submitted.isFailed)
        assertTrue(failed.isFailed)
    }

    // ─── resultOrNull ────────────────────────────────────────────────────────

    @Test
    fun `resultOrNull returns result only for Submitted`() {
        assertNull(idle.resultOrNull)
        assertNull(submitting.resultOrNull)
        assertEquals("result", submitted.resultOrNull)
        assertNull(failed.resultOrNull)
    }

    // ─── errorOrNull ─────────────────────────────────────────────────────────

    @Test
    fun `errorOrNull returns error only for Failed`() {
        assertNull(idle.errorOrNull)
        assertNull(submitting.errorOrNull)
        assertNull(submitted.errorOrNull)
        assertEquals("oops", failed.errorOrNull?.message)
    }

    // ─── categoryOrNull ──────────────────────────────────────────────────────

    @Test
    fun `categoryOrNull returns category only for Failed`() {
        assertNull(idle.categoryOrNull)
        assertNull(submitting.categoryOrNull)
        assertNull(submitted.categoryOrNull)
        assertEquals(ErrorCategory.Generic, failed.categoryOrNull)
    }

    // ─── category variants ───────────────────────────────────────────────────

    @Test
    fun `Failed with Network category exposed correctly`() {
        val state: SubmitState<Unit> = SubmitState.Failed(
            error = RuntimeException("timeout"),
            category = ErrorCategory.Network,
        )
        assertEquals(ErrorCategory.Network, state.categoryOrNull)
        assertTrue(state.isFailed)
        assertFalse(state.isSubmitted)
    }

    @Test
    fun `Failed with Auth category exposed correctly`() {
        val state: SubmitState<Unit> = SubmitState.Failed(
            error = RuntimeException("401"),
            category = ErrorCategory.Auth,
        )
        assertEquals(ErrorCategory.Auth, state.categoryOrNull)
    }

    @Test
    fun `Failed with Server category exposed correctly`() {
        val state: SubmitState<Unit> = SubmitState.Failed(
            error = RuntimeException("500"),
            category = ErrorCategory.Server(httpCode = 500),
        )
        assertEquals(ErrorCategory.Server(httpCode = 500), state.categoryOrNull)
    }
}
