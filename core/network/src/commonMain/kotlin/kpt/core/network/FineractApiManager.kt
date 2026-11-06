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

class FineractApiManager(
    private val ktorfitClient: KtorfitClient,
) {

    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    val clientsApi by lazy { ktorfitClient.clientsApi }

    val registrationAPi by lazy { ktorfitClient.registrationAPi }

    val searchApi by lazy { ktorfitClient.searchApi }

    val documentApi by lazy { ktorfitClient.documentApi }

    val runReportApi by lazy { ktorfitClient.runReportApi }

    val twoFactorAuthApi by lazy { ktorfitClient.twoFactorAuthApi }

    val accountTransfersApi by lazy { ktorfitClient.accountTransfersApi }

    val savedCardApi by lazy { ktorfitClient.savedCardApi }

    val kycLevel1Api by lazy { ktorfitClient.kycLevel1Api }

    val invoiceApi by lazy { ktorfitClient.invoiceApi }

    val userApi by lazy { ktorfitClient.userApi }

    val thirdPartyTransferApi by lazy { ktorfitClient.thirdPartyTransferApi }

    val notificationApi by lazy { ktorfitClient.notificationApi }

    val savingsAccountsApi by lazy { ktorfitClient.savingsAccountsApi }

    val standingInstructionApi by lazy { ktorfitClient.standingInstructionApi }

    val autoPayApi by lazy { ktorfitClient.autoPayApi }

    val billerApi by lazy { ktorfitClient.billerApi }

    val billApi by lazy { ktorfitClient.billApi }
}
