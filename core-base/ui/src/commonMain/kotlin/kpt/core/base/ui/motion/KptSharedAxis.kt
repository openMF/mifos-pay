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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kpt.core.base.designsystem.theme.Motion
import kpt.core.base.designsystem.theme.motion

/**
 * Shared-axis-X enter/exit factories, per Material Motion. Use for
 * **forward/back navigation in a single stack** — pushing onto the stack
 * slides left-and-fades, popping reverses.
 *
 * Two API surfaces:
 *  - `@Composable` no-arg variants (`enterForward()` etc.) — read `MaterialTheme.motion`
 *    inline. Use from `@Composable` scopes (e.g. nav-graph builders).
 *  - `fun(motion: Motion)` variants — take Motion explicitly. Use from non-`@Composable`
 *    contexts (e.g. `NonNullEnterTransitionProvider` lambdas).
 *
 * Token flow: `MaterialTheme.motion.{durationLong1, durationShort4, easingEmphasized,
 * easingStandard, sharedAxisSlideDistance}` — change those once and every shared-axis
 * surface in the app shifts.
 */
object KptSharedAxis {

    /** Forward push — new screen slides in from the right, old screen slides out left. */
    @Composable
    fun enterForward(): EnterTransition = enterForward(MaterialTheme.motion)

    /** Shared-axis enter for moving forward/deeper in a flow. */
    fun enterForward(motion: Motion): EnterTransition {
        val slide = motion.sharedAxisSlideDistance
        return slideInHorizontally(
            animationSpec = tween(SharedAxisSpec.enterForwardSlideMs(motion), easing = motion.easingEmphasized),
            initialOffsetX = { fullWidth -> (slide.value.toInt()).coerceAtMost(fullWidth) },
        ) + fadeIn(animationSpec = tween(SharedAxisSpec.enterForwardFadeMs(motion), easing = motion.easingStandard))
    }

    /** Forward push — companion exit (the popped/replaced screen). */
    @Composable
    fun exitForward(): ExitTransition = exitForward(MaterialTheme.motion)

    /** Shared-axis exit for moving forward/deeper in a flow. */
    fun exitForward(motion: Motion): ExitTransition {
        val slide = motion.sharedAxisSlideDistance
        return slideOutHorizontally(
            animationSpec = tween(SharedAxisSpec.exitForwardSlideMs(motion), easing = motion.easingEmphasized),
            targetOffsetX = { fullWidth -> -(slide.value.toInt().coerceAtMost(fullWidth)) },
        ) + fadeOut(animationSpec = tween(SharedAxisSpec.exitForwardFadeMs(motion), easing = motion.easingStandard))
    }

    /** Back/pop — new screen slides in from the left, mirroring [enterForward]. */
    @Composable
    fun enterBack(): EnterTransition = enterBack(MaterialTheme.motion)

    /** Shared-axis enter for moving back. */
    fun enterBack(motion: Motion): EnterTransition {
        val slide = motion.sharedAxisSlideDistance
        return slideInHorizontally(
            animationSpec = tween(SharedAxisSpec.enterBackSlideMs(motion), easing = motion.easingEmphasized),
            initialOffsetX = { fullWidth -> -(slide.value.toInt().coerceAtMost(fullWidth)) },
        ) + fadeIn(animationSpec = tween(SharedAxisSpec.enterBackFadeMs(motion), easing = motion.easingStandard))
    }

    /** Back/pop — companion exit (the screen being popped). */
    @Composable
    fun exitBack(): ExitTransition = exitBack(MaterialTheme.motion)

    /** Shared-axis exit for moving back. */
    fun exitBack(motion: Motion): ExitTransition {
        val slide = motion.sharedAxisSlideDistance
        return slideOutHorizontally(
            animationSpec = tween(SharedAxisSpec.exitBackSlideMs(motion), easing = motion.easingEmphasized),
            targetOffsetX = { fullWidth -> (slide.value.toInt().coerceAtMost(fullWidth)) },
        ) + fadeOut(animationSpec = tween(SharedAxisSpec.exitBackFadeMs(motion), easing = motion.easingStandard))
    }
}
