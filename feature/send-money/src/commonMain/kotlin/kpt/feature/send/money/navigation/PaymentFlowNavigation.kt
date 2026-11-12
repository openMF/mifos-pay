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
import kpt.feature.send.money.PaymentChatHistoryScreen
import kpt.feature.send.money.PaymentDetailsScreen
import kpt.feature.send.money.PaymentProcessingScreen
import kpt.feature.send.money.PaymentSuccessScreen
import kpt.feature.send.money.UpiPinScreen
import kpt.feature.send.money.UpiTransactionHistoryScreen
/** Route of the UPI PIN screen. */
const val UPI_PIN_ROUTE = "upi_pin_route"

/** Navigation argument carrying the payee name for the UPI PIN screen. */
const val UPI_PIN_PAYEE_NAME_ARG = "payeeName"

/** Navigation argument carrying the amount for the UPI PIN screen. */
const val UPI_PIN_AMOUNT_ARG = "amount"

/** Navigation argument flagging a UPI code payment for the UPI PIN screen. */
const val UPI_PIN_IS_UPI_ARG = "isUpiCode"

/** Navigation argument carrying the bank name for the UPI PIN screen. */
const val UPI_PIN_BANK_NAME_ARG = "bankName"

/** Navigation argument carrying the account number for the UPI PIN screen. */
const val UPI_PIN_ACCOUNT_NO_ARG = "accountNo"

/** Navigation argument carrying the reference id for the UPI PIN screen. */
const val UPI_PIN_REF_ID_ARG = "refId"

/** UPI PIN route pattern including its optional arguments. */
const val UPI_PIN_BASE_ROUTE =
    "$UPI_PIN_ROUTE?$UPI_PIN_PAYEE_NAME_ARG={$UPI_PIN_PAYEE_NAME_ARG}" +
        "&$UPI_PIN_AMOUNT_ARG={$UPI_PIN_AMOUNT_ARG}" +
        "&$UPI_PIN_REF_ID_ARG={$UPI_PIN_REF_ID_ARG}" +
        "&$UPI_PIN_IS_UPI_ARG={$UPI_PIN_IS_UPI_ARG}" +
        "&$UPI_PIN_BANK_NAME_ARG={$UPI_PIN_BANK_NAME_ARG}" +
        "&$UPI_PIN_ACCOUNT_NO_ARG={$UPI_PIN_ACCOUNT_NO_ARG}"

/** Route of the payment processing screen. */
const val PAYMENT_PROCESSING_ROUTE = "payment_processing_route"

/** Navigation argument carrying the payee name for the processing screen. */
const val PAYMENT_PROCESSING_PAYEE_NAME_ARG = "payeeName"

/** Navigation argument carrying the amount for the processing screen. */
const val PAYMENT_PROCESSING_AMOUNT_ARG = "amount"

/** Navigation argument flagging a UPI code payment for the processing screen. */
const val PAYMENT_PROCESSING_IS_UPI_ARG = "isUpiCode"

/** Payment processing route pattern including its optional arguments. */
const val PAYMENT_PROCESSING_BASE_ROUTE =
    "$PAYMENT_PROCESSING_ROUTE?$PAYMENT_PROCESSING_PAYEE_NAME_ARG={$PAYMENT_PROCESSING_PAYEE_NAME_ARG}" +
        "&$PAYMENT_PROCESSING_AMOUNT_ARG={$PAYMENT_PROCESSING_AMOUNT_ARG}" +
        "&$PAYMENT_PROCESSING_IS_UPI_ARG={$PAYMENT_PROCESSING_IS_UPI_ARG}"

/** Route of the payment success screen. */
const val PAYMENT_SUCCESS_ROUTE = "payment_success_route"

/** Navigation argument carrying the payee name for the success screen. */
const val PAYMENT_SUCCESS_PAYEE_NAME_ARG = "payeeName"

/** Navigation argument carrying the amount for the success screen. */
const val PAYMENT_SUCCESS_AMOUNT_ARG = "amount"

/** Navigation argument carrying the UPI name for the success screen. */
const val PAYMENT_SUCCESS_UPI_NAME_ARG = "upiName"

/** Navigation argument carrying the transaction timestamp for the success screen. */
const val PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG = "transactionTimestamp"

