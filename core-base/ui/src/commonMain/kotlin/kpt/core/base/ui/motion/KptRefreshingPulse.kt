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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kpt.core.base.designsystem.theme.motion

/**
 * Infinite scale + alpha "breath" while [active] is true. Use to signal
 * **"this surface is fetching live data right now"** — freshness dots,
 * inline refresh badges, etc.
 *
 * When `active = false`, the modifier is identity-transformed (no scale,
 * full alpha) — switching to `false` smoothly returns to rest because the
 * surrounding `animateFloat` decays.
 *
 * Lifecycle-scoped: when the composable leaves composition (off-screen via
 * LazyColumn unloading), the `rememberInfiniteTransition` is disposed —
 * no off-screen CPU burn.
 *
 * Duration comes from `MaterialTheme.motion.refreshingPulseDurationMs` —
 * change once, every pulse in the app shifts.
 */
fun Modifier.kptRefreshingPulse(active: Boolean): Modifier = composed {
    val motion = MaterialTheme.motion
    val transition = rememberInfiniteTransition(label = "kptRefreshingPulse")
    val scale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (active) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.refreshingPulseDurationMs, easing = motion.easingStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "kptRefreshingPulseScale",
    )
    val alpha by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (active) 0.6f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.refreshingPulseDurationMs, easing = motion.easingStandard),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "kptRefreshingPulseAlpha",
    )

    this.graphicsLayer {
        if (active) {
            this.scaleX = scale
            this.scaleY = scale
            this.alpha = alpha
        }
    }
}
