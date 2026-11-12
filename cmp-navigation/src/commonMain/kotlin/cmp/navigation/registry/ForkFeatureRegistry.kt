/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navOptions
import cmp.navigation.generated.resources.feature_finance_accounts
import cmp.navigation.generated.resources.feature_finance_beneficiaries
import cmp.navigation.registry.app.ForkAppModules
import cmp.navigation.registry.app.navigation.navigateToTransferOptions
import kpt.core.data.di.ForkRepositoryModule
import kpt.core.data.repository.UserVerificationRepository
import kpt.core.database.di.ForkDatabaseModule
import kpt.core.datastore.di.ForkDatastoreModule
import kpt.core.network.di.ForkNetworkModule
import kpt.core.store.di.ForkStoreModule
import kpt.core.ui.utility.TabContent
import kpt.feature.accounts.AccountsScreen
import kpt.feature.accounts.savingsaccount.SavingsAddEditType
import kpt.feature.accounts.savingsaccount.addEditSavingAccountScreen
import kpt.feature.accounts.savingsaccount.details.navigateToSavingAccountDetails
import kpt.feature.accounts.savingsaccount.details.savingAccountDetailRoute
import kpt.feature.accounts.savingsaccount.navigateToSavingAccountAddEdit
import kpt.feature.autopay.AutoPayScreen
import kpt.feature.autopay.navigateToAddBill
import kpt.feature.autopay.navigateToAddBiller
import kpt.feature.autopay.navigateToAutoPay
import kpt.feature.autopay.navigateToAutoPayHistory
import kpt.feature.autopay.navigateToAutoPayPreferences
import kpt.feature.autopay.navigateToAutoPayScheduleDetails
import kpt.feature.autopay.navigateToBillList
import kpt.feature.autopay.navigateToBillerList
import kpt.feature.autopay.navigateToScheduleManagement
import kpt.feature.beneficiary.addupdatebeneficiary.BeneficiaryAddEditType
import kpt.feature.beneficiary.addupdatebeneficiary.addEditBeneficiaryScreen
import kpt.feature.beneficiary.addupdatebeneficiary.navigateToBeneficiaryAddEdit
import kpt.feature.beneficiary.list.BeneficiaryListScreen
import kpt.feature.editpassword.navigation.editPasswordScreen
import kpt.feature.editpassword.navigation.navigateToEditPassword
import kpt.feature.faq.navigation.navigateToFAQ
import kpt.feature.fastmpay.navigation.FAST_MPAY_ROUTE
import kpt.feature.fastmpay.navigation.fastMpayScreen
import kpt.feature.fastmpay.navigation.navigateToFastMpay
import kpt.feature.finance.navigation.FINANCE_ROUTE
import kpt.feature.finance.navigation.financeScreen
import kpt.feature.history.HistoryScreen
import kpt.feature.history.navigation.historyNavigation
import kpt.feature.history.navigation.navigateToHistory
import kpt.feature.history.navigation.navigateToSpecificTransaction
import kpt.feature.history.navigation.navigateToTransactionDetail
import kpt.feature.history.navigation.specificTransactionsScreen
import kpt.feature.history.navigation.transactionDetailNavigation
import kpt.feature.home.navigation.HOME_ROUTE
import kpt.feature.home.navigation.homeScreen
import kpt.feature.kyc.navigation.kycLevel1Screen
import kpt.feature.kyc.navigation.kycLevel2Screen
import kpt.feature.kyc.navigation.kycLevel3Screen
import kpt.feature.kyc.navigation.navigateToKYCLevel2
import kpt.feature.kyc.navigation.navigateToKYCLevel3
import kpt.feature.mpay.qr.navigation.navigateToMpayQrScreen
import kpt.feature.mpay.qr.scan.navigation.SCAN_QR_ROUTE
import kpt.feature.mpay.qr.scan.navigation.navigateToScanQr
import kpt.feature.mpay.qr.scan.navigation.scanQrScreen
import kpt.feature.notification.navigateToNotification
import kpt.feature.passcode.internalPasscodeScreen
import kpt.feature.passcode.navigateToInternalPasscodeScreen
import kpt.feature.payments.PAYMENTS_ROUTE
import kpt.feature.payments.PaymentsScreenContents
import kpt.feature.payments.RequestScreen
import kpt.feature.payments.generated.resources.Res
import kpt.feature.payments.generated.resources.feature_payments_history
import kpt.feature.payments.generated.resources.feature_payments_request
import kpt.feature.payments.generated.resources.feature_payments_send
import kpt.feature.payments.paymentsScreen
import kpt.feature.payments.selectTransferType.SelectTransferTypeScreen
import kpt.feature.pocket.navigation.managePocketDestination
import kpt.feature.pocket.navigation.navigateToManagePocket
import kpt.feature.pocket.navigation.navigateToPocketDashboard
import kpt.feature.pocket.navigation.pocketDashboardScreen
import kpt.feature.profile.navigation.navigateToProfile
import kpt.feature.profile.navigation.profileNavGraph
import kpt.feature.receipt.navigation.navigateToReceipt
import kpt.feature.send.money.AmountUtils
import kpt.feature.send.money.SendMoneyScreen
import kpt.feature.send.money.navigation.PAYMENT_SUCCESS_ROUTE
import kpt.feature.send.money.navigation.PAY_ANYONE_ROUTE
import kpt.feature.send.money.navigation.SEND_MONEY_OPTIONS_ROUTE
import kpt.feature.send.money.navigation.bankTransferScreen
import kpt.feature.send.money.navigation.contactsPickerScreen
import kpt.feature.send.money.navigation.navigateToBankTransferScreen
import kpt.feature.send.money.navigation.navigateToContactsPickerScreen
import kpt.feature.send.money.navigation.navigateToPayAnyoneScreen
import kpt.feature.send.money.navigation.navigateToPayeeDetailsScreen
import kpt.feature.send.money.navigation.navigateToPaymentChatHistoryScreen
import kpt.feature.send.money.navigation.navigateToPaymentDetailsScreen
import kpt.feature.send.money.navigation.navigateToPaymentProcessingScreen
import kpt.feature.send.money.navigation.navigateToPaymentSuccessScreen
import kpt.feature.send.money.navigation.navigateToSearchIfscScreen
import kpt.feature.send.money.navigation.navigateToSendMoneyOptionsScreen
import kpt.feature.send.money.navigation.navigateToSendMoneyScreen
import kpt.feature.send.money.navigation.navigateToUpiPinScreen
import kpt.feature.send.money.navigation.navigateToUpiTransactionHistoryScreen
import kpt.feature.send.money.navigation.payAnyoneScreen
import kpt.feature.send.money.navigation.payeeDetailsScreen
import kpt.feature.send.money.navigation.paymentChatHistoryScreen
import kpt.feature.send.money.navigation.paymentDetailsScreen
import kpt.feature.send.money.navigation.paymentProcessingScreen
import kpt.feature.send.money.navigation.paymentSuccessScreen
import kpt.feature.send.money.navigation.searchIfscScreen
import kpt.feature.send.money.navigation.sendMoneyOptionsScreen
import kpt.feature.send.money.navigation.sendMoneyScreen
import kpt.feature.send.money.navigation.upiPinScreen
import kpt.feature.send.money.navigation.upiTransactionHistoryScreen
import kpt.feature.settings.navigation.settingsScreen
import kpt.feature.transfer.interbank.navigation.interbankTransferScreen
import kpt.feature.transfer.interbank.navigation.navigateToInterbankTransfer
import kpt.feature.transfer.intrabank.navigation.intraBankHubScreen
import kpt.feature.transfer.intrabank.navigation.navigateToIntraBankHub
import kpt.feature.transfer.intrabank.navigation.navigateToTransferConfirm
import kpt.feature.transfer.intrabank.navigation.transferConfirmScreen
import kpt.feature.transfer.intrabank.selectScreen.navigateToSelectAccountScreen
import kpt.feature.transfer.intrabank.selectScreen.selectAccountScreenDestination
import kpt.feature.transfer.intrabank.success.navigateTransferSuccess
import kpt.feature.transfer.intrabank.success.transferSuccessScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.core.module.Module
import org.koin.mp.KoinPlatform
import org.mifos.lib.loan.navigation.loanApplicationGraph
import org.mifos.lib.loan.navigation.navigateToLoanApplicationGraph
import cmp.navigation.generated.resources.Res as SharedRes

