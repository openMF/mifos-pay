/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.send.money.BankTransferScreen
import kpt.feature.send.money.ContactsPickerScreen
import kpt.feature.send.money.IfscCode
import kpt.feature.send.money.PayAnyoneScreen
import kpt.feature.send.money.PayeeDetailsScreen
import kpt.feature.send.money.PayeeDetailsState
import kpt.feature.send.money.PaymentChatHistoryScreen
import kpt.feature.send.money.PaymentDetailsScreen
import kpt.feature.send.money.PaymentProcessingScreen
import kpt.feature.send.money.PaymentSuccessScreen
import kpt.feature.send.money.SearchIfscScreen
import kpt.feature.send.money.SendMoneyOptionsScreen
import kpt.feature.send.money.SendMoneyScreen
import kpt.feature.send.money.UpiPinScreen
import kpt.feature.send.money.UpiTransactionHistoryScreen
const val SEND_MONEY_ROUTE = "send_money_route"
const val SEND_MONEY_ARG = "requestData"

const val SEND_MONEY_BASE_ROUTE = "$SEND_MONEY_ROUTE?$SEND_MONEY_ARG={$SEND_MONEY_ARG}"

const val SEND_MONEY_OPTIONS_ROUTE = "send_money_options_route"
const val BANK_TRANSFER_ROUTE = "bank_transfer_route"
const val SEARCH_IFSC_ROUTE = "search_ifsc_route"
const val PAYEE_DETAILS_ROUTE = "payee_details_route"
const val PAYEE_DETAILS_ARG = "qrCodeData"

const val PAYEE_DETAILS_BASE_ROUTE = "$PAYEE_DETAILS_ROUTE?$PAYEE_DETAILS_ARG={$PAYEE_DETAILS_ARG}"

const val PAY_ANYONE_ROUTE = "pay_anyone_route"
const val PAY_ANYONE_SELECTED_CONTACT_ARG = "selectedContact"
const val PAY_ANYONE_BASE_ROUTE = "$PAY_ANYONE_ROUTE?$PAY_ANYONE_SELECTED_CONTACT_ARG={$PAY_ANYONE_SELECTED_CONTACT_ARG}"
const val CONTACTS_PICKER_ROUTE = "contacts_picker_route"

const val UPI_PIN_ROUTE = "upi_pin_route"
const val UPI_PIN_PAYEE_NAME_ARG = "payeeName"
const val UPI_PIN_AMOUNT_ARG = "amount"
const val UPI_PIN_IS_UPI_ARG = "isUpiCode"
const val UPI_PIN_BANK_NAME_ARG = "bankName"
const val UPI_PIN_ACCOUNT_NO_ARG = "accountNo"
const val UPI_PIN_REF_ID_ARG = "refId"

const val UPI_PIN_BASE_ROUTE = "$UPI_PIN_ROUTE?$UPI_PIN_PAYEE_NAME_ARG={$UPI_PIN_PAYEE_NAME_ARG}&$UPI_PIN_AMOUNT_ARG={$UPI_PIN_AMOUNT_ARG}&$UPI_PIN_REF_ID_ARG={$UPI_PIN_REF_ID_ARG}&$UPI_PIN_IS_UPI_ARG={$UPI_PIN_IS_UPI_ARG}&$UPI_PIN_BANK_NAME_ARG={$UPI_PIN_BANK_NAME_ARG}&$UPI_PIN_ACCOUNT_NO_ARG={$UPI_PIN_ACCOUNT_NO_ARG}"

const val PAYMENT_PROCESSING_ROUTE = "payment_processing_route"
const val PAYMENT_PROCESSING_PAYEE_NAME_ARG = "payeeName"
const val PAYMENT_PROCESSING_AMOUNT_ARG = "amount"
const val PAYMENT_PROCESSING_IS_UPI_ARG = "isUpiCode"

const val PAYMENT_PROCESSING_BASE_ROUTE = "$PAYMENT_PROCESSING_ROUTE?$PAYMENT_PROCESSING_PAYEE_NAME_ARG={$PAYMENT_PROCESSING_PAYEE_NAME_ARG}&$PAYMENT_PROCESSING_AMOUNT_ARG={$PAYMENT_PROCESSING_AMOUNT_ARG}&$PAYMENT_PROCESSING_IS_UPI_ARG={$PAYMENT_PROCESSING_IS_UPI_ARG}"

