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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

/**
 * Render tests for [KptFlowRow].
 *
 * It shipped with NO test and could not render any content: the cross-axis position loop iterated the
 * list it was filling rather than the one holding the sizes, leaving it empty, so the indexed read in
 * `layout {}` threw `IndexOutOfBoundsException` on the very first row. The composable is documented in
 * `core-base/designsystem/README.md` but never called from app code, which is why nobody hit it.
 *
 * These tests are deliberately about RENDERING AT ALL rather than about pixel positions — the defect
 * was a crash, and the cheapest guard against its return is a composition that has to succeed.
 */
@OptIn(ExperimentalTestApi::class)
class KptFlowRowUiTest {

    @Test
    fun rendersASingleRowWithoutCrashing() = runComposeUiTest {
        setContent {
            KptFlowRow {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(20.dp).testTag("b"))
            }
        }
        onNodeWithTag("a").assertIsDisplayed()
        onNodeWithTag("b").assertIsDisplayed()
    }

    @Test
    fun wrapsOntoASecondRowWithoutCrashing() = runComposeUiTest {
        // More than one row is the case that actually indexed the empty list.
        setContent {
            KptFlowRow(maxItemsInEachRow = 2) {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(20.dp).testTag("b"))
                Box(Modifier.size(20.dp).testTag("c"))
            }
        }
        onNodeWithTag("a").assertIsDisplayed()
        onNodeWithTag("c").assertIsDisplayed()
    }

    @Test
    fun acceptsANonDefaultArrangementAndAlignment() = runComposeUiTest {
        // Both parameters used to be accepted and ignored. This asserts only that passing them still
        // composes — the point is that they now reach the placement code instead of being dead.
        setContent {
            KptFlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(40.dp).testTag("b"))
            }
        }
        onNodeWithTag("a").assertIsDisplayed()
        onNodeWithTag("b").assertIsDisplayed()
    }
}
