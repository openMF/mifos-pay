/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

/** Shared string and numeric constants used by the data layer. */
object Constants {
    /** Prefix of the HTTP Basic authorization header value. */
    const val BASIC = "Basic "

    /** Resource name for savings accounts. */
    const val SAVINGS = "savingsAccounts"

    /** Resource name for transactions. */
    const val TRANSACTIONS = "transactions"

    /** Savings product id of the default wallet account. */
    const val WALLET_ACCOUNT_SAVINGS_PRODUCT_ID = 1

    /** Savings product id used for merchant accounts. */
    const val MIFOS_MERCHANT_SAVINGS_PRODUCT_ID = 165 // 372

    /** Savings product id used for consumer accounts. */
    const val MIFOS_CONSUMER_SAVINGS_PRODUCT_ID = 165 // 373
    private const val MOBILE_WALLET_ROLE_ID = 2
    private const val SUPER_USER_ROLE_ID = 1

    /** Role ids assigned to newly created users: the mobile-wallet role and the super-user role. */
    val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)

    /** Entity type name for clients. */
    const val ENTITY_TYPE_CLIENTS = "clients"

    /** Use-case name used when fetching an account transfer. */
    const val FETCH_ACCOUNT_TRANSFER_USECASE = "FetchAccountTransfer"

    /** Message shown when the client has no saved cards. */
    const val NO_SAVED_CARDS = "No saved cards."

    /** Default password for new mifos users. */
    const val MIFOS_PASSWORD = "mifos:password"

    /** Error message shown when verifying a user fails. */
    const val ERROR_VERIFYING_USER = "Error verifying user"

    /** Error message shown when registering a user fails. */
    const val ERROR_REGISTERING_USER = "Error registering user"

    /** Message shown when no clients are found. */
    const val NO_CLIENTS_FOUND = "No clients found"

    /** Error message shown when searching clients fails. */
    const val ERROR_SEARCHING_CLIENTS = "Error searching clients"

    /** Resource name for clients. */
    const val CLIENTS = "clients"

    /** Message shown when no accounts are found. */
    const val NO_ACCOUNTS_FOUND = "No accounts found"

    /** Error message shown when fetching accounts fails. */
    const val ERROR_FETCHING_ACCOUNTS = "Error fetching accounts"

    /** Error message shown when making a transfer fails. */
    const val ERROR_MAKING_TRANSFER = "Error making transfer"

    /** Description used for wallet-to-wallet transfers. */
    const val WALLET_TRANSFER = "Wallet transfer"

    /** Error message shown when adding a beneficiary fails. */
    const val ERROR_ADDING_BENEFICIARY = "Error adding beneficiary"

    /** Error message shown when fetching beneficiaries fails. */
    const val ERROR_FETCHING_BENEFICIARIES = "Error fetching beneficiaries"

    /** Error message shown when fetching the destination account fails. */
    const val ERROR_FETCHING_TO_ACCOUNT = "Error fetching to account"

    /** Message shown when no wallet is found. */
    const val NO_WALLET_FOUND = "No wallet found"

    /** Error message shown when fetching the source account fails. */
    const val ERROR_FETCHING_FROM_ACCOUNT = "Error fetching from account"

    /** Error message shown when fetching client data fails. */
    const val ERROR_FETCHING_CLIENT_DATA = "Error fetching client data"

    /** Message shown when no client is found. */
    const val NO_CLIENT_FOUND = "No client found"

    /** Error message shown when logging in fails. */
    const val ERROR_LOGGING_IN = "Error logging in"

    /** Error message shown when fetching remote account transactions fails. */
    const val ERROR_FETCHING_REMOTE_ACCOUNT_TRANSACTIONS =
        "Error fetching remote account transactions"

    /** File type label for PDF documents. */
    const val PDF = "PDF"

    /** Error message shown when fetching an account fails. */
    const val ERROR_FETCHING_ACCOUNT = "Error fetching account"

    /** Message shown when no account is found. */
    const val NO_ACCOUNT_FOUND = "No account found"

    /** Message shown when an invoice does not exist. */
    const val INVOICE_DOES_NOT_EXIST = "Invoice does not exist."

    /** Error message for an invalid UPL. */
    const val INVALID_UPL = "Invalid UPL"

    /** Client type value for merchants. */
    const val MERCHANT = "merchant"

    /** Use-case name used when fetching client details. */
    const val FETCH_CLIENT_DETAILS_USE_CASE = "Fetch Client Details UseCase"

    /** Error message shown when fetching notifications fails. */
    const val ERROR_FETCHING_NOTIFICATIONS = "Error fetching notifications"

    /** HTTP status text of an unauthorized response. */
    const val UNAUTHORIZED_ERROR = "401 Unauthorized"
}
