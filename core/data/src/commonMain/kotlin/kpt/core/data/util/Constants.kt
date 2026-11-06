/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

object Constants {
    const val BASIC = "Basic "
    const val SAVINGS = "savingsAccounts"
    const val TRANSACTIONS = "transactions"
    const val WALLET_ACCOUNT_SAVINGS_PRODUCT_ID = 1
    const val MIFOS_MERCHANT_SAVINGS_PRODUCT_ID = 165 // 372
    const val MIFOS_CONSUMER_SAVINGS_PRODUCT_ID = 165 // 373
    private const val MOBILE_WALLET_ROLE_ID = 2
    private const val SUPER_USER_ROLE_ID = 1

    val NEW_USER_ROLE_IDS: List<Int> = listOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)
    const val ENTITY_TYPE_CLIENTS = "clients"
    const val FETCH_ACCOUNT_TRANSFER_USECASE = "FetchAccountTransfer"
    const val NO_SAVED_CARDS = "No saved cards."
    const val MIFOS_PASSWORD = "mifos:password"
    const val ERROR_VERIFYING_USER = "Error verifying user"
    const val ERROR_REGISTERING_USER = "Error registering user"
    const val NO_CLIENTS_FOUND = "No clients found"
    const val ERROR_SEARCHING_CLIENTS = "Error searching clients"
    const val CLIENTS = "clients"
    const val NO_ACCOUNTS_FOUND = "No accounts found"
    const val ERROR_FETCHING_ACCOUNTS = "Error fetching accounts"
    const val ERROR_MAKING_TRANSFER = "Error making transfer"
    const val WALLET_TRANSFER = "Wallet transfer"
    const val ERROR_ADDING_BENEFICIARY = "Error adding beneficiary"
    const val ERROR_FETCHING_BENEFICIARIES = "Error fetching beneficiaries"
    const val ERROR_FETCHING_TO_ACCOUNT = "Error fetching to account"
    const val NO_WALLET_FOUND = "No wallet found"
    const val ERROR_FETCHING_FROM_ACCOUNT = "Error fetching from account"
    const val ERROR_FETCHING_CLIENT_DATA = "Error fetching client data"
    const val NO_CLIENT_FOUND = "No client found"
    const val ERROR_LOGGING_IN = "Error logging in"
    const val ERROR_FETCHING_REMOTE_ACCOUNT_TRANSACTIONS =
        "Error fetching remote account transactions"
    const val PDF = "PDF"
    const val ERROR_FETCHING_ACCOUNT = "Error fetching account"
    const val NO_ACCOUNT_FOUND = "No account found"
    const val INVOICE_DOES_NOT_EXIST = "Invoice does not exist."
    const val INVALID_UPL = "Invalid UPL"
    const val MERCHANT = "merchant"
    const val FETCH_CLIENT_DETAILS_USE_CASE = "Fetch Client Details UseCase"
    const val ERROR_FETCHING_NOTIFICATIONS = "Error fetching notifications"
    const val UNAUTHORIZED_ERROR = "401 Unauthorized"
}