/**
 * ForkFeatureRegistry — the FORK-OWNED white-label seam for feature contributions.
 *
 * The template infra modules READ from this registry; a fork extends the app by editing THIS ONE file
 * (+ its build.gradle deps + settings.gradle include), never the template infra files:
 *   - `cmp-navigation/di/KoinModules.kt` includes [featureKoinModules] into the app DI graph.
 *   - `cmp-navigation/.../AuthenticatedNavigation.kt` invokes [featureDestinations] to register routes.
 *
 * Ownership: `owner: fork` in customization-surface.yaml — `sync-dirs`/`white-label-doctor` NEVER
 * overwrite it, so a template sync full-copies the infra modules while your features survive. The
 * template ships this file pre-populated with its demo feature set as the default; a fork replaces the
 * contents with its own (the customizer `--clean` empties both lists).
 */
object ForkFeatureRegistry {
    /**
     * The four per-layer fork seams, plus every `feature/<f>/di` Koin module.
     *
     * The feature half is DERIVED — `:cmp-navigation:generateFeatureKoinBindings` reads each
     * feature module's `di` package and emits [GeneratedFeatureKoinBindings]. Adding a
     * feature no longer means editing this file: previously it took an import AND a list entry
     * here, and forgetting either compiled cleanly while the feature's ViewModels failed to
     * resolve at runtime.
     *
     * The `Fork*Module` seams stay listed BY HAND on purpose — they are core-layer fork seams,
     * not features, and nothing under `feature/` declares them.
     *
     * Feature Koin modules the app installs. The framework SHELL modules (Home, Settings) live in
     * [cmp.navigation.di.KoinModules] and are always present; this is the fork's own features.
     */
    val featureKoinModules: List<Module> = listOf(
        ForkRepositoryModule,
        ForkNetworkModule,
        ForkDatabaseModule,
        ForkDatastoreModule,
        ForkStoreModule,
        GeneratedFeatureKoinBindings,
    ) + ForkAppModules.allModules // the wallet app shell rendered by ForkAppShell.root

