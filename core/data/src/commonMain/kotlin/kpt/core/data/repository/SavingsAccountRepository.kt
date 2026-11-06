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
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.common.ScreenStateStream
import kpt.core.model.network.entity.Page
import kpt.core.model.savingsaccount.CreateNewSavingEntity
import kpt.core.model.savingsaccount.SavingAccountDetail
import kpt.core.model.savingsaccount.SavingAccountTemplate
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.UpdateSavingAccountEntity
interface SavingsAccountRepository {
    // Phase-3 cutover — Flow-shaped reads on ScreenState.
    // Page<T> is preserved inside Content (mirrors
    // `SelfServiceRepository.getSelfClientDetails(): Page<Client>` treatment).
    suspend fun getSavingsAccounts(limit: Int): ScreenStateStream<Page<SavingsWithAssociationsEntity>>

    suspend fun getSavingsWithAssociations(
        accountId: Long,
        associationType: String,
    ): ScreenStateStream<SavingsWithAssociationsEntity>

    fun getAccountDetail(accountId: Long): ScreenStateStream<SavingAccountDetail>

    /**
     * Phase-5 Batch-1 **SINGLE-ROW-PER-KEY read** for the `accountDetail` archetype
     * (GOAL D13) — returns an offline-first `ScreenStateStream<SavingAccountDetail>`
     * consumed through the `accountDetail` Store5
     * [`Store`][org.mobilenativefoundation.store.store5.Store]
     * (`createStore` + Room
     * [`SourceOfTruth`][org.mobilenativefoundation.store.store5.SourceOfTruth]
     * + single-row upsert writer) via
     * [`Store.asScreenStream`][kpt.core.base.store.screen.asScreenStream].
     *
     * The store is driven by
     * [`FetchPolicy.CACHE_FIRST_SWR`][kpt.core.base.store.screen.FetchPolicy.CACHE_FIRST_SWR]
     * (Phase-5 T10 default). Distinct from the transitional
     * [`asScreenStateFlow`][kpt.core.common.asScreenStateFlow] the
     * legacy [getAccountDetail] path uses; both signatures return
     * `ScreenStateStream<SavingAccountDetail>` so a consumer can swap between
     * them without any downstream shape change.
     *
     * ### Write path (GOAL D1)
     *
     * This is a READ path. Account management writes ([blockAccount],
     * [unblockAccount], [createSavingsAccount], [updateSavingsAccount]) continue
     * to flow through their existing online paths and appear here on the next
     * refresh cycle.
     *
     * @param accountId Fineract savings-account id (SoT key).
     * @param scope Coroutine scope for the stream's internal helper coroutines
     *   (typically `viewModelScope`).
     */
    fun getAccountDetailStream(
        accountId: Long,
        scope: CoroutineScope,
    ): ScreenDataStream<SavingAccountDetail>

    suspend fun getSavingAccountTransaction(
        accountId: Long,
        transactionId: Long,
    ): ScreenStateStream<Transaction>

    suspend fun payViaMobile(accountId: Long): ScreenStateStream<Transaction>

    fun getSavingAccountTemplate(clientId: Long): ScreenStateStream<SavingAccountTemplate>

    // Writes throw on failure (template idiom — consumed by a SubmitHandler).
    suspend fun createSavingsAccount(savingAccount: CreateNewSavingEntity)

    suspend fun updateSavingsAccount(
        accountId: Long,
        savingAccount: UpdateSavingAccountEntity,
    )

    suspend fun unblockAccount(
        accountId: Long,
    )

    suspend fun blockAccount(
        accountId: Long,
    )
}
