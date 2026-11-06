/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.enums.AccountType
import kpt.feature.pocket.viewmodels.ManagePocketAccount
import kpt.feature.pocket.viewmodels.ManagePocketAction
import mifos_pay.feature.pocket.generated.resources.Res
import mifos_pay.feature.pocket.generated.resources.feature_pocket_link_more_accounts
import mifos_pay.feature.pocket.generated.resources.feature_pocket_no_linked_accounts
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UI coverage for the linked-account portion of the Manage Pocket screen.
 *
 * These tests render the stateless content composable directly so they verify
 * presentation and user actions without requiring navigation or a real repository.
 */
@OptIn(ExperimentalTestApi::class)
class ManagePocketScreenTest {

    /** Verifies that linked account details are visible in the normal content state. */
    @Test
    fun givenSuccessState_withLinkedAccounts_thenAccountsAreDisplayed() = runComposeUiTest {
        val linkedUiState = ScreenState.Content(
            listOf(
                ManagePocketAccount(
                    accountId = 1L,
                    name = "Vacation Savings",
                    accountNumber = "9988776655",
                    accountType = AccountType.SAVINGS,
                    mappingId = 1L,
                ),
            ),
        )

        setContent {
            ManagePocketContent(
                linkedUiState = linkedUiState,
                linkedFreshness = FreshnessSignal.initial(),
                onAction = {},
            )
        }

        onNodeWithText("Vacation Savings").assertIsDisplayed()
        onNodeWithText("9988776655").assertIsDisplayed()
    }

    /** Verifies that the empty-state call to action emits the link-account action. */
    @Test
    fun givenSuccessState_whenLinkMoreAccountsIsClicked_thenLinkActionIsEmitted() = runComposeUiTest {
        var emittedAction: ManagePocketAction? = null

        lateinit var linkMoreLabel: String
        setContent {
            linkMoreLabel = stringResource(Res.string.feature_pocket_link_more_accounts)
            ManagePocketContent(
                linkedUiState = ScreenState.Content(emptyList()),
                linkedFreshness = FreshnessSignal.initial(),
                onAction = { emittedAction = it },
            )
        }

        onNodeWithText(linkMoreLabel).performClick()

        assertEquals(ManagePocketAction.OpenLinkAccounts, emittedAction)
    }

    /** Verifies that the localized empty-state message is shown when no account is linked. */
    @Test
    fun givenSuccessState_withoutLinkedAccounts_thenEmptyMessageIsDisplayed() = runComposeUiTest {
        lateinit var emptyMessage: String
        setContent {
            emptyMessage = stringResource(Res.string.feature_pocket_no_linked_accounts)
            ManagePocketContent(
                linkedUiState = ScreenState.Content(emptyList()),
                linkedFreshness = FreshnessSignal.initial(),
                onAction = {},
            )
        }

        onNodeWithText(emptyMessage).assertIsDisplayed()
    }
}
