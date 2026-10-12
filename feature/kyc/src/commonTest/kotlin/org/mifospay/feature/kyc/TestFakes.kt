/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mobile-wallet/blob/master/LICENSE.md
 */
package org.mifospay.feature.kyc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.mifospay.core.common.DataState
import org.mifospay.core.data.repository.KycLevelRepository
import org.mifospay.core.datastore.UserPreferencesRepository
import org.mifospay.core.model.account.DefaultAccount
import org.mifospay.core.model.client.Client
import org.mifospay.core.model.client.UpdatedClient
import org.mifospay.core.model.instance.InterbankServer
import org.mifospay.core.model.instance.ServerInstance
import org.mifospay.core.model.kyc.KYCLevel1Details
import org.mifospay.core.model.user.UserInfo

internal class FakeKycLevelRepository : KycLevelRepository {
    private var level1Details: KYCLevel1Details? = null
    private var shouldReturnError = false

    fun setLevel1Details(details: KYCLevel1Details?) {
        level1Details = details
    }

    fun setShouldReturnError(error: Boolean) {
        shouldReturnError = error
    }

    override fun fetchKYCLevel1Details(clientId: Long): Flow<DataState<KYCLevel1Details?>> {
        return if (shouldReturnError) {
            flowOf(DataState.Error(Throwable("Network error")))
        } else {
            flowOf(DataState.Success(level1Details))
        }
    }

    override suspend fun addKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    ): DataState<String> = DataState.Success("KYC Level 1 details added successfully")

    override suspend fun updateKYCLevel1Details(
        clientId: Long,
        kycLevel1Details: KYCLevel1Details,
    ): DataState<String> = DataState.Success("KYC Level 1 details updated successfully")
}

internal class FakeUserPreferencesRepository(
    clientId: Long = 1L,
) : UserPreferencesRepository {
    override val clientId: StateFlow<Long?> = MutableStateFlow(clientId)

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
    override val selectedInstance: StateFlow<ServerInstance?> = MutableStateFlow(null)
    override val selectedInterbankInstance: StateFlow<InterbankServer?> = MutableStateFlow(null)
    override val accountExternalIds: StateFlow<Map<Long, String>> = MutableStateFlow(emptyMap())

    override suspend fun updateToken(token: String): DataState<Unit> = DataState.Success(Unit)
    override suspend fun updateUserInfo(user: UserInfo): DataState<Unit> = DataState.Success(Unit)
    override suspend fun updateClientInfo(client: Client): DataState<Unit> = DataState.Success(Unit)
    override suspend fun updateClientProfile(client: UpdatedClient): DataState<Unit> =
        DataState.Success(Unit)

    override suspend fun updateDefaultAccount(account: DefaultAccount): DataState<Unit> =
        DataState.Success(Unit)

    override suspend fun updateSelectedInstance(instance: ServerInstance): DataState<Unit> =
        DataState.Success(Unit)

    override suspend fun updateSelectedInterbankInstance(instance: InterbankServer): DataState<Unit> =
        DataState.Success(Unit)

    override suspend fun updateAccountExternalIds(
        accountExternalIds: Map<Long, String>,
    ): DataState<Unit> = DataState.Success(Unit)

    override fun getAccountExternalId(accountId: Long): String? = null
    override suspend fun logOut() {}
}
