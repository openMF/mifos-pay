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

import de.jensklingenberg.ktorfit.Ktorfit
import kpt.core.network.main.services.createAccountTransfersService
import kpt.core.network.main.services.createAuthenticationService
import kpt.core.network.main.services.createAutoPayService
import kpt.core.network.main.services.createBeneficiaryService
import kpt.core.network.main.services.createBillService
import kpt.core.network.main.services.createBillerService
import kpt.core.network.main.services.createClientService
import kpt.core.network.main.services.createDocumentService
import kpt.core.network.main.services.createInterBankService
import kpt.core.network.main.services.createInvoiceService
import kpt.core.network.main.services.createKYCLevel1Service
import kpt.core.network.main.services.createNotificationService
import kpt.core.network.main.services.createOfficeService
import kpt.core.network.main.services.createPocketService
import kpt.core.network.main.services.createRegistrationService
import kpt.core.network.main.services.createRunReportService
import kpt.core.network.main.services.createSavedCardService
import kpt.core.network.main.services.createSavingsAccountsService
import kpt.core.network.main.services.createSearchService
import kpt.core.network.main.services.createShareAccountService
import kpt.core.network.main.services.createStandingInstructionService
import kpt.core.network.main.services.createThirdPartyTransferService
import kpt.core.network.main.services.createTwoFactorAuthService
import kpt.core.network.main.services.createUserService
class KtorfitClient(
    ktorfit: Ktorfit,
) {
    internal val authenticationApi by lazy { ktorfit.createAuthenticationService() }

    internal val clientsApi by lazy { ktorfit.createClientService() }

    internal val registrationAPi by lazy { ktorfit.createRegistrationService() }

    internal val searchApi by lazy { ktorfit.createSearchService() }

    internal val documentApi by lazy { ktorfit.createDocumentService() }

    internal val runReportApi by lazy { ktorfit.createRunReportService() }

    internal val twoFactorAuthApi by lazy { ktorfit.createTwoFactorAuthService() }

    internal val accountTransfersApi by lazy { ktorfit.createAccountTransfersService() }

    internal val savedCardApi by lazy { ktorfit.createSavedCardService() }

    internal val kycLevel1Api by lazy { ktorfit.createKYCLevel1Service() }

    internal val invoiceApi by lazy { ktorfit.createInvoiceService() }

    internal val userApi by lazy { ktorfit.createUserService() }

    internal val thirdPartyTransferApi by lazy { ktorfit.createThirdPartyTransferService() }

    internal val notificationApi by lazy { ktorfit.createNotificationService() }

    internal val savingsAccountsApi by lazy { ktorfit.createSavingsAccountsService() }

    internal val standingInstructionApi by lazy { ktorfit.createStandingInstructionService() }

    internal val autoPayApi by lazy { ktorfit.createAutoPayService() }

    internal val beneficiaryApi by lazy { ktorfit.createBeneficiaryService() }

    internal val interBankApi by lazy { ktorfit.createInterBankService() }

    internal val officeApi by lazy { ktorfit.createOfficeService() }

    internal val billerApi by lazy { ktorfit.createBillerService() }

    internal val billApi by lazy { ktorfit.createBillService() }

    internal val pocketApi by lazy { ktorfit.createPocketService() }

    internal val shareAccountApi by lazy { ktorfit.createShareAccountService() }
}
