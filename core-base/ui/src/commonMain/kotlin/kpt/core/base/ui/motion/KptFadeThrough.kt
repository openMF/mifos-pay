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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kpt.core.base.designsystem.theme.Motion
import kpt.core.base.designsystem.theme.motion

/**
 * Fade-through enter/exit factories. Use for **sibling navigation** —
 * bottom-nav tab switches, settings section switches, paged tabs. The outgoing
 * surface fades to nothing before the incoming surface fades in, so there's
 * no overlap of two competing UIs.
 *
 * Two API surfaces (same as [KptSharedAxis]): `@Composable` no-arg variants for
 * `@Composable` scopes, and `fun(motion: Motion)` variants for non-Composable lambdas
 * (provider definitions).
 *
 * Sequence: exit fades to 0 over `durationShort4`, then enter fades to 1
 * over `durationMedium2` after a `durationShort4` delay.
 */
object KptFadeThrough {

    /** Fade-through enter using the ambient theme motion. Composable — call from composition. */
    @Composable
    fun enter(): EnterTransition = enter(MaterialTheme.motion)

    /** Fade-through enter using an explicit [motion], so it is callable outside composition. */
    fun enter(motion: Motion): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = motion.durationMedium2,
            // No delay: old screen fades out while new screen fades in simultaneously
            // (crossfade). The previous sequential-fade (delay = durationShort4) created
            // a ~200ms window where both screens were at alpha≈0, showing the background
            // as a dark blink on every screen-open.
            delayMillis = 0,
            easing = motion.easingStandard,
        ),
    )

    /** Fade-through exit using the ambient theme motion. */
    @Composable
    fun exit(): ExitTransition = exit(MaterialTheme.motion)

    /** Fade-through exit using an explicit [motion]. */
    fun exit(motion: Motion): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = motion.durationShort4,
            easing = motion.easingStandard,
        ),
    )
}
