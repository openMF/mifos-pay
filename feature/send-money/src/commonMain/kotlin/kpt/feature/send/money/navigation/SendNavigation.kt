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
import kpt.feature.send.money.SearchIfscScreen
import kpt.feature.send.money.SendMoneyOptionsScreen
import kpt.feature.send.money.SendMoneyScreen
/** Route of the send-money screen. */
const val SEND_MONEY_ROUTE = "send_money_route"

/** Navigation argument carrying the payment request data. */
const val SEND_MONEY_ARG = "requestData"

/** Send-money route pattern including the optional request data argument. */
const val SEND_MONEY_BASE_ROUTE = "$SEND_MONEY_ROUTE?$SEND_MONEY_ARG={$SEND_MONEY_ARG}"

/** Route of the send-money options screen. */
const val SEND_MONEY_OPTIONS_ROUTE = "send_money_options_route"

/** Route of the bank transfer screen. */
const val BANK_TRANSFER_ROUTE = "bank_transfer_route"

/** Route of the IFSC search screen. */
const val SEARCH_IFSC_ROUTE = "search_ifsc_route"

/** Route of the payee details screen. */
const val PAYEE_DETAILS_ROUTE = "payee_details_route"

/** Navigation argument carrying the scanned QR code data. */
const val PAYEE_DETAILS_ARG = "qrCodeData"

/** Payee details route pattern including the optional QR code data argument. */
const val PAYEE_DETAILS_BASE_ROUTE = "$PAYEE_DETAILS_ROUTE?$PAYEE_DETAILS_ARG={$PAYEE_DETAILS_ARG}"

/** Route of the pay-anyone screen. */
const val PAY_ANYONE_ROUTE = "pay_anyone_route"

/** Navigation argument carrying the contact selected in the picker. */
const val PAY_ANYONE_SELECTED_CONTACT_ARG = "selectedContact"

/** Pay-anyone route pattern including the optional selected contact argument. */
const val PAY_ANYONE_BASE_ROUTE =
    "$PAY_ANYONE_ROUTE?$PAY_ANYONE_SELECTED_CONTACT_ARG={$PAY_ANYONE_SELECTED_CONTACT_ARG}"

/** Route of the contacts picker screen. */
const val CONTACTS_PICKER_ROUTE = "contacts_picker_route"

/** Navigates to the pay-anyone screen. */
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

/** Navigates to the contacts picker screen. */
fun NavController.navigateToContactsPickerScreen(
    navOptions: NavOptions? = null,
) = navigate(CONTACTS_PICKER_ROUTE, navOptions)

/** Navigates to the send-money screen. */
fun NavController.navigateToSendMoneyScreen(
    navOptions: NavOptions? = null,
) = navigate(SEND_MONEY_ROUTE, navOptions)

/** Navigates to the send-money options screen. */
fun NavController.navigateToSendMoneyOptionsScreen(
    navOptions: NavOptions? = null,
) = navigate(SEND_MONEY_OPTIONS_ROUTE, navOptions)

/** Navigates to the bank transfer screen. */
fun NavController.navigateToBankTransferScreen(
    navOptions: NavOptions? = null,
) = navigate(BANK_TRANSFER_ROUTE, navOptions)

/** Navigates to the IFSC search screen. */
fun NavController.navigateToSearchIfscScreen(
    navOptions: NavOptions? = null,
) = navigate(SEARCH_IFSC_ROUTE, navOptions)

/** Navigates to the payee details screen. */
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
/** Registers the send-money screen in the navigation graph. */
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

/** Registers the send-money options screen in the navigation graph. */
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

/** Registers the bank transfer screen in the navigation graph. */
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

/** Registers the IFSC search screen in the navigation graph. */
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

/** Registers the pay-anyone screen in the navigation graph. */
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

/** Registers the contacts picker screen in the navigation graph. */
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

/** Registers the payee details screen in the navigation graph. */
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

/** Navigates to the send-money screen with [requestData], clearing it from the back stack first. */
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
internal fun String.urlEncode(): String {
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