    /**
     * Every destination of the signed-in graph.
     *
     * A feature whose screens only open each other declares its graph `@FeatureDestination` in its
     * `navigation` package; `:cmp-navigation:generateFeatureDestinations` collects those into
     * [GeneratedFeatureDestinations]. A screen that opens another feature's screen is wired below
     * instead: features never depend on each other, so this registry is the one place that connects them.
     */
    val featureDestinations: NavGraphBuilder.(NavController) -> Unit = { navController ->
        GeneratedFeatureDestinations(navController)
        homeDestinations(navController)
        historyDestinations(navController)
        financeDestinations(navController)
        paymentDestinations(navController)
        qrPaymentDestinations(navController)
        transferDestinations(navController)
    }

    /**
     * The destinations that act on the app session: the internal passcode gate, settings and change
     * password. They log the user out or change the app locale, which only the app root can do, so the
     * root passes those two actions in.
     *
     * @param onLogout Ends the session and returns to sign-in.
     * @param handleAppLocale Applies a locale on the platform.
     */
    fun NavGraphBuilder.shellDestinations(
        navController: NavController,
        onLogout: () -> Unit,
        handleAppLocale: (locale: String?) -> Unit,
    ) {
        internalPasscodeScreen(
            navigateToLogin = onLogout,
            onAuthenticationSuccess = { verificationKey ->
                KoinPlatform.getKoin().get<UserVerificationRepository>().recordVerification()
                verificationKey?.let {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(it, true)
                }
                navController.popBackStack()
            },
            onAuthenticationFailed = { verificationKey ->
                verificationKey?.let {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(it, false)
                }
                navController.popBackStack()
            },
            onPasscodeChanged = {
                navController.popBackStack()
            },
            onBackPress = {
                navController.popBackStack()
            },
        )

        settingsScreen(
            onBackPress = navController::navigateUp,
            onLogout = onLogout,
            handleAppLocale = { handleAppLocale(it) },
            navigateToPasscodeScreen = { verificationKey ->
                navController.navigateToInternalPasscodeScreen(
                    verificationKey = verificationKey,
                    allowBiometricAuth = false,
                )
            },
            navigateToEditPasswordScreen = navController::navigateToEditPassword,
            navigateToFaqScreen = navController::navigateToFAQ,
            navigateToNotificationScreen = navController::navigateToNotification,
            navigateToProfile = navController::navigateToProfile,
        )

        editPasswordScreen(
            navigateBack = navController::navigateUp,
            onLogOut = onLogout,
        )
    }
}

