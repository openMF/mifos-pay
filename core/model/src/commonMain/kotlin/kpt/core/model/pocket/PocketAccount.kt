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
/**
 * An account that has been linked to a pocket.
 *
 * @property pocketId Identifier of the pocket the account is linked to.
 * @property id Identifier of the pocket account link.
 * @property accountId Identifier of the linked account.
 * @property accountType Type of the account.
 * @property accountNumber Account number.
 */
data class PocketAccount(
    val pocketId: Long,
    val id: Long,
    val accountId: Long,
    val accountType: AccountType,
    val accountNumber: String,
)

/**
 * A pocket-linked account enriched with product, balance and currency details for display.
 *
 * @property pocket The linked pocket account.
 * @property productName Name of the savings product.
 * @property balance Current account balance.
 * @property currencyCode ISO currency code.
 * @property decimalPlaces Number of decimal places for the currency.
 * @property status Status of the account.
 * @property currencyDisplaySymbol Display symbol of the currency, for example "$"; null when unavailable.
 * @property syncStatus Local sync state of the record, "SYNCED" by default.
 */
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

/**
 * An account that the user can link to a pocket, with its balance and currency details.
 *
 * @property accountId Identifier of the account.
 * @property productName Name of the savings product.
 * @property accountNumber Account number.
 * @property accountType Type of the account.
 * @property balance Current account balance.
 * @property currencyCode ISO currency code.
 * @property decimalPlaces Number of decimal places for the currency.
 * @property status Status of the account.
 * @property currencyDisplaySymbol Display symbol of the currency, for example "$"; null when unavailable.
 */
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

/**
 * Lifecycle status of a savings account as shown for pocket accounts.
 */
enum class AccountStatus {
    /** Submitted and awaiting approval. */
    PENDING,

    /** Approved but not yet activated. */
    APPROVED,

    /** Active and usable. */
    ACTIVE,

    /** Closed. */
    CLOSED,

    /** Application was rejected. */
    REJECTED,

    /** Application was withdrawn by the applicant. */
    WITHDRAWN,

    /** Loan account that has been overpaid. */
    OVERPAID,

    /** Reached its maturity date. */
    MATURED,

    /** Status not recognised. */
    UNKNOWN,
}