const val PAYMENT_SUCCESS_ROUTE = "payment_success_route"
const val PAYMENT_SUCCESS_PAYEE_NAME_ARG = "payeeName"
const val PAYMENT_SUCCESS_AMOUNT_ARG = "amount"
const val PAYMENT_SUCCESS_UPI_NAME_ARG = "upiName"
const val PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG = "transactionTimestamp"

const val PAYMENT_SUCCESS_BASE_ROUTE = "$PAYMENT_SUCCESS_ROUTE?$PAYMENT_SUCCESS_PAYEE_NAME_ARG={$PAYMENT_SUCCESS_PAYEE_NAME_ARG}&$PAYMENT_SUCCESS_AMOUNT_ARG={$PAYMENT_SUCCESS_AMOUNT_ARG}&$PAYMENT_SUCCESS_UPI_NAME_ARG={$PAYMENT_SUCCESS_UPI_NAME_ARG}&$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG={$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG}"

const val PAYMENT_CHAT_HISTORY_ROUTE = "payment_chat_history_route"
const val UPI_TRANSACTION_HISTORY_ROUTE = "upi_transaction_history_route"
const val PAYMENT_DETAILS_ROUTE = "payment_details_route"
const val PAYMENT_DETAILS_TRANSACTION_ID_ARG = "transactionId"
const val PAYMENT_DETAILS_BASE_ROUTE = "$PAYMENT_DETAILS_ROUTE/{$PAYMENT_DETAILS_TRANSACTION_ID_ARG}"

fun NavController.navigateToPaymentDetailsScreen(
    transactionId: String,
    navOptions: NavOptions? = null,
) {
    val route = "$PAYMENT_DETAILS_ROUTE/$transactionId"
    val options = navOptions ?: navOptions {
        popUpTo(PAYMENT_CHAT_HISTORY_ROUTE) { inclusive = false }
    }
    navigate(route, options)
}

fun NavController.navigateToPayAnyoneScreen(
    selectedContactPhone: String? = null,
    navOptions: NavOptions? = null,
) {
    val route = if (selectedContactPhone != null) {
        "$PAY_ANYONE_ROUTE?$PAY_ANYONE_SELECTED_CONTACT_ARG=$selectedContactPhone"
    } else {
        PAY_ANYONE_ROUTE
    }
    navigate(route, navOptions)
}

fun NavController.navigateToContactsPickerScreen(
    navOptions: NavOptions? = null,
) = navigate(CONTACTS_PICKER_ROUTE, navOptions)

fun NavController.navigateToSendMoneyScreen(
    navOptions: NavOptions? = null,
) = navigate(SEND_MONEY_ROUTE, navOptions)

fun NavController.navigateToSendMoneyOptionsScreen(
    navOptions: NavOptions? = null,
) = navigate(SEND_MONEY_OPTIONS_ROUTE, navOptions)

fun NavController.navigateToBankTransferScreen(
    navOptions: NavOptions? = null,
) = navigate(BANK_TRANSFER_ROUTE, navOptions)

fun NavController.navigateToSearchIfscScreen(
    navOptions: NavOptions? = null,
) = navigate(SEARCH_IFSC_ROUTE, navOptions)

fun NavController.navigateToPayeeDetailsScreen(
    qrCodeData: String,
    navOptions: NavOptions? = null,
) {
    // URL encode the QR code data to handle special characters like &, =, etc.
    val encodedQrCodeData = qrCodeData.urlEncode()
    val route = "$PAYEE_DETAILS_ROUTE?$PAYEE_DETAILS_ARG=$encodedQrCodeData"
    val options = navOptions ?: navOptions {
        popUpTo(SEND_MONEY_OPTIONS_ROUTE) { inclusive = false }
    }
    navigate(route, options)
}

