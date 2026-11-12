/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.PocketAccount
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

/**
 * Behavioral coverage for [PocketDashboardViewModel].
 *
 * The fake repository supplies each Store5 state explicitly, allowing the tests to verify
 * state mapping, refresh behavior, account categorization, and navigation independently.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PocketDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferences: FakeUserPreferencesRepository
    private lateinit var repository: FakePocketRepository
    private lateinit var viewModel: PocketDashboardViewModel

    /** Installs the controlled dispatcher and resets the test dependencies. */
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferences = FakeUserPreferencesRepository()
        repository = FakePocketRepository()
    }

    /** Restores the main dispatcher after each test. */
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Verifies that an empty account result becomes the dashboard empty state. */
    @Test
    fun givenEmptyRepository_whenViewModelLoads_thenEmptyStateIsPublished() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Success(emptyList()))
        createViewModel(backgroundScope)

        advanceUntilIdle()

        assertIs<ScreenState.Empty>(viewModel.uiState.value)
        assertEquals(1L, viewModel.stateFlow.value.clientId)
    }

    /** Verifies that an in-flight repository result becomes the dashboard loading state. */
    @Test
    fun givenRepositoryLoading_whenViewModelLoads_thenLoadingStateIsPublished() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Loading)
        createViewModel(backgroundScope)

        advanceUntilIdle()

        assertIs<ScreenState.Loading>(viewModel.uiState.value)
    }

    /** Verifies that a repository failure becomes the dashboard error state. */
    @Test
    fun givenRepositoryError_whenViewModelLoads_thenErrorStateIsPublished() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Error(Exception("network")))
        createViewModel(backgroundScope)

        advanceUntilIdle()

        assertIs<ScreenState.Error>(viewModel.uiState.value)
    }

    /** Verifies account categorization, total calculation, and handling of incomplete data. */
    @Test
    fun givenAccountsOfEachType_whenViewModelLoads_thenAccountsAreCategorizedAndTotalIsFormatted() =
        runTest(testDispatcher) {
            repository.setDetailedPocketAccounts(
                DataState.Success(
                    listOf(
                        detailedAccount(101L, AccountType.SAVINGS, "Savings", 100.0),
                        detailedAccount(102L, AccountType.LOAN, "Loan", 50.0),
                        detailedAccount(103L, AccountType.SHARE, "Shares", 25.0),
                        detailedAccount(
                            accountId = 104L,
                            type = AccountType.SAVINGS,
                            productName = null,
                            balance = null,
                            status = AccountStatus.PENDING,
                        ),
                    ),
                ),
            )
            createViewModel(backgroundScope)

            advanceUntilIdle()

            val buckets = assertIs<ScreenState.Content<PocketBuckets>>(viewModel.uiState.value).data
            assertEquals(listOf(101L, 104L), buckets.savingsAccounts.map { it.accountId })
            assertEquals(listOf(102L), buckets.loanAccounts.map { it.accountId })
            assertEquals(listOf(103L), buckets.shareAccounts.map { it.accountId })
            assertEquals("USD $175.00", buckets.totalBalance)
            assertEquals("PENDING", buckets.savingsAccounts[1].balanceOrStatus)
            assertEquals(null, buckets.savingsAccounts[1].name)
        }

    /** Verifies that retry requests force a fresh repository read after an initial failure. */
    @Test
    fun whenRetryIsClicked_thenForceRefreshLoadsTheLatestState() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Error(Exception("first attempt")))
        createViewModel(backgroundScope)
        advanceUntilIdle()

        repository.setDetailedPocketAccounts(DataState.Success(emptyList()))
        viewModel.trySendAction(PocketDashboardAction.Retry)
        advanceUntilIdle()

        assertIs<ScreenState.Empty>(viewModel.uiState.value)
        assertEquals(1L, repository.lastDetailedClientId)
    }

    /** Verifies that totals remain separated when accounts use different currencies. */
    @Test
    fun givenMultipleCurrencies_whenViewModelLoads_thenTotalIsFormattedPerCurrency() =
        runTest(testDispatcher) {
            repository.setDetailedPocketAccounts(
                DataState.Success(
                    listOf(
                        detailedAccount(
                            accountId = 101L,
                            type = AccountType.SAVINGS,
                            productName = "US savings",
                            balance = 100.0,
                        ),
                        detailedAccount(
                            accountId = 102L,
                            type = AccountType.LOAN,
                            productName = "Euro loan",
                            balance = 50.0,
                        ).copy(
                            currencyCode = "EUR",
                            currencyDisplaySymbol = "€",
                        ),
                    ),
                ),
            )
            createViewModel(backgroundScope)

            advanceUntilIdle()

            val buckets = assertIs<ScreenState.Content<PocketBuckets>>(viewModel.uiState.value).data
            assertEquals("USD $100.00\nEUR €50.00", buckets.totalBalance)
        }

    /** Verifies that refresh completion clears the visible refreshing indicator. */
    @Test
    fun whenRefreshIsClicked_thenRefreshingStateIsClearedAfterSuccess() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Success(emptyList()))
        createViewModel(backgroundScope)
        advanceUntilIdle()

        repository.setDetailedPocketAccounts(DataState.Loading)
        viewModel.trySendAction(PocketDashboardAction.Refresh)
        advanceUntilIdle()
        repository.setDetailedPocketAccounts(DataState.Success(emptyList()))
        advanceUntilIdle()

        assertFalse(viewModel.freshness.value.isRefreshing)
        assertEquals(1L, repository.lastDetailedClientId)
    }

    /** Verifies that each supported navigation action emits its matching dashboard event. */
    @Test
    fun whenNavigationActionsAreHandled_thenExpectedEventsAreEmitted() = runTest(testDispatcher) {
        createViewModel(backgroundScope)

        suspend fun assertEvent(action: PocketDashboardAction, expected: PocketDashboardEvent) {
            val event = async { viewModel.eventFlow.first() }
            viewModel.trySendAction(action)
            assertEquals(expected, event.await())
        }
        assertEvent(PocketDashboardAction.NavigateBack, PocketDashboardEvent.NavigateBack)
        assertEvent(PocketDashboardAction.ManagePocket, PocketDashboardEvent.ManagePocket)
        assertEvent(PocketDashboardAction.LinkFirstAccount, PocketDashboardEvent.ManagePocket)
        assertEvent(
            PocketDashboardAction.NavigateToLoanDetail(10L),
            PocketDashboardEvent.NavigateToLoanDetail(10L),
        )
        assertEvent(
            PocketDashboardAction.NavigateToSavingsDetail(11L),
            PocketDashboardEvent.NavigateToSavingsDetail(11L),
        )
        assertEvent(
            PocketDashboardAction.NavigateToShareDetail(12L),
            PocketDashboardEvent.NavigateToShareDetail(12L),
        )
    }

    /** Creates the view model and collects its state flows so actions can be processed. */
    private fun createViewModel(scope: CoroutineScope) {
        viewModel = PocketDashboardViewModel(
            pocketRepository = repository,
            userPreferencesRepository = preferences,
        )
        viewModel.uiState.launchIn(scope)
        viewModel.freshness.launchIn(scope)
    }

    private fun detailedAccount(
        accountId: Long,
        type: AccountType,
        productName: String?,
        balance: Double?,
        status: AccountStatus = AccountStatus.ACTIVE,
    ) = DetailedPocketAccount(
        pocket = PocketAccount(
            pocketId = accountId,
            id = accountId,
            accountId = accountId,
            accountType = type,
            accountNumber = "ACC-$accountId",
        ),
        productName = productName,
        balance = balance,
        currencyCode = "USD",
        currencyDisplaySymbol = "$",
        decimalPlaces = 2,
        status = status,
    )
}
