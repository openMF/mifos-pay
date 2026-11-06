/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.repository.PocketRepository
import kpt.core.database.wallet.pocket.PocketDao
import kpt.core.database.wallet.pocket.toDomain
import kpt.core.database.wallet.pocket.toPendingLinkEntity
import kpt.core.model.network.entity.pocket.PocketDelinkRequest
import kpt.core.model.network.entity.pocket.PocketLinkRequest
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.wallet.WalletStoreTtl
import kpt.core.store.wallet.linkableaccount.LinkableAccountKey
import kpt.core.store.wallet.pocket.PocketKey
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock
import kpt.core.base.data.infra.NetworkMonitor as StoreNetworkMonitor

class PocketRepositoryImp(
    private val dataManager: SelfServiceApiManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val pocketStore: Store<PocketKey, List<DetailedPocketAccount>>? = null,
    private val linkableAccountsStore: Store<
        LinkableAccountKey,
        List<LinkableAccount>,
        >? = null,
    private val storeNetworkMonitor: StoreNetworkMonitor? = null,
    private val fetchedAtRepository: FetchedAtRepository? = null,
    private val pocketDao: PocketDao? = null,
) : PocketRepository {

    /**
     * Retrieves the detailed pocket accounts.
     *
     * **FetchPolicy Decision**: Uses `NETWORK_ONLY` because this endpoint fetches highly
     * granular, ephemeral data (specific ledger transactions) which we do not want to serve stale.
     * Caching this would risk showing outdated financial transactions.
     */
    override fun getDetailedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> {
        val store = checkNotNull(pocketStore) { "Requires pocketStore." }
        val netMon = checkNotNull(storeNetworkMonitor) { "Requires kmptoolkit NetworkMonitor." }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) { "Requires FetchedAtRepository." }
        return store.asScreenStream(
            key = PocketKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Pocket.detailed(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.NETWORK_ONLY,
            ttl = WalletStoreTtl.POCKET,
        )
    }

    /**
     * Retrieves the linked pocket accounts.
     *
     * **FetchPolicy Decision**: Uses `CACHE_FIRST_SWR` (Stale-While-Revalidate) because
     * the user's linked pockets are core critical data that must be visible instantly on cold start.
     * This guarantees zero shimmer if local DB data exists, making the app feel incredibly fast
     * while silently updating in the background.
     */
    override fun getLinkedPocketAccountsStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<DetailedPocketAccount>> {
        val store = checkNotNull(pocketStore) { "Requires pocketStore." }
        val netMon = checkNotNull(storeNetworkMonitor) { "Requires kmptoolkit NetworkMonitor." }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) { "Requires FetchedAtRepository." }
        return store.asScreenStream(
            key = PocketKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.Pocket.linked(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.POCKET,
        )
    }

    /**
     * Attempts to push pocket account links to the remote API.
     *
     * **Offline-First Architecture**:
     * If the network request fails, this method catches the exception and falls back to saving
     * the intended links into the local Room database (`wallet_pockets`) with a `syncStatus` of
     * `PENDING_LINK`. To prevent primary key collisions with future server IDs, a random negative
     * ID is assigned. A random ID is used because a user might link multiple accounts offline in a row,
     * and hardcoding `0` would cause SQLite `UNIQUE` constraint collisions.
     * **Data Preservation (explicitlyAddedAccounts)**: The function accepts `DetailedPocketAccount`
     * models instead of just account IDs. This is required because when we optimistically write
     * to the local DB, we need all the details (balance, productName, currency) to render a proper
     * UI card instantly. If we only had the ID, the user would see an "Unknown Account" with zero
     * balance until the network returned.
     *
     * **Background ID Resolution**: The server assigns a permanent real `id` (pocket mapping ID)
     * to the relationship once the network push succeeds. This ID must be fetched because
     * `delinkAccounts` strictly requires that real server ID to successfully remove the mapping.
     * The `PocketStore` fetcher automatically pulls this real ID in the background once online.
     */
    override suspend fun linkAccounts(
        explicitlyAddedAccounts: List<DetailedPocketAccount>,
        clientId: Long,
    ) {
        withContext(ioDispatcher) {
            val dao = pocketDao ?: return@withContext
            val stamp = Clock.System.now().toEpochMilliseconds()

            try {
                // Try to push to network directly to avoid overhead of waiting for the Fetcher
                val request = PocketLinkRequest(
                    accountsDetail = explicitlyAddedAccounts.map {
                        PocketLinkRequest.AccountDetail(
                            accountId = it.pocket.accountId.toString(),
                            accountType = it.pocket.accountType.name,
                        )
                    },
                )
                dataManager.pocketApi.linkAccounts(request = request)

                // If successful, save directly as SYNCED so the UI updates instantly
                explicitlyAddedAccounts.forEach {
                    dao.upsert(it.toPendingLinkEntity(clientId, stamp).copy(syncStatus = "SYNCED"))
                }
            } catch (e: Exception) {
                // If network fails (e.g. offline), fallback to PENDING_LINK
                explicitlyAddedAccounts.forEach {
                    dao.upsert(it.toPendingLinkEntity(clientId, stamp))
                }
                // Do NOT throw. Swallow the error so the UI loading dialog closes normally
                // and the user sees an immediate success state (optimistic UI), since the
                // action was successfully saved offline.
            }
        }
    }

    /**
     * Attempts to remove pocket account links via the remote API.
     *
     * **Offline-First Architecture**:
     * This method explicitly filters out any `id <= 0` (fake optimistic IDs assigned during
     * an offline link operation). If a user links and then delinks an account entirely offline,
     * the system gracefully deletes the local row without sending it to the network, because
     * the backend never knew about the fake negative ID to begin with (sending it would result
     * in an API error).
     *
     * If the network request fails for valid server IDs, the method catches the exception and
     * falls back to marking the local Room rows as `PENDING_DELINK`. The UI updates instantly,
     * and the `PocketStore` fetcher will cleanly handle the network push when connectivity returns.
     */
    override suspend fun delinkAccounts(
        pocketAccountMappingIds: List<Long>,
        clientId: Long,
    ) {
        withContext(ioDispatcher) {
            val dao = pocketDao ?: return@withContext
            val currentPockets = dao.observeLinkedByClient(clientId).first()
            val accountsToDelink = currentPockets.filter { it.id in pocketAccountMappingIds }

            try {
                // Try to push to network directly
                val serverIds = accountsToDelink.filter { it.id > 0 }.map { it.id }
                if (serverIds.isNotEmpty()) {
                    val request = PocketDelinkRequest(serverIds)
                    dataManager.pocketApi.delinkAccounts(request = request)
                }

                // If successful, we can just delete them from Room (or mark them as SYNCED
                // if we want to keep history, but usually delink means delete)
                // The subsequent Fetcher run will wipe them anyway. For instant UI update, we delete.
                accountsToDelink.forEach {
                    dao.deleteById(it.id, clientId)
                }
            } catch (e: Exception) {
                // If network fails, mark as PENDING_DELINK
                accountsToDelink.forEach {
                    dao.upsert(it.copy(syncStatus = "PENDING_DELINK"))
                }
                // Do NOT throw. Swallow the error so the UI loading dialog closes normally
                // and the user sees an immediate success state (optimistic UI), since the
                // action was successfully saved offline.
            }
        }
    }

    /**
     * Retrieves the available accounts that can be linked.
     *
     * **FetchPolicy Decision**: Uses `CACHE_FIRST_SWR` (Stale-While-Revalidate) because
     * fetching available accounts aggregates data across Loans, Savings, and Shares, which can be
     * a slow network operation. Caching it allows the "Manage Pockets" bottom sheet to open
     * instantly without forcing the user to stare at a loader every time they want to link an account.
     */
    override fun observeLinkedPocketAccounts(clientId: Long): Flow<List<PocketAccount>> {
        val dao = pocketDao ?: return emptyFlow()
        return dao.observeLinkedByClient(clientId).map { entities ->
            entities.map { it.toDomain().pocket }
        }
    }

    override fun getAvailableAccountsToLinkStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<LinkableAccount>> {
        val store = checkNotNull(linkableAccountsStore) { "Requires linkableAccountsStore." }
        val netMon = checkNotNull(storeNetworkMonitor) { "Requires kmptoolkit NetworkMonitor." }
        val fetchedAtRepo = checkNotNull(fetchedAtRepository) { "Requires FetchedAtRepository." }
        return store.asScreenStream(
            key = LinkableAccountKey(clientId),
            networkMonitor = netMon,
            fetchedAtRepository = fetchedAtRepo,
            cacheKey = AppCacheKeys.LinkableAccounts.of(clientId),
            scope = scope,
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.CACHE_FIRST_SWR,
            ttl = WalletStoreTtl.LINKABLE_ACCOUNTS,
        )
    }
}
