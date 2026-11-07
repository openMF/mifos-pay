/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import cmp.navigation.generated.resources.feature_finance_accounts
import cmp.navigation.generated.resources.feature_finance_beneficiaries
import cmp.navigation.registry.app.ui.MifosAppState
import kpt.core.data.repository.UserVerificationRepository
import kpt.core.ui.utility.TabContent
import kpt.core.ui.utils.RootTransitionProviders
import kpt.feature.accounts.AccountsScreen
import kpt.feature.accounts.savingsaccount.SavingsAddEditType
import kpt.feature.accounts.savingsaccount.addEditSavingAccountScreen
import kpt.feature.accounts.savingsaccount.details.navigateToSavingAccountDetails
import kpt.feature.accounts.savingsaccount.details.savingAccountDetailRoute
import kpt.feature.accounts.savingsaccount.navigateToSavingAccountAddEdit
import kpt.feature.autopay.AutoPayScreen
import kpt.feature.autopay.autoPayGraph
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
import kpt.feature.faq.navigation.faqScreen
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
import kpt.feature.invoices.navigation.invoiceDetailScreen
import kpt.feature.kyc.navigation.kycLevel1Screen
import kpt.feature.kyc.navigation.kycLevel2Screen
import kpt.feature.kyc.navigation.kycLevel3Screen
import kpt.feature.kyc.navigation.navigateToKYCLevel2
import kpt.feature.kyc.navigation.navigateToKYCLevel3
import kpt.feature.merchants.navigation.merchantTransferScreen
import kpt.feature.mpay.qr.navigation.mpayQrScreen
import kpt.feature.mpay.qr.navigation.navigateToMpayQrScreen
import kpt.feature.mpay.qr.scan.navigation.SCAN_QR_ROUTE
import kpt.feature.mpay.qr.scan.navigation.navigateToScanQr
import kpt.feature.mpay.qr.scan.navigation.scanQrScreen
import kpt.feature.notification.navigateToNotification
import kpt.feature.notification.notificationScreen
import kpt.feature.passcode.internalMifosPasscodeScreen
import kpt.feature.passcode.navigateToInternalMifosPasscodeScreen
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
import kpt.feature.receipt.navigation.receiptScreen
import kpt.feature.savedcards.createOrUpdate.addEditCardScreen
import kpt.feature.savedcards.details.cardDetailRoute
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
import kpt.feature.standing.instruction.createOrUpdate.addEditSIScreen
import kpt.feature.standing.instruction.details.siDetailsScreen
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
import kpt.feature.upi.setup.navigation.setupUpiPinScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.mifos.lib.loan.navigation.loanApplicationGraph
import org.mifos.lib.loan.navigation.navigateToLoanApplicationGraph
import cmp.navigation.generated.resources.Res as SharedRes

/**
 * `SavedStateHandle` key used by callers of [internalMifosPasscodeScreen] for
 * the round-trip "did the user verify their passcode?" boolean. Currently
 * consumed by the intra-bank transfer auth gate
 * (`TransferConfirmScreen` → `TransferConfirmViewModel`); the settings
 * disable-biometrics flow uses its own
 * `DISABLE_BIOMETRICS_VERIFICATION_KEY` from `:feature:settings`.
 */
const val AUTHENTICATION_VERIFICATION_KEY = "org.mifospay.mifos.authentication_verification_success"

