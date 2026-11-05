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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kpt.core.base.designsystem.theme.motion

/**
 * Staggered fade + translate-Y enter modifier for `LazyColumn` / `LazyRow` items.
 *
 * Items at indices `[0, totalAnimated)` (default cap: 20) stagger their entrance
 * by `MaterialTheme.motion.listItemEnterStaggerMs` per index — first 20 items
 * cascade in, rest snap into place. This protects long lists from accumulating
 * delays that would feel sluggish.
 *
 * Usage:
 * ```
 * LazyColumn {
 *     itemsIndexed(loans) { index, loan ->
 *         LoanRowCard(
 *             loan,
 *             modifier = Modifier.kptListItemEnter(index),
 *         )
 *     }
 * }
 * ```
 *
 * @param index Item position in the list.
 * @param totalAnimated Maximum number of items that animate; defaults to
 *                      `MaterialTheme.motion.listItemEnterMaxAnimated`.
 */
fun Modifier.kptListItemEnter(
    index: Int,
    totalAnimated: Int? = null,
): Modifier = composed {
    val motion = MaterialTheme.motion
    val cap = totalAnimated ?: motion.listItemEnterMaxAnimated
    val animate = ListItemEnterMath.shouldAnimate(index, cap)

    var visible by remember { mutableStateOf(!animate) }
    LaunchedEffect(Unit) {
        if (animate) {
            val staggerMs = ListItemEnterMath.staggerDelayMs(index, cap, motion.listItemEnterStaggerMs)
            if (staggerMs > 0L) delay(staggerMs)
            visible = true
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(motion.durationMedium2, easing = motion.easingStandard),
        label = "kptListItemEnterAlpha",
    )
    val translateY by animateDpAsState(
        targetValue = if (visible) 0.dp else 16.dp,
        animationSpec = tween(motion.durationMedium2, easing = motion.easingDecelerated),
        label = "kptListItemEnterTranslate",
    )

    this
        .alpha(animatedAlpha)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, translateY.roundToPx())
            }
        }
}
