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

import androidx.room3.Entity
import androidx.room3.Index
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a single beneficiary — the Room mirror of the
 * `kpt.core.model.beneficiary.Beneficiary` domain model.
 *
 * Stored in the `wallet_beneficiaries` table. LEDGER shape — many rows per client,
 * page-keyed by [clientId]. The Fineract `beneficiaryList()` endpoint returns the
 * list scoped to the authenticated session (no `clientId` path parameter), but the
 * cache is scoped per-clientId so a logout/login as a different client never surfaces
 * the previous user's list.
 *
 * **Read is offline-first, WRITE STAYS ONLINE (GOAL D1).** This entity is populated
 * exclusively by the Store5 `provideBeneficiaryStore` fetcher's writer lambda — no
 * user-facing write path (`SelfServiceRepository.createBeneficiary` /
 * `updateBeneficiary` / `deleteBeneficiary`) ever calls `BeneficiaryDao.upsert*`
 * directly. Server-echoed rows appear here on the next refresh cycle.
 *
 * ## Column mapping
 *
 * The domain `Beneficiary.accountType` nested `AccountType` payload is flat-packed
 * inline (`accountType*` columns) so no JSON encode/decode is needed on the hot
 * read path and the schema stays free of nested-type converters. `id + clientId`
 * is the composite PK — the same beneficiary `id` from Fineract paired with the
 * owning client so a global unique-id collision across clients cannot conflate
 * rows.
 *
 * @property id Fineract beneficiary id (unique per client-list).
 * @property clientId Owning client id (the store key, doubles as page cursor).
 * @property name User-set display name.
 * @property officeName Fineract office name.
 * @property clientName Fineract client-name value from the server row (may differ
 *   from the owning client's display name).
 * @property accountNumber Human-visible account number.
 * @property transferLimit Signed transfer-limit ceiling.
 * @property officeId Nullable office id.
 * @property accountTypeId / [accountTypeCode] / [accountTypeValue] — flat-packed
 *   `Beneficiary.AccountType` fields.
 * @property fetchedAtEpochMs Local cache-write timestamp — set by the store's writer
 *   for observability + a future stale-row prune.
 *
 * @property accountTypeCode Code of the beneficiary account type.
 * @property accountTypeValue Display value of the beneficiary account type.
 */
@DbEntity
@Entity(
    tableName = "wallet_beneficiaries",
    primaryKeys = ["id", "clientId"],
    indices = [
        Index(value = ["clientId"]),
    ],
)
data class BeneficiaryEntity(
    val id: Long,
    val clientId: Long,
    val name: String,
    val officeName: String,
    val clientName: String,
    val accountNumber: String,
    val transferLimit: Int,
    val officeId: Long?,
    val accountTypeId: Int,
    val accountTypeCode: String,
    val accountTypeValue: String,
    val fetchedAtEpochMs: Long = 0L,
)
