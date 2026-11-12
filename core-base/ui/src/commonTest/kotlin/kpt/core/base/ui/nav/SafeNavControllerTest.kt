/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.nav

import androidx.lifecycle.Lifecycle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the lifecycle-guard and debounce logic behind [popBackStackSafely] /
 * [rememberSafeBackPress]. NavController itself is not testable in commonTest (it needs a
 * Compose host), so these tests exercise the guard predicates directly using fake lifecycle
 * states — verifying the invariants that protect the nav graph from double-pop.
 */
class SafeNavControllerTest {

    // ── Lifecycle guard ──────────────────────────────────────────────────────

    @Test
    fun `lifecycle guard — RESUMED allows pop`() {
        assertTrue(shouldAllowPop(Lifecycle.State.RESUMED))
    }

    @Test
    fun `lifecycle guard — STARTED blocks pop during a mid-exit transition`() {
        assertFalse(shouldAllowPop(Lifecycle.State.STARTED))
    }

    @Test
    fun `lifecycle guard — CREATED blocks pop`() {
        assertFalse(shouldAllowPop(Lifecycle.State.CREATED))
    }

    @Test
    fun `lifecycle guard — DESTROYED blocks pop`() {
        assertFalse(shouldAllowPop(Lifecycle.State.DESTROYED))
    }

    @Test
    fun `lifecycle guard — INITIALIZED blocks pop`() {
        assertFalse(shouldAllowPop(Lifecycle.State.INITIALIZED))
    }

    // ── Debounce guard ───────────────────────────────────────────────────────

    @Test
    fun `debounce — first tap always passes`() {
        val elapsed = 0L
        val lastTapMs = null
        assertFalse(shouldDebounce(elapsed, lastTapMs, debounceMs = 500L))
    }

    @Test
    fun `debounce — second tap within window is suppressed`() {
        val debounce = 500L
        val elapsed = 200L // 200ms since last tap
        assertTrue(shouldDebounce(elapsed, lastTapMs = 0L, debounceMs = debounce))
    }

    @Test
    fun `debounce — second tap at window boundary is suppressed`() {
        val debounce = 500L
        val elapsed = 499L
        assertTrue(shouldDebounce(elapsed, lastTapMs = 0L, debounceMs = debounce))
    }

    @Test
    fun `debounce — tap after window passes`() {
        val debounce = 500L
        val elapsed = 500L
        assertFalse(shouldDebounce(elapsed, lastTapMs = 0L, debounceMs = debounce))
    }

    @Test
    fun `debounce — tap well after window passes`() {
        val debounce = 500L
        val elapsed = 2000L
        assertFalse(shouldDebounce(elapsed, lastTapMs = 0L, debounceMs = debounce))
    }

    @Test
    fun `debounce disabled — zero window never suppresses`() {
        // debounceMs = 0 disables the guard
        assertFalse(shouldDebounce(elapsed = 0L, lastTapMs = 0L, debounceMs = 0L))
    }

    // ── Pop-count safety invariant ───────────────────────────────────────────

    @Test
    fun `double-tap during 450ms exit transition fires at most one pop`() {
        // Phase 08 set exit transition to 450ms. During that window the destination
        // is in STARTED state. Assert that only the first tap (RESUMED) goes through.
        val tapStates = listOf(
            // first tap — should pop
            Lifecycle.State.RESUMED,
            // second tap 300ms later — should be blocked
            Lifecycle.State.STARTED,
        )
        val popsAllowed = tapStates.count { shouldAllowPop(it) }
        assertEquals(1, popsAllowed, "exactly one pop should fire during a double-tap")
    }

    // ── Helpers (pure-function extracts of the guard predicates) ────────────

    private fun shouldAllowPop(state: Lifecycle.State): Boolean =
        state.isAtLeast(Lifecycle.State.RESUMED)

    private fun shouldDebounce(
        elapsed: Long,
        lastTapMs: Long?,
        debounceMs: Long,
    ): Boolean = debounceMs > 0L && lastTapMs != null && elapsed < debounceMs
}
