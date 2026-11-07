/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-pay/blob/master/LICENSE.md
 */
package kpt.core.datastore.prefs

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.datastore.UserPreferencesDataSource
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.fork.user.Language
import kpt.core.model.fork.user.UserInfo
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
/**
 * Implementation of [ForkPreferencesRepository]. `owner: fork` — never synced.
 *
 * `by delegate` forwards every framework preference to the template's implementation, so this class
 * stays complete when the template adds one. The wallet's preferences are stored through
 * [preferenceManager] exactly as before the migration onto this seam.
 */
class ForkPreferencesRepositoryImpl(
    /** The framework-shared implementation every framework preference forwards to. */
    val delegate: UserPreferencesRepository,
    /** Unencrypted preferences — theme, language, onboarding. */
    val plainSettings: Settings,
    /** Encrypted preferences — passcode and auth token. */
    val secureSettings: Settings,
    /** Dispatcher provider for writes. */
    val dispatcher: DispatcherManager,
    /** The wallet's own store: user, client, token, accounts, server instances. */
    private val preferenceManager: UserPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    unconfinedDispatcher: CoroutineDispatcher,
) : ForkPreferencesRepository, UserPreferencesRepository by delegate {
    private val unconfinedScope = CoroutineScope(unconfinedDispatcher)

    private fun <T> Flow<T>.shared(initial: T): StateFlow<T> =
        stateIn(scope = unconfinedScope, initialValue = initial, started = SharingStarted.Eagerly)

    override val userInfo: Flow<UserInfo>
        get() = preferenceManager.userInfo.flowOn(ioDispatcher)

    override val token: StateFlow<String?>
        get() = preferenceManager.token.shared(null)

    override val client: StateFlow<Client?>
        get() = preferenceManager.clientInfo.shared(null)

    override val clientId: StateFlow<Long?>
        get() = preferenceManager.clientId.shared(null)

    // The wallet's sign-in writes this token; the framework's is the fallback.
    override val authToken: String?
        get() = preferenceManager.getAuthToken() ?: delegate.authToken

    override val defaultAccount: StateFlow<DefaultAccount?>
        get() = preferenceManager.defaultAccount.shared(null)

    override val defaultAccountId: StateFlow<Long?>
        get() = preferenceManager.defaultAccount.map { it?.accountId }.shared(null)

    override val selectedInstance: StateFlow<ServerInstance?>
        get() = preferenceManager.selectedInstance.shared(null)

    override val selectedInterbankInstance: StateFlow<InterbankServer?>
        get() = preferenceManager.selectedInterbankInstance.shared(null)

    override val accountExternalIds: StateFlow<Map<Long, String>>
        get() = preferenceManager.accountExternalIds.shared(emptyMap())

    override val language: StateFlow<Language>
        get() = preferenceManager.language.shared(Language.DEFAULT)

    override suspend fun updateDefaultAccount(account: DefaultAccount) =
        preferenceManager.updateDefaultAccount(account)

    override suspend fun updateSelectedInstance(instance: ServerInstance) =
        preferenceManager.updateSelectedInstance(instance)

    override suspend fun updateSelectedInterbankInstance(instance: InterbankServer) =
        preferenceManager.updateSelectedInterbankInstance(instance)

    override suspend fun updateAccountExternalIds(accountExternalIds: Map<Long, String>) =
        preferenceManager.updateAccountExternalIds(accountExternalIds)

    override fun getAccountExternalId(accountId: Long): String? =
        preferenceManager.getAccountExternalId(accountId)

    override suspend fun updateToken(token: String) = preferenceManager.updateAuthToken(token)

    override suspend fun updateClientInfo(client: Client) = preferenceManager.updateClientInfo(client)

    override suspend fun updateClientProfile(client: UpdatedClient) =
        preferenceManager.updateClientProfile(client)

    override suspend fun setLanguage(language: Language) = preferenceManager.setLanguage(language)

    override suspend fun updateUserInfo(user: UserInfo) = preferenceManager.updateUserInfo(user)

    // Store caches (pocket, linkable accounts, …) are drained by StoreCacheManager.clearAll() in
    // UserLogoutManagerImpl; this clears the wallet's own preferences.
    override suspend fun logOut() = preferenceManager.clearInfo()
}
