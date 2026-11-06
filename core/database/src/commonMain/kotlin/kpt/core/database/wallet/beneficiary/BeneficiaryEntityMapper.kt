/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.beneficiary

import kpt.core.model.beneficiary.Beneficiary
/**
 * Entity ↔ domain mapper for the `wallet_beneficiaries` LEDGER table.
 *
 * The nested `AccountType` payload is flat-packed into `accountType*` columns;
 * no JSON encode/decode step is needed for `Beneficiary` (unlike the
 * `wallet_transactions` LEDGER which JSON-encodes `Transfer` +
 * `PaymentDetailData`).
 */

/** Convert a persisted row back into the domain [Beneficiary] the app renders. */
fun BeneficiaryEntity.toDomain(): Beneficiary = Beneficiary(
    id = id,
    name = name,
    officeName = officeName,
    clientName = clientName,
    accountType = Beneficiary.AccountType(
        id = accountTypeId,
        code = accountTypeCode,
        value = accountTypeValue,
    ),
    accountNumber = accountNumber,
    transferLimit = transferLimit,
    officeId = officeId,
)

/**
 * Convert a fetched domain [Beneficiary] into a persistable row.
 *
 * The Fineract `beneficiaryList()` endpoint does NOT round-trip a `clientId` per
 * row (the list is scoped to the authenticated session), so the caller passes
 * the owning [clientId] from the store key.
 *
 * @param clientId owning client id — the store's page key.
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Beneficiary.toEntity(clientId: Long, fetchedAtEpochMs: Long): BeneficiaryEntity =
    BeneficiaryEntity(
        id = id,
        clientId = clientId,
        name = name,
        officeName = officeName,
        clientName = clientName,
        accountNumber = accountNumber,
        transferLimit = transferLimit,
        officeId = officeId,
        accountTypeId = accountType.id,
        accountTypeCode = accountType.code,
        accountTypeValue = accountType.value,
        fetchedAtEpochMs = fetchedAtEpochMs,
    )
