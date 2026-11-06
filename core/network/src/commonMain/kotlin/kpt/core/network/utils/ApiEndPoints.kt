/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.utils

object ApiEndPoints {
    // This class contains all the Constants for API End Points
    const val AUTHENTICATION = "authentication"
    const val AUTHENTICATION_OAUTH = "oauth/token"
    const val REGISTRATION = "registration"
    const val CLIENTS = "clients"
    const val SAVINGS_ACCOUNTS = "savingsaccounts"
    const val TRANSACTIONS = "transactions"
    const val RECURRING_ACCOUNTS = "recurringdepositaccounts"
    const val SEARCH = "search"
    const val BENEFICIARIES = "beneficiaries"
    const val ACCOUNT_TRANSFER = "accounttransfers"
    const val DOCUMENTS = "documents"
    const val DATATABLES = "datatables"
    const val TWOFACTOR = "twofactor"
    const val RUN_REPORT = "runreports"
    const val USER = "users"

    const val USER_SELF = "user"
    const val STANDING_INSTRUCTION = "standinginstructions"
    const val OFFICES = "offices"

    // TODO: Verify with backend team and update according to finalized API contract
    const val AUTO_PAY = "autopay"

    // TODO: Update endpoint path when backend APIs are finalized
    const val BILLERS = "billers"

    const val POCKETS = "pockets"
    const val SHARE_ACCOUNTS = "shareaccounts"
}
