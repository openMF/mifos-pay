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

/**
 * Pure-function math behind [Modifier.kptListItemEnter]. Extracted so the
 * stagger/cap rules can be unit-tested without a Compose UI test rule.
 *
 * **Contracts**:
 *  - Items outside `[0, totalAnimated)` snap into place (no animation).
 *  - Stagger delay is `index * staggerMs`, capped at [STAGGER_CAP_MS] so long lists
 *    don't accumulate seconds of delay on the last animated item.
 *  - Negative indices coerce to zero delay.
 */
internal object ListItemEnterMath {

    /** Hard cap on per-item stagger delay (ms). Anchors the worst-case animated item. */
    const val STAGGER_CAP_MS = 600L

    /**
     * Returns the delay before the item at [index] should start fading in.
     * Returns zero for items at or past [totalAnimated] (they snap in).
     */
    fun staggerDelayMs(index: Int, totalAnimated: Int, staggerMs: Int): Long = when {
        !shouldAnimate(index, totalAnimated) -> 0L
        index <= 0 -> 0L
        else -> (index.toLong() * staggerMs).coerceAtMost(STAGGER_CAP_MS)
    }

    /** Whether the item at [index] participates in the staggered entrance animation. */
    fun shouldAnimate(index: Int, totalAnimated: Int): Boolean =
        index in 0 until totalAnimated
}
