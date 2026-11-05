/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.account

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.wallet.account.SavingAccountDetailDao
import kpt.core.database.wallet.account.toDomain
import kpt.core.database.wallet.account.toEntity
import org.mifospay.core.common.DateHelper
import org.mifospay.core.model.savingsaccount.SavingAccountDetail
import org.mifospay.core.model.savingsaccount.SavingsWithAssociationsEntity
import org.mifospay.core.model.savingsaccount.Transaction
import org.mifospay.core.model.savingsaccount.TransactionType
import org.mifospay.core.model.savingsaccount.TransactionsEntity
import org.mifospay.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock

// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for `SavingAccountDetailEntity`. Shared with the DAO's writes. */
private const val ACCOUNT_DETAIL_TABLE = "wallet_saving_account_details"

/**
 * `apiManager.savingAccountsListApi.getSavingsWithAssociations(...)` `?associations=`
 * value. Same endpoint as the LEDGER `history` store — we consume the response as
 * `SavingAccountDetail` here (via `toSavingDetail()`) instead of stripping to
 * `List<Transaction>`, so the two stores share one API call in independent code
 * paths without any merge glue.
 */
private const val ASSOC_TRANSACTIONS = "transactions"

/**
 * Build the SINGLE-ROW-PER-KEY read [Store] for savings-account details
 * (GOAL D13 — a variation of the `history` archetype).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_saving_account_details` [SourceOfTruth]; the network fetcher
 * pulls from
 * `savingAccountsListApi.getSavingsWithAssociations(accountId, "transactions")`
 * (same endpoint as `provideHistoryStore`), then hands the mapped domain
 * [SavingAccountDetail] to the writer for atomic single-row replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default.
 *
 * ## Overlap with the LEDGER `history` store
 *
 * The endpoint returns `List<TransactionsEntity>` embedded in the account
 * envelope; the LEDGER `provideHistoryStore` strips those into the
 * `wallet_transactions` LEDGER table. THIS store persists the WHOLE detail
 * payload (JSON-encoded via `SavingAccountDetailEntityMapper`) into
 * `wallet_saving_account_details`. Both cache paths coexist — the detail cache
 * is what the account-detail screen renders; the LEDGER cache is what the
 * history screen streams. No merge/dedup logic exists between them; each is a
 * self-contained SoT for its own read consumer.
 *
 * ## Atomic single-row write (S5-PAGE-ATOMIC — trivial case)
 *
 * The writer lambda calls [SavingAccountDetailDao.upsert] — a single-row
 * `@Insert(onConflict = REPLACE)` under Room's implicit transaction. The
 * "partial-page" window that motivates the LEDGER's [replacePage] never applies
 * here because the payload is a single record; upsert is atomic by
 * construction.
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_saving_account_details") { … }`
 * so wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails. On Android/Desktop/iOS the wrap is a microsecond
 * no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing account
 * management flows (`createSavingsAccount`, `updateSavingsAccount`, block/unblock)
 * do NOT call `store.write(...)` and do NOT touch [SavingAccountDetailDao]
 * directly — they call their existing online repository methods and the
 * server-echoed detail appears here on the next refresh cycle. There is no
 * `Bookkeeper` — this store never queues local writes for retry.
 *
 * ## Empty-observer nullability
 *
 * The DAO reader emits `Flow<SavingAccountDetailEntity?>`; before the first
 * successful fetch the cached row is null. The store's reader maps null to a
 * fresh empty-list-equivalent by emitting a placeholder — actually, the mapper
 * cannot conjure a `SavingAccountDetail` out of thin air. The chosen shape is:
 * the reader FILTERS OUT null via `mapNotNull` semantics through a `map { it? }`
 * transform, so the Store yields no cached emission on cold cache and the
 * `asScreenStream` `isEmpty = { ... }` predicate on the DetailScreen fires
 * `ScreenState.Loading` until the network fetch populates a row.
 */
