/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay.model

import kotlinx.serialization.Serializable
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
/**
 * Sealed interface representing the result of processing a QR code.
 *
 * Used by [FastMpayProcessor] to determine navigation target based on [QrCodeData.type].
 */
@Serializable
sealed interface QrProcessResult {

    /**
     * Navigate to Add Beneficiary screen with pre-filled data.
     *
     * @param beneficiaryData JSON-encoded beneficiary data for pre-filling the form
     * @param sourceQrType The QR type that triggered this navigation (for post-add routing)
     * @param sourceQrData JSON-encoded QrCodeData for post-add navigation to transfer screen
     */
    @Serializable
    data class NavigateToAddBeneficiary(
        val beneficiaryData: String,
        val sourceQrType: QrCodeType,
        val sourceQrData: String,
    ) : QrProcessResult

    /**
     * Navigate to Inter-bank Transfer screen.
     *
     * @param accountExternalId Account external ID for participant lookup (REQUIRED)
     * @param recipientName Display name from QR (optional, used as hint while loading)
     * @param amount Pre-filled amount from QR (optional)
     */
    @Serializable
    data class NavigateToInterbankTransfer(
        val accountExternalId: String,
        val recipientName: String? = null,
        val amount: String? = null,
    ) : QrProcessResult

    /**
     * Navigate to Intra-bank Transfer screen.
     *
     * @param qrData Full QR code data for the transfer
     */
    @Serializable
    data class NavigateToIntraBankTransfer(val qrData: QrCodeData) : QrProcessResult

    /**
     * Navigate to Merchant Payment screen.
     *
     * @param qrData Full QR code data for the merchant payment
     */
    @Serializable
    data class NavigateToMerchantPayment(val qrData: QrCodeData) : QrProcessResult

    /**
     * Navigate to Make Transfer screen with existing beneficiary.
     *
     * Used when scanning an INTRA_BANK QR and the beneficiary already exists.
     * Contains all necessary data for the transfer since the QR code has
     * the internal IDs (clientId, accountId) needed for the transfer.
     *
     * @param qrData The QR code data containing transfer details
     * @param beneficiaryName The name of the existing beneficiary
     */
    @Serializable
    data class NavigateToMakeTransfer(
        val qrData: QrCodeData,
        val beneficiaryName: String,
    ) : QrProcessResult

    /**
     * Error occurred during QR processing.
     *
     * @param message Error message to display to the user
     */
    @Serializable
    data class Error(val message: String) : QrProcessResult

    /**
     * Bank mismatch detected - QR belongs to a different bank.
     *
     * Used when scanning an INTRA_BANK QR code that belongs to a different
     * financial service provider (bank). The user should be informed and
     * offered the option to try inter-bank transfer instead.
     *
     * @param qrData The original QR code data
     * @param currentBankId The user's current bank/FSP ID
     * @param qrBankId The bank/FSP ID from the QR code
     */
    @Serializable
    data class BankMismatch(
        val qrData: QrCodeData,
        val currentBankId: String,
        val qrBankId: String,
    ) : QrProcessResult
}
