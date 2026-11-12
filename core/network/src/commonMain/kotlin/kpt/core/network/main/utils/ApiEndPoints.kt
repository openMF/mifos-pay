/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.utils

/** Relative endpoint paths of the Fineract APIs used by the services. */
object ApiEndPoints {
    // This class contains all the Constants for API End Points
    /** Path segment for authentication. */
    const val AUTHENTICATION = "authentication"

    /** Path segment for OAuth token requests. */
    const val AUTHENTICATION_OAUTH = "oauth/token"

    /** Path segment for registration. */
    const val REGISTRATION = "registration"

    /** Path segment for clients. */
    const val CLIENTS = "clients"

    /** Path segment for savings accounts. */
    const val SAVINGS_ACCOUNTS = "savingsaccounts"

    /** Path segment for transactions. */
    const val TRANSACTIONS = "transactions"

    /** Path segment for recurring deposit accounts. */
    const val RECURRING_ACCOUNTS = "recurringdepositaccounts"

    /** Path segment for search. */
    const val SEARCH = "search"

    /** Path segment for beneficiaries. */
    const val BENEFICIARIES = "beneficiaries"

    /** Path segment for account transfers. */
    const val ACCOUNT_TRANSFER = "accounttransfers"

    /** Path segment for documents. */
    const val DOCUMENTS = "documents"

    /** Path segment for data tables. */
    const val DATATABLES = "datatables"

    /** Path segment for two-factor authentication. */
    const val TWOFACTOR = "twofactor"

    /** Path segment for running reports. */
    const val RUN_REPORT = "runreports"

    /** Path segment for users. */
    const val USER = "users"

    /** Path segment for the signed-in (self) user. */
    const val USER_SELF = "user"

    /** Path segment for standing instructions. */
    const val STANDING_INSTRUCTION = "standinginstructions"

    /** Path segment for offices. */
    const val OFFICES = "offices"

    // TODO: Verify with backend team and update according to finalized API contract
    /** Path segment for auto-pay. */
    const val AUTO_PAY = "autopay"

    // TODO: Update endpoint path when backend APIs are finalized
    /** Path segment for billers. */
    const val BILLERS = "billers"

    /** Path segment for pockets. */
    const val POCKETS = "pockets"

    /** Path segment for share accounts. */
    const val SHARE_ACCOUNTS = "shareaccounts"
}
