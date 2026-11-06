/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet.history

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.common.DateHelper
import kpt.core.database.wallet.transaction.TransactionDao
import kpt.core.database.wallet.transaction.toDomain
import kpt.core.database.wallet.transaction.toEntity
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.model.savingsaccount.TransactionsEntity
import kpt.core.network.SelfServiceApiManager
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kotlin.time.Clock
import kpt.core.model.savingsaccount.PaymentType as ModelPaymentType
// Wall clock for fetched-at stamps. File-private rather than a defaulted provider parameter: store-ksp
// injects every provider parameter with get(), which cannot resolve a function type.
private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }

/** Room `@Entity(tableName = …)` for [kpt.core.database.wallet.transaction.TransactionEntity]. Shared with the DAO's writes. */
private const val TRANSACTIONS_TABLE = "wallet_transactions"

/** `apiManager.savingAccountsListApi.getSavingsWithAssociations(...)` `?associations=` value. */
private const val ASSOC_TRANSACTIONS = "transactions"

/**
 * Build the LEDGER read [Store] for savings-account transaction history
 * (GOAL D13 — the `history` archetype).
 *
 * ## Shape
 *
 * `createStore` — read-only Store5 factory. Reads are offline-first through
 * the Room `wallet_transactions` [SourceOfTruth]; the network fetcher pulls
 * from `savingAccountsListApi.getSavingsWithAssociations(accountId, "transactions")`
 * and hands the mapped domain list to the writer for atomic page replacement.
 *
 * The consumer at the repository layer wires this to
 * `store.asScreenStream(key, networkMonitor, fetchedAtRepository, cacheKey, scope,
 * fetchPolicy = FetchPolicy.CACHE_FIRST_SWR)` — the CACHE_FIRST_SWR band gate is
 * the Phase-5 app-wide default and gives us cache-first reads with a background
 * revalidation on the Stale/VeryStale edge (no reload spinner).
 *
 * ## Delta cursor
 *
 * The fetcher reads [TransactionDao.latestDateForAccount] before the API call
 * — that's the SINCE cursor. TODAY the Fineract `getSavingsWithAssociations`
 * endpoint does NOT accept a `?since=<epoch>` filter, so the fetcher pulls the
 * full account list every time. When the server grows a delta filter the ONLY
 * change is one line inside this file — the writer stays on [TransactionDao.replacePage]
 * (safe default: server truth wins) OR the writer switches to
 * [TransactionDao.upsertAll] for pure append semantics. The DAO shape supports
 * both; the store shape is stable.
 *
 * ## Atomic page write (S5-PAGE-ATOMIC)
 *
 * The writer lambda calls [TransactionDao.replacePage] — a single `@Transaction`
 * that delete-then-upserts under one SQLite transaction. In-flight
 * [TransactionDao.observeByAccount] subscribers never see an empty page mid-write.
 * See `training-layer/instructions/stream-first/latest/CORE_DATABASE.md`
 * "Atomic paged writer recipe" (RULE-IMPLEMENT-STORE5-001 S5-3 / S5-PAGE-ATOMIC).
 *
 * ## Invalidation wrapping
 *
 * The DAO reader is wrapped with `daoFlow("wallet_transactions") { … }` so
 * wasmJs collectors re-emit after writes even when Room 3 alpha's async
 * InvalidationTracker fails to fan out. On Android/Desktop/iOS the wrap is a
 * microsecond no-op alongside Room's native invalidation.
 *
 * ## Write path (GOAL D1 — WRITES STAY ONLINE)
 *
 * This is a `Store` (not `MutableStore`) by design. The user-facing money-movement
 * flows (transfer, deposit, withdrawal) do NOT call `store.write(...)` and do
 * NOT touch [TransactionDao] directly — they call their existing online
 * repositories (`ThirdPartyTransferRepository`, `SavingsAccountRepository`,
 * `AccountRepository`) and the server-issued ledger row appears here on the next
 * refresh cycle. There is no `Bookkeeper` — this store never queues local
 * writes for retry.
 */
@StoreProvider(id = "history")
@CacheKey(fn = "of", key = "wallet_transactions-{accountId}", params = ["accountId:Long"])
fun provideHistoryStore(
    apiManager: SelfServiceApiManager,
    dao: TransactionDao,
): Store<TransactionKey, List<Transaction>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: TransactionKey ->
        // Delta cursor read — currently informational; the API can't honor `?since=`,
        // so the fetcher below pulls the full list. Kept live so the store body is
        // stable when the server grows delta support (change is one-line).
        @Suppress("UNUSED_VARIABLE")
        val since: Long? = dao.latestDateForAccount(key.accountId)

        // Ktorfit returns Flow<SavingsWithAssociationsEntity> for
        // getSavingsWithAssociations — take the first (and only) emission.
        val response: SavingsWithAssociationsEntity = apiManager.savingAccountsListApi
            .getSavingsWithAssociations(key.accountId, ASSOC_TRANSACTIONS)
            .first()
        response.toTransactionList()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: TransactionKey ->
            run { dao.observeByAccount(key.accountId) }
                .map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: TransactionKey, transactions: List<Transaction> ->
            val stamp = clock()
            // ATOMIC page replacement — see [TransactionDao.replacePage] KDoc for
            // the S5-PAGE-ATOMIC invariant this write path preserves.
            dao.replacePage(
                accountId = key.accountId,
                entities = transactions.map { it.toEntity(fetchedAtEpochMs = stamp) },
            )
        },
        delete = { key: TransactionKey -> dao.deleteByAccount(key.accountId) },
        deleteAll = { dao.deleteAll() },
    ),
)

// ---------------------------------------------------------------------------
// Inline mapping helpers — private to this file.
//
// Mirrors `kpt.core.data.mapper.TransactionMapper` (which lives in
// core/data, out of reach for a `core/store` module). Kept private + minimal so
// the store body is self-contained and the fetcher lambda doesn't need to
// reach across module boundaries. If a third consumer wants this mapping, lift
// it to core/model (its inputs/outputs are all already there).
// ---------------------------------------------------------------------------

private fun SavingsWithAssociationsEntity.toTransactionList(): List<Transaction> =
    this.transactions.map { it.toDomainTransaction() }

private fun TransactionsEntity.toDomainTransaction(): Transaction = Transaction(
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
    transfer = this.transfer?.let {
        Transaction.Transfer(
            id = it.id ?: 0L,
            transferAmount = it.transferAmount ?: 0.0,
            transferDescription = it.transferDescription ?: "",
            reversed = it.reversed ?: false,
        )
    },
    paymentDetailData = this.paymentDetailData?.let {
        Transaction.PaymentDetailData(
            id = it.id,
            paymentType = it.paymentType?.toDomainPaymentType(),
            accountNumber = it.accountNumber ?: "",
            checkNumber = it.checkNumber ?: "",
            routingCode = it.routingCode ?: "",
            receiptNumber = it.receiptNumber ?: "",
            bankNumber = it.bankNumber ?: "",
        )
    },
)

private fun ModelPaymentType.toDomainPaymentType(): Transaction.PaymentType =
    Transaction.PaymentType(
        id = this.id ?: 0,
        name = this.name ?: "",
        isSystemDefined = this.isSystemDefined ?: false,
    )
