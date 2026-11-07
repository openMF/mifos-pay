/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network

/** Entry point for the Fineract API services; exposes each service created by [KtorfitClient]. */
class FineractApiManager(
    private val ktorfitClient: KtorfitClient,
) {

    /** Authentication API. */
    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    /** Clients API. */
    val clientsApi by lazy { ktorfitClient.clientsApi }

    /** Registration API. */
    val registrationAPi by lazy { ktorfitClient.registrationAPi }

    /** Search API. */
    val searchApi by lazy { ktorfitClient.searchApi }

    /** Documents API. */
    val documentApi by lazy { ktorfitClient.documentApi }

    /** Run-reports API. */
    val runReportApi by lazy { ktorfitClient.runReportApi }

    /** Two-factor authentication API. */
    val twoFactorAuthApi by lazy { ktorfitClient.twoFactorAuthApi }

    /** Account transfers API. */
    val accountTransfersApi by lazy { ktorfitClient.accountTransfersApi }

    /** Saved cards API. */
    val savedCardApi by lazy { ktorfitClient.savedCardApi }

    /** KYC level 1 API. */
    val kycLevel1Api by lazy { ktorfitClient.kycLevel1Api }

    /** Invoices API. */
    val invoiceApi by lazy { ktorfitClient.invoiceApi }

    /** Users API. */
    val userApi by lazy { ktorfitClient.userApi }

    /** Third-party transfers API. */
    val thirdPartyTransferApi by lazy { ktorfitClient.thirdPartyTransferApi }

    /** Notifications API. */
    val notificationApi by lazy { ktorfitClient.notificationApi }

    /** Savings accounts API. */
    val savingsAccountsApi by lazy { ktorfitClient.savingsAccountsApi }

    /** Standing instructions API. */
    val standingInstructionApi by lazy { ktorfitClient.standingInstructionApi }

    /** Auto-pay API. */
    val autoPayApi by lazy { ktorfitClient.autoPayApi }

    /** Billers API. */
    val billerApi by lazy { ktorfitClient.billerApi }

    /** Bills API. */
    val billApi by lazy { ktorfitClient.billApi }
}
