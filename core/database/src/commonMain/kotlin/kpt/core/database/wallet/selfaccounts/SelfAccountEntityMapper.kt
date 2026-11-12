/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.selfaccounts

import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.AccountType
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
/**
 * Entity ↔ domain mapper for the `wallet_self_accounts` table.
 *
 * Three nested `@Serializable` payloads (`Currency`, `Status`, nullable
 * `AccountType`) are persisted as JSON string columns. See [SelfAccountEntity]'s
 * KDoc for the rationale (single-record whole-payload read model, no per-field
 * query need).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }
private val accountTypeNullableSerializer = AccountType.serializer().nullable

/** Convert a persisted row back into the domain [Account] the app renders. */
fun SelfAccountEntity.toDomain(): Account = Account(
    image = image,
    name = name,
    number = number,
    balance = balance,
    id = id,
    externalId = externalId,
    productName = productName,
    productId = productId,
    currency = json.decodeFromString(Currency.serializer(), currencyJson),
    status = json.decodeFromString(Status.serializer(), statusJson),
    clientName = clientName,
    accountType = json.decodeFromString(accountTypeNullableSerializer, accountTypeJson),
    officeName = officeName,
    officeId = officeId,
)

/**
 * Convert a fetched domain [Account] into a persistable row.
 *
 * The Fineract `clientsApi.getAccounts(clientId, ...)` endpoint scopes the list
 * to a specific client, so the caller passes the owning [clientId] from the
 * store key (parity with the `Beneficiary` and `Pocket` mappers).
 *
 * @param clientId owning client id — the store's page key.
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Account.toEntity(clientId: Long, fetchedAtEpochMs: Long): SelfAccountEntity =
    SelfAccountEntity(
        id = id,
        clientId = clientId,
        image = image,
        name = name,
        number = number,
        balance = balance,
        externalId = externalId,
        productName = productName,
        productId = productId,
        clientName = clientName,
        officeName = officeName,
        officeId = officeId,
        currencyJson = json.encodeToString(Currency.serializer(), currency),
        statusJson = json.encodeToString(Status.serializer(), status),
        accountTypeJson = json.encodeToString(accountTypeNullableSerializer, accountType),
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