/** Payment success route pattern including its optional arguments. */
const val PAYMENT_SUCCESS_BASE_ROUTE =
    "$PAYMENT_SUCCESS_ROUTE?$PAYMENT_SUCCESS_PAYEE_NAME_ARG={$PAYMENT_SUCCESS_PAYEE_NAME_ARG}" +
        "&$PAYMENT_SUCCESS_AMOUNT_ARG={$PAYMENT_SUCCESS_AMOUNT_ARG}" +
        "&$PAYMENT_SUCCESS_UPI_NAME_ARG={$PAYMENT_SUCCESS_UPI_NAME_ARG}" +
        "&$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG={$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG}"

/** Route of the payment chat history screen. */
const val PAYMENT_CHAT_HISTORY_ROUTE = "payment_chat_history_route"

/** Route of the UPI transaction history screen. */
const val UPI_TRANSACTION_HISTORY_ROUTE = "upi_transaction_history_route"

/** Route of the payment details screen. */
const val PAYMENT_DETAILS_ROUTE = "payment_details_route"

/** Navigation argument carrying the transaction id for the payment details screen. */
const val PAYMENT_DETAILS_TRANSACTION_ID_ARG = "transactionId"

/** Payment details route pattern with the transaction id path segment. */
const val PAYMENT_DETAILS_BASE_ROUTE = "$PAYMENT_DETAILS_ROUTE/{$PAYMENT_DETAILS_TRANSACTION_ID_ARG}"

/** Navigates to the payment details screen for [transactionId]. */
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

/** Navigates to the UPI PIN screen. */
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
    val route = "$UPI_PIN_ROUTE?$UPI_PIN_PAYEE_NAME_ARG=$encodedPayeeName" +
        "&$UPI_PIN_AMOUNT_ARG=$encodedAmount" +
        "&$UPI_PIN_REF_ID_ARG=$encodedRefId" +
        "&$UPI_PIN_IS_UPI_ARG=$isUpiCode" +
        "&$UPI_PIN_BANK_NAME_ARG=$encodedBankName" +
        "&$UPI_PIN_ACCOUNT_NO_ARG=$encodedAccountNo"
    val options = navOptions ?: navOptions {
        popUpTo(PAYEE_DETAILS_ROUTE) { inclusive = false }
    }
    navigate(route, options)
}

// amount in paise
/** Navigates to the payment processing screen. */
fun NavController.navigateToPaymentProcessingScreen(
    payeeName: String,
    amount: String,
    isUpiCode: Boolean,
    navOptions: NavOptions? = null,
) {
    val encodedPayeeName = payeeName.urlEncode()
    val encodedAmount = amount.urlEncode()
    val route = "$PAYMENT_PROCESSING_ROUTE?$PAYMENT_PROCESSING_PAYEE_NAME_ARG=$encodedPayeeName" +
        "&$PAYMENT_PROCESSING_AMOUNT_ARG=$encodedAmount" +
        "&$PAYMENT_PROCESSING_IS_UPI_ARG=$isUpiCode"
    val options = navOptions ?: navOptions {
        popUpTo(UPI_PIN_ROUTE) { inclusive = true }
    }
    navigate(route, options)
}

// Expected to be in paise
/** Navigates to the payment success screen. */
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
    val route = "$PAYMENT_SUCCESS_ROUTE?$PAYMENT_SUCCESS_PAYEE_NAME_ARG=$encodedPayeeName" +
        "&$PAYMENT_SUCCESS_AMOUNT_ARG=$encodedAmount" +
        "&$PAYMENT_SUCCESS_UPI_NAME_ARG=$encodedUpiName" +
        "&$PAYMENT_SUCCESS_TRANSACTION_TIMESTAMP_ARG=$encodedTransactionTimestamp"
    val options = navOptions ?: navOptions {
        popUpTo(PAYMENT_PROCESSING_ROUTE) { inclusive = true }
    }
    navigate(route, options)
}

/** Navigates to the payment chat history screen. */
fun NavController.navigateToPaymentChatHistoryScreen(
    navOptions: NavOptions? = null,
) = navigate(PAYMENT_CHAT_HISTORY_ROUTE, navOptions)

/** Navigates to the UPI transaction history screen. */
fun NavController.navigateToUpiTransactionHistoryScreen(
    navOptions: NavOptions? = null,
) = navigate(UPI_TRANSACTION_HISTORY_ROUTE, navOptions)

/** Registers the UPI PIN screen in the navigation graph. */
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

/** Registers the payment processing screen in the navigation graph. */
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

/** Registers the payment success screen in the navigation graph. */
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

/** Registers the payment chat history screen in the navigation graph. */
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

/** Registers the UPI transaction history screen in the navigation graph. */
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

/** Registers the payment details screen in the navigation graph. */
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
