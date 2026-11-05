/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Asserts the delay-symmetry invariant for push transitions. The original
 * `Transition.kt` exit path delayed the slide by ~50ms (totalDuration/7)
 * while the enter slide started immediately — producing a visible mismatch
 * during navigation pushes.
 */
class TransitionPushSpecTest {

    @Test
    fun enterAndExitSlideDelaysMustBeEqual() {
        assertEquals(
            TransitionPushSpec.enterSlideDelayMs(),
            TransitionPushSpec.exitSlideDelayMs(),
            "enter and exit slide delays must be equal — asymmetry causes " +
                "the new screen to appear before the old one starts exiting",
        )
    }

    @Test
    fun enterAndExitSlideDurationsMustBeEqual() {
        assertEquals(
            TransitionPushSpec.enterSlideDurationMs(),
            TransitionPushSpec.exitSlideDurationMs(),
            "enter and exit slide durations must be equal",
        )
    }

    @Test
    fun exitSlideHasNoDelay() {
        assertEquals(
            0,
            TransitionPushSpec.exitSlideDelayMs(),
            "exit slide delay must be 0 — non-zero delay was the 2026-05-27 push asymmetry bug",
        )
    }

    @Test
    fun isSymmetricReturnsTrueForDefaultSpec() {
        assertTrue(
            TransitionPushSpec.isSymmetric(),
            "TransitionPushSpec must be symmetric at all times",
        )
    }
}
