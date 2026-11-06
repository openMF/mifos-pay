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
 * Enum representing the type of QR code for MPay transactions.
 *
 * @property INTRA_BANK Same-bank transfer - contains internal Mifos IDs (clientId, accountId)
 * @property INTER_BANK Cross-bank transfer - contains phone number for participant lookup
 * @property BENEFICIARY Add beneficiary - pre-fill beneficiary form
 * @property MERCHANT Merchant payment - for future merchant payment support
 */
@Serializable
enum class QrCodeType {
    INTRA_BANK,
    INTER_BANK,
    BENEFICIARY,
    MERCHANT,
    ;

    companion object {
        fun fromOrdinal(ordinal: Int): QrCodeType = entries.getOrElse(ordinal) { INTRA_BANK }
    }
}
