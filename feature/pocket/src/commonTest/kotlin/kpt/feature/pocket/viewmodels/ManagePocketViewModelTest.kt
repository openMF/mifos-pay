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
import kpt.core.model.payload.PocketLinkPayload
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behavioral coverage for [ManagePocketViewModel].
 *
 * Each test drives the view model through its public action channel and observes state,
 * events, or repository calls. The fake repository keeps these tests deterministic while
 * still exercising the Store5-backed state mapping used by production code.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManagePocketViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferences: FakeUserPreferencesRepository
    private lateinit var repository: FakePocketRepository
    private lateinit var viewModel: ManagePocketViewModel

    /** Installs the test dispatcher and creates isolated preferences and repository fakes. */
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferences = FakeUserPreferencesRepository()
        repository = FakePocketRepository()
    }

    /** Restores the main dispatcher so this test class cannot affect other test classes. */
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Verifies that detailed repository models are mapped into the UI account model. */
    @Test
    fun givenLinkedAccounts_whenViewModelLoads_thenAccountsAreMappedToManageState() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(
            DataState.Success(
                listOf(
                    detailedAccount(
                        accountId = 101L,
                        mappingId = 201L,
                        type = AccountType.SAVINGS,
                        productName = null,
                    ),
                ),
            ),
        )
        createViewModel(backgroundScope)

        advanceUntilIdle()

        val state = viewModel.stateFlow.value
        val account = assertIs<ScreenState.Content<List<ManagePocketAccount>>>(
            viewModel.mappedLinkedAccounts.value,
        ).data.single()
        assertEquals(1L, state.clientId)
        assertEquals(101L, account.accountId)
        assertEquals(201L, account.mappingId)
        assertEquals(null, account.name)
        assertEquals("ACC-101", account.accountNumber)
    }

    /** Verifies that a repository failure is exposed as an error state for the screen. */
    @Test
    fun givenLinkedAccountsError_whenViewModelLoads_thenErrorStateIsPublished() = runTest(testDispatcher) {
        repository.setDetailedPocketAccounts(DataState.Error(Exception("network")))
        createViewModel(backgroundScope)

        advanceUntilIdle()

        assertIs<ScreenState.Error>(viewModel.mappedLinkedAccounts.value)
    }

    /** Verifies that tab and search actions update the corresponding screen state fields. */
    @Test
    fun whenTabAndSearchActionsAreHandled_thenSelectionAndQueryAreStored() = runTest(testDispatcher) {
        createViewModel(backgroundScope)

        viewModel.trySendAction(ManagePocketAction.TabSelected(AccountType.LOAN))
        viewModel.trySendAction(ManagePocketAction.SearchQueryChanged("car"))
        advanceUntilIdle()

        assertEquals(AccountType.LOAN, viewModel.stateFlow.value.selectedTab)
        assertEquals("car", viewModel.stateFlow.value.searchQuery)
    }

    /** Verifies that selecting and unselecting an account updates the stable selection key. */
    @Test
    fun whenAccountSelectionChanges_thenIdentifierIsAddedAndRemoved() = runTest(testDispatcher) {
        createViewModel(backgroundScope)

        viewModel.trySendAction(
            ManagePocketAction.AccountSelectionChanged(
                accountId = 101L,
                accountType = AccountType.SAVINGS,
                selected = true,
            ),
        )
        advanceUntilIdle()
        assertTrue("101_SAVINGS" in viewModel.stateFlow.value.selectedAccountIdentifiers)

        viewModel.trySendAction(
            ManagePocketAction.AccountSelectionChanged(
                accountId = 101L,
                accountType = AccountType.SAVINGS,
                selected = false,
            ),
        )
        advanceUntilIdle()
        assertFalse("101_SAVINGS" in viewModel.stateFlow.value.selectedAccountIdentifiers)
    }

    /** Verifies that opening the link sheet loads available accounts for the active client. */
    @Test
    fun whenOpenLinkAccountsIsHandled_thenAvailableAccountsAreLoaded() = runTest(testDispatcher) {
        repository.setAvailableAccountsToLink(
            DataState.Success(
                listOf(
                    LinkableAccount(
                        accountId = 301L,
                        productName = "Available savings",
                        accountNumber = "SAV-301",
                        accountType = AccountType.SAVINGS,
                        balance = 10.0,
                        currencyCode = "USD",
                        currencyDisplaySymbol = "$",
                        decimalPlaces = 2,
                        status = AccountStatus.ACTIVE,
                    ),
                ),
            ),
        )
        createViewModel(backgroundScope)

        viewModel.trySendAction(ManagePocketAction.OpenLinkAccounts)
        advanceUntilIdle()

        val state = viewModel.stateFlow.value
        assertEquals(ManagePocketDialogState.LinkAccounts, state.dialogState)
        val available = assertIs<ScreenState.Content<List<LinkableAccount>>>(viewModel.availableUiState.value).data
        assertEquals(1, available.size)
        assertEquals("Available savings", available.single().productName)
        assertEquals(1L, repository.lastAvailableClientId)
    }

    /** Verifies that a successful link clears transient selection state and sends the payload. */
    @Test
    fun whenLinkingSelectedAccountsSucceeds_thenSelectionIsClearedAndRepositoryReceivesPayload() =
        runTest(testDispatcher) {
            val available = LinkableAccount(
                accountId = 301L,
                productName = "Available savings",
                accountNumber = "SAV-301",
                accountType = AccountType.SAVINGS,
                balance = 10.0,
                currencyCode = "USD",
                currencyDisplaySymbol = "$",
                decimalPlaces = 2,
                status = AccountStatus.ACTIVE,
            )
            repository.setAvailableAccountsToLink(DataState.Success(listOf(available)))
            createViewModel(backgroundScope)
            advanceUntilIdle()

            viewModel.trySendAction(ManagePocketAction.OpenLinkAccounts)
            advanceUntilIdle()
            viewModel.trySendAction(
                ManagePocketAction.AccountSelectionChanged(301L, AccountType.SAVINGS, true),
            )
            advanceUntilIdle()

            viewModel.trySendAction(ManagePocketAction.LinkSelectedAccounts)
            advanceUntilIdle()

            val state = viewModel.stateFlow.value
            assertNull(state.dialogState)
            assertTrue(state.selectedAccountIdentifiers.isEmpty())
            assertEquals("", state.searchQuery)
            assertEquals(
                PocketLinkPayload.AccountDetail("301", AccountType.SAVINGS),
                repository.lastLinkPayload?.accountsDetail?.single(),
            )
            assertEquals(1L, repository.lastLinkClientId)
        }

    /** Verifies that a failed link request opens the link error dialog. */
    @Test
    fun whenLinkingSelectedAccountsFails_thenLinkErrorDialogIsShown() = runTest(testDispatcher) {
        repository.setAvailableAccountsToLink(DataState.Success(listOf(availableAccount())))
        repository.linkAccountsResult = DataState.Error(Exception("link failed"))
        createViewModel(backgroundScope)
        advanceUntilIdle()

        viewModel.trySendAction(ManagePocketAction.OpenLinkAccounts)
        advanceUntilIdle()
        viewModel.trySendAction(ManagePocketAction.AccountSelectionChanged(301L, AccountType.SAVINGS, true))
        viewModel.trySendAction(ManagePocketAction.LinkSelectedAccounts)
        advanceUntilIdle()

        assertIs<ManagePocketDialogState.Error>(viewModel.stateFlow.value.dialogState)
    }

    /** Verifies that a successful delink refreshes accounts and closes the confirmation dialog. */
    @Test
    fun whenDelinkSucceeds_thenAccountIsRemovedAndLinkedAccountsReload() = runTest(testDispatcher) {
        val account = manageAccount()
        repository.setDetailedPocketAccounts(DataState.Success(listOf(account.toDetailed())))
        repository.detailedAccountsAfterDelink = DataState.Success(emptyList())
        createViewModel(backgroundScope)
        advanceUntilIdle()

        viewModel.trySendAction(
            ManagePocketAction.OpenDelinkConfirmation(
                accountId = account.mappingId,
                accountName = requireNotNull(account.name),
                accountNumber = account.accountNumber,
            ),
        )
        viewModel.trySendAction(ManagePocketAction.DelinkAccount(account.mappingId))
        advanceUntilIdle()

        assertNull(viewModel.stateFlow.value.dialogState)
        assertIs<ScreenState.Empty>(viewModel.mappedLinkedAccounts.value)
        assertEquals(listOf(account.mappingId), repository.lastDelinkMappingIds)
        assertEquals(1L, repository.lastDelinkClientId)
    }

    /** Verifies that a failed delink request opens the delink error dialog. */
    @Test
    fun whenDelinkFails_thenDelinkErrorDialogIsShown() = runTest(testDispatcher) {
        repository.delinkAccountsResult = DataState.Error(Exception("delink failed"))
        createViewModel(backgroundScope)
        advanceUntilIdle()
        val account = manageAccount()

        viewModel.trySendAction(
            ManagePocketAction.DelinkAccount(account.mappingId),
        )
        advanceUntilIdle()

        assertIs<ManagePocketDialogState.Error>(viewModel.stateFlow.value.dialogState)
    }

    /** Verifies that the back action emits the navigation event consumed by the host screen. */
    @Test
    fun whenNavigateBackIsHandled_thenNavigateBackEventIsEmitted() = runTest(testDispatcher) {
        createViewModel(backgroundScope)

        val event = async { viewModel.eventFlow.first() }
        viewModel.trySendAction(ManagePocketAction.NavigateBack)
        assertEquals(ManagePocketEvent.NavigateBack, event.await())
    }

    /** Creates the view model and starts the flows that represent its externally visible state. */
    private fun createViewModel(scope: CoroutineScope) {
        viewModel = ManagePocketViewModel(
            pocketRepository = repository,
            userPreferencesRepository = preferences,
        )
        viewModel.linkedUiState.launchIn(scope)
        viewModel.mappedLinkedAccounts.launchIn(scope)
        viewModel.availableUiState.launchIn(scope)
        viewModel.searchResults.launchIn(scope)
    }

    private fun detailedAccount(
        accountId: Long,
        mappingId: Long,
        type: AccountType,
        productName: String?,
    ) = DetailedPocketAccount(
        pocket = PocketAccount(
            pocketId = accountId,
            id = mappingId,
            accountId = accountId,
            accountType = type,
            accountNumber = "ACC-$accountId",
        ),
        productName = productName,
        balance = 10.0,
        currencyCode = "USD",
        currencyDisplaySymbol = "$",
        decimalPlaces = 2,
        status = AccountStatus.ACTIVE,
    )

    private fun manageAccount() = ManagePocketAccount(
        accountId = 301L,
        mappingId = 401L,
        name = "Savings",
        accountNumber = "SAV-301",
        accountType = AccountType.SAVINGS,
    )

    private fun ManagePocketAccount.toDetailed() = detailedAccount(
        accountId = accountId,
        mappingId = mappingId,
        type = accountType,
        productName = name,
    )

    private fun availableAccount() = LinkableAccount(
        accountId = 301L,
        productName = "Available savings",
        accountNumber = "SAV-301",
        accountType = AccountType.SAVINGS,
        balance = 10.0,
        currencyCode = "USD",
        currencyDisplaySymbol = "$",
        decimalPlaces = 2,
        status = AccountStatus.ACTIVE,
    )
}