/**
 * Authenticated-area navigation host hung off `MifosNavGraph.MAIN_GRAPH` from
 * [RootNavGraph]. Composes every in-app feature destination plus the
 * **internal passcode screen** that backs sensitive-operation gates.
 *
 * The internal passcode wiring is the only library-touching piece in this
 * file:
 *  - `internalMifosPasscodeScreen(...)` is registered with
 *    `onAuthenticationSuccess` / `onAuthenticationFailed` callbacks that
 *    write `true` / `false` to the **previous** back-stack entry's
 *    `SavedStateHandle` under the verification-key the caller passed via
 *    [navigateToInternalMifosPasscodeScreen]. On success, also calls
 *    [UserVerificationRepository.recordVerification] to mint a 30-second
 *    one-shot token that the caller must consume on its side
 *    (`consumeVerification()`) before performing the protected action.
 *  - Two callers currently use this gate:
 *      * The **settings** flow (change-passcode + disable-biometrics) —
 *        unconditionally passes `allowBiometricAuth = false` when navigating
 *        to the internal passcode screen, since both flows would be defeated
 *        by allowing biometric bypass.
 *      * The **intra-bank** `transferConfirmScreen`'s
 *        `navigateForPasscodeVerification` callback — does not override
 *        `allowBiometricAuth`, so biometric is allowed there.
 */
