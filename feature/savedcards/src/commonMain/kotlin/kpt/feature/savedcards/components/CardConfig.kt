/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared visual constants for the card widgets.
 */
object CardConfig {
    /**
     * Size of the card network logo.
     */
    val logoSize: Dp = 60.dp

    /**
     * Optional tint applied to the card logo; null keeps original colors.
     */
    val logoTint: Color? = null

    /**
     * Animation used when the card logo appears.
     */
    val logoEnterTransition: EnterTransition = fadeIn() + expandIn()

    /**
     * Animation used when the card logo disappears.
     */
    val logoExitTransition: ExitTransition = fadeOut() + shrinkOut()

    /**
     * Radius of a masked-digit dot.
     */
    val dotRadius = 4.dp

    /**
     * Horizontal gap between masked-digit dots.
     */
    val spaceBetweenDots = 8.dp

    /**
     * Horizontal gap between groups of card digits.
     */
    val spaceBetweenGroups = 16.dp
}
