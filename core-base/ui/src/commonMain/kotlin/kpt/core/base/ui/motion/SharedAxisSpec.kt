/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.motion

import kpt.core.base.designsystem.theme.Motion

/**
 * Pure-function duration tokens for shared-axis-X transitions. Centralizing here
 * makes the [KptSharedAxis] enter/exit symmetry invariant directly testable
 * (see `SharedAxisSpecTest`).
 *
 * **Invariant (enforced by [isSymmetric]):** for any [Motion], the enter and exit
 * phases of a single navigation event MUST use identical slide and fade durations.
 * Asymmetric durations produce a visible "blink" — the older screen vanishes
 * before the new screen finishes appearing.
 */
internal object SharedAxisSpec {

    /** Slide duration for the entering screen during a forward push. */
    fun enterForwardSlideMs(motion: Motion): Int = motion.durationLong1

    /** Fade duration for the entering screen during a forward push. */
    fun enterForwardFadeMs(motion: Motion): Int = motion.durationLong1

    /** Slide duration for the exiting screen during a forward push. */
    fun exitForwardSlideMs(motion: Motion): Int = motion.durationLong1

    /** Fade duration for the exiting screen during a forward push. */
    fun exitForwardFadeMs(motion: Motion): Int = motion.durationLong1

    /** Slide duration for the entering screen during a back/pop. */
    fun enterBackSlideMs(motion: Motion): Int = motion.durationLong1

    /** Fade duration for the entering screen during a back/pop. */
    fun enterBackFadeMs(motion: Motion): Int = motion.durationLong1

    /** Slide duration for the exiting screen during a back/pop. */
    fun exitBackSlideMs(motion: Motion): Int = motion.durationLong1

    /** Fade duration for the exiting screen during a back/pop. */
    fun exitBackFadeMs(motion: Motion): Int = motion.durationLong1

    /**
     * Symmetry check: are enter/exit slide and fade durations equal for both
     * forward push and back/pop? Returns true if invariant holds, false otherwise.
     */
    fun isSymmetric(motion: Motion): Boolean =
        enterForwardSlideMs(motion) == exitForwardSlideMs(motion) &&
            enterForwardFadeMs(motion) == exitForwardFadeMs(motion) &&
            enterBackSlideMs(motion) == exitBackSlideMs(motion) &&
            enterBackFadeMs(motion) == exitBackFadeMs(motion)
}
