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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.pocket.AccountStatus
import kpt.feature.pocket.generated.resources.Res
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_manage
import kpt.feature.pocket.generated.resources.feature_pocket_empty_action
import kpt.feature.pocket.generated.resources.feature_pocket_error_load_accounts
import kpt.feature.pocket.viewmodels.DetailedPocket
import kpt.feature.pocket.viewmodels.PocketBuckets
import kpt.feature.pocket.viewmodels.PocketDashboardAction
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UI coverage for the Pocket dashboard content states and navigation actions.
 *
 * Rendering the content composable with explicit states makes loading-result behavior
 * deterministic and ensures that tests remain independent of networking and navigation.
 */
@OptIn(ExperimentalTestApi::class)
class PocketDashboardScreenUiTest {

    /** Verifies that balances and account groups are shown when dashboard data is available. */
    @Test
    fun givenSuccessState_whenRendering_thenDashboardShowsBalanceAndAllAccountCategories() = runComposeUiTest {
        val buckets = PocketBuckets(
            totalBalance = "MX$ 10,000.00",
            savingsAccounts = listOf(
                DetailedPocket(
                    accountId = 1L,
                    name = "Emergency Fund",
                    accountNumber = "1004859238",
                    balanceOrStatus = "$ 5,000.00",
                    status = AccountStatus.ACTIVE,
                ),
            ),
            loanAccounts = listOf(
                DetailedPocket(
                    accountId = 2L,
                    name = "Personal Loan",
                    accountNumber = "3009284756",
                    balanceOrStatus = "ACTIVE",
                    status = AccountStatus.ACTIVE,
                ),
            ),
            shareAccounts = listOf(
                DetailedPocket(
                    accountId = 3L,
                    name = "Company Shares",
                    accountNumber = "5001129384",
                    balanceOrStatus = "PENDING",
                    status = AccountStatus.PENDING,
                ),
            ),
        )

        setContent {
            PocketDashboardContent(
                state = ScreenState.Content(buckets),
                freshness = FreshnessSignal.initial(),
                onAction = {},
                onRetry = {},
            )
        }

        onNodeWithText("MX$ 10,000.00").performScrollTo().assertIsDisplayed()
        onNodeWithText("Emergency Fund").performScrollTo().assertIsDisplayed()
        onNodeWithText("1004859238").performScrollTo().assertIsDisplayed()
        onNodeWithText("Personal Loan").performScrollTo().assertIsDisplayed()
        onNodeWithText("3009284756").performScrollTo().assertIsDisplayed()
        onNodeWithText("Company Shares").performScrollTo().assertIsDisplayed()
        onNodeWithText("5001129384").performScrollTo().assertIsDisplayed()
    }

    /** Verifies that the management action is emitted when the user opens Manage Pockets. */
    @Test
    fun givenSuccessState_whenManageClicked_thenManageActionIsEmitted() = runComposeUiTest {
        var emittedAction: PocketDashboardAction? = null
        lateinit var manageLabel: String

        setContent {
            manageLabel = stringResource(Res.string.feature_pocket_dashboard_manage)
            PocketDashboardContent(
                state = ScreenState.Content(
                    PocketBuckets("MX$ 10,000.00", emptyList(), emptyList(), emptyList()),
                ),
                freshness = FreshnessSignal.initial(),
                onAction = { emittedAction = it },
                onRetry = {},
            )
        }

        onNodeWithText(manageLabel).performClick()

        assertEquals(PocketDashboardAction.ManagePocket, emittedAction)
    }

    /** Verifies that the empty dashboard directs the user to link the first account. */
    @Test
    fun givenEmptyState_whenLinkFirstAccountClicked_thenLinkActionIsEmitted() = runComposeUiTest {
        var emittedAction: PocketDashboardAction? = null

        lateinit var emptyAction: String
        setContent {
            emptyAction = stringResource(Res.string.feature_pocket_empty_action)
            PocketDashboardContent(
                state = ScreenState.Empty,
                freshness = FreshnessSignal.initial(),
                onAction = { emittedAction = it },
                onRetry = {},
            )
        }

        onNodeWithText(emptyAction).performClick()

        assertEquals(PocketDashboardAction.LinkFirstAccount, emittedAction)
    }

    /** Verifies that the localized error state is rendered when loading accounts fails. */
    @Test
    fun givenErrorState_whenRendering_thenErrorMessageIsDisplayed() = runComposeUiTest {
        lateinit var errorMessage: String
        setContent {
            errorMessage = stringResource(Res.string.feature_pocket_error_load_accounts)
            PocketDashboardContent(
                state = ScreenState.Error(IllegalStateException("load accounts")),
                freshness = FreshnessSignal.initial(),
                onAction = {},
                onRetry = {},
            )
        }

        onNodeWithText("load accounts").assertIsDisplayed()
    }
}
