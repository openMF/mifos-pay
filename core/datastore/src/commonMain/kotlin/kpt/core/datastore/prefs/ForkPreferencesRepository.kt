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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.client.UpdatedClient
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import kpt.core.model.user.Language
import kpt.core.model.user.UserInfo
/**
 * The wallet's preferences, extending the framework's [UserPreferencesRepository].
 *
 * Framework preferences (theme, language config, passcode, biometrics, onboarding, consent) are
 * inherited and delegated to the template's implementation; the wallet's own — signed-in user,
 * client, token, default account, server instances — are declared here. One injected type gives
 * every consumer both. `owner: fork`: a template sync never copies this file.
 */
interface ForkPreferencesRepository : UserPreferencesRepository {
    /** The signed-in self-service user. */
    val userInfo: Flow<UserInfo>

    /** The session's base64 authentication key, or null when signed out. */
    val token: StateFlow<String?>

    /** The signed-in user's client record. */
    val client: StateFlow<Client?>

    /** The signed-in user's client id. */
    val clientId: StateFlow<Long?>

    /** Declared by the framework too; the wallet's sign-in token is the session's token. */
    override val authToken: String?

    /** The account payments default to. */
    val defaultAccount: StateFlow<DefaultAccount?>

    /** The id of [defaultAccount]. */
    val defaultAccountId: StateFlow<Long?>

    /** The Fineract instance the app is connected to. */
    val selectedInstance: StateFlow<ServerInstance?>

    /** The interbank (payment hub) instance the app is connected to. */
    val selectedInterbankInstance: StateFlow<InterbankServer?>

    /** External ids of the user's accounts, by account id. */
    val accountExternalIds: StateFlow<Map<Long, String>>

    /** The wallet's display language. */
    val language: StateFlow<Language>

    /** Stores the session token after sign-in. */
    suspend fun updateToken(token: String)

    /** Stores the signed-in user. */
    suspend fun updateUserInfo(user: UserInfo)

    /** Stores the wallet's display language. */
    suspend fun setLanguage(language: Language)

    /** Stores the signed-in user's client record. */
    suspend fun updateClientInfo(client: Client)

    /** Applies a profile edit to the stored client record. */
    suspend fun updateClientProfile(client: UpdatedClient)

    /** Stores the account payments default to. */
    suspend fun updateDefaultAccount(account: DefaultAccount)

    /** Stores the Fineract instance to connect to. */
    suspend fun updateSelectedInstance(instance: ServerInstance)

    /** Stores the interbank instance to connect to. */
    suspend fun updateSelectedInterbankInstance(instance: InterbankServer)

    /** Stores the external ids of the user's accounts. */
    suspend fun updateAccountExternalIds(accountExternalIds: Map<Long, String>)

    /** The external id of [accountId], or null when unknown. */
    fun getAccountExternalId(accountId: Long): String?

    /** Clears the wallet's preferences on sign-out. */
    suspend fun logOut()
}
