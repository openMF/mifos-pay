/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [KptSplitPane]'s divider drag.
 *
 * It documented "a draggable divider" and shipped with no gesture code at all: `splitRatio` was a
 * `remember` that nothing ever wrote, so the pane was fixed at `initialSplitRatio` and `resizable`,
 * `minLeftWidth` and `minRightWidth` were three parameters that could not affect anything.
 */
@OptIn(ExperimentalTestApi::class)
class KptSplitPaneUiTest {

    private fun leftWidth(test: androidx.compose.ui.test.ComposeUiTest): Int =
        test.onNodeWithTag("left").fetchSemanticsNode().size.width

    @Test
    fun draggingTheDividerMovesTheSplit() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                minLeftWidth = 0.dp,
                minRightWidth = 0.dp,
            )
        }
        val before = leftWidth(this)
        onNodeWithTag("KptSplitPaneDivider").performTouchInput {
            down(center)
            moveBy(Offset(200f, 0f))
            up()
        }
        waitForIdle()
        assertTrue(leftWidth(this) > before, "a rightward drag should widen the left pane")
    }

    @Test
    fun aNonResizablePaneIgnoresTheDrag() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                resizable = false,
            )
        }
        val before = leftWidth(this)
        onNodeWithTag("KptSplitPaneDivider").performTouchInput {
            down(center)
            moveBy(Offset(200f, 0f))
            up()
        }
        waitForIdle()
        assertEquals(before, leftWidth(this), "resizable = false must pin the split")
    }

    @Test
    fun theLeftMinimumStopsTheDrag() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                // A minimum well above the resting half-width, so any leftward drag must be refused.
                minLeftWidth = 10_000.dp,
                minRightWidth = 0.dp,
            )
        }
        onNodeWithTag("KptSplitPaneDivider").performTouchInput {
            down(center)
            moveBy(Offset(-200f, 0f))
            up()
        }
        waitForIdle()
        // Clamped, not collapsed: a pane dragged past its minimum used to have no floor at all.
        assertTrue(leftWidth(this) > 0, "the left pane must not be collapsed past its minimum")
    }

    @Test
    fun aResizableDividerOffersAGrabbableTouchTarget() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
            )
        }
        // A hairline divider is a 1px hit target: the drag worked but no finger could start it, so
        // a plain `swipeRight()` across the node travelled 1px and never crossed touch slop.
        val handle = onNodeWithTag("KptSplitPaneDivider").fetchSemanticsNode().size.width
        assertTrue(handle >= 24, "resizable handle should be a real touch target, was ${handle}px")
    }

    @Test
    fun aSwipeAcrossTheHandleIsEnoughToResize() = runComposeUiTest {
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                minLeftWidth = 0.dp,
                minRightWidth = 0.dp,
            )
        }
        val before = leftWidth(this)
        onNodeWithTag("KptSplitPaneDivider").performTouchInput { swipeRight() }
        waitForIdle()
        assertTrue(leftWidth(this) > before, "the handle must be wide enough for an ordinary swipe")
    }

    @Test
    fun anExtremeInitialRatioDoesNotCrashOnFirstComposition() = runComposeUiTest {
        // Row's weight() throws on a non-positive weight, so 0f and 1f crashed before composition
        // reached any content — no drag involved.
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                initialSplitRatio = 0f,
            )
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize()) },
                rightContent = { Box(Modifier.fillMaxSize()) },
                initialSplitRatio = 1f,
            )
        }
        assertTrue(leftWidth(this) > 0, "a 0f ratio must still leave the left pane composable")
    }

    @Test
    fun bothMinimumsWiderThanTheContainerStillResolves() = runComposeUiTest {
        // Together these demand more room than exists, so the lower clamp bound lands above the
        // upper one — the case that made coerceIn throw.
        setContent {
            KptSplitPane(
                leftContent = { Box(Modifier.fillMaxSize().testTag("left")) },
                rightContent = { Box(Modifier.fillMaxSize().testTag("right")) },
                minLeftWidth = 9_000.dp,
                minRightWidth = 9_000.dp,
            )
        }
        onNodeWithTag("KptSplitPaneDivider").performTouchInput {
            down(center)
            moveBy(Offset(120f, 0f))
            up()
        }
        waitForIdle()
        assertTrue(leftWidth(this) > 0, "an unsatisfiable pair of minimums must not collapse a pane")
    }
}
