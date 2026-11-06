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

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.FetchedAtRepository
import kpt.core.base.store.screen.ScreenState
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.network.KtorfitClient
import kpt.core.network.SelfServiceApiManager
import kpt.core.store.wallet.linkableaccount.LinkableAccountKey
import kpt.core.store.wallet.pocket.PocketKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Instant

/**
 * Contract tests for the Store5-backed [PocketRepositoryImp].
 *
 * These tests deliberately provide Store5 instances instead of mocking repository internals.
 * That keeps the contract explicit: read streams must expose the data supplied by their
 * matching store, and missing store dependencies must fail clearly rather than silently
 * falling back to the pre-Store5 implementation.
 */
class PocketRepositoryTest {

    /** Verifies that detailed pocket accounts are read from the supplied Store5 stream. */
    @Test
    fun getDetailedPocketAccountsStream_readsFromStore5Stream() = runTest {
        val expected = listOf(samplePocket(10L))
        val store = StoreBuilder
            .from<PocketKey, List<DetailedPocketAccount>>(
                fetcher = Fetcher.of { expected },
            )
            .build()
        val repository = repository(pocketStore = store)

        val state = repository.getDetailedPocketAccountsStream(42L, backgroundScope)
            .state
            .first { it is ScreenState.Content }

        assertEquals(expected, assertIs<ScreenState.Content<List<DetailedPocketAccount>>>(state).data)
    }

    /** Verifies that linked accounts use the linked-account Store5 stream. */
    @Test
    fun getLinkedPocketAccountsStream_usesStore5LinkedStream() = runTest {
        val expected = listOf(samplePocket(11L))
        val store = StoreBuilder
            .from<PocketKey, List<DetailedPocketAccount>>(
                fetcher = Fetcher.of { expected },
            )
            .build()
        val state = repository(pocketStore = store)
            .getLinkedPocketAccountsStream(42L, backgroundScope)
            .state
            .first { it is ScreenState.Content }

        assertEquals(expected, assertIs<ScreenState.Content<List<DetailedPocketAccount>>>(state).data)
    }

    /** Verifies that accounts eligible for linking use the linkable-account Store5 stream. */
    @Test
    fun getAvailableAccountsToLinkStream_readsFromLinkableStore5Stream() = runTest {
        val expected = listOf(sampleLinkable(12L))
        val store = StoreBuilder
            .from<LinkableAccountKey, List<LinkableAccount>>(
                fetcher = Fetcher.of { expected },
            )
            .build()
        val state = repository(linkableAccountsStore = store)
            .getAvailableAccountsToLinkStream(42L, backgroundScope)
            .state
            .first { it is ScreenState.Content }

        assertEquals(expected, assertIs<ScreenState.Content<List<LinkableAccount>>>(state).data)
    }

    /** Verifies that a repository without its required Store5 dependencies fails explicitly. */
    @Test
    fun storeBackedStreams_requireTheirStore5Dependencies() = runTest {
        val repository = repository()
        assertFailsWith<IllegalStateException> {
            repository.getDetailedPocketAccountsStream(42L, backgroundScope)
        }
        assertFailsWith<IllegalStateException> {
            repository.getAvailableAccountsToLinkStream(42L, backgroundScope)
        }
    }

    /** Builds the repository with only the Store5 dependencies needed by a given test. */
    private fun repository(
        pocketStore: Store<PocketKey, List<DetailedPocketAccount>>? = null,
        linkableAccountsStore: Store<LinkableAccountKey, List<LinkableAccount>>? = null,
    ) = PocketRepositoryImp(
        dataManager = fakeSelfServiceApiManager,
        ioDispatcher = Dispatchers.Default,
        pocketStore = pocketStore,
        linkableAccountsStore = linkableAccountsStore,
        storeNetworkMonitor = FakeNetworkMonitor,
        fetchedAtRepository = FakeFetchedAtRepository,
    )

    /** Creates a detailed account fixture with stable values for stream assertions. */
    private fun samplePocket(accountId: Long) = DetailedPocketAccount(
        pocket = PocketAccount(accountId, accountId, accountId, AccountType.SAVINGS, "SAV-$accountId"),
        productName = "Savings",
        balance = 100.0,
        currencyCode = "USD",
        currencyDisplaySymbol = "$",
        decimalPlaces = 2,
        status = AccountStatus.ACTIVE,
    )

    /** Creates a linkable account fixture with stable values for stream assertions. */
    private fun sampleLinkable(accountId: Long) = LinkableAccount(
        accountId, "Savings", "SAV-$accountId", AccountType.SAVINGS, 100.0, "USD", 2,
        AccountStatus.ACTIVE, "$",
    )
}

/**
 * Uses the production manager type required by [PocketRepositoryImp] without making network calls.
 * Its lazily-created services are never touched because these tests exercise read-store wiring.
 */
private val fakeSelfServiceApiManager = SelfServiceApiManager(
    KtorfitClient(
        Ktorfit.Builder()
            .httpClient(HttpClient())
            .baseUrl("https://example.com/")
            .build(),
    ),
)

/** Keeps Store5 network behavior online and deterministic for repository stream tests. */
private object FakeNetworkMonitor : NetworkMonitor {
    private val online = NetworkStatus.Available(NetworkInfo(NetworkType.WiFi, false))
    override val networkStatus = MutableStateFlow<NetworkStatus>(online)
    override val isOnline = MutableStateFlow(true)
    override val networkChanges = MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
    override fun close() = Unit
}

/** Disables persistence in tests; no freshness timestamp should affect these stream contracts. */
private object FakeFetchedAtRepository : FetchedAtRepository {
    override suspend fun read(storeKey: String): Instant? = null
    override suspend fun write(storeKey: String, instant: Instant) = Unit
}
