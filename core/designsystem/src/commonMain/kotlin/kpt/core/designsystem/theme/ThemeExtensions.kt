/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.core.KptShapes

/**
 * Converts the given corner values into a [RoundedCornerShape].
 *
 * This extension function provides a convenient way to create a [RoundedCornerShape]
 * using custom corner dimensions while maintaining the context of the current [KptShapes] instance.
 *
 * @param topStart The radius for the top-start corner. Defaults to 0.dp.
 * @param topEnd The radius for the top-end corner. Defaults to 0.dp.
 * @param bottomStart The radius for the bottom-start corner. Defaults to 0.dp.
 * @param bottomEnd The radius for the bottom-end corner. Defaults to 0.dp.
 *
 * @return A [RoundedCornerShape] with the specified corner sizes.
 *
 * @see KptShapes
 */

fun KptShapes.toRoundedCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
) = RoundedCornerShape(
    topStart = topStart,
    topEnd = topEnd,
    bottomStart = bottomStart,
    bottomEnd = bottomEnd,
)
