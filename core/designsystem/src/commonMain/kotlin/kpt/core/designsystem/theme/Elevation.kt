/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared elevation tier scale — five named tiers matching Material 3 elevation guidance.
 *
 * Access from any Composable via [MaterialTheme.elevation]. Sub-plan 02's [androidx.compose.material3.Card]
 * variants pick tiers from this scale rather than hardcoded `.dp` values.
 *
 * | Tier | Dp | Use case |
 * |------|----|----------|
 * | [resting] | 0 | Surface-level cards, filled cards, tonal cards |
 * | [low] | 1 | Standard elevated cards (M3 default) |
 * | [medium] | 3 | Hover state, pressed state, highlighted card |
 * | [high] | 6 | Floating action button, snackbar |
 * | [dragging] | 12 | Drag-and-drop visual state |
 *
 * @property resting Elevation of surface-level cards (0dp).
 * @property low Elevation of standard elevated cards (1dp).
 * @property medium Elevation for hover, pressed or highlighted states (3dp).
 * @property high Elevation for floating action buttons and snackbars (6dp).
 * @property dragging Elevation while an item is being dragged (12dp).
 */
@Immutable
data class Elevation(
    val resting: Dp = 0.dp,
    val low: Dp = 1.dp,
    val medium: Dp = 3.dp,
    val high: Dp = 6.dp,
    val dragging: Dp = 12.dp,
)

/** Composition local providing the active [Elevation] scale. */
val LocalElevation = staticCompositionLocalOf { Elevation() }

/** Resolve the active [Elevation] tier scale from composition. */
val MaterialTheme.elevation: Elevation
    @Composable
    @ReadOnlyComposable
    get() = LocalElevation.current
