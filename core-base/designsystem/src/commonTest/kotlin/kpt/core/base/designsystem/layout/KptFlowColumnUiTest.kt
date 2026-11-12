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
 * Render tests for [KptFlowColumn], the twin of [KptFlowRow].
 *
 * It never had the crash its sibling did — it read the right list — but it shared the other defect:
 * `verticalArrangement` and `horizontalAlignment` were accepted and ignored. Tested alongside the row
 * so the pair cannot drift apart again.
 */
@OptIn(ExperimentalTestApi::class)
class KptFlowColumnUiTest {

    @Test
    fun rendersASingleColumnWithoutCrashing() = runComposeUiTest {
        setContent {
            KptFlowColumn {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(20.dp).testTag("b"))
            }
        }
        onNodeWithTag("a").assertIsDisplayed()
        onNodeWithTag("b").assertIsDisplayed()
    }

    @Test
    fun acceptsANonDefaultArrangementAndAlignment() = runComposeUiTest {
        setContent {
            KptFlowColumn(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(20.dp).testTag("a"))
                Box(Modifier.size(40.dp).testTag("b"))
            }
        }
        onNodeWithTag("a").assertIsDisplayed()
        onNodeWithTag("b").assertIsDisplayed()
    }
}
