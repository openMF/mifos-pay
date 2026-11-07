/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.common.ScreenState
import kpt.core.data.repository.AccountRepository
import kpt.core.data.repository.LocalAssetRepository
import kpt.core.data.repository.PocketRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.account.Account
import kpt.core.model.account.AccountTransferPayload
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.model.search.AccountResult
import kpt.core.model.user.Language
import kpt.core.model.user.UserInfo
import kpt.core.model.utils.CurrencyCode
import kpt.core.model.utils.Locale
import kpt.core.model.utils.QrCodeType
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_no_default_account
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kpt.core.base.store.screen.ScreenState as StoreScreenState

/**
 * Unit tests for [MpayQrViewModel].
 *
 * Tests cover:
 * - QR code generation with valid client and account data
 * - Error state when no default account is set
 * - Amount and currency changes
 * - Page navigation (intra-bank vs inter-bank)
 * - Dialog state management
 * - Event emission (navigate back, snackbar, etc.)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MpayQrViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeLocalAssetRepository: FakeLocalAssetRepository
    private lateinit var fakeAccountRepository: FakeAccountRepository
    private lateinit var fakePocketRepository: FakePocketRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Initialize with valid values in constructor to ensure they're available immediately
        fakeUserPreferencesRepository = FakeUserPreferencesRepository(
            initialClient = createTestClient(),
            initialDefaultAccount = createTestDefaultAccount(),
            initialSelectedInstance = createTestServerInstance(),
        )
        fakeLocalAssetRepository = FakeLocalAssetRepository()
        fakeAccountRepository = FakeAccountRepository()
        fakePocketRepository = FakePocketRepository()
        savedStateHandle = SavedStateHandle()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MpayQrViewModel {
        return MpayQrViewModel(
            localRepository = fakeLocalAssetRepository,
            repository = fakeUserPreferencesRepository,
            accountRepository = fakeAccountRepository,
            pocketRepository = fakePocketRepository,
            savedStateHandle = savedStateHandle,
            ioDispatcher = testDispatcher,
        )
    }

    // region Initialization Tests

    /**
     * TODO: This test requires investigating the ViewModel initialization timing with test dispatchers.
     * The MpayQrViewModel reads repository values during initialState construction which happens
     * before viewModelScope is fully set up with the test dispatcher.
     */
    @Test
    fun givenValidClientAndAccount_whenViewModelCreated_thenInitialStateIsLoading() = runTest {
        // Given - values are pre-set in setUp()

        // When
        val viewModel = createViewModel()

        // Then - initial state should be loading before QR generation completes
        // Note: Full QR generation test would require synchronizing test dispatcher with viewModelScope
        assertEquals(MpayQrState.ViewState.Loading, viewModel.stateFlow.value.viewState)
    }

    @Test
    fun givenNoDefaultAccount_whenViewModelCreated_thenShowsError() = runTest {
        // Given - override the pre-set default account with empty one
        fakeUserPreferencesRepository.setDefaultAccount(DefaultAccount.DEFAULT)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(
            MpayQrState.ViewState.Error(Res.string.feature_mpay_qr_no_default_account),
            viewModel.stateFlow.value.viewState,
        )
    }

    @Test
    fun givenValidData_whenViewModelCreated_thenStateHasCorrectFspId() = runTest {
        // Given - values are pre-set in setUp()

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("mifos-bank", viewModel.stateFlow.value.fspId)
    }

    // endregion

    // region Amount and Currency Tests

    @Test
    fun givenInitialState_whenAmountChanged_thenQrDataUpdated() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.trySendAction(MpayQrAction.AmountChanged("500"))
        advanceUntilIdle()

        // Then
        assertEquals("500", viewModel.stateFlow.value.qrData.amount)
    }

    @Test
    fun givenInitialState_whenCurrencyChanged_thenQrDataUpdated() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.trySendAction(MpayQrAction.CurrencyChanged("EUR"))
        advanceUntilIdle()

        // Then
        assertEquals("EUR", viewModel.stateFlow.value.qrData.currency)
    }

    // endregion

    // region Page Navigation Tests

    @Test
    fun givenInitialState_whenPageChanged_thenSelectedPageUpdated() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.stateFlow.value.selectedPage)

        // When
        viewModel.trySendAction(MpayQrAction.PageChanged(1))
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.stateFlow.value.selectedPage)
    }

    // endregion

    // region Dialog State Tests

    @Test
    fun givenNoDialog_whenShowSetAmountDialog_thenDialogStateIsShowSetAmountDialog() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.stateFlow.value.dialogState)

        // When
        viewModel.trySendAction(MpayQrAction.ShowSetAmountDialog)
        advanceUntilIdle()

        // Then
        assertEquals(MpayQrState.DialogState.ShowSetAmountDialog, viewModel.stateFlow.value.dialogState)
    }

    @Test
    fun givenDialogVisible_whenDismissDialog_thenDialogStateIsNull() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.trySendAction(MpayQrAction.ShowSetAmountDialog)
        advanceUntilIdle()
        assertEquals(MpayQrState.DialogState.ShowSetAmountDialog, viewModel.stateFlow.value.dialogState)

        // When
        viewModel.trySendAction(MpayQrAction.DismissDialog)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.stateFlow.value.dialogState)
    }

    // Note: ConfirmSetAmount test requires proper test dispatcher setup with viewModelScope
    // which is complex in KMP. The action triggers async QR regeneration that's hard to
    // observe synchronously in tests.

    // endregion

    // region Event Tests

    @Test
    fun givenAnyState_whenNavigateBack_thenOnNavigateBackEventEmitted() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.eventFlow.test {
            viewModel.trySendAction(MpayQrAction.NavigateBack)
            val event = awaitItem()
            assertIs<MpayQrEvent.OnNavigateBack>(event)
        }
    }

    @Test
    fun givenAnyState_whenCopyToClipboard_thenShowSnackbarEventEmitted() = runTest {
        // Given - values pre-set in setUp()
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.eventFlow.test {
            viewModel.trySendAction(MpayQrAction.CopyToClipboard("ACC001"))
            val event = awaitItem()
            assertIs<MpayQrEvent.ShowSnackbar>(event)
            assertEquals("ACC001", event.message)
        }
    }

    // endregion

    // region InterBank QR Data Tests

    @Test
    fun givenValidAccountExternalId_whenGetInterBankQrData_thenContainsExternalId() = runTest {
        // Given
        fakeUserPreferencesRepository.setAccountExternalIds(mapOf(456L to "EXT123456"))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("EXT123456", viewModel.stateFlow.value.accountExternalId)
        assertEquals("EXT123456", viewModel.stateFlow.value.interBankQrData.accountExternalId)
    }

    @Test
    fun givenInterBankQrData_whenGenerated_thenHasCorrectType() = runTest {
        // Given
        fakeUserPreferencesRepository.setAccountExternalIds(mapOf(456L to "EXT123456"))

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val interBankQr = viewModel.stateFlow.value.interBankQrData
        assertEquals(QrCodeType.INTER_BANK, interBankQr.type)
        assertEquals(0L, interBankQr.clientId)
        assertEquals(0L, interBankQr.accountId)
        assertEquals("", interBankQr.accountNo)
    }

    // endregion

    // region Helper Methods

    private fun createTestClient(): Client {
        return Client(
            id = 123L,
            accountNo = "CLIENT001",
            externalId = "",
            active = true,
            activationDate = emptyList(),
            firstname = "John",
            lastname = "Doe",
            displayName = "John Doe",
            mobileNo = "+1234567890",
            emailAddress = "john@example.com",
            dateOfBirth = emptyList(),
            isStaff = false,
            officeId = 1L,
            officeName = "Head Office",
            savingsProductName = "",
        )
    }

    private fun createTestDefaultAccount(): DefaultAccount {
        return DefaultAccount(
            accountId = 456L,
            accountNo = "ACC001",
        )
    }

    private fun createTestServerInstance(): ServerInstance {
        return ServerInstance(
            endpoint = "test.com",
            protocol = "https://",
            path = "/api/v1",
            platformTenantId = "mifos-bank",
            label = "Test Bank",
        )
    }

    // endregion
}