// Expected to be in paise
fun NavController.navigateToUpiPinScreen(
    payeeName: String,
    amount: String,
    refId: String,
    isUpiCode: Boolean,
    bankName: String,
    accountNo: String,
    navOptions: NavOptions? = null,
) {
    val encodedPayeeName = payeeName.urlEncode()
    val encodedAmount = amount.urlEncode()
    val encodedRefId = refId.urlEncode()
    val encodedBankName = bankName.urlEncode()
    val encodedAccountNo = accountNo.urlEncode()
    val route = "$UPI_PIN_ROUTE?$UPI_PIN_PAYEE_NAME_ARG=$encodedPayeeName&$UPI_PIN_AMOUNT_ARG=$encodedAmount&$UPI_PIN_REF_ID_ARG=$encodedRefId&$UPI_PIN_IS_UPI_ARG=$isUpiCode&$UPI_PIN_BANK_NAME_ARG=$encodedBankName&$UPI_PIN_ACCOUNT_NO_ARG=$encodedAccountNo"
    val options = navOptions ?: navOptions {
        popUpTo(PAYEE_DETAILS_ROUTE) { inclusive = false }
    }
    navigate(route, options)
}

// amount in paise
fun NavController.navigateToPaymentProcessingScreen(
    payeeName: String,
    amount: String,
    isUpiCode: Boolean,
    navOptions: NavOptions? = null,
) {
    val encodedPayeeName = payeeName.urlEncode()
    val encodedAmount = amount.urlEncode()
    val route = "$PAYMENT_PROCESSING_ROUTE?$PAYMENT_PROCESSING_PAYEE_NAME_ARG=$encodedPayeeName&$PAYMENT_PROCESSING_AMOUNT_ARG=$encodedAmount&$PAYMENT_PROCESSING_IS_UPI_ARG=$isUpiCode"
    val options = navOptions ?: navOptions {
        popUpTo(UPI_PIN_ROUTE) { inclusive = true }
    }
    navigate(route, options)
}

// Expected to be in paise
fun NavController.navigateToPaymentSuccessScreen(
    payeeName: String,
    amount: String,
    upiName: String,
    transactionTimestamp: String,
    navOptions: NavOptions? = null,
) {
    val encodedPayeeName = payeeName.urlEncode()
    val encodedAmount = amount.urlEncode()
    val encodedUpiName = upiName.urlEncode()
    val encodedTransactionTimestamp = transactionTimestamp.urlEncode()
    val route = "$PAYMENT_SUCCESS_ROUTE?$PAYMENT_SUCCESS_PAYEE_NAME_ARG=$encodedPayeeName&$PAYMENT_SUCCESS_AMOUNT_ARG=$encodedAmount&$PAYMENT_SUCCESS_UPI_NAME_ARG=$encodedUpiName&$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG=$encodedTransactionTimestamp"
    val options = navOptions ?: navOptions {
        popUpTo(PAYMENT_PROCESSING_ROUTE) { inclusive = true }
    }
    navigate(route, options)
}

fun NavController.navigateToPaymentChatHistoryScreen(
    navOptions: NavOptions? = null,
) = navigate(PAYMENT_CHAT_HISTORY_ROUTE, navOptions)

fun NavController.navigateToUpiTransactionHistoryScreen(
    navOptions: NavOptions? = null,
) = navigate(UPI_TRANSACTION_HISTORY_ROUTE, navOptions)

