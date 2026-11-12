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

/**
 * Data class representing standard UPI QR code data
 * Based on UPI QR code specification
 *
 * @property payeeName Name of the payee
 * @property payeeVpa Virtual payment address (VPA) of the payee
 * @property amount Payment amount as a string, empty if not preset
 * @property currency Currency code, INR by default
 * @property transactionNote Note shown with the transaction
 * @property merchantCode Merchant category code, empty for personal payees
 * @property transactionReference Reference id for the transaction
 * @property url Optional URL carried in the QR code
 * @property mode UPI mode code, "02" for QR code
 */
@Serializable
data class StandardUpiQrData(
    val payeeName: String,
    val payeeVpa: String,
    val amount: String = "",
    val currency: String = "INR",
    val transactionNote: String = "",
    val merchantCode: String = "",
    val transactionReference: String = "",
    val url: String = "",
    // 02 for QR code
    val mode: String = "02",
) {
    /** Default values for standard UPI QR data. */
    companion object {
        /** Currency code used when none is given. */
        const val DEFAULT_CURRENCY = "INR"
    }
}
