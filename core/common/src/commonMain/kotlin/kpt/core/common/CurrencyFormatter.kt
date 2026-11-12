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

/** Platform-specific formatter that renders wallet balances as currency or plain grouped numbers. */
expect object CurrencyFormatter {
    /** Formats [balance] as a currency string for [currencyCode] with up to [maximumFractionDigits] decimals. */
    fun format(balance: Double?, currencyCode: String?, maximumFractionDigits: Int?): String

    /** Formats [balance] as a plain number string with [maximumFractionDigits] decimals. */
    fun format(balance: Double?, maximumFractionDigits: Int?): String
}

/** Copies this list into a new [ArrayList]. */
fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}
