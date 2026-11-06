/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.addupdatebeneficiary

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kpt.core.model.utils.QrCodeType
import kpt.core.ui.composableWithSlideTransitions
private const val ADD_TYPE: String = "add_beneficiary"
private const val EDIT_TYPE: String = "edit_beneficiary"
private const val BENEFICIARY_DATA: String = "beneficiary_data"
private const val SOURCE_QR_TYPE: String = "source_qr_type"
private const val SOURCE_QR_DATA: String = "source_qr_data"

private const val ADD_EDIT_ITEM_PREFIX: String = "beneficiary_add_edit_item"
private const val ADD_EDIT_ITEM_TYPE: String = "beneficiary_add_edit_type"

private const val ADD_EDIT_ITEM_ROUTE: String =
    ADD_EDIT_ITEM_PREFIX +
        "/{$ADD_EDIT_ITEM_TYPE}" +
        "?$BENEFICIARY_DATA={$BENEFICIARY_DATA}" +
        "&$SOURCE_QR_TYPE={$SOURCE_QR_TYPE}" +
        "&$SOURCE_QR_DATA={$SOURCE_QR_DATA}"

data class BeneficiaryAddEditArgs(
    val addEditType: BeneficiaryAddEditType,
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        addEditType = when (requireNotNull(savedStateHandle[ADD_EDIT_ITEM_TYPE])) {
            ADD_TYPE -> {
                val beneficiaryData: String? = savedStateHandle[BENEFICIARY_DATA]
                val data = beneficiaryData?.takeIf { it != "null" }
                val qrTypeOrdinal: String? = savedStateHandle[SOURCE_QR_TYPE]
                val qrType = qrTypeOrdinal?.toIntOrNull()?.let { QrCodeType.fromOrdinal(it) }
                val qrData: String? = savedStateHandle[SOURCE_QR_DATA]
                val sourceQrData = qrData?.takeIf { it != "null" }
                BeneficiaryAddEditType.AddItem(data, qrType, sourceQrData)
            }
            EDIT_TYPE -> BeneficiaryAddEditType.EditItem(
                requireNotNull(savedStateHandle[BENEFICIARY_DATA]),
            )
            else -> throw IllegalStateException("Unknown BeneficiaryAddEditType.")
        },
    )
}

fun NavGraphBuilder.addEditBeneficiaryScreen(
    navigateBack: () -> Unit,
    navigateToQrReaderScreen: () -> Unit,
    navigateToIntraBankTransfer: (
        officeId: Int,
        clientId: Long,
        accountTypeId: Int,
        accountId: Int,
        amount: Int,
        accountName: String,
        accountNo: String,
    ) -> Unit,
    navigateToInterbankTransfer: (accountNumber: String, recipientName: String) -> Unit,
) {
    composableWithSlideTransitions(
        route = ADD_EDIT_ITEM_ROUTE,
        arguments = listOf(
            navArgument(ADD_EDIT_ITEM_TYPE) { type = NavType.StringType },
        ),
    ) {
        AddEditBeneficiaryScreen(
            navigateBack = navigateBack,
            navigateToQrReaderScreen = navigateToQrReaderScreen,
            navigateToIntraBankTransfer = navigateToIntraBankTransfer,
            navigateToInterbankTransfer = navigateToInterbankTransfer,
        )
    }
}

fun NavController.navigateToBeneficiaryAddEdit(
    addEditType: BeneficiaryAddEditType,
    navOptions: NavOptions? = null,
) {
    navigate(
        route = "$ADD_EDIT_ITEM_PREFIX/${addEditType.toTypeString()}" +
            "?$BENEFICIARY_DATA=${addEditType.toDataOrNull()}" +
            "&$SOURCE_QR_TYPE=${addEditType.toQrTypeOrNull()}" +
            "&$SOURCE_QR_DATA=${addEditType.toQrDataOrNull()}",
        navOptions = navOptions,
    )
}

private fun BeneficiaryAddEditType.toTypeString(): String =
    when (this) {
        is BeneficiaryAddEditType.AddItem -> ADD_TYPE
        is BeneficiaryAddEditType.EditItem -> EDIT_TYPE
    }

private fun BeneficiaryAddEditType.toDataOrNull(): String? =
    when (this) {
        is BeneficiaryAddEditType.AddItem -> beneficiary
        is BeneficiaryAddEditType.EditItem -> beneficiary
    }

private fun BeneficiaryAddEditType.toQrTypeOrNull(): String? =
    when (this) {
        is BeneficiaryAddEditType.AddItem -> sourceQrType?.ordinal?.toString()
        is BeneficiaryAddEditType.EditItem -> null
    }

private fun BeneficiaryAddEditType.toQrDataOrNull(): String? =
    when (this) {
        is BeneficiaryAddEditType.AddItem -> sourceQrData
        is BeneficiaryAddEditType.EditItem -> null
    }