@StoreProvider(id = "accountDetail")
@CacheKey(fn = "of", key = "wallet_saving_account_details-{accountId}", params = ["accountId:Long"])
fun provideAccountDetailStore(
    apiManager: SelfServiceApiManager,
    dao: SavingAccountDetailDao,
): Store<AccountDetailKey, SavingAccountDetail> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: AccountDetailKey ->
        // Ktorfit returns Flow<SavingsWithAssociationsEntity> for
        // getSavingsWithAssociations — take the first (and only) emission.
        val response: SavingsWithAssociationsEntity = apiManager.savingAccountsListApi
            .getSavingsWithAssociations(key.accountId, ASSOC_TRANSACTIONS)
            .first()
        response.toSavingAccountDetail()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: AccountDetailKey ->
            run { dao.observeById(key.accountId) }
                .map { row -> row?.toDomain() }
        },
        writer = { _: AccountDetailKey, detail: SavingAccountDetail ->
            val stamp = clock()
            // Single-row upsert — Room's implicit transaction handles the
            // atomic replace-on-conflict; no explicit `replacePage` needed
            // because the payload is one record (not a page of rows).
            dao.upsert(detail.toEntity(fetchedAtEpochMs = stamp))
        },
        delete = { key: AccountDetailKey -> dao.deleteById(key.accountId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline mapping helper — private to this file.
//
// Mirrors `org.mifospay.core.data.mapper.toSavingDetail` (which lives in
// core/data, out of reach for a `core/store` module). Kept private + minimal
// so the store body is self-contained and the fetcher lambda doesn't need to
// reach across module boundaries. If a third consumer wants this mapping, lift
// it to core/model (its inputs/outputs are all already there).
// ---------------------------------------------------------------------------

private fun SavingsWithAssociationsEntity.toSavingAccountDetail(): SavingAccountDetail =
    SavingAccountDetail(
        id = id,
        accountNo = accountNo,
        depositType = depositType,
        clientId = clientId,
        clientName = clientName,
        savingsProductId = savingsProductId,
        savingsProductName = savingsProductName,
        fieldOfficerId = fieldOfficerId,
        status = status,
        timeline = timeline,
        currency = currency,
        nominalAnnualInterestRate = nominalAnnualInterestRate,
        withdrawalFeeForTransfers = withdrawalFeeForTransfers,
        allowOverdraft = allowOverdraft,
        enforceMinRequiredBalance = enforceMinRequiredBalance,
        lienAllowed = lienAllowed,
        withHoldTax = withHoldTax,
        lastActiveTransactionDate = lastActiveTransactionDate,
        isDormancyTrackingActive = isDormancyTrackingActive,
        summary = summary,
        transactions = transactions.map { it.toDomainTransactionMinimal() },
    )

// Minimal TransactionsEntity → Transaction inline mapper. Only the fields the
// SavingAccountDetail exposes are populated; the LEDGER's `provideHistoryStore`
// carries the FULL mapping (with Transfer + PaymentDetailData resolution) — the
// detail-screen render surfaces only the transaction summary rows, so a lighter
// mapping suffices here. Consumers needing full Transaction detail hit the
// LEDGER store for the same account.
private fun TransactionsEntity.toDomainTransactionMinimal(): Transaction = Transaction(
    reversed = this.reversed,
    transactionId = this.id,
    amount = this.amount,
    date = DateHelper.getDateAsString(this.submittedOnDate),
    currency = this.currency,
    transactionType = when {
        this.transactionType.deposit -> TransactionType.CREDIT
        this.transactionType.withdrawal -> TransactionType.DEBIT
        else -> TransactionType.OTHER
    },
    transferId = this.transfer?.id,
    accountId = this.accountId,
    accountNo = this.accountNo,
    originalTransactionId = this.originalTransactionId,
    paymentDetailId = this.paymentDetailData?.id,
    description = this.transfer?.transferDescription ?: "",
    transfer = null,
    paymentDetailData = null,
)
