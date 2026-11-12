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
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.LinkableAccount
import kpt.feature.pocket.generated.resources.Res
import kpt.feature.pocket.generated.resources.feature_pocket_link_selected
import kpt.feature.pocket.viewmodels.ManagePocketAction
import kpt.feature.pocket.viewmodels.ManagePocketDialogState
import kpt.feature.pocket.viewmodels.ManagePocketState
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI coverage for selecting accounts in the Link Accounts sheet.
 *
 * The sheet receives its state and available accounts as inputs; the tests therefore
 * focus on what a contributor can observe and on the actions emitted by user input.
 */
@OptIn(ExperimentalTestApi::class)
class LinkAccountsUiTest {
    /** Keeps the fixture representative of a real savings account available for linking. */
    private fun account() = LinkableAccount(
        101L,
        "New Savings",
        "1234567890",
        AccountType.SAVINGS,
        0.0,
        "USD",
        2,
        AccountStatus.ACTIVE,
    )

    /** Verifies that an available account is rendered with its product name and number. */
    @Test
    fun givenLinkAccountsState_thenAvailableAccountsAreDisplayed() = runComposeUiTest {
        val state = ManagePocketState(dialogState = ManagePocketDialogState.LinkAccounts)
        val available = ScreenState.Content(listOf(account()))
        setContent { LinkAccountsSheet(state, available, listOf(account()), {}) }
        onNodeWithText("New Savings").assertIsDisplayed()
        onNodeWithText("1234567890").assertIsDisplayed()
    }

    /** Verifies that selecting an account emits the identifier and selected state to the parent. */
    @Test
    fun whenAvailableAccountIsClicked_thenSelectionActionIsEmitted() = runComposeUiTest {
        var emittedAction: ManagePocketAction? = null
        val state = ManagePocketState(dialogState = ManagePocketDialogState.LinkAccounts)
        val available = ScreenState.Content(listOf(account()))
        setContent { LinkAccountsSheet(state, available, listOf(account()), { emittedAction = it }) }
        onNodeWithText("New Savings").performClick()
        assertTrue(emittedAction is ManagePocketAction.AccountSelectionChanged)
        val action = emittedAction as ManagePocketAction.AccountSelectionChanged
        assertEquals(101L, action.accountId)
        assertEquals(AccountType.SAVINGS, action.accountType)
        assertTrue(action.selected)
    }

    /** Verifies that submitting the current selection emits the link command. */
    @Test
    fun whenSelectedAccountsAreSubmitted_thenLinkActionIsEmitted() = runComposeUiTest {
        var emittedAction: ManagePocketAction? = null
        val state = ManagePocketState(
            dialogState = ManagePocketDialogState.LinkAccounts,
            selectedAccountIdentifiers = setOf("101_SAVINGS"),
        )
        val available = ScreenState.Content(listOf(account()))
        lateinit var linkSelected: String
        setContent {
            linkSelected = stringResource(Res.string.feature_pocket_link_selected, 1)
            LinkAccountsSheet(state, available, listOf(account()), { emittedAction = it })
        }
        onNodeWithText(linkSelected).performClick()
        assertEquals(ManagePocketAction.LinkSelectedAccounts, emittedAction)
    }
}