/** Home, pockets and profile. */
private fun NavGraphBuilder.homeDestinations(navController: NavController) {
    homeScreen(
        onNavigateBack = navController::popBackStack,
        onRequest = {
            navController.navigateToMpayQrScreen()
        },
        onPay = navController::navigateToTransferOptions,
        onAutoPay = {
            navController.navigateToAutoPay()
        },
        navigateToPocketDashboard = navController::navigateToPocketDashboard,
        navigateToTransactionDetail = navController::navigateToSpecificTransaction,
        navigateToAccountDetail = navController::navigateToSavingAccountDetails,
        navigateToHistory = navController::navigateToHistory,
    )

    pocketDashboardScreen(
        navigateBack = navController::navigateUp,
        navigateToManagePocket = navController::navigateToManagePocket,
        navigateToLoanAccountDetail = { /* TODO: Implement loan account detail */ },
        navigateToShareAccountDetail = { /* TODO: Implement share account detail */ },
        navigateToSavingsAccountDetail = navController::navigateToSavingAccountDetails,
    )

    managePocketDestination(
        navigateBack = navController::navigateUp,
    )

    profileNavGraph(
        navController = navController,
        onLinkBankAccount = {
            navController.navigateToSavingAccountAddEdit(SavingsAddEditType.AddItem)
        },
        showQrCode = navController::navigateToMpayQrScreen,
        navigateBack = navController::popBackStack,
    )
}

/** Transaction history and its detail screens. */
private fun NavGraphBuilder.historyDestinations(navController: NavController) {
    historyNavigation(
        viewTransactionDetail = navController::navigateToSpecificTransaction,
    )

    specificTransactionsScreen(
        navigateBack = navController::navigateUp,
        viewTransactionDetail = navController::navigateToTransactionDetail,
    )

    transactionDetailNavigation(
        navigateBack = navController::navigateUp,
        navigateToReceipt = navController::navigateToReceipt,
    )
}