fun NavGraphBuilder.sendMoneyScreen(
    onBackClick: () -> Unit,
    navigateToTransferScreen: (String) -> Unit,
    navigateToPayeeDetailsScreen: (String) -> Unit,
    navigateToScanQrScreen: () -> Unit,
) {
    composableWithSlideTransitions(
        route = SEND_MONEY_BASE_ROUTE,
        arguments = listOf(
            navArgument(SEND_MONEY_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        SendMoneyScreen(
            onBackClick = onBackClick,
            navigateToTransferScreen = navigateToTransferScreen,
            navigateToScanQrScreen = navigateToScanQrScreen,
            navigateToPayeeDetails = navigateToPayeeDetailsScreen,
        )
    }
}

fun NavGraphBuilder.sendMoneyOptionsScreen(
    onBackClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onPayAnyoneClick: () -> Unit,
    onBankTransferClick: () -> Unit,
    onFineractPaymentsClick: () -> Unit,
    onAutoPayClick: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
    onNavigateToPayeeDetails: (String) -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onUpiTransactionHistoryClick: () -> Unit,
) {
    composableWithSlideTransitions(
        route = SEND_MONEY_OPTIONS_ROUTE,
    ) {
        SendMoneyOptionsScreen(
            onBackClick = onBackClick,
            onScanQrClick = onScanQrClick,
            onPayAnyoneClick = onPayAnyoneClick,
            onBankTransferClick = onBankTransferClick,
            onFineractPaymentsClick = onFineractPaymentsClick,
            onAutoPayClick = onAutoPayClick,
            onQrCodeScanned = onQrCodeScanned,
            onNavigateToPayeeDetails = onNavigateToPayeeDetails,
            onPaymentHistoryClick = onPaymentHistoryClick,
            onUpiTransactionHistoryClick = onUpiTransactionHistoryClick,
        )
    }
}

fun NavGraphBuilder.bankTransferScreen(
    onBackClick: () -> Unit,
    onSearchIfscClick: () -> Unit,
) {
    composableWithSlideTransitions(
        route = BANK_TRANSFER_ROUTE,
    ) {
        BankTransferScreen(
            onBackClick = onBackClick,
            onSearchIfscClick = onSearchIfscClick,
        )
    }
}

fun NavGraphBuilder.searchIfscScreen(
    onBackClick: () -> Unit,
    onIfscSelected: (IfscCode) -> Unit,
) {
    composableWithSlideTransitions(
        route = SEARCH_IFSC_ROUTE,
    ) {
        SearchIfscScreen(
            onBackClick = onBackClick,
            onIfscSelected = onIfscSelected,
        )
    }
}

fun NavGraphBuilder.payAnyoneScreen(
    onBackClick: () -> Unit,
    onContactPickerClick: () -> Unit,
    onContactSelected: (String) -> Unit = {},
) {
    composableWithSlideTransitions(
        route = PAY_ANYONE_BASE_ROUTE,
        arguments = listOf(
            navArgument(PAY_ANYONE_SELECTED_CONTACT_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->

        PayAnyoneScreen(
            onBackClick = onBackClick,
            onContactPickerClick = onContactPickerClick,
            onContactSelected = onContactSelected,
        )
    }
}

fun NavGraphBuilder.contactsPickerScreen(
    onBackClick: () -> Unit,
    onContactSelected: (String) -> Unit,
) {
    composableWithSlideTransitions(
        route = CONTACTS_PICKER_ROUTE,
    ) {
        ContactsPickerScreen(
            onBackClick = onBackClick,
            onContactSelected = onContactSelected,
        )
    }
}

fun NavGraphBuilder.payeeDetailsScreen(
    onBackClick: () -> Unit,
    onNavigateToUpiPin: (PayeeDetailsState) -> Unit,
    onNavigateToUpiPayment: (PayeeDetailsState) -> Unit,
    onNavigateToFineractPayment: (PayeeDetailsState) -> Unit,
) {
    composableWithSlideTransitions(
        route = PAYEE_DETAILS_BASE_ROUTE,
        arguments = listOf(
            navArgument(PAYEE_DETAILS_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) {
        PayeeDetailsScreen(
            onBackClick = onBackClick,
            onNavigateToPaymentProcessing = onNavigateToUpiPin,
            onNavigateToUpiPayment = onNavigateToUpiPayment,
            onNavigateToFineractPayment = onNavigateToFineractPayment,
        )
    }
}

fun NavGraphBuilder.upiPinScreen(
    onBackClick: () -> Unit,
    onNavigateToPaymentProcessing: (String, String, Boolean) -> Unit,
) {
    composableWithSlideTransitions(
        route = UPI_PIN_BASE_ROUTE,
        arguments = listOf(
            navArgument(UPI_PIN_PAYEE_NAME_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(UPI_PIN_AMOUNT_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(UPI_PIN_REF_ID_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(UPI_PIN_IS_UPI_ARG) {
                type = NavType.BoolType
                nullable = false
            },
            navArgument(UPI_PIN_BANK_NAME_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(UPI_PIN_ACCOUNT_NO_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) {
        UpiPinScreen(
            onBackClick = onBackClick,
            onUpiPinEntered = { payeeName, amount, pin, isUpiCode ->
                onNavigateToPaymentProcessing(payeeName, amount, isUpiCode)
            },
        )
    }
}

fun NavGraphBuilder.paymentProcessingScreen(
    onPaymentComplete: (String, String, String, String) -> Unit,
    onPaymentFailed: (String) -> Unit,
) {
    composableWithSlideTransitions(
        route = PAYMENT_PROCESSING_BASE_ROUTE,
        arguments = listOf(
            navArgument(PAYMENT_PROCESSING_PAYEE_NAME_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PAYMENT_PROCESSING_AMOUNT_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PAYMENT_PROCESSING_IS_UPI_ARG) {
                type = NavType.BoolType
                nullable = false
            },
        ),
    ) {
        PaymentProcessingScreen(
            onPaymentComplete = onPaymentComplete,
            onPaymentFailed = onPaymentFailed,
        )
    }
}

fun NavGraphBuilder.paymentSuccessScreen(
    onShareScreenshot: () -> Unit,
    onDone: () -> Unit,
    onNavigateToSendMoneyOptions: () -> Unit,
) {
    composableWithSlideTransitions(
        route = PAYMENT_SUCCESS_BASE_ROUTE,
        arguments = listOf(
            navArgument(PAYMENT_SUCCESS_PAYEE_NAME_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PAYMENT_SUCCESS_AMOUNT_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PAYMENT_SUCCESS_UPI_NAME_ARG) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        PaymentSuccessScreen(
            onShareScreenshot = onShareScreenshot,
            onDone = onDone,
            onNavigateToSendMoneyOptions = onNavigateToSendMoneyOptions,
        )
    }
}

fun NavGraphBuilder.paymentChatHistoryScreen(
    onBackClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
) {
    composableWithSlideTransitions(
        route = PAYMENT_CHAT_HISTORY_ROUTE,
    ) {
        PaymentChatHistoryScreen(
            onBackClick = onBackClick,
            onPaymentClick = onPaymentClick,
            onTransactionClick = onTransactionClick,
        )
    }
}

fun NavGraphBuilder.upiTransactionHistoryScreen(
    onBackClick: () -> Unit,
) {
    composableWithSlideTransitions(
        route = UPI_TRANSACTION_HISTORY_ROUTE,
    ) {
        UpiTransactionHistoryScreen(
            onBackClick = onBackClick,
        )
    }
}

fun NavGraphBuilder.paymentDetailsScreen(
    onBackClick: () -> Unit,
    onPayAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareScreenshot: () -> Unit,
) {
    composableWithSlideTransitions(
        route = PAYMENT_DETAILS_BASE_ROUTE,
        arguments = listOf(
            navArgument(PAYMENT_DETAILS_TRANSACTION_ID_ARG) {
                type = NavType.StringType
                nullable = false
            },
        ),
    ) { backStackEntry ->
        val transactionId = backStackEntry.savedStateHandle.get<String>(PAYMENT_DETAILS_TRANSACTION_ID_ARG) ?: ""
        PaymentDetailsScreen(
            onBackClick = onBackClick,
            onPayAgainClick = onPayAgainClick,
            onRetryClick = onRetryClick,
            onShareScreenshot = onShareScreenshot,
            transactionId = transactionId,
        )
    }
}

fun NavController.navigateToSendMoneyScreen(
    requestData: String,
    navOptions: NavOptions? = null,
) {
    val route = "$SEND_MONEY_ROUTE?$SEND_MONEY_ARG=$requestData"
    val options = navOptions ?: navOptions {
        popUpTo(SEND_MONEY_ROUTE) {
            inclusive = true
        }
    }

    navigate(route, options)
}

/**
 * URL encodes a string to handle special characters in navigation
 *
 * Optimized for UPI QR codes with future-proofing for common special characters.
 *
 * Essential UPI characters (12):
 * - URL structure: ?, &, =, %
 * - VPA format: @
 * - Common text: space, ", ', comma
 * - URLs: /, :, #, +
 *
 * Future-proofing characters (5):
 * - Currency symbols: $
 * - URL parameters: ;
 * - JSON/structured data: [, ], {, }
 */
private fun String.urlEncode(): String {
    return this.replace("%", "%25")
        .replace(" ", "%20")
        .replace("&", "%26")
        .replace("=", "%3D")
        .replace("?", "%3F")
        .replace("@", "%40")
        .replace("+", "%2B")
        .replace("/", "%2F")
        .replace(":", "%3A")
        .replace("#", "%23")
        .replace("\"", "%22")
        .replace("'", "%27")
        .replace(",", "%2C")
        .replace("$", "%24")
        .replace(";", "%3B")
        .replace("[", "%5B")
        .replace("]", "%5D")
        .replace("{", "%7B")
        .replace("}", "%7D")
}
