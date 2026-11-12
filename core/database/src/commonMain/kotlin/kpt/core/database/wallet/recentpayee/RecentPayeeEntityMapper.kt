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

import kpt.core.model.account.RecentPayee
/**
 * Entity ↔ domain mapper for the `wallet_recent_payees` LOCAL-DERIVED table.
 *
 * All fields are scalar — no nested types, no JSON blobs. Sort-key derivation
 * (from `RecentPayee.lastTransferDate`) is the repository's job; the mapper
 * receives the pre-computed [sortKey] verbatim.
 */

/** Convert a persisted row back into the domain [RecentPayee]. */
fun RecentPayeeEntity.toDomain(): RecentPayee = RecentPayee(
    clientId = clientId,
    clientName = clientName,
    accountId = accountId,
    accountNo = accountNo,
    officeId = officeId,
    officeName = officeName,
    lastTransferDate = lastTransferDate,
    lastAmount = lastAmount,
    currency = currency,
)

/**
 * Convert a derived domain [RecentPayee] into a persistable row.
 *
 * @param sourceAccountId Owning savings-account id — the store's page key.
 * @param sortKey Pre-computed sort key (typically an epoch-derived Long from
 *   [RecentPayee.lastTransferDate]) — used by the DAO's `ORDER BY` at read time
 *   so the sort is a scalar compare, not a runtime parse. Pass `0L` when no
 *   parse is available; the DAO falls back to lexical order on the raw string.
 * @param fetchedAtEpochMs local derive-write timestamp.
 */
fun RecentPayee.toEntity(
    sourceAccountId: Long,
    sortKey: Long,
    fetchedAtEpochMs: Long,
): RecentPayeeEntity = RecentPayeeEntity(
    sourceAccountId = sourceAccountId,
    clientId = clientId,
    clientName = clientName,
    accountId = accountId,
    accountNo = accountNo,
    officeId = officeId,
    officeName = officeName,
    lastTransferDate = lastTransferDate,
    lastAmount = lastAmount,
    currency = currency,
    lastTransferDateSortKey = sortKey,
    fetchedAtEpochMs = fetchedAtEpochMs,
)
