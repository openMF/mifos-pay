/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.common.ScreenState
import kpt.core.data.repository.OfficeRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.account.Account
import kpt.core.model.account.AccountContent
import kpt.core.model.account.DefaultAccount
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.model.network.entity.Page
import kpt.core.model.network.entity.authentication.AuthenticationPayload
import kpt.core.model.network.entity.user.User
import kpt.core.model.office.Office
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.user.Language
import kpt.core.model.user.UserInfo
import kpt.core.base.store.screen.ScreenState as StoreScreenState

/**
 * Fake implementation of [OfficeRepository] for testing.
 *
 * Both read paths ([getOffices] + Phase-5 Batch-3 [getOfficesScreen]) share
 * the same in-memory `officeList` — the store-backed reader was introduced by
 * the ScreenState migration but from the test's point of view returns the
 * same content, so no separate fixture is needed. Errors surface as
 * [ScreenState.Error] on both paths.
 */
internal class FakeOfficeRepository : OfficeRepository {
    private var officeList: List<Office> = listOf(
        Office(id = 1, name = "Head Office"),
        Office(id = 5, name = "Lagos Branch"),
    )
    private var shouldReturnError = false

    fun setOfficeList(list: List<Office>) {
        officeList = list
    }

    fun setShouldReturnError(error: Boolean) {
        shouldReturnError = error
    }

    override fun getOffices(): Flow<ScreenState<List<Office>>> {
        return if (shouldReturnError) {
            flowOf(ScreenState.Error(Throwable("Network error")))
        } else {
            flowOf(ScreenState.Content(officeList))
        }
    }

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getOfficesStream(scope: CoroutineScope): ScreenDataStream<List<Office>> {
        return screenDataStreamForTesting(
            state = if (shouldReturnError) {
                flowOf(StoreScreenState.Error(Throwable("Network error")))
            } else {
                flowOf(StoreScreenState.Content(officeList))
            },
        )
    }
}

/**
 * Fake implementation of [SelfServiceRepository] for testing.
 *
 * Replaces the pre-migration `FakeBeneficiaryRepository`: after Phase-5
 * Batch-3 the [FastMpayProcessor] reads the beneficiary list through
 * [SelfServiceRepository.getBeneficiaryListScreen] (offline-first, store-
 * backed) instead of the standalone [kpt.core.data.repository.BeneficiaryRepository].
 * Only the beneficiary read + a couple of write-side stubs are exercised by
 * the fast-mpay tests; the rest of the interface returns benign defaults so
 * the compile satisfies the full interface contract.
 */
internal class FakeSelfServiceRepository : SelfServiceRepository {
    private var beneficiaryList: List<Beneficiary> = emptyList()
    private var shouldReturnError = false

    fun setBeneficiaryList(list: List<Beneficiary>) {
        beneficiaryList = list
    }

    fun setShouldReturnError(error: Boolean) {
        shouldReturnError = error
    }

    override suspend fun loginSelf(payload: AuthenticationPayload): User =
        throw UnsupportedOperationException("Not used in fast-mpay tests")

    override fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>> =
        flowOf(ScreenState.Empty)

    override suspend fun getSelfClientDetails(): Flow<ScreenState<Page<Client>>> =
        flowOf(ScreenState.Empty)

    override fun getSelfAccountTransactions(accountId: Long): Flow<List<Transaction>> =
        flowOf(emptyList())

    override suspend fun getSelfAccountTransactionFromId(
        accountId: Long,
        transactionId: Long,
    ): Flow<Transaction> =
        throw UnsupportedOperationException("Not used in fast-mpay tests")

    override fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>> =
        flowOf(ScreenState.Empty)

    override fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>> {
        return if (shouldReturnError) {
            flowOf(ScreenState.Error(Throwable("Network error")))
        } else {
            flowOf(ScreenState.Content(beneficiaryList))
        }
    }

    override fun getActiveAccountsWithTransactionsPerAccount(
        clientId: Long,
        limit: Int?,
    ): Flow<ScreenState<Map<Account, List<Transaction>>>> = flowOf(ScreenState.Empty)

    override fun getActiveAccounts(clientId: Long): Flow<ScreenState<List<Account>>> =
        flowOf(ScreenState.Empty)

    override fun getActiveAccountsWithAccountTransferTemplate(
        clientId: Long,
    ): Flow<ScreenState<List<Account>>> = flowOf(ScreenState.Empty)

    override fun getAccountsTransactions(clientId: Long): Flow<ScreenState<List<Transaction>>> =
        flowOf(ScreenState.Empty)

    override fun getTransactions(accountId: List<Long>, limit: Int?): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getTransactions(
        accountId: Long,
        limit: Int?,
    ): Flow<ScreenState<List<Transaction>>> = flowOf(ScreenState.Empty)

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getTransactionsStream(
        accountId: Long,
        limit: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Transaction>> =
        screenDataStreamForTesting(state = flowOf(StoreScreenState.Empty))

    @OptIn(ExperimentalScreenDataStreamTestingApi::class)
    override fun getBeneficiaryListStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Beneficiary>> {
        return screenDataStreamForTesting(
            state = if (shouldReturnError) {
                flowOf(StoreScreenState.Error(Throwable("Network error")))
            } else {
                flowOf(StoreScreenState.Content(beneficiaryList))
            },
        )
    }

    override fun getAccountAndBeneficiaryList(
        clientId: Long,
    ): Flow<ScreenState<AccountContent>> =
        flowOf(ScreenState.Empty)

    override fun getAccountAndBeneficiaryListScreen(
        clientId: Long,
        scope: CoroutineScope,
    ): Flow<ScreenState<AccountContent>> = flowOf(ScreenState.Empty)

    override suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload) {}

    override suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    ) {}

    override suspend fun deleteBeneficiary(beneficiaryId: Long) {}
}

/**
 * Fake implementation of [ForkPreferencesRepository] for testing.
 */
internal class FakeUserPreferencesRepository : ForkPreferencesRepository, UserPreferencesRepository by templatePreferences() {
    private val _selectedInstance = MutableStateFlow<ServerInstance?>(null)
    private val _clientId = MutableStateFlow<Long?>(0L)

    override val selectedInstance: StateFlow<ServerInstance?> = _selectedInstance

    fun setSelectedInstance(instance: ServerInstance?) {
        _selectedInstance.value = instance
    }

    fun setClientId(id: Long?) {
        _clientId.value = id
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
    override val client: StateFlow<Client?> = MutableStateFlow(null)
    override val clientId: StateFlow<Long?> = _clientId
    override val authToken: String? = null
    override val defaultAccount: StateFlow<DefaultAccount?> = MutableStateFlow(null)
    override val defaultAccountId: StateFlow<Long?> = MutableStateFlow(null)
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val accountExternalIds: StateFlow<Map<Long, String>> = MutableStateFlow(emptyMap())
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

    override fun getAccountExternalId(accountId: Long): String? = null
    override suspend fun logOut() {}
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