// TODO(phase-2-nav): migrate ~60 feature nav-extensions to cmp-navigation type-safe
// routes (Phase 2 T2 of 02-topology-reconciliation.md). Until then this ~1000-line
// string-route nav host stays in place — retiring it in this chunk would strand
// every feature composable that still resolves via its string route. The template
// shell (`cmp.navigation.ComposeApp` → `RootNavScreen` → `authenticatedGraph`) is
// wired for the four HOME/PAYMENTS/FINANCE/HISTORY top-level tabs (see
// `cmp/navigation/authenticated/AuthenticatedNavigation.kt`) but does not yet
// register the fork's ~60 feature destinations; the T2 rewrite converts each
// `composable(route = "…/{arg}")` here into `composable<AppRoute.X> { … }` under
// `:cmp-navigation`, then this file becomes deletable. Shell-security behaviors
// and the Supabase instance selector already live in `:cmp-navigation` per T4/T5.
@Composable
internal fun MifosNavHost(
    appState: MifosAppState,
    onClickLogout: () -> Unit,
    handleAppLocale: (locale: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    val userVerificationRepository = koinInject<UserVerificationRepository>()

    val paymentsTabContents = listOf(
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

//    TODO Cards and Merchants are not using self api
    val tabContents = listOf(
        TabContent(stringResource(SharedRes.string.feature_finance_accounts)) {
            AccountsScreen(
                onAddEditSavingsAccount = navController::navigateToSavingAccountAddEdit,
                onViewSavingAccountDetails = navController::navigateToSavingAccountDetails,
                onAddOrEditBeneficiary = navController::navigateToBeneficiaryAddEdit,
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

    NavHost(
        route = MifosNavGraph.MAIN_GRAPH,
        startDestination = HOME_ROUTE,
        navController = navController,
        modifier = modifier,
        // Explicit lightweight fade for every destination that does NOT declare its own
        // transition (the four bottom-nav tabs + ~10 other plain composables). Without this the
        // JetBrains nav lib falls back to a 700ms fadeIn/fadeOut crossfade that keeps two heavy
        // screens alpha-composited for 0.7s — the primary source of the transition lag. Fade (not
        // slide) is the correct motion for sibling bottom-nav tabs; per-destination slide/push
        // transitions (composableWithSlideTransitions/…PushTransitions) still override this default.
        enterTransition = { RootTransitionProviders.Enter.fadeIn(this) },
        exitTransition = { RootTransitionProviders.Exit.fadeOut(this) },
        popEnterTransition = { RootTransitionProviders.Enter.fadeIn(this) },
        popExitTransition = { RootTransitionProviders.Exit.fadeOut(this) },
    ) {
        internalMifosPasscodeScreen(
            navigateToLogin = onClickLogout,
            onAuthenticationSuccess = { verificationKey ->
                userVerificationRepository.recordVerification()
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

        settingsScreen(
            onBackPress = navController::navigateUp,
            onLogout = onClickLogout,
            handleAppLocale = { handleAppLocale(it) },
            navigateToPasscodeScreen = { verificationKey ->
                navController.navigateToInternalMifosPasscodeScreen(
                    verificationKey = verificationKey,
                    allowBiometricAuth = false,
                )
            },
            navigateToEditPasswordScreen = navController::navigateToEditPassword,
            navigateToFaqScreen = navController::navigateToFAQ,
            navigateToNotificationScreen = navController::navigateToNotification,
            navigateToProfile = navController::navigateToProfile,
        )

        faqScreen(
            navigateBack = navController::navigateUp,
        )

        editPasswordScreen(
            navigateBack = navController::navigateUp,
            onLogOut = onClickLogout,
        )

        profileNavGraph(
            navController = navController,
            onLinkBankAccount = {
                navController.navigateToSavingAccountAddEdit(SavingsAddEditType.AddItem)
            },
            showQrCode = navController::navigateToMpayQrScreen,
            navigateBack = navController::popBackStack,
        )

        historyNavigation(
            viewTransactionDetail = navController::navigateToSpecificTransaction,
        )

        paymentsScreen(tabContents = paymentsTabContents)

        financeScreen(tabContents = tabContents)

        specificTransactionsScreen(
            navigateBack = navController::navigateUp,
            viewTransactionDetail = navController::navigateToTransactionDetail,
        )

        transactionDetailNavigation(
            navigateBack = navController::navigateUp,
            navigateToReceipt = navController::navigateToReceipt,
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

        savingAccountDetailRoute(
            navigateBack = navController::navigateUp,
            onViewTransaction = navController::navigateToSpecificTransaction,
        )

        addEditSavingAccountScreen(
            navigateBack = navController::navigateUp,
        )

        invoiceDetailScreen(
            onNavigateBack = navController::navigateUp,
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

        notificationScreen(
            navigateBack = navController::navigateUp,
        )

        cardDetailRoute(
            navigateBack = navController::navigateUp,
        )

        addEditCardScreen(
            navigateBack = navController::navigateUp,
        )

        receiptScreen(
            onBackClick = navController::navigateUp,
        )

        addEditSIScreen(
            navigateBack = navController::navigateUp,
        )

        siDetailsScreen(navigateBack = navController::navigateUp)

        mpayQrScreen(
            navigateBack = navController::navigateUp,
            // from #1906 pr
            navigateToSendScreen = {},
            navigateToPayeeDetailsScreen = {},
        )

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
            onNavigateToUpiPayment = { state ->
                // TODO: Handle UPI payment navigation
            },
            onNavigateToFineractPayment = { state ->
                // TODO: Handle Fineract payment navigation
            },
        )

        sendMoneyScreen(
            onBackClick = navController::popBackStack,
            navigateToTransferScreen = navController::navigateToSendMoneyScreen,
            navigateToPayeeDetailsScreen = navController::navigateToPayeeDetailsScreen,
            navigateToScanQrScreen = navController::navigateToScanQr,
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
            onNavigateToUpiPayment = { state ->
                // TODO: Handle UPI payment navigation
            },
            onNavigateToFineractPayment = { state ->
                // TODO: Handle Fineract payment navigation
            },
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
                navController.navigateToInternalMifosPasscodeScreen(verificationKey)
            },
        )

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

        loanApplicationGraph(
            navController = navController,
            navigateBack = navController::navigateUp,
            navigateForPasscodeVerification = { verificationKey ->
                navController.navigateToInternalMifosPasscodeScreen(verificationKey)
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

        merchantTransferScreen(
            proceedWithMakeTransferFlow = { _, _ -> },
            onBackPressed = navController::navigateUp,
        )

        setupUpiPinScreen(
            navigateBack = navController::navigateUp,
        )

        autoPayGraph(
            navController = navController,
            onNavigateBack = navController::navigateUp,
        )

        transferOptionsDialog(
            onIntraBankTransferClick = navController::navigateToIntraBankHub,
            onInterBankTransferClick = navController::navigateToInterbankTransfer,
            onUpiSendMoney = navController::navigateToSendMoneyOptionsScreen,
            onDismiss = {
                navController.popBackStack()
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
}
