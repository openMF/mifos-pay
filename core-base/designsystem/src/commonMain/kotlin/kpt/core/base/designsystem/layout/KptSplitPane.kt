/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme

/**
 * Two panes with a draggable divider, for wide windows.
 *
 * [minLeftWidth] stops the divider being dragged to a width where the left pane is unusable.
 */
@Composable
fun KptSplitPane(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialSplitRatio: Float = 0.5f,
    minLeftWidth: Dp = 200.dp,
    minRightWidth: Dp = 200.dp,
    resizable: Boolean = true,
    dividerColor: Color = KptTheme.colorScheme.outline,
    dividerWidth: Dp = 1.dp,
) {
    var splitRatio by remember {
        mutableFloatStateOf(initialSplitRatio.coerceIn(MIN_SPLIT_RATIO, 1f - MIN_SPLIT_RATIO))
    }
    var totalWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { totalWidthPx = it.width }
            .testTag("KptSplitPane"),
    ) {
        Box(
            modifier = Modifier.weight(splitRatio),
        ) {
            leftContent()
        }

        // The divider is the drag handle, so it carries the gesture rather than the Row: a
        // Row-level drag would also steal horizontal gestures from the panes' own content.
        //
        // A hairline divider is a 1px hit target, which no finger can acquire — so when the pane is
        // resizable the handle occupies a real touch target and the hairline is drawn centred inside
        // it. A pinned divider keeps its hairline footprint, having nothing to grab.
        if (resizable) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(maxOf(dividerWidth, DividerTouchTargetWidth))
                    .testTag("KptSplitPaneDivider")
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            // Until the first layout pass totalWidthPx is 0; dividing by it would
                            // yield an infinite ratio, so a drag before measurement is dropped.
                            if (totalWidthPx > 0) {
                                // The BOUNDS are clamped into the legal band before they are used,
                                // not the result afterwards: a minimum wider than the container
                                // yields a ratio above 1, and the midpoint of two such bounds is
                                // out of range too. Clamping here makes every value below provably
                                // inside (0, 1), which is what Row's weight() demands.
                                val lo = (with(density) { minLeftWidth.toPx() } / totalWidthPx)
                                    .coerceIn(MIN_SPLIT_RATIO, 1f - MIN_SPLIT_RATIO)
                                val hi = (1f - with(density) { minRightWidth.toPx() } / totalWidthPx)
                                    .coerceIn(MIN_SPLIT_RATIO, 1f - MIN_SPLIT_RATIO)
                                // The two minimums can together exceed the available width, leaving
                                // the lower bound above the upper one — which coerceIn rejects.
                                splitRatio = if (lo >= hi) {
                                    (lo + hi) / 2f
                                } else {
                                    (splitRatio + delta / totalWidthPx).coerceIn(lo, hi)
                                }
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                VerticalDivider(thickness = dividerWidth, color = dividerColor)
            }
        } else {
            VerticalDivider(
                thickness = dividerWidth,
                color = dividerColor,
                modifier = Modifier.testTag("KptSplitPaneDivider"),
            )
        }

        Box(
            modifier = Modifier.weight(1f - splitRatio),
        ) {
            rightContent()
        }
    }
}

/**
 * Narrowest share of the width either pane may be reduced to.
 *
 * `Row`'s `weight()` throws on a non-positive weight, so a pane can never be collapsed to nothing —
 * not by a drag, and not by an `initialSplitRatio` of 0f or 1f, which crashed on first composition
 * before this floor existed. A layout that genuinely needs one pane hidden should stop composing it
 * rather than give it zero width.
 */
private const val MIN_SPLIT_RATIO = 0.02f

/**
 * Width of [KptSplitPane]'s drag handle when the pane is resizable.
 *
 * 24.dp rather than Material's 48.dp minimum: the handle sits between two panes, so the touch slop
 * either side of it is usable space, and a 48.dp gap reads as a column of its own.
 */
private val DividerTouchTargetWidth = 24.dp
