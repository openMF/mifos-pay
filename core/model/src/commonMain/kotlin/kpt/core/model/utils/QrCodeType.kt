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
    /** Payment to an account in the same Fineract instance. */
    INTRA_BANK,

    /** Payment routed to another bank through the payment hub. */
    INTER_BANK,

    /** A saved beneficiary to add, rather than a payment. */
    BENEFICIARY,

    /** Payment to a merchant. */
    MERCHANT,
    ;

    /** Helpers for decoding a [QrCodeType]. */
    companion object {
        /** Returns the [QrCodeType] at [ordinal], or [INTRA_BANK] if the ordinal is out of range. */
        fun fromOrdinal(ordinal: Int): QrCodeType = entries.getOrElse(ordinal) { INTRA_BANK }
    }
}
