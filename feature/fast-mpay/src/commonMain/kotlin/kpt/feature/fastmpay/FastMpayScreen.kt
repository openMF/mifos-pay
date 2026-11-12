/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.core.ui.InfoBottomSheet
import kpt.core.ui.InfoType
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_cancel
import kpt.core.ui.generated.resources.core_ui_confirm_qr_amount_message
import kpt.core.ui.generated.resources.core_ui_confirm_qr_amount_title
import kpt.core.ui.generated.resources.core_ui_different_bank_message
import kpt.core.ui.generated.resources.core_ui_different_bank_title
import kpt.core.ui.generated.resources.core_ui_interbank_unavailable_message
import kpt.core.ui.generated.resources.core_ui_interbank_unavailable_title
import kpt.core.ui.generated.resources.core_ui_proceed_payment
import kpt.core.ui.generated.resources.core_ui_try_interbank
import kpt.core.ui.generated.resources.core_ui_understood
import kpt.feature.fastmpay.model.QrProcessResult
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/**
 * Fast MPay processing screen.
 *
 * This screen acts as a routing hub - it receives encoded QR data via navigation,
 * decodes and processes it using [FastMpayProcessor], and immediately navigates to the
 * appropriate destination based on the QR type.
 *
 * The screen shows a brief loading indicator while processing.
 *
 * @param onNavigateToAddBeneficiary Callback for INTRA_BANK/BENEFICIARY type when beneficiary doesn't exist
 * @param onNavigateToMakeTransfer Callback for INTRA_BANK type when beneficiary exists (qrData, beneficiaryName)
 * @param onNavigateToInterbankTransfer Callback for INTER_BANK type
 * @param onNavigateToIntraBankTransfer Callback for future intra-bank direct transfer
 * @param onNavigateToMerchantPayment Callback for MERCHANT type
 * @param onNavigateBack Callback to navigate back (for cancel action)
 * @param onError Callback when processing fails
 * @param viewModel The ViewModel for processing
 */
