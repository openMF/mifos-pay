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

/**
 * Pure-function timing helper for `TransitionProviders.{Enter,Exit}.{pushLeft,pushRight}`.
 * Centralizing here makes the enter/exit delay-symmetry invariant directly testable
 * (see `TransitionPushSpecTest`). Companion to [SharedAxisSpec] for the M3 shared-axis
 * transitions in `core-base/ui/motion/`.
 *
 * **Invariant:** enter and exit slide phases of a push transition MUST start at the
 * same `delayMillis`. Asymmetric delays produce a visible mismatch — the new screen
 * appears to slide in for ~50ms before the old screen begins exiting.
 */
internal object TransitionPushSpec {

    /** Total push transition duration in milliseconds. */
    const val TOTAL_DURATION_MS: Int = DEFAULT_PUSH_TRANSITION_TIME_MS

    /** Slide duration for the entering screen. */
    fun enterSlideDurationMs(): Int = TOTAL_DURATION_MS

    /** Slide start delay for the entering screen. */
    @Suppress("FunctionOnlyReturningConstant")
    fun enterSlideDelayMs(): Int = 0

    /** Fade duration for the entering screen. */
    fun enterFadeDurationMs(): Int = TOTAL_DURATION_MS / 2

    /** Fade start delay for the entering screen. */
    fun enterFadeDelayMs(): Int = TOTAL_DURATION_MS / 2

    /** Slide duration for the exiting screen. */
    fun exitSlideDurationMs(): Int = TOTAL_DURATION_MS

    /**
     * Slide start delay for the exiting screen.
     *
     * Was `TOTAL_DURATION_MS / 7` (≈50ms) — asymmetric with [enterSlideDelayMs]
     * and contributed to the 2026-05-27 nav-blink. Now 0 to match enter.
     */
    @Suppress("FunctionOnlyReturningConstant")
    fun exitSlideDelayMs(): Int = 0

    /** Fade duration for the exiting screen. */
    fun exitFadeDurationMs(): Int = TOTAL_DURATION_MS / 2

    /** Fade start delay for the exiting screen. */
    @Suppress("FunctionOnlyReturningConstant")
    fun exitFadeDelayMs(): Int = 0

    /**
     * Symmetry check: are enter/exit slide delays equal? Returns true if invariant
     * holds, false otherwise.
     */
    fun isSymmetric(): Boolean =
        enterSlideDelayMs() == exitSlideDelayMs() &&
            enterSlideDurationMs() == exitSlideDurationMs()
}