/**
 * Fake implementation of [LocalAssetRepository] for testing.
 */
private class FakeLocalAssetRepository : LocalAssetRepository {
    override val currencyList: StateFlow<List<CurrencyCode>> = MutableStateFlow(
        listOf(
            CurrencyCode("United States", "USD", "$"),
            CurrencyCode("Eurozone", "EUR", "€"),
            CurrencyCode("United Kingdom", "GBP", "£"),
            CurrencyCode("India", "INR", "₹"),
        ),
    )
    override val localeList: StateFlow<List<Locale>> = MutableStateFlow(
        listOf(
            Locale("United States", "en_US", "English"),
            Locale("India", "en_IN", "English"),
        ),
    )
}

/**
 * Fake implementation of [ForkPreferencesRepository] for testing.
 */
private class FakeUserPreferencesRepository(
    initialClient: Client? = null,
    initialDefaultAccount: DefaultAccount? = null,
    initialSelectedInstance: ServerInstance? = null,
    initialAccountExternalIds: Map<Long, String> = emptyMap(),
) : ForkPreferencesRepository, UserPreferencesRepository by templatePreferences() {
    private val _selectedInstance = MutableStateFlow(initialSelectedInstance)
    private val _client = MutableStateFlow(initialClient)
    private val _defaultAccount = MutableStateFlow(initialDefaultAccount)
    private val _accountExternalIds = MutableStateFlow(initialAccountExternalIds)

    override val selectedInstance: StateFlow<ServerInstance?> = _selectedInstance
    override val client: StateFlow<Client?> = _client
    override val defaultAccount: StateFlow<DefaultAccount?> = _defaultAccount
    override val accountExternalIds: StateFlow<Map<Long, String>> = _accountExternalIds

    fun setSelectedInstance(instance: ServerInstance?) {
        _selectedInstance.value = instance
    }

    fun setClient(client: Client?) {
        _client.value = client
    }

    fun setDefaultAccount(account: DefaultAccount?) {
        _defaultAccount.value = account
    }

    fun setAccountExternalIds(ids: Map<Long, String>) {
        _accountExternalIds.value = ids
    }

    override val userInfo: Flow<UserInfo> = flowOf(
        UserInfo(
            username = "",
            userId = 0L,
            base64EncodedAuthenticationKey = "",
            authenticated = false,
            officeId = 0,
            officeName = "",
            roles = emptyList(),
            permissions = emptyList(),
            clients = emptyList(),
            shouldRenewPassword = false,
            isTwoFactorAuthenticationRequired = false,
        ),
    )
    override val token: StateFlow<String?> = MutableStateFlow(null)
    override val clientId: StateFlow<Long?> = MutableStateFlow(null)
    override val authToken: String? = null
    override val defaultAccountId: StateFlow<Long?> = MutableStateFlow(null)
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val language: StateFlow<Language> = MutableStateFlow(Language.DEFAULT)

    override suspend fun updateToken(token: String) {}
    override suspend fun updateUserInfo(user: UserInfo) {}
    override suspend fun setLanguage(language: Language) {}
    override suspend fun updateClientInfo(client: Client) {}
    override suspend fun updateClientProfile(client: UpdatedClient) {}

    override suspend fun updateDefaultAccount(account: DefaultAccount) {}

    override suspend fun updateSelectedInstance(instance: ServerInstance) {}

    override suspend fun updateSelectedInterbankInstance(instance: InterbankServer) {}

    override suspend fun updateAccountExternalIds(
        accountExternalIds: Map<Long, String>,
    ) {}

    override fun getAccountExternalId(accountId: Long): String? =
        _accountExternalIds.value[accountId]

    override suspend fun logOut() {}
}

