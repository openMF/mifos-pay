/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [ProfileScreen] — the top-level shell, always rendered
 * regardless of what `profileBody` supplies (RULE-KMP-COMPOSE-UITEST-001 CU-1..CU-3).
 *
 * This fork has no demo profile body, so `profileBody` is empty here too — production wires it
 * via `cmp-navigation`'s `ForkBackboneRegistry.profileBody`. Asserts the always-present root scaffold
 * node identified by [TestTags.Profile.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class ProfileScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                ProfileScreen(profileBody = {})
            }
        }
        onNodeWithTag(TestTags.Profile.SCREEN).assertIsDisplayed()
    }
}
