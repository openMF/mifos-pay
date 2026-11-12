/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.home.ui.TestTags
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [HomeScreen] — the top-level bottom-nav shell, always
 * rendered regardless of what `homeBody` supplies (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * This fork has no demo dashboard (`customizer --clean` empties the `homeBody` seam), so
 * `homeBody` is empty here too — production wires it via `cmp-navigation`'s
 * `ForkBackboneRegistry.homeBody`. Asserts [TestTags.Home.SCREEN] — the root [Scaffold] testTag
 * added to [HomeScreen].
 */
@OptIn(ExperimentalTestApi::class)
class HomeScreenUiTest {

    @Test
    fun screenScaffoldIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                HomeScreen(
                    onSettingsClick = {},
                    homeBody = {},
                )
            }
        }
        onNodeWithTag(TestTags.Home.SCREEN).assertIsDisplayed()
    }
}