/** The finance tab: accounts, beneficiaries, KYC and loan applications. */
private fun NavGraphBuilder.financeDestinations(navController: NavController) {
    financeScreen(tabContents = { financeTabs(navController) })

    savingAccountDetailRoute(
        navigateBack = navController::navigateUp,
        onViewTransaction = navController::navigateToSpecificTransaction,
    )

    addEditSavingAccountScreen(
        navigateBack = navController::navigateUp,
    )

    addEditBeneficiaryScreen(
        navigateBack = navController::navigateUp,
        navigateToQrReaderScreen = navController::navigateToScanQr,
        navigateToIntraBankTransfer = { officeId, clientId, accountTypeId, accountId, amount, accountName, accountNo ->
            // Navigate to transfer confirm with full QR data
            navController.navigateToTransferConfirm(
                toOfficeId = officeId,
                toClientId = clientId,
                toAccountTypeId = accountTypeId,
                toAccountId = accountId,
                amount = amount,
                toAccountName = accountName,
                toAccountNo = accountNo,
                returnDestination = "home",
            )
        },
        navigateToInterbankTransfer = { accountNumber, recipientName ->
            // Navigate to interbank transfer with pre-filled data
            navController.navigateToInterbankTransfer(
                phoneNumber = accountNumber,
                recipientName = recipientName,
                amount = "",
            )
        },
    )

    kycLevel1Screen(
        navigateBack = navController::navigateUp,
        navigateToKycLevel2 = {
            navController.navigateToKYCLevel2(
                navOptions {
                    restoreState = true
                    popUpTo(FINANCE_ROUTE)
                },
            )
        },
    )

    kycLevel2Screen(
        navigateBack = navController::navigateUp,
        navigateToLevel3 = {
            navController.navigateToKYCLevel3(
                navOptions {
                    restoreState = true
                    popUpTo(FINANCE_ROUTE)
                },
            )
        },
    )

    kycLevel3Screen(
        navigateBack = navController::navigateUp,
    )

    loanApplicationGraph(
        navController = navController,
        navigateBack = navController::navigateUp,
        navigateForPasscodeVerification = { verificationKey ->
            navController.navigateToInternalPasscodeScreen(verificationKey)
        },
        onSubmitSuccess = {
            navController.navigate(FINANCE_ROUTE) {
                popUpTo(FINANCE_ROUTE) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        },
    )
}

/** The payments tab and every send-money flow it starts, including QR payments. */
private fun NavGraphBuilder.paymentDestinations(navController: NavController) {
    paymentsScreen(tabContents = { paymentsTabs(navController) })

    sendMoneyOptionsScreen(
        onBackClick = navController::popBackStack,
        onScanQrClick = {
            // This is now handled by the ViewModel using ML Kit scanner
        },
        onPayAnyoneClick = {
            // TODO: Navigate to Pay Anyone screen
            navController.navigateToPayAnyoneScreen()
        },
        onBankTransferClick = {
            navController.navigateToBankTransferScreen()
        },
        onFineractPaymentsClick = {
            navController.navigateToSendMoneyScreen()
        },
        onAutoPayClick = {
            navController.navigateToAutoPay()
        },
        onQrCodeScanned = { qrData ->
            navController.navigateToSendMoneyScreen(
                requestData = qrData,
                navOptions = navOptions {
                    popUpTo(SEND_MONEY_OPTIONS_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        onNavigateToPayeeDetails = { qrCodeData ->
            navController.navigateToPayeeDetailsScreen(qrCodeData)
        },
        onPaymentHistoryClick = {
            navController.navigateToPaymentChatHistoryScreen()
        },
        onUpiTransactionHistoryClick = {
            navController.navigateToUpiTransactionHistoryScreen()
        },
    )

    sendMoneyScreen(
        onBackClick = navController::popBackStack,
        navigateToTransferScreen = navController::navigateToSendMoneyScreen,
        navigateToPayeeDetailsScreen = navController::navigateToPayeeDetailsScreen,
        navigateToScanQrScreen = navController::navigateToScanQr,
    )

    paymentChatHistoryScreen(
        onBackClick = navController::popBackStack,
        onPaymentClick = {
            navController.navigateToSendMoneyOptionsScreen()
        },
        onTransactionClick = { transactionId ->
            navController.navigateToPaymentDetailsScreen(transactionId)
        },
    )

    upiTransactionHistoryScreen(
        onBackClick = navController::popBackStack,
    )

    paymentDetailsScreen(
        onBackClick = navController::popBackStack,
        onPayAgainClick = {
            navController.navigateToSendMoneyOptionsScreen()
        },
        onRetryClick = {
            navController.popBackStack()
        },
        onShareScreenshot = {
            // Screenshot functionality is handled by the Android-specific PaymentDetailsScreen implementation
            // The actual screenshot and sharing is done within the screen itself
            // This callback is used by the Android-specific implementation to trigger the screenshot
        },
    )

    payAnyoneDestinations(navController)
}

/** Paying a scanned code or a payee: payee details, UPI PIN, processing and success. */
private fun NavGraphBuilder.qrPaymentDestinations(navController: NavController) {
    payeeDetailsScreen(
        onBackClick = navController::popBackStack,
        onNavigateToUpiPin = { state ->
            navController.navigateToUpiPinScreen(
                payeeName = state.payeeName,
                amount = state.amount,
                isUpiCode = state.isUpiCode,
                bankName = state.selectedAccount?.bankName ?: "Bank",
                accountNo = state.selectedAccount?.accountNumber ?: "1234567890123456",
                refId = state.refId,
            )
        },
        onNavigateToUpiPayment = {},
        onNavigateToFineractPayment = {},
    )

    upiPinScreen(
        onBackClick = navController::popBackStack,
        onNavigateToPaymentProcessing = { payeeName, amount, isUpiCode ->
            val amountInPaise = AmountUtils.rupeesToPaise(amount)
            navController.navigateToPaymentProcessingScreen(
                payeeName = payeeName,
                amount = amountInPaise,
                isUpiCode = isUpiCode,
            )
        },
    )

    paymentProcessingScreen(
        onPaymentComplete = { payeeName, amount, upiName, transactionTimestamp ->
            navController.navigateToPaymentSuccessScreen(
                payeeName = payeeName,
                amount = amount,
                upiName = upiName,
                transactionTimestamp = transactionTimestamp,
            )
        },
        onPaymentFailed = { errorMessage ->
            navController.popBackStack()
        },
    )

    paymentSuccessScreen(
        onShareScreenshot = {
            // Screenshot functionality is handled by the Android-specific PaymentSuccessScreen implementation
            // The actual screenshot and sharing is done within the screen itself
            // This callback is used by the Android-specific implementation to trigger the screenshot
        },
        onDone = {
            navController.navigate(HOME_ROUTE) {
                popUpTo(HOME_ROUTE) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        },
        onNavigateToSendMoneyOptions = {
            navController.navigateToSendMoneyOptionsScreen(
                navOptions {
                    popUpTo(PAYMENT_SUCCESS_ROUTE) {
                        inclusive = true
                    }
                    launchSingleTop = true
                },
            )
        },
    )

    qrScanDestinations(navController)
}

/** Scanning a payment code: the scanner and the fast-pay screen that routes what it read. */
private fun NavGraphBuilder.qrScanDestinations(navController: NavController) {
    scanQrScreen(
        navigateBack = navController::popBackStack,
        navigateToIntraBankTransfer = { qrData ->
            // Route through FastMpay for processing (bank mismatch, beneficiary check, etc.)
            navController.navigateToFastMpay(
                qrData = qrData,
                navOptions = navOptions {
                    popUpTo(SCAN_QR_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        navigateToInterbankTransfer = { accountExternalId, recipientName, amount ->
            navController.navigateToInterbankTransfer(
                phoneNumber = accountExternalId,
                recipientName = recipientName,
                amount = amount,
                navOptions = navOptions {
                    popUpTo(SCAN_QR_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        navigateToAddBeneficiaryScreen = {
            navController.navigateToBeneficiaryAddEdit(
                BeneficiaryAddEditType.AddItem(it),
                navOptions = navOptions {
                    popUpTo(SCAN_QR_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        // from send money pr

//            navigateToPayeeDetailsScreen = {
//                navController.navigateToPayeeDetailsScreen(
//                    qrCodeData = it,
//                    navOptions = navOptions {
//                        popUpTo(SCAN_QR_ROUTE) {
//                            inclusive = true
//                        }
//                    },
//                )
//            },
    )

    fastMpayScreen(
        onNavigateToAddBeneficiary = { beneficiaryData, sourceQrType, sourceQrData ->
            navController.navigateToBeneficiaryAddEdit(
                BeneficiaryAddEditType.AddItem(
                    beneficiary = beneficiaryData,
                    sourceQrType = sourceQrType,
                    sourceQrData = sourceQrData,
                ),
                navOptions = navOptions {
                    popUpTo(FAST_MPAY_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        onNavigateToMakeTransfer = { qrData, _ ->
            navController.navigateToTransferConfirm(
                toOfficeId = qrData.officeId.toInt(),
                toClientId = qrData.clientId,
                toAccountTypeId = qrData.accountTypeId.toInt(),
                toAccountId = qrData.accountId.toInt(),
                amount = qrData.amount.toIntOrNull() ?: 0,
                toAccountName = qrData.clientName,
                toAccountNo = qrData.accountNo,
                returnDestination = "home",
                navOptions = navOptions {
                    popUpTo(FAST_MPAY_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        onNavigateToInterbankTransfer = { accountExternalId, recipientName, amount ->
            navController.navigateToInterbankTransfer(
                phoneNumber = accountExternalId,
                recipientName = recipientName,
                amount = amount,
                navOptions = navOptions {
                    popUpTo(FAST_MPAY_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        onNavigateToIntraBankTransfer = { qrData ->
            navController.navigateToTransferConfirm(
                toOfficeId = qrData.officeId.toInt(),
                toClientId = qrData.clientId,
                toAccountTypeId = qrData.accountTypeId.toInt(),
                toAccountId = qrData.accountId.toInt(),
                amount = qrData.amount.toIntOrNull() ?: 0,
                toAccountName = qrData.clientName,
                toAccountNo = qrData.accountNo,
                returnDestination = "home",
                navOptions = navOptions {
                    popUpTo(FAST_MPAY_ROUTE) {
                        inclusive = true
                    }
                },
            )
        },
        onNavigateToMerchantPayment = { qrData ->
            // TODO: Navigate to merchant payment screen when implemented
            navController.navigateUp()
        },
        onNavigateBack = {
            navController.navigateUp()
        },
        onError = { _ ->
            // Navigate back on error
            navController.navigateUp()
        },
    )
}

/** Paying a phone contact or a bank account directly: pay anyone, the contact picker, bank transfer and IFSC search. */
private fun NavGraphBuilder.payAnyoneDestinations(navController: NavController) {
    payAnyoneScreen(
        onBackClick = navController::popBackStack,
        onContactPickerClick = {
            navController.navigateToContactsPickerScreen()
        },
        onContactSelected = { phoneNumber ->
            // Contact selection updates the input field via ViewModel
            // No navigation needed - user stays on Pay Anyone screen
        },
    )

    contactsPickerScreen(
        onBackClick = navController::popBackStack,
        onContactSelected = { phoneNumber ->
            // Navigate back to Pay Anyone screen with selected phone number
            navController.navigateToPayAnyoneScreen(
                selectedContactPhone = phoneNumber,
                navOptions = navOptions {
                    popUpTo(PAY_ANYONE_ROUTE) { inclusive = true }
                },
            )
        },
    )

    bankTransferScreen(
        onBackClick = navController::popBackStack,
        onSearchIfscClick = {
            navController.navigateToSearchIfscScreen()
        },
    )

    searchIfscScreen(
        onBackClick = navController::popBackStack,
        onIfscSelected = { ifscCode ->
            // The IFSC code will be handled by the BankTransferViewModel
            // when the user returns to the Bank Transfer screen
        },
    )
}

/** Intra-bank and inter-bank transfers. */
private fun NavGraphBuilder.transferDestinations(navController: NavController) {
    intraBankHubScreen(
        navigateToSelectAccountScreen = {
            navController.navigateToSelectAccountScreen()
        },
        navigateToBeneficiary = {
            navController.navigateToBeneficiaryAddEdit(
                BeneficiaryAddEditType.AddItem(),
            )
        },
        navigateBack = navController::popBackStack,
        navigateToTransferConfirm = { toOfficeId, toClientId, toAccountId, accountName, accountNo ->
            navController.navigateToTransferConfirm(
                toOfficeId = toOfficeId,
                toClientId = toClientId,
                // Savings account type
                toAccountTypeId = 2,
                toAccountId = toAccountId,
                amount = 0,
                toAccountName = accountName,
                toAccountNo = accountNo,
                returnDestination = "home",
            )
        },
        navigateToHistory = navController::navigateToHistory,
        navigateToScanQr = navController::navigateToScanQr,
        navigateToRequestMoney = navController::navigateToMpayQrScreen,
        navigateToTransferBeneficiary = { beneficiary ->
            // Default office ID since beneficiary doesn't have office info
            navController.navigateToTransferConfirm(
                toOfficeId = 1,
                toClientId = beneficiary.id,
                toAccountTypeId = beneficiary.accountType.id,
                toAccountId = beneficiary.id.toInt(),
                amount = 0,
                toAccountName = beneficiary.name,
                toAccountNo = beneficiary.accountNumber,
                returnDestination = "home",
            )
        },
    )

    selectAccountScreenDestination(
        navigateToTransferConfirm = navController::navigateToTransferConfirm,
        navigateBack = navController::popBackStack,
    )

    transferConfirmScreen(
        navigateBack = navController::popBackStack,
        onTransferSuccess = { transferResult, returnDestination ->
            navController.navigateTransferSuccess(
                transferResult = transferResult,
                returnDestination = returnDestination,
                navOptions {
                    when (returnDestination) {
                        "payments" -> popUpTo(PAYMENTS_ROUTE) { inclusive = true }
                        else -> popUpTo(HOME_ROUTE) { inclusive = true }
                    }
                    launchSingleTop = true
                },
            )
        },
        navigateForPasscodeVerification = { verificationKey ->
            navController.navigateToInternalPasscodeScreen(verificationKey)
        },
    )

    transferSuccessScreen(
        navigateBack = { returnDestination ->
            when (returnDestination) {
                "payments" -> {
                    navController.navigate(PAYMENTS_ROUTE) {
                        popUpTo(PAYMENTS_ROUTE) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }

                else -> {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(HOME_ROUTE) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            }
        },
    )

    interbankTransferScreen(
        onBackClick = navController::popBackStack,
        onTransferSuccess = {
            navController.navigate(HOME_ROUTE) {
                popUpTo(HOME_ROUTE) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        },
    )
}

/** The payments tab's pages; each opens screens owned by other features. */
@Composable
private fun paymentsTabs(navController: NavController): List<TabContent> = listOf(
    TabContent(stringResource(Res.string.feature_payments_send)) {
        SelectTransferTypeScreen(
            onIntraBankTransferClick = {
                navController.navigateToIntraBankHub()
            },
            onInterBankTransferClick = {
                navController.navigateToInterbankTransfer()
            },
        )
    },
    // from send money pr
    TabContent(PaymentsScreenContents.SEND.name) {
        SendMoneyScreen(
            onBackClick = navController::navigateUp,
            // TODO Need clarification
            navigateToTransferScreen = navController::navigateToSendMoneyScreen,
            navigateToScanQrScreen = navController::navigateToScanQr,
            navigateToPayeeDetails = navController::navigateToPayeeDetailsScreen,
            showTopBar = false,
        )
    },
    TabContent(stringResource(Res.string.feature_payments_request)) {
        RequestScreen(
            showQr = navController::navigateToMpayQrScreen,
        )
    },
    TabContent(stringResource(Res.string.feature_payments_history)) {
        HistoryScreen(
            viewTransferDetail = navController::navigateToSpecificTransaction,
        )
    },
//        TabContent(PaymentsScreenContents.SI.name) {
//            StandingInstructionsScreen(
//                onAddEditSI = navController::navigateToSIAddEdit,
//                onShowSIDetails = navController::navigateSIDetails,
//            )
//        },
//        TabContent(PaymentsScreenContents.INVOICES.name) {
//            InvoiceScreen(
//                navigateToInvoiceDetailScreen = navController::navigateToInvoiceDetail,
//            )
//        },

    TabContent(PaymentsScreenContents.AUTOPAY.name) {
        AutoPayScreen(
            onNavigateToScheduleManagement = {
                navController.navigateToScheduleManagement()
            },
//                onNavigateToSetup = {
//                    navController.navigateToAutoPaySetup()
//                },
//                onNavigateToRules = {
//                    navController.navigateToAutoPayRules()
//                },
            onNavigateToPreferences = {
                navController.navigateToAutoPayPreferences()
            },
            onNavigateToHistory = {
                navController.navigateToAutoPayHistory()
            },
            onNavigateToScheduleDetails = { scheduleId ->
                navController.navigateToAutoPayScheduleDetails(scheduleId)
            },

            onNavigateToAddBiller = {
                navController.navigateToAddBiller()
            },
            onNavigateToBillerList = {
                navController.navigateToBillerList()
            },
            onNavigateToAddBill = {
                navController.navigateToAddBill()
            },
            onNavigateToBillList = {
                navController.navigateToBillList()
            },
            showTopBar = false,
        )
    },
)

/** The finance tab's pages. */
@Composable
private fun financeTabs(navController: NavController): List<TabContent> = listOf(
    TabContent(stringResource(SharedRes.string.feature_finance_accounts)) {
        AccountsScreen(
            onAddEditSavingsAccount = navController::navigateToSavingAccountAddEdit,
            onViewSavingAccountDetails = navController::navigateToSavingAccountDetails,
            onApplyForLoanClick = { clientId ->
                navController.navigateToLoanApplicationGraph(clientId)
            },
        )
    },

    TabContent(stringResource(SharedRes.string.feature_finance_beneficiaries)) {
        BeneficiaryListScreen(
            onAddOrEditBeneficiary = navController::navigateToBeneficiaryAddEdit,
        )
    },
//        TabContent(FinanceScreenContents.CARDS.name) {
//            CardsScreen(
//                navigateToViewDetail = navController::navigateToCardDetails,
//                navigateToAddEdit = navController::navigateToCardAddEdit,
//            )
//        },
//        TabContent(FinanceScreenContents.MERCHANTS.name) {
//            MerchantScreen()
//        },
//        TabContent(FinanceScreenContents.KYC.name) {
//            KYCScreen(
//                onLevel1Clicked = navController::navigateToKYCLevel1,
//                onLevel2Clicked = navController::navigateToKYCLevel2,
//                onLevel3Clicked = navController::navigateToKYCLevel3,
//            )
//        },
)
