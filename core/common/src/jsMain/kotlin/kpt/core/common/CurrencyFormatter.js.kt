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

actual object CurrencyFormatter {
    actual fun format(
        balance: Double?,
        currencyCode: String?,
        maximumFractionDigits: Int?,
    ): String {
        if (balance == null || currencyCode == null) {
            return ""
        }

        val options = js("{}").unsafeCast<dynamic>()
        options.style = "currency"
        options.currency = currencyCode
        if (maximumFractionDigits != null) {
            options.maximumFractionDigits = maximumFractionDigits
        }

        return try {
            numberFormat(options).format(balance).toString()
        } catch (e: Throwable) {
            console.error("Error formatting currency: ${e.message}")
            balance.toString()
        }
    }

    actual fun format(
        balance: Double?,
        maximumFractionDigits: Int?,
    ): String {
        if (balance == null) {
            return ""
        }
        val options = js("{}").unsafeCast<dynamic>()
        if (maximumFractionDigits != null) {
            options.maximumFractionDigits = maximumFractionDigits
            options.minimumFractionDigits = maximumFractionDigits
        }
        return try {
            numberFormat(options).format(balance).toString()
        } catch (e: Throwable) {
            console.error("Error formatting number: ${e.message}")
            balance.toString()
        }
    }
}

// Constructed in its own js() call: the compiler reprints `new X(o).format(b)` as
// `new (X(o).format)(b)`, which throws "format is not a constructor".
private fun numberFormat(options: dynamic): dynamic = js("new Intl.NumberFormat('en-US', options)")
