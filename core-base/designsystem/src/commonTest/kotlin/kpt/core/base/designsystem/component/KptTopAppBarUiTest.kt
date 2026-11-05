/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that all four size shorthands honour `onNavigationIconClick`.
 *
 * [KptMediumTopAppBar] and [KptLargeTopAppBar] accepted the callback and discarded it, so a caller
 * asking for a back affordance got a bar with no navigation icon at all — while the Small and
 * CenterAligned shorthands, declared a few lines above them, handled it. The four now share one
 * body, which is what stops them drifting apart again.
 */
@OptIn(ExperimentalTestApi::class)
class KptTopAppBarUiTest {

    @Test
    fun smallForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptSmallTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }

    @Test
    fun centerAlignedForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptCenterAlignedTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }

    @Test
    fun mediumForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptMediumTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }

    @Test
    fun largeForwardsTheNavigationClick() = runComposeUiTest {
        var clicked = 0
        setContent { KptLargeTopAppBar(title = "T", onNavigationIconClick = { clicked++ }) }
        onNodeWithContentDescription("Navigation").performClick()
        assertEquals(1, clicked)
    }

    @Test
    fun noCallbackMeansNoNavigationIcon() = runComposeUiTest {
        setContent { KptLargeTopAppBar(title = "T") }
        onAllNodesWithContentDescription("Navigation").assertCountEquals(0)
    }
}
