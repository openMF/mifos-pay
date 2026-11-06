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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.enums.AccountType
import kpt.feature.pocket.viewmodels.ManagePocketAccount
import kpt.feature.pocket.viewmodels.ManagePocketAction
import mifos_pay.feature.pocket.generated.resources.Res
import mifos_pay.feature.pocket.generated.resources.feature_pocket_action_cancel
import mifos_pay.feature.pocket.generated.resources.feature_pocket_action_remove
import mifos_pay.feature.pocket.generated.resources.feature_pocket_delink_account_message
import mifos_pay.feature.pocket.generated.resources.feature_pocket_remove_account_detail
import mifos_pay.feature.pocket.generated.resources.feature_pocket_remove_account_title
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI coverage for removing an account from the Manage Pocket screen.
 *
 * The tests cover both transitions into the confirmation sheet and the two possible
 * user decisions, while keeping callbacks observable through the screen action model.
 */
@OptIn(ExperimentalTestApi::class)
class DelinkAccountsTest {

    /** Provides a linked account with a mapping ID, which is the ID required for delinking. */
    private fun linkedAccount() = ManagePocketAccount(
        accountId = 1L,
        name = "Vacation Savings",
        accountNumber = "9988776655",
        accountType = AccountType.SAVINGS,
        mappingId = 1L,
    )

    /** Verifies that tapping the remove affordance opens confirmation for the right account. */
    @Test
    fun givenLinkedAccount_whenRemoveIsClicked_thenDelinkConfirmationIsOpened() = runComposeUiTest {
        var emittedAction: ManagePocketAction? = null

        lateinit var removeLabel: String
        setContent {
            removeLabel = stringResource(Res.string.feature_pocket_action_remove)
            ManagePocketContent(
                linkedUiState = ScreenState.Content(listOf(linkedAccount())),
                linkedFreshness = FreshnessSignal.initial(),
                onAction = { emittedAction = it },
            )
        }

        onNodeWithContentDescription(removeLabel).performClick()

        assertTrue(emittedAction is ManagePocketAction.OpenDelinkConfirmation)
        assertEquals(
            1L,
            (emittedAction as ManagePocketAction.OpenDelinkConfirmation).accountId,
        )
    }

    /** Verifies that the confirmation sheet exposes localized details and both actions. */
    @Test
    fun givenDelinkConfirmationState_whenRendering_thenAccountDetailsAndActionsAreDisplayed() = runComposeUiTest {
        val account = linkedAccount()

        lateinit var title: String
        lateinit var detail: String
        lateinit var message: String
        lateinit var cancel: String
        lateinit var remove: String
        setContent {
            title = stringResource(Res.string.feature_pocket_remove_account_title)
            detail = stringResource(Res.string.feature_pocket_remove_account_detail, account.accountNumber)
            message = stringResource(Res.string.feature_pocket_delink_account_message)
            cancel = stringResource(Res.string.feature_pocket_action_cancel)
            remove = stringResource(Res.string.feature_pocket_action_remove)
            RemoveLinkedAccountSheet(requireNotNull(account.name), account.accountNumber, {}, {})
        }

        onNodeWithText(title).assertIsDisplayed()
        onNodeWithText("Vacation Savings").assertIsDisplayed()
        onNodeWithText(detail).assertIsDisplayed()
        onNodeWithText(message).assertIsDisplayed()
        onNodeWithText(cancel).assertIsDisplayed()
        onNodeWithText(remove).assertIsDisplayed()
    }

    /** Verifies that confirming removal emits the mapping ID used by the repository operation. */
    @Test
    fun whenRemoveIsConfirmed_thenDelinkAccountActionIsEmitted() = runComposeUiTest {
        val account = linkedAccount()
        var emittedAction: ManagePocketAction? = null

        setContent {
            RemoveLinkedAccountSheet(
                accountName = requireNotNull(account.name),
                accountNumber = account.accountNumber,
                onCancelClick = { emittedAction = ManagePocketAction.DismissDialog },
                onRemoveClick = { emittedAction = ManagePocketAction.DelinkAccount(account.mappingId) },
            )
        }

        onNodeWithText("Remove").performClick()

        assertTrue(emittedAction is ManagePocketAction.DelinkAccount)
        assertEquals(account.mappingId, (emittedAction as ManagePocketAction.DelinkAccount).mappingId)
    }

    /** Verifies that cancellation dismisses the confirmation without requesting a removal. */
    @Test
    fun whenDelinkConfirmationIsCancelled_thenDismissDialogActionIsEmitted() = runComposeUiTest {
        var emittedAction: ManagePocketAction? = null
        val account = linkedAccount()

        setContent {
            RemoveLinkedAccountSheet(
                accountName = requireNotNull(account.name),
                accountNumber = account.accountNumber,
                onCancelClick = { emittedAction = ManagePocketAction.DismissDialog },
                onRemoveClick = { emittedAction = ManagePocketAction.DelinkAccount(account.mappingId) },
            )
        }

        onNodeWithText("Cancel").performClick()

        assertTrue(emittedAction is ManagePocketAction.DismissDialog)
    }
}
