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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [KptSidebarLayout]'s two shipped defects.
 *
 * `onSidebarVisibilityChange` was accepted and never called — there was no dismiss affordance at all,
 * so an `overlay` sidebar could be opened and never closed from inside the layout. And the docked
 * branch was gated only on `sidebarVisible`, not on `!overlay`, so an overlay sidebar was composed
 * TWICE: once inset beside the content and once floating over it.
 */
@OptIn(ExperimentalTestApi::class)
class KptSidebarLayoutUiTest {

    private val panelTag = "sidebar-panel"

    @Test
    fun anOverlaySidebarIsComposedExactlyOnce() = runComposeUiTest {
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true),
                sidebarVisible = true,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
        // Two nodes here means the docked branch drew the panel as well as the overlay branch.
        onAllNodesWithTag(panelTag).assertCountEquals(1)
    }

    @Test
    fun aDockedSidebarIsComposedExactlyOnce() = runComposeUiTest {
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = false),
                sidebarVisible = true,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
        onAllNodesWithTag(panelTag).assertCountEquals(1)
    }

    @Test
    fun tappingTheScrimOfAnOverlaySidebarReportsADismissal() = runComposeUiTest {
        var reported: Boolean? = null
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true, collapsible = true),
                sidebarVisible = true,
                onSidebarVisibilityChange = { reported = it },
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
        assertNull(reported, "nothing should be reported before the gesture")
        onNodeWithTag("KptSidebarScrim").performClick()
        // The callback reports the REQUESTED state, and visibility stays hoisted: false, not a toggle.
        assertEquals(false, reported)
    }

    @Test
    fun aPinnedSidebarHasNoScrimToDismiss() = runComposeUiTest {
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true, collapsible = false),
                sidebarVisible = true,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
        onAllNodesWithTag("KptSidebarScrim").assertCountEquals(0)
    }

    @Test
    fun aHiddenSidebarComposesNeitherPanelNorScrim() = runComposeUiTest {
        setContent {
            KptSidebarLayout(
                sidebarContent = { Box(Modifier.fillMaxSize().testTag(panelTag)) },
                configuration = SidebarConfiguration(overlay = true),
                sidebarVisible = false,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
        onAllNodesWithTag(panelTag).assertCountEquals(0)
        onAllNodesWithTag("KptSidebarScrim").assertCountEquals(0)
    }
}
