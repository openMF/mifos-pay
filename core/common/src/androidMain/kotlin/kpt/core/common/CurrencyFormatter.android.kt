/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import java.text.NumberFormat
import java.util.Locale

/** Platform actual of [CurrencyFormatter] for wallet balance formatting. */
actual object CurrencyFormatter {

    /**
     * Platform actual: formats [balance] as a currency string for [currencyCode] with [maximumFractionDigits] decimals.
     */
    actual fun format(
        balance: Double?,
        currencyCode: String?,
        maximumFractionDigits: Int?,
    ): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = maximumFractionDigits ?: 0
        formatter.minimumFractionDigits = maximumFractionDigits ?: 0
        return formatter.format(balance)
    }

    /** Platform actual: formats [balance] as a plain number string with [maximumFractionDigits] decimals. */
    actual fun format(
        balance: Double?,
        maximumFractionDigits: Int?,
    ): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = maximumFractionDigits ?: 0
        formatter.minimumFractionDigits = maximumFractionDigits ?: 0
        return formatter.format(balance)
    }
}
