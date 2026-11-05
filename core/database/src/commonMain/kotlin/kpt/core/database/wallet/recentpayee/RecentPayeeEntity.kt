/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.recentpayee

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a derived recent-payee — the Room mirror of the
 * `org.mifospay.core.model.account.RecentPayee` domain model.
 *
 * Stored in the `wallet_recent_payees` table. LEDGER-shaped but LOCAL-DERIVED
 * (GOAL D12 — OFFLINE_LOCAL_ONLY variant used to persist runtime-derived data):
 * the source values are the user's own transaction history + transfer-detail
 * lookups (N+1 fan-out in `RecentPayeeRepositoryImpl`), and this table caches
 * the *result* of that walk so subsequent screen entries skip the N+1 fetch
 * and render offline.
 *
 * Page key = `sourceAccountId` (the user's own savings account whose transactions
 * derived the recent-payee list). Each row is a distinct downstream client the
 * user has transferred to. Composite PK on [sourceAccountId] + [clientId] keeps
 * a rebuild for the same source-account idempotent (`replacePage(accountId, ...)`)
 * while allowing the same recipient to appear under multiple source accounts.
 *
 * **Read is offline-first, WRITE is LOCAL-DERIVED (not server writes — GOAL D1
 * write-online invariant still holds: the app never invents "recent-payee" data
 * on the server, this table caches what the server already knows).** Rows land
 * here exclusively via `RecentPayeeRepositoryImpl.deriveAndPersist(...)`
 * inside `notifyingWrite("wallet_recent_payees") { … }`.
 *
 * ## Column mapping
 *
 * Every domain field lands on a scalar column — no nested types, no JSON blobs.
 * `lastTransferDate` stays as the raw String from the transfer-detail (the
 * domain model already carries it as a `String`; sort-order use is handled by
 * the caller via `RecentPayee.lastTransferDate` comparisons at read time).
 *
 * @property sourceAccountId Owning savings-account id (the store key, doubles as page cursor).
 * @property clientId Recipient client id — component of the composite PK.
 * @property clientName Recipient display name.
 * @property accountId Recipient savings-account id (`RecentPayee.accountId`).
 * @property accountNo Recipient account number.
 * @property officeId Recipient office id.
 * @property officeName Recipient office name.
 * @property lastTransferDate Raw transfer-date string as returned by TransferDetail.
 * @property lastAmount Amount of the most recent transfer to this recipient.
 * @property currency ISO currency code (`Currency.code`).
 * @property lastTransferDateSortKey Cached sort key (epoch-ms-ish or lexical) —
 *   filled at write time from the raw string so the DAO can `ORDER BY` without
 *   a runtime parse. Currently `0L` if the derive path doesn't compute one.
 * @property fetchedAtEpochMs Local derive-write timestamp — set by the repo for
 *   observability + a future stale-row prune.
 */
@DbEntity
@Entity(
    tableName = "wallet_recent_payees",
    primaryKeys = ["sourceAccountId", "clientId"],
    indices = [
        Index(value = ["sourceAccountId"]),
        Index(value = ["clientId"]),
    ],
)
data class RecentPayeeEntity(
    val sourceAccountId: Long,
    val clientId: Long,
    val clientName: String,
    val accountId: Long,
    val accountNo: String,
    val officeId: Long,
    val officeName: String,
    val lastTransferDate: String,
    val lastAmount: Double,
    val currency: String,
    val lastTransferDateSortKey: Long = 0L,
    val fetchedAtEpochMs: Long = 0L,
)
