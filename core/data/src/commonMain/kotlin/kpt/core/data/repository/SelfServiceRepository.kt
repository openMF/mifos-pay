/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenState
import kpt.core.model.account.Account
import kpt.core.model.account.AccountContent
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.client.Client
import kpt.core.model.network.entity.Page
import kpt.core.model.network.entity.authentication.AuthenticationPayload
import kpt.core.model.network.entity.user.User
import kpt.core.model.savingsaccount.Transaction
interface SelfServiceRepository {
    // Point-lookup — one-shot suspend; returns the value and throws on error.
    suspend fun loginSelf(payload: AuthenticationPayload): User

    // Phase-3 cutover — streaming reads on ScreenState.
    fun getSelfClientDetails(clientId: Long): Flow<ScreenState<Client>>

    suspend fun getSelfClientDetails(): Flow<ScreenState<Page<Client>>>

    // Untyped stream (raw list) — no state envelope; leave as-is.
    fun getSelfAccountTransactions(
        accountId: Long,
    ): Flow<List<Transaction>>

    // One-shot suspend returning the raw transaction flow; throws on error.
    suspend fun getSelfAccountTransactionFromId(
        accountId: Long,
        transactionId: Long,
    ): Flow<Transaction>

    fun getSelfAccounts(clientId: Long): Flow<ScreenState<List<Account>>>

    fun getBeneficiaryList(): Flow<ScreenState<List<Beneficiary>>>

    fun getActiveAccountsWithTransactionsPerAccount(
        clientId: Long,
        limit: Int?,
    ): Flow<ScreenState<Map<Account, List<Transaction>>>>

    fun getActiveAccounts(
        clientId: Long,
    ): Flow<ScreenState<List<Account>>>

    fun getActiveAccountsWithAccountTransferTemplate(
        clientId: Long,
    ): Flow<ScreenState<List<Account>>>

    fun getAccountsTransactions(clientId: Long): Flow<ScreenState<List<Transaction>>>

    // Untyped list stream — no state envelope; leave as-is.
    fun getTransactions(accountId: List<Long>, limit: Int?): Flow<List<Transaction>>

    fun getTransactions(accountId: Long, limit: Int?): Flow<ScreenState<List<Transaction>>>

    /**
     * Phase-4 Batch-A **LEDGER read** for the `history` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<Transaction>>>` consumed
     * through the `history` Store5 [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.transaction.TransactionDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream] — the
     * Store5-native bridge, distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [getTransactions] path uses.
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) so subscribers see the cached page instantly and
     * a background revalidation fires on the Stale/VeryStale band edge — no
     * reload spinner on tab-switch.
     *
     * ### Why the scope parameter
     *
     * `Store.asScreenStream(...)` launches child coroutines (reconnect debounce,
     * periodic refresh, CACHE_FIRST_SWR side-fetch). It needs a lifecycle-bound
     * scope so those coroutines stop when the ViewModel is cleared. Pass
     * `viewModelScope` from the call site.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Money-movement writes (transfer, deposit, withdrawal)
     * continue to flow through their existing online repositories
     * (`ThirdPartyTransferRepository`, `SavingsAccountRepository`, etc.) and
     * appear here on the next refresh cycle — the LEDGER cache is server-truth.
     *
     * @param accountId Fineract savings-account id (LEDGER page key).
     * @param limit Optional client-side row limit applied after the store's
     *   `Content(list)` state. Pass `null` for the full list.
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getTransactionsStream(
        accountId: Long,
        limit: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Transaction>>

    /**
     * Phase-5 Batch-1 **LEDGER read** for the `beneficiary` archetype (GOAL D13) —
     * returns an offline-first `Flow<ScreenState<List<Beneficiary>>>` consumed
     * through the `beneficiary` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + atomic
     * [`replacePage`][kpt.core.database.wallet.beneficiary.BeneficiaryDao.replacePage]
     * writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream] — the
     * Store5-native bridge, distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [getBeneficiaryList] path uses.
     *
     * The store is keyed on [clientId] for per-client cache scoping — the
     * Fineract `beneficiaryList()` endpoint returns the list scoped to the
     * authenticated session (no `clientId` path parameter), but the cache is
     * still scoped so a logout-then-login as a different client never surfaces
     * the previous user's cached list. The mapper stamps [clientId] into each
     * persisted row at write time.
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default) — subscribers see the cached page instantly and a
     * background revalidation fires on the Stale/VeryStale band edge.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Beneficiary CRUD writes (`createBeneficiary`,
     * `updateBeneficiary`, `deleteBeneficiary`) continue to flow through their
     * existing online paths and appear here on the next refresh cycle — the
     * LEDGER cache is server-truth. The VM's explicit `RefreshList` action
     * still forces a refetch via the store's built-in refresh trigger by
     * re-subscribing to a new `asScreenStream(...)` invocation.
     *
     * @param clientId owning client id — the store's page key.
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getBeneficiaryListStream(
        clientId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Beneficiary>>

    // Dashboard-style aggregation of two upstream streams, lifted into
    // ScreenState via `asScreenStateFlow`. New callers SHOULD prefer
    // [getAccountAndBeneficiaryListScreen], the Phase-5 Batch-4 store-native
    // combinator; this simpler network-only variant is kept for BC.
    fun getAccountAndBeneficiaryList(clientId: Long): Flow<ScreenState<AccountContent>>

    /**
     * Phase-5 Batch-4 **combinator read** for the account+beneficiary
     * dashboard (GOAL D13 — aggregation variant of the `beneficiary` archetype)
     * — returns an offline-first `Flow<ScreenState<AccountContent>>` folded
     * from TWO independent upstream streams:
     *
     * 1. The active-accounts network stream (`getSelfAccounts(clientId)` —
     *    still a raw network flow lifted through `asScreenStateFlow`), and
     * 2. The store-backed beneficiary stream ([getBeneficiaryListScreen]).
     *
     * ### Priority + fold semantics
     *
     * The fold mirrors the template's `combineScreenStates` priority ladder —
     * `NoNetwork > Loading > Unauthenticated > Error > Empty > Content` —
     * but is written as a manual `combine {}` over the FORK's
     * [`kpt.core.common.ScreenState`], because the template's
     * `combineScreenStates` helper in `core-base/store/screen/` is typed
     * against `kpt.core.base.store.screen.ScreenState` (a distinct type
     * carrying `FreshnessSignal` on `Content`) and cannot be used verbatim
     * on fork streams. Both sources must reach `Content` before the fold
     * emits `Content(AccountContent(...))`.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ-only combinator. Downstream writes (create/update/delete
     * beneficiary + create/update savings-account) continue to flow through
     * their existing online repositories; the store-backed beneficiary source
     * re-emits on the next refresh cycle, and the network account stream
     * emits fresh values on the next subscription. There is no bookkeeper.
     *
     * @param clientId Owning client id — passed to both upstream streams so
     *   the beneficiary source's cache slot is client-scoped.
     * @param scope Coroutine scope for the beneficiary stream's internal
     *   helper coroutines (typically `viewModelScope`).
     */
    fun getAccountAndBeneficiaryListScreen(
        clientId: Long,
        scope: CoroutineScope,
    ): Flow<ScreenState<AccountContent>>

    // Beneficiary writes — plain suspend (throw on error). The success message is
    // now owned by each consumer VM as a feature StringResource.
    suspend fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload)

    suspend fun updateBeneficiary(
        beneficiaryId: Long,
        payload: BeneficiaryUpdatePayload,
    )

    suspend fun deleteBeneficiary(beneficiaryId: Long)
}
