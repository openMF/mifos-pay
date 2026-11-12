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
            (intlNumberFormat("en-US", options).format(balance) as String)
        } catch (e: Throwable) {
            console.error("Error formatting currency: ${e.message}")
            balance.toString()
        }
    }

    /** Platform actual: formats [balance] as a plain number string with [maximumFractionDigits] decimals. */
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
            (intlNumberFormat("en-US", options).format(balance) as String)
        } catch (e: Throwable) {
            console.error("Error formatting number: ${e.message}")
            balance.toString()
        }
    }
}

/**
 * The browser's `Intl.NumberFormat` constructor. Called as a plain function it returns a new
 * formatter, so no `js()` string (whose `new … .format()` the compiler once mis-printed) is needed.
 */
private val intlNumberFormat: dynamic = js("Intl.NumberFormat")
