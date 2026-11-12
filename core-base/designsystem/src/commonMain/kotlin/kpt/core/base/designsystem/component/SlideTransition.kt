/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

/**
 * Slides [content] in and out along [direction], using the shared motion durations.
 */
@Composable
fun KptSlideTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    direction: SlideDirection = SlideDirection.Left,
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(
        300,
        easing = KptAnimationSpecs.emphasizedEasing,
    ),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = when (direction) {
            SlideDirection.Left -> slideInHorizontally(animationSpec) { -it }
            SlideDirection.Right -> slideInHorizontally(animationSpec) { it }
            SlideDirection.Up -> slideInVertically(animationSpec) { -it }
            SlideDirection.Down -> slideInVertically(animationSpec) { it }
        },
        exit = when (direction) {
            SlideDirection.Left -> slideOutHorizontally(animationSpec) { -it }
            SlideDirection.Right -> slideOutHorizontally(animationSpec) { it }
            SlideDirection.Up -> slideOutVertically(animationSpec) { -it }
            SlideDirection.Down -> slideOutVertically(animationSpec) { it }
        },
        content = content,
    )
}

/**
 * Direction a [KptSlideTransition] enters from.
 */
enum class SlideDirection {
    /** Enters from the left edge. Mirrored automatically in an RTL locale. */
    Left,

    /** Enters from the right edge. Mirrored automatically in an RTL locale. */
    Right,

    /** Enters from the bottom, rising — the sheet-like arrival. */
    Up,

    /** Enters from the top, descending. */
    Down,
}
