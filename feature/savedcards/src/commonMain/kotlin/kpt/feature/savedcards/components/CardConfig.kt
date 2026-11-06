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

object CardConfig {
    val logoSize: Dp = 60.dp
    val logoTint: Color? = null
    val logoEnterTransition: EnterTransition = fadeIn() + expandIn()
    val logoExitTransition: ExitTransition = fadeOut() + shrinkOut()

    val dotRadius = 4.dp
    val spaceBetweenDots = 8.dp
    val spaceBetweenGroups = 16.dp
}
