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

/** Entry point for the Fineract self-service API services created by [KtorfitClient]. */
class SelfServiceApiManager(
    private val ktorfitClient: KtorfitClient,
) : PocketDataManager {
    /** Authentication API. */
    val authenticationApi by lazy { ktorfitClient.authenticationApi }

    override val clientsApi by lazy { ktorfitClient.clientsApi }

    /** Savings accounts list API. */
    val savingAccountsListApi by lazy { ktorfitClient.savingsAccountsApi }

    /** Registration API. */
    val registrationAPi by lazy { ktorfitClient.registrationAPi }

    /** Beneficiaries API. */
    val beneficiaryApi by lazy { ktorfitClient.beneficiaryApi }

    /** Third-party transfers API. */
    val thirdPartyTransferApi by lazy { ktorfitClient.thirdPartyTransferApi }

    /** Account transfers API. */
    val accountTransfersApi by lazy { ktorfitClient.accountTransfersApi }

    /** Offices API. */
    val officeApi by lazy { ktorfitClient.officeApi }

    /** Users API. */
    val userApi by lazy { ktorfitClient.userApi }

    override val pocketApi by lazy { ktorfitClient.pocketApi }

    override val shareAccountApi by lazy { ktorfitClient.shareAccountApi }
}
