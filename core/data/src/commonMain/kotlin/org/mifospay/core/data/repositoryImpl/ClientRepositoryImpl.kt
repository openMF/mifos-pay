/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.client.ClientDetailKey
import org.mifospay.core.common.ScreenState
import org.mifospay.core.common.asScreenStateFlow
import org.mifospay.core.data.mapper.toAccount
import org.mifospay.core.data.mapper.toEntity
import org.mifospay.core.data.mapper.toModel
import org.mifospay.core.data.repository.ClientRepository
import org.mifospay.core.model.account.Account
import org.mifospay.core.model.client.Client
import org.mifospay.core.model.client.NewClient
import org.mifospay.core.model.client.UpdatedClient
import org.mifospay.core.model.network.entity.Page
import org.mifospay.core.model.network.entity.client.ClientAccountsEntity
import org.mifospay.core.network.FineractApiManager
import org.mifospay.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Store
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

class ClientRepositoryImpl(
    private val apiManager: SelfServiceApiManager,
    private val fineractApiManager: FineractApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    // Phase-5 Batch-2 SINGLE-ROW-PER-KEY wiring — injected by RepositoryModule so
    // the store-backed getClientInfoScreen(...) can consume Store5 via
    // asScreenStream(...). Nullable-default so existing unit tests without the
    // store harness continue to compile; getClientInfo(...) — the legacy
    // asScreenStateFlow path — is unaffected.
    private val clientDetailStore: Store<ClientDetailKey, Client>? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
) : ClientRepository {
    override suspend fun getClients(): Flow<ScreenState<Page<Client>>> {
        return apiManager.clientsApi.clients()
            .map { it.toModel() }
            .asScreenStateFlow(isEmpty = { it.pageItems.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override fun getClientInfo(clientId: Long): Flow<ScreenState<Client>> {
        return apiManager.clientsApi
            .getClient(clientId)
            .map { it.toModel() }
            .asScreenStateFlow()
            .flowOn(ioDispatcher)
    }

    // Phase-5 Batch-2 SINGLE-ROW-PER-KEY read — GOAL D13 (`createStore` + CACHE_FIRST_SWR).
    //
    // See the interface KDoc for the shape contract. Requires the three store-adapter
    // dependencies (clientDetailStore + NetworkMonitor + FetchedAtRepository).
    // If any is null (test wiring), we IllegalState — production DI in
    // RepositoryModule wires all three unconditionally.
    override fun getClientInfoStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<Client> {
        val store = checkNotNull(clientDetailStore) {
            "getClientInfoStream requires the `clientDetail` Store5 wiring. Verify " +
                "RepositoryModule bound AppStoreRegistry.ClientDetail and injected it here."
        }
        val netMon = checkNotNull(storeNetworkMonitor) {
            "getClientInfoStream requires kmptoolkit NetworkMonitor. Verify DataModule bound it."
        }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) {
            "getClientInfoStream requires FetchedAtRepository. Verify DataModule bound it."
        }
        return store.asScreenStream(
            key = ClientDetailKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.ClientDetail.of(clientId),
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.CLIENT_DETAIL,
        )
    }

    override suspend fun getClient(clientId: Long): Client {
        return withContext(ioDispatcher) {
            apiManager.clientsApi.getClientForId(clientId).toModel()
        }
    }

    override suspend fun updateClient(clientId: Long, client: UpdatedClient) {
        withContext(ioDispatcher) {
            fineractApiManager.clientsApi.updateClient(clientId, client.toEntity())
        }
    }

    override fun getClientImage(clientId: Long): Flow<ScreenState<String>> {
        return apiManager.clientsApi
            .getClientImage(clientId)
            .asScreenStateFlow(isEmpty = { it.isBlank() })
            .flowOn(ioDispatcher)
    }

    override suspend fun updateClientImage(clientId: Long, image: String) {
        withContext(ioDispatcher) {
            fineractApiManager.clientsApi.updateClientImage(
                clientId = clientId,
                typedFile = "data:image/png;base64,$image",
            )
        }
    }

    override suspend fun getClientAccounts(clientId: Long): ClientAccountsEntity {
        return apiManager.clientsApi
            .getClientAccounts(clientId)
    }

    override suspend fun getAccounts(
        clientId: Long,
        accountType: String,
    ): Flow<ScreenState<List<Account>>> {
        return apiManager.clientsApi
            .getAccounts(clientId, accountType)
            .map { it.toAccount() }
            .asScreenStateFlow(isEmpty = { it.isEmpty() })
            .flowOn(ioDispatcher)
    }

    override suspend fun createClient(newClient: NewClient): Int {
        return withContext(ioDispatcher) {
            fineractApiManager.clientsApi.createClient(newClient.toEntity()).clientId
        }
    }

    override suspend fun deleteClient(clientId: Int): Int {
        return withContext(ioDispatcher) {
            fineractApiManager.clientsApi.deleteClient(clientId).clientId
        }
    }
}
