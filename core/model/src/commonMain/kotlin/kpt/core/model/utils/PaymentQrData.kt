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
 * Parcelable data class representing the UPI payment request details
 * @property clientName Payee name
 * @property clientId Virtual Payment Address (VPA) of the payee
 * @property accountNo Account number
 * @property currency Currency code
 * @property amount Payment amount as a string
 * @property accountId Internal identifier of the payee account
 * @property officeId Identifier of the payee office
 * @property accountTypeId Identifier of the payee account type
 */
@Serializable
data class PaymentQrData(
    val clientId: Long,
    val clientName: String,
    val accountNo: String,
    val amount: String,
    val accountId: Long,
    val currency: String = DEFAULT_CURRENCY,
    val officeId: Long = OFFICE_ID,
    val accountTypeId: Long = ACCOUNT_TYPE_ID,
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

/** Converts the payment QR data into an [AccountResult] describing the wallet account. */
fun PaymentQrData.toAccount(): AccountResult {
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
