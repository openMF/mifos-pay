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

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.common.StringProvider
import kpt.core.data.repository.PocketRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.model.payload.PocketLinkPayload
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.model.user.UserInfo
import org.jetbrains.compose.resources.StringResource
/** Small fixture-only state wrapper used to feed deterministic values into Store5-shaped streams. */
sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val exception: Throwable) : DataState<Nothing>
}

/** Returns one predictable localized value for view-model tests that do not exercise resources. */
class FakeStringProvider(private val value: String = "Unknown Account") : StringProvider {
    override suspend fun get(resource: StringResource, vararg formatArgs: Any): String = value
}

/** Supplies an isolated client and preference state without requiring persistent storage. */
class FakeUserPreferencesRepository : ForkPreferencesRepository, UserPreferencesRepository by templatePreferences() {
    private val _clientId = MutableStateFlow<Long?>(1L)
    override val clientId: StateFlow<Long?> = _clientId

    private val _selectedInstance = MutableStateFlow<ServerInstance?>(null)
    override val selectedInstance: StateFlow<ServerInstance?> = _selectedInstance

    private val _language = MutableStateFlow(kpt.core.model.user.Language.DEFAULT)
    override val language: StateFlow<kpt.core.model.user.Language> = _language

    override suspend fun setLanguage(language: kpt.core.model.user.Language) {
        _language.value = language
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
    override val authToken: String? = null
    override val defaultAccount: StateFlow<DefaultAccount?> = MutableStateFlow(null)
    override val defaultAccountId: StateFlow<Long?> = MutableStateFlow(null)
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val accountExternalIds: StateFlow<Map<Long, String>> = MutableStateFlow(emptyMap())

    override suspend fun updateToken(token: String) {}
    override suspend fun updateUserInfo(user: UserInfo) {}
    override suspend fun updateClientInfo(client: Client) {}
    override suspend fun updateClientProfile(client: UpdatedClient) {}
    override suspend fun updateDefaultAccount(account: DefaultAccount) {}
    override suspend fun updateSelectedInstance(instance: ServerInstance) {}
    override suspend fun updateSelectedInterbankInstance(instance: InterbankServer) {}
    override suspend fun updateAccountExternalIds(accountExternalIds: Map<Long, String>) {}
    override fun getAccountExternalId(accountId: Long): String? = null
    override suspend fun logOut() {}
}

/**
 * In-memory Pocket repository fake that exposes controllable Store5-style states and records
 * commands. Recorded arguments let tests verify the contract without mocking implementation code.
 */
class FakePocketRepository : PocketRepository {
    private val detailedState = MutableSharedFlow<ScreenState<List<DetailedPocketAccount>>>(replay = 1)
    private val availableState = MutableSharedFlow<ScreenState<List<LinkableAccount>>>(replay = 1)
    private val detailedPocketAccounts = screenDataStreamForTesting(
        detailedState.asSharedFlow(),
        freshness = emptyFlow(),
    )
    private val availableAccounts = screenDataStreamForTesting(
        availableState.asSharedFlow(),
        freshness = emptyFlow(),
    )

    var linkAccountsResult: DataState<Unit> = DataState.Success(Unit)
    var delinkAccountsResult: DataState<Unit> = DataState.Success(Unit)
    var detailedAccountsAfterLink: DataState<List<DetailedPocketAccount>>? = null
    var detailedAccountsAfterDelink: DataState<List<DetailedPocketAccount>>? = null
    var lastDetailedClientId: Long? = null
    var lastDetailedForceRefresh: Boolean? = null
    var lastAvailableClientId: Long? = null
    var lastLinkPayload: PocketLinkPayload? = null
    var lastExplicitlyAddedAccounts: List<DetailedPocketAccount> = emptyList()
    var lastLinkClientId: Long? = null
    var lastDelinkMappingIds: List<Long> = emptyList()
    var lastDelinkClientId: Long? = null
    var resetPocketCacheCalled: Boolean = false

    /** Publishes the next detailed-account state observed by dashboard and manage-pocket tests. */
    fun setDetailedPocketAccounts(state: DataState<List<DetailedPocketAccount>>) {
        detailedState.tryEmit(state.toScreenState())
    }

    /** Publishes the accounts eligible for linking in the link-account flow. */
    fun setAvailableAccountsToLink(state: DataState<List<LinkableAccount>>) {
        availableState.tryEmit(state.toScreenState())
    }

    override fun getLinkedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> = detailedPocketAccounts

    /** Supplies the linked accounts used by consumers that combine Pocket data with account data. */
    override fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>> = flowOf(emptyList())

    override fun getDetailedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> {
        lastDetailedClientId = clientId
        return detailedPocketAccounts
    }

    override fun getAvailableAccountsToLinkStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<LinkableAccount>> {
        lastAvailableClientId = clientId
        return availableAccounts
    }

    override suspend fun linkAccounts(
        explicitlyAddedAccounts: List<DetailedPocketAccount>,
        clientId: Long,
    ) {
        // Recreate the production payload shape so tests can verify the repository boundary.
        lastLinkPayload = PocketLinkPayload(
            explicitlyAddedAccounts.map {
                PocketLinkPayload.AccountDetail(it.pocket.accountId.toString(), it.pocket.accountType)
            },
        )
        lastExplicitlyAddedAccounts = explicitlyAddedAccounts
        lastLinkClientId = clientId
        // Emit the post-write state before returning, matching the refresh visible to the UI.
        detailedAccountsAfterLink?.let { detailedState.tryEmit(it.toScreenState()) }
        val result = linkAccountsResult
        if (result is DataState.Error) throw result.exception
    }

    override suspend fun delinkAccounts(
        pocketAccountMappingIds: List<Long>,
        clientId: Long,
    ) {
        lastDelinkMappingIds = pocketAccountMappingIds
        lastDelinkClientId = clientId
        // Emit refreshed linked accounts so successful delink tests can observe removal.
        detailedAccountsAfterDelink?.let { detailedState.tryEmit(it.toScreenState()) }
        val result = delinkAccountsResult
        if (result is DataState.Error) throw result.exception
    }

    /** Maps the compact fixture state to the same ScreenState variants used by production code. */
    private fun <T> DataState<T>.toScreenState(): ScreenState<T> = when (this) {
        DataState.Loading -> ScreenState.Loading
        is DataState.Success -> if (data is List<*> && data.isEmpty()) ScreenState.Empty else ScreenState.Content(data)
        is DataState.Error -> ScreenState.Error(exception)
    }
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
