/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kotlinx.serialization.Serializable
import kpt.core.model.search.AccountResult
/**
 * Data class representing QR code data for MPay transactions.
 *
 * Supports multiple QR formats based on [type]:
 * 1. INTRA_BANK: clientId > 0, accountId > 0
 *    Contains internal Mifos IDs for direct transfer within same bank.
 *
 * 2. INTER_BANK: Uses accountExternalId for participant lookup
 *    Routes to payment hub (Mojaloop) for cross-bank transfers.
 *
 * 3. BENEFICIARY: For adding a beneficiary with pre-filled data.
 *
 * 4. MERCHANT: For future merchant payment support.
 *
 * The [fspId] field enables automatic routing:
 * - If scanner's fspId == QR's fspId → Intra-bank transfer
 * - If scanner's fspId != QR's fspId → Inter-bank transfer
 *
 * @property type The type of QR code determining how it should be processed
 * @property fspId Financial Service Provider ID (bank/tenant identifier) for routing
 * @property clientId Internal client ID (0 for inter-bank QR)
 * @property clientName Payee name
 * @property accountNo Account number (empty for inter-bank QR)
 * @property amount Payment amount as a string
 * @property accountId Internal account ID (0 for inter-bank QR)
 * @property currency Currency code
 * @property officeId Office ID (0 for inter-bank QR)
 * @property officeName Office name for beneficiary creation
 * @property accountTypeId Account type ID (0 for inter-bank QR)
 * @property phoneNumber Deprecated: use accountExternalId instead
 * @property accountExternalId External ID of the account for inter-bank transfers
 */
@Serializable
data class QrCodeData(
    val type: QrCodeType = QrCodeType.INTRA_BANK,
    val fspId: String? = null,
    val clientId: Long,
    val clientName: String,
    val accountNo: String,
    val amount: String,
    val accountId: Long,
    val currency: String = DEFAULT_CURRENCY,
    val officeId: Long = OFFICE_ID,
    val officeName: String = "",
    val accountTypeId: Long = ACCOUNT_TYPE_ID,
    @Deprecated("Use accountExternalId instead")
    val phoneNumber: String? = null,
    val accountExternalId: String? = null,
) {

    /**
     * Companion object containing constants for default values
     * currently Savings Account to Savings Account Transaction are allowed
     */
    companion object {
        /** Currency code used when none is given. */
        const val DEFAULT_CURRENCY = "USD"

        /** Default office id. */
        const val OFFICE_ID: Long = 1

        /** Default account type id (wallet). */
        // WALLET
        const val ACCOUNT_TYPE_ID: Long = 2
    }
}

/** Converts the scanned QR data into an [AccountResult] describing the wallet account. */
fun QrCodeData.toAccount(): AccountResult {
    return AccountResult(
        entityId = accountId,
        entityAccountNo = accountNo,
        entityExternalId = "",
        entityName = "WALLET",
        entityType = "SAVING",
        parentId = clientId,
        parentName = clientName,
        parentType = "client",
        subEntityType = "depositAccountType.savingsDeposit",
    )
}
