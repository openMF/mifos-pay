/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.ktor.http.encodeURLPathPart
import kpt.core.data.util.MpayQrCodeProcessor
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.core.ui.composableWithSlideTransitions
import kpt.feature.fastmpay.FastMpayScreen
private const val FAST_MPAY_ROUTE_BASE = "fast_mpay_route"
internal const val QR_DATA_ARG = "qrData"

const val FAST_MPAY_ROUTE = "$FAST_MPAY_ROUTE_BASE/{$QR_DATA_ARG}"

/**
 * Navigate to Fast MPay processing screen.
 *
 * @param qrData The decoded QR code data from scanner
 * @param navOptions Optional navigation options
 */
fun NavController.navigateToFastMpay(
    qrData: QrCodeData,
    navOptions: NavOptions? = null,
) {
    val encodedData = MpayQrCodeProcessor.encodeMpayString(qrData)
    // URL-encode the Base64 string to handle special characters (+, /, =)
    val urlSafeData = encodedData.encodeURLPathPart()
    navigate(
        route = "$FAST_MPAY_ROUTE_BASE/$urlSafeData",
        navOptions = navOptions,
    )
}

/**
 * Registers the Fast MPay screen in the navigation graph.
 *
 * @param onNavigateToAddBeneficiary Callback for INTRA_BANK/BENEFICIARY type when beneficiary doesn't exist
 *        (beneficiaryData, sourceQrType, sourceQrData)
 * @param onNavigateToMakeTransfer Callback for INTRA_BANK type when beneficiary exists (qrData, beneficiaryName)
 * @param onNavigateToInterbankTransfer Callback for INTER_BANK type (accountExternalId, recipientName, amount)
 * @param onNavigateToIntraBankTransfer Callback for future intra-bank direct transfer
 * @param onNavigateToMerchantPayment Callback for MERCHANT type
 * @param onNavigateBack Callback to navigate back (for cancel action on bank mismatch)
 * @param onError Callback when processing fails
 */
fun NavGraphBuilder.fastMpayScreen(
    onNavigateToAddBeneficiary: (beneficiaryData: String, sourceQrType: QrCodeType, sourceQrData: String) -> Unit,
    onNavigateToMakeTransfer: (qrData: QrCodeData, beneficiaryName: String) -> Unit,
    onNavigateToInterbankTransfer: (accountExternalId: String, recipientName: String?, amount: String?) -> Unit,
    onNavigateToIntraBankTransfer: (qrData: QrCodeData) -> Unit,
    onNavigateToMerchantPayment: (qrData: QrCodeData) -> Unit,
    onNavigateBack: () -> Unit,
    onError: (message: String) -> Unit,
) {
    composableWithSlideTransitions(
        route = FAST_MPAY_ROUTE,
        arguments = listOf(
            navArgument(QR_DATA_ARG) {
                type = NavType.StringType
            },
        ),
    ) {
        FastMpayScreen(
            onNavigateToAddBeneficiary = onNavigateToAddBeneficiary,
            onNavigateToMakeTransfer = onNavigateToMakeTransfer,
            onNavigateToInterbankTransfer = onNavigateToInterbankTransfer,
            onNavigateToIntraBankTransfer = onNavigateToIntraBankTransfer,
            onNavigateToMerchantPayment = onNavigateToMerchantPayment,
            onNavigateBack = onNavigateBack,
            onError = onError,
        )
    }
}
