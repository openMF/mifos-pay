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
    companion object {
        const val DEFAULT_CURRENCY = "INR"
    }
}
