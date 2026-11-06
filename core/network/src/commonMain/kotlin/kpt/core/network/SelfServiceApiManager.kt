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

class SelfServiceApiManager(
    private val ktorfitClient: KtorfitClient,
) : PocketDataManager {
    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    override val clientsApi by lazy { ktorfitClient.clientsApi }

    val savingAccountsListApi by lazy { ktorfitClient.savingsAccountsApi }

    val registrationAPi by lazy { ktorfitClient.registrationAPi }

    val beneficiaryApi by lazy { ktorfitClient.beneficiaryApi }

    val thirdPartyTransferApi by lazy { ktorfitClient.thirdPartyTransferApi }

    val accountTransfersApi by lazy { ktorfitClient.accountTransfersApi }

    val officeApi by lazy { ktorfitClient.officeApi }

    val userApi by lazy { ktorfitClient.userApi }

    override val pocketApi by lazy { ktorfitClient.pocketApi }

    override val shareAccountApi by lazy { ktorfitClient.shareAccountApi }
}
