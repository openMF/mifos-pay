/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.pocket

import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.PocketAccount
/**
 * Entity ↔ domain mapper for the `wallet_pockets` LEDGER table.
 *
 * All columns are scalar; enums are stored as `.name` strings. Enum decode uses
 * `valueOf` with a defensive fallback:
 * - Unknown `AccountType` values fall back to `SAVINGS` — the domain's
 *   AccountType enum has three fixed values (`LOAN`/`SAVINGS`/`SHARE`); a
 *   valueOf failure indicates a schema drift the store5 layer cannot recover
 *   from cleanly. In practice this never fires (values are written by the
 *   store itself, not the network).
 * - Unknown `AccountStatus` values fall back to `AccountStatus.UNKNOWN` — the
 *   domain enum already includes `UNKNOWN` for exactly this class of situation.
 */

/** Convert a persisted row back into the domain [DetailedPocketAccount] the app renders. */
fun PocketEntity.toDomain(): DetailedPocketAccount = DetailedPocketAccount(
    pocket = PocketAccount(
        pocketId = pocketId,
        id = id,
        accountId = accountId,
        accountType = decodeAccountType(accountType),
        accountNumber = accountNumber,
    ),
    productName = productName,
    balance = balance,
    currencyCode = currencyCode,
    currencyDisplaySymbol = currencyDisplaySymbol,
    decimalPlaces = decimalPlaces,
    status = status?.let(::decodeAccountStatus),
    syncStatus = syncStatus,
)

/**
 * Convert a fetched domain [DetailedPocketAccount] into a persistable row.
 *
 * @param clientId owning client id — the store's page key. The Fineract
 *   pocket endpoint does not round-trip a `clientId` per row (the list is
 *   scoped to the authenticated session + the caller-provided clientId
 *   drives per-client account resolution), so the caller passes the owning
 *   [clientId] from the store key (parity with the `Beneficiary` mapper).
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun DetailedPocketAccount.toEntity(clientId: Long, fetchedAtEpochMs: Long): PocketEntity =
    PocketEntity(
        id = pocket.id,
        clientId = clientId,
        pocketId = pocket.pocketId,
        accountId = pocket.accountId,
        accountType = pocket.accountType.name,
        accountNumber = pocket.accountNumber,
        productName = productName,
        balance = balance,
        currencyCode = currencyCode,
        currencyDisplaySymbol = currencyDisplaySymbol,
        decimalPlaces = decimalPlaces,
        status = status?.name,
        fetchedAtEpochMs = fetchedAtEpochMs,
        syncStatus = "SYNCED",
    )

private fun decodeAccountType(name: String): AccountType =
    runCatching { AccountType.valueOf(name) }.getOrDefault(AccountType.SAVINGS)

private fun decodeAccountStatus(name: String): AccountStatus =
    runCatching { AccountStatus.valueOf(name) }.getOrDefault(AccountStatus.UNKNOWN)

fun DetailedPocketAccount.toPendingLinkEntity(clientId: Long, fetchedAtEpochMs: Long): PocketEntity =
    PocketEntity(
        id = if (pocket.id == 0L) kotlin.random.Random.nextLong(Long.MIN_VALUE, -1L) else pocket.id,
        clientId = clientId,
        pocketId = pocket.pocketId,
        accountId = pocket.accountId,
        accountType = pocket.accountType.name,
        accountNumber = pocket.accountNumber,
        productName = productName,
        balance = balance,
        currencyCode = currencyCode,
        currencyDisplaySymbol = currencyDisplaySymbol,
        decimalPlaces = decimalPlaces,
        status = status?.name,
        fetchedAtEpochMs = fetchedAtEpochMs,
        syncStatus = "PENDING_LINK",
    )
