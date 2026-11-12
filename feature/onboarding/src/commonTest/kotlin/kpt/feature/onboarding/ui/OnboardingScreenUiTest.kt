/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Renders [OnboardingScreenContent] over state the test drives, so each assertion is about what the user sees
 * and which intent a tap sends — the persistence itself is pinned by [OnboardingViewModelTest].
 */
@OptIn(ExperimentalTestApi::class)
class OnboardingScreenUiTest {

    @Test
    fun skipIsOfferedOnTheIntroAndSendsSkip() = runComposeUiTest {
        val sent = mutableListOf<OnboardingAction>()
        setContent { KptTheme { content(OnboardingState(), sent::add) } }

        // Title + body are merged into one announcement for screen readers, so the tag lives in the unmerged tree.
        onNodeWithTag(OnboardingTestTags.TITLE, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag(OnboardingTestTags.SKIP).performClick()

        assertEquals(listOf<OnboardingAction>(OnboardingAction.Skip), sent)
    }

    @Test
    fun thePrivacyPageStartsWithBothSwitchesOffAndNoSkip() = runComposeUiTest {
        setContent { KptTheme { content(OnboardingState(page = OnboardingPage.Privacy)) {} } }

        onNodeWithTag(OnboardingTestTags.ANALYTICS_SWITCH).assertIsOff()
        onNodeWithTag(OnboardingTestTags.CRASH_REPORTS_SWITCH).assertIsOff()
        onNodeWithTag(OnboardingTestTags.SKIP).assertDoesNotExist()
        onNodeWithTag(OnboardingTestTags.AGREEMENT).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun bothFinishingButtonsAreOfferedAndSendDistinctIntents() = runComposeUiTest {
        val sent = mutableListOf<OnboardingAction>()
        setContent { KptTheme { content(OnboardingState(page = OnboardingPage.Privacy), sent::add) } }

        // Pinned to the bottom bar: both are on screen without scrolling, so neither can be scrolled out of sight.
        onNodeWithTag(OnboardingTestTags.ALLOW_GET_STARTED).assertIsDisplayed().performClick()
        onNodeWithTag(OnboardingTestTags.CONTINUE_CHOICES).assertIsDisplayed().performClick()

        assertEquals(listOf(OnboardingAction.AllowAll, OnboardingAction.ContinueWithChoices), sent)
    }

    @Test
    fun theSecondaryButtonNamesWhatItWillDo() = runComposeUiTest {
        var state by mutableStateOf(OnboardingState(page = OnboardingPage.Privacy))
        setContent {
            KptTheme {
                content(state) { action ->
                    if (action is OnboardingAction.SetCrashReports) state = state.copy(crashReports = action.granted)
                }
            }
        }
        val secondary = onNodeWithTag(OnboardingTestTags.CONTINUE_CHOICES, useUnmergedTree = false)

        secondary.assertTextEquals("Continue without sharing")
        onNodeWithTag(OnboardingTestTags.CRASH_REPORTS_SWITCH).performScrollTo().performClick()
        onNodeWithTag(OnboardingTestTags.CRASH_REPORTS_SWITCH).assertIsOn()
        secondary.assertTextEquals("Continue with my choices")
    }

    @Test
    fun theServerLinkIsHiddenWhenTheAppHasNoServerSetup() = runComposeUiTest {
        setContent {
            KptTheme {
                OnboardingScreenContent(
                    state = OnboardingState(page = OnboardingPage.Privacy),
                    onAction = {},
                    onOpenServerConfig = null,
                    onOpenPrivacyPolicy = {},
                    onOpenTerms = {},
                )
            }
        }
        // The template default: no server-setup screen, so no link that would lead nowhere.
        onNodeWithTag(OnboardingTestTags.CONNECT_INSTANCE).assertDoesNotExist()
    }

    @Test
    fun theServerLinkOpensServerSetupWhenTheAppHasOne() = runComposeUiTest {
        var opened = 0
        setContent {
            KptTheme {
                OnboardingScreenContent(
                    state = OnboardingState(page = OnboardingPage.Privacy),
                    onAction = {},
                    onOpenServerConfig = { opened++ },
                    onOpenPrivacyPolicy = {},
                    onOpenTerms = {},
                )
            }
        }
        onNodeWithTag(OnboardingTestTags.CONNECT_INSTANCE).performScrollTo().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun aFailedSaveIsShown() = runComposeUiTest {
        setContent { KptTheme { content(OnboardingState(page = OnboardingPage.Privacy, saveFailed = true)) {} } }
        // Shown in the bottom bar, right above the buttons that retry.
        onNodeWithTag(OnboardingTestTags.SAVE_FAILED).assertIsDisplayed()
    }

    @androidx.compose.runtime.Composable
    private fun content(state: OnboardingState, onAction: (OnboardingAction) -> Unit) = OnboardingScreenContent(
        state = state,
        onAction = onAction,
        onOpenServerConfig = {},
        onOpenPrivacyPolicy = {},
        onOpenTerms = {},
    )
}