@Composable
fun FastMpayScreen(
    onNavigateToAddBeneficiary: (beneficiaryData: String, sourceQrType: QrCodeType, sourceQrData: String) -> Unit,
    onNavigateToMakeTransfer: (QrCodeData, String) -> Unit,
    onNavigateToInterbankTransfer: (String, String?, String?) -> Unit,
    onNavigateToIntraBankTransfer: (QrCodeData) -> Unit,
    onNavigateToMerchantPayment: (QrCodeData) -> Unit,
    onNavigateBack: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FastMpayViewModel = koinViewModel(),
) {
    val result by viewModel.resultFlow.collectAsStateWithLifecycle()
    val error by viewModel.errorFlow.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // State for bank mismatch bottom sheet
    var showBankMismatchSheet by remember { mutableStateOf(false) }
    var bankMismatchData by remember { mutableStateOf<QrProcessResult.BankMismatch?>(null) }

    // State for external ID missing bottom sheet
    var showExternalIdMissingSheet by remember { mutableStateOf(false) }

    // State for amount confirmation dialog
    var showAmountConfirmation by remember { mutableStateOf(false) }
    var pendingAmountConfirmation by remember {
        mutableStateOf<PendingAmountConfirmation?>(null)
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let { message ->
            onError(message)
            viewModel.clearError()
        }
    }

    // Helper function to check if QR data has a pre-filled amount
    fun hasPrefilledAmount(qrData: QrCodeData): Boolean {
        return qrData.amount.isNotBlank() && qrData.amount != "0" && qrData.amount != "0.0"
    }

    // Navigate based on result
    LaunchedEffect(result) {
        when (val r = result) {
            is QrProcessResult.NavigateToAddBeneficiary -> {
                viewModel.clearResult()
                onNavigateToAddBeneficiary(r.beneficiaryData, r.sourceQrType, r.sourceQrData)
            }

            is QrProcessResult.NavigateToMakeTransfer -> {
                viewModel.clearResult()
                // Check if QR has pre-filled amount - show confirmation
                if (hasPrefilledAmount(r.qrData)) {
                    pendingAmountConfirmation = PendingAmountConfirmation.MakeTransfer(
                        qrData = r.qrData,
                        beneficiaryName = r.beneficiaryName,
                    )
                    showAmountConfirmation = true
                } else {
                    onNavigateToMakeTransfer(r.qrData, r.beneficiaryName)
                }
            }

            is QrProcessResult.NavigateToInterbankTransfer -> {
                viewModel.clearResult()
                // Check if QR has pre-filled amount - show confirmation
                if (!r.amount.isNullOrBlank() && r.amount != "0" && r.amount != "0.0") {
                    // Default currency for display when not available from QR
                    pendingAmountConfirmation = PendingAmountConfirmation.InterbankTransfer(
                        accountExternalId = r.accountExternalId,
                        recipientName = r.recipientName,
                        amount = r.amount,
                        currency = "USD",
                    )
                    showAmountConfirmation = true
                } else {
                    onNavigateToInterbankTransfer(r.accountExternalId, r.recipientName, r.amount)
                }
            }

            is QrProcessResult.NavigateToIntraBankTransfer -> {
                viewModel.clearResult()
                // Check if QR has pre-filled amount - show confirmation
                if (hasPrefilledAmount(r.qrData)) {
                    pendingAmountConfirmation = PendingAmountConfirmation.IntraBankTransfer(
                        qrData = r.qrData,
                    )
                    showAmountConfirmation = true
                } else {
                    onNavigateToIntraBankTransfer(r.qrData)
                }
            }

            is QrProcessResult.NavigateToMerchantPayment -> {
                viewModel.clearResult()
                // Check if QR has pre-filled amount - show confirmation
                if (hasPrefilledAmount(r.qrData)) {
                    pendingAmountConfirmation = PendingAmountConfirmation.MerchantPayment(
                        qrData = r.qrData,
                    )
                    showAmountConfirmation = true
                } else {
                    onNavigateToMerchantPayment(r.qrData)
                }
            }

            is QrProcessResult.BankMismatch -> {
                bankMismatchData = r
                showBankMismatchSheet = true
                viewModel.clearResult()
            }

            is QrProcessResult.Error -> {
                viewModel.clearResult()
                onError(r.message)
            }

            null -> {
                // Still processing - show loading indicator
            }
        }
    }

    // Show brief loading indicator while processing
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }

    // Bank mismatch info bottom sheet
    if (showBankMismatchSheet && bankMismatchData != null) {
        val mismatchData = bankMismatchData
        InfoBottomSheet(
            title = stringResource(Res.string.core_ui_different_bank_title),
            message = stringResource(Res.string.core_ui_different_bank_message),
            infoType = InfoType.WARNING,
            onDismiss = {
                showBankMismatchSheet = false
                bankMismatchData = null
            },
            primaryActionText = stringResource(Res.string.core_ui_try_interbank),
            onPrimaryAction = {
                val accountExternalId = mismatchData?.qrData?.accountExternalId
                if (!accountExternalId.isNullOrBlank()) {
                    onNavigateToInterbankTransfer(
                        accountExternalId,
                        mismatchData.qrData.clientName.takeIf { it.isNotBlank() },
                        mismatchData.qrData.amount.takeIf { it.isNotBlank() },
                    )
                    showBankMismatchSheet = false
                    bankMismatchData = null
                } else {
                    showBankMismatchSheet = false
                    bankMismatchData = null
                    showExternalIdMissingSheet = true
                }
            },
            secondaryActionText = stringResource(Res.string.core_ui_cancel),
            onSecondaryAction = {
                onNavigateBack()
            },
        )
    }

    // External ID missing info bottom sheet
    if (showExternalIdMissingSheet) {
        InfoBottomSheet(
            title = stringResource(Res.string.core_ui_interbank_unavailable_title),
            message = stringResource(Res.string.core_ui_interbank_unavailable_message),
            infoType = InfoType.INFO,
            onDismiss = {
                showExternalIdMissingSheet = false
                onNavigateBack()
            },
            secondaryActionText = stringResource(Res.string.core_ui_understood),
            onSecondaryAction = {
                onNavigateBack()
            },
        )
    }

    // Amount confirmation bottom sheet
    if (showAmountConfirmation && pendingAmountConfirmation != null) {
        val pending = pendingAmountConfirmation!!
        val (amount, currency) = when (pending) {
            is PendingAmountConfirmation.MakeTransfer -> {
                pending.qrData.amount to pending.qrData.currency
            }
            is PendingAmountConfirmation.InterbankTransfer -> {
                (pending.amount ?: "0") to pending.currency
            }
            is PendingAmountConfirmation.IntraBankTransfer -> {
                pending.qrData.amount to pending.qrData.currency
            }
            is PendingAmountConfirmation.MerchantPayment -> {
                pending.qrData.amount to pending.qrData.currency
            }
        }

        InfoBottomSheet(
            title = stringResource(Res.string.core_ui_confirm_qr_amount_title),
            message = stringResource(Res.string.core_ui_confirm_qr_amount_message, currency, amount),
            infoType = InfoType.WARNING,
            onDismiss = {
                showAmountConfirmation = false
                pendingAmountConfirmation = null
                onNavigateBack()
            },
            primaryActionText = stringResource(Res.string.core_ui_proceed_payment),
            onPrimaryAction = {
                showAmountConfirmation = false
                when (pending) {
                    is PendingAmountConfirmation.MakeTransfer -> {
                        onNavigateToMakeTransfer(pending.qrData, pending.beneficiaryName)
                    }
                    is PendingAmountConfirmation.InterbankTransfer -> {
                        onNavigateToInterbankTransfer(pending.accountExternalId, pending.recipientName, pending.amount)
                    }
                    is PendingAmountConfirmation.IntraBankTransfer -> {
                        onNavigateToIntraBankTransfer(pending.qrData)
                    }
                    is PendingAmountConfirmation.MerchantPayment -> {
                        onNavigateToMerchantPayment(pending.qrData)
                    }
                }
                pendingAmountConfirmation = null
            },
            secondaryActionText = stringResource(Res.string.core_ui_cancel),
            onSecondaryAction = {
                onNavigateBack()
            },
        )
    }
}

/**
 * Sealed interface representing pending navigation actions that require amount confirmation.
 */
private sealed interface PendingAmountConfirmation {
    data class MakeTransfer(
        val qrData: QrCodeData,
        val beneficiaryName: String,
    ) : PendingAmountConfirmation

    data class InterbankTransfer(
        val accountExternalId: String,
        val recipientName: String?,
        val amount: String?,
        val currency: String,
    ) : PendingAmountConfirmation

    data class IntraBankTransfer(
        val qrData: QrCodeData,
    ) : PendingAmountConfirmation

    data class MerchantPayment(
        val qrData: QrCodeData,
    ) : PendingAmountConfirmation
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun FastMpayScreenPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
