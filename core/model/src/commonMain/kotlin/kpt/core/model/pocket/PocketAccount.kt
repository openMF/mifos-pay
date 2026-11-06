/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.pocket

import kpt.core.model.enums.AccountType
data class PocketAccount(
    val pocketId: Long,
    val id: Long,
    val accountId: Long,
    val accountType: AccountType,
    val accountNumber: String,
)

data class DetailedPocketAccount(
    val pocket: PocketAccount,
    val productName: String?,
    val balance: Double?,
    val currencyCode: String?,
    val decimalPlaces: Int?,
    val status: AccountStatus?,
    // Upstream PR #2057 (manage-pocket) added the per-currency display symbol
    // (e.g. "$"/"₹") separately from the ISO `currencyCode`. Defaulted to null so
    // pre-manage-pocket call sites (PocketStore, PocketEntityMapper, Store5 fetcher
    // decoders that were written before this field existed) continue to compile
    // without change; those paths surface `null` here and the UI falls back to the
    // pre-manage-pocket rendering (code-only). Fresh network fetches that pass the
    // symbol through populate it end-to-end.
    val currencyDisplaySymbol: String? = null,
    val syncStatus: String = "SYNCED",
)

data class LinkableAccount(
    val accountId: Long,
    val productName: String?,
    val accountNumber: String?,
    val accountType: AccountType,
    val balance: Double?,
    val currencyCode: String?,
    val decimalPlaces: Int?,
    val status: AccountStatus?,
    // See rationale on [DetailedPocketAccount.currencyDisplaySymbol].
    val currencyDisplaySymbol: String? = null,
)

enum class AccountStatus {
    PENDING,
    APPROVED,
    ACTIVE,
    CLOSED,
    REJECTED,
    WITHDRAWN,
    OVERPAID,
    MATURED,
    UNKNOWN,
}
