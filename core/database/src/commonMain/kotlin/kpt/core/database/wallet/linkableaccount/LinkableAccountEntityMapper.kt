/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.linkableaccount

import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.LinkableAccount
/**
 * Entity ↔ domain mapper for the `wallet_linkable_accounts` LEDGER table.
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

/** Convert a persisted row back into the domain [LinkableAccount] the app renders. */
fun LinkableAccountEntity.toDomain(): LinkableAccount = LinkableAccount(
    accountId = accountId,
    productName = productName,
    accountNumber = accountNumber,
    accountType = decodeAccountType(accountType),
    balance = balance,
    currencyCode = currencyCode,
    currencyDisplaySymbol = currencyDisplaySymbol,
    decimalPlaces = decimalPlaces,
    status = status?.let(::decodeAccountStatus),
)

/**
 * Convert a fetched domain [LinkableAccount] into a persistable row.
 *
 * The `clientsApi.getClientAccounts` endpoint does not round-trip a `clientId`
 * per row (it is scoped to the path parameter that identifies the client), so
 * the caller passes the owning [clientId] from the store key.
 *
 * @param clientId owning client id — the store's page key.
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun LinkableAccount.toEntity(clientId: Long, fetchedAtEpochMs: Long): LinkableAccountEntity =
    LinkableAccountEntity(
        accountId = accountId,
        clientId = clientId,
        accountType = accountType.name,
        productName = productName,
        accountNumber = accountNumber,
        balance = balance,
        currencyCode = currencyCode,
        currencyDisplaySymbol = currencyDisplaySymbol,
        decimalPlaces = decimalPlaces,
        status = status?.name,
        fetchedAtEpochMs = fetchedAtEpochMs,
    )

private fun decodeAccountType(name: String): AccountType =
    runCatching { AccountType.valueOf(name) }.getOrDefault(AccountType.SAVINGS)

private fun decodeAccountStatus(name: String): AccountStatus =
    runCatching { AccountStatus.valueOf(name) }.getOrDefault(AccountStatus.UNKNOWN)