/**
 * Fake implementation of [AccountRepository] for testing.
 *
 * The MpayQr ViewModel only reads accounts via [getSelfAccountsScreen] (the
 * Phase-5 Batch-3 store-backed reader). The other methods are stubbed with
 * safe empty defaults — sufficient for the tests in this file and preserving
 * "still generate QR with default account" fallback (Empty state) as the
 * default emission.
 */
private class FakeAccountRepository : AccountRepository {
    override fun getTransaction(
        accountId: Long,
        transactionId: Long,
    ): Flow<ScreenState<Transaction>> = flowOf(ScreenState.Empty)

    override fun getAccountTransfer(transferId: Long): Flow<ScreenState<TransferDetail>> =
        flowOf(ScreenState.Empty)

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getAccountTransferStream(
        transferId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<TransferDetail> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override fun searchAccounts(query: String): Flow<ScreenState<List<AccountResult>>> =
        flowOf(ScreenState.Empty)

    override fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>> =
        flowOf(ScreenState.Empty)

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getSelfAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Account>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override suspend fun makeTransfer(payload: AccountTransferPayload) {}
}

private class FakePocketRepository : PocketRepository {
    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getLinkedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getDetailedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>> = flowOf(emptyList())

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getAvailableAccountsToLinkStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<LinkableAccount>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    override suspend fun linkAccounts(
        explicitlyAddedAccounts: List<DetailedPocketAccount>,
        clientId: Long,
    ) = Unit

    override suspend fun delinkAccounts(
        pocketAccountMappingIds: List<Long>,
        clientId: Long,
    ) = Unit
}

/** The template's own preferences over in-memory settings — the fake's framework half. */
private fun templatePreferences(): UserPreferencesRepository = UserPreferencesRepositoryImpl(
    plainSettings = MapSettings(),
    secureSettings = MapSettings(),
    dispatcher = object : DispatcherManager {
        override val default = Dispatchers.Unconfined
        override val main = Dispatchers.Main
        override val io = Dispatchers.Unconfined
        override val unconfined = Dispatchers.Unconfined
        override val appScope = CoroutineScope(Dispatchers.Unconfined)
    },
)
