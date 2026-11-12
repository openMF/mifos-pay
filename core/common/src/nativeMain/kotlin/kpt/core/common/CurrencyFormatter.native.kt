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

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle

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
        val numberFormatter = NSNumberFormatter()
        numberFormatter.locale = NSLocale(localeIdentifier = "en_US_POSIX")
        numberFormatter.numberStyle = NSNumberFormatterCurrencyStyle
        numberFormatter.currencyCode = currencyCode ?: "$"
        val fractionDigits = (maximumFractionDigits ?: 0).toULong()
        numberFormatter.maximumFractionDigits = fractionDigits
        numberFormatter.minimumFractionDigits = fractionDigits
        return numberFormatter.stringFromNumber(NSNumber(balance ?: 0.0)) ?: ""
    }

    /** Platform actual: formats [balance] as a plain number string with [maximumFractionDigits] decimals. */
    actual fun format(
        balance: Double?,
        maximumFractionDigits: Int?,
    ): String {
        val numberFormatter = NSNumberFormatter()
        numberFormatter.locale = NSLocale(localeIdentifier = "en_US_POSIX")
        numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
        val fractionDigits = (maximumFractionDigits ?: 0).toULong()
        numberFormatter.maximumFractionDigits = fractionDigits
        numberFormatter.minimumFractionDigits = fractionDigits
        return numberFormatter
            .stringFromNumber(NSNumber(balance ?: 0.0))
            ?: ""
    }
}
