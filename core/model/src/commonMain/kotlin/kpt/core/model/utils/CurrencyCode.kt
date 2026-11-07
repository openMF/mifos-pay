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
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * A currency with the country that uses it and its symbol.
 *
 * @property countryName Name of the country.
 * @property currencyCode ISO currency code.
 * @property currencySymbol Currency symbol.
 */
@Serializable
@Parcelize
data class CurrencyCode(
    val countryName: String,
    val currencyCode: String,
    val currencySymbol: String,
) : Parcelable

/** Returns the entries whose country name or currency code contains [text], ignoring case; all if blank. */
fun List<CurrencyCode>.filterList(text: String): List<CurrencyCode> {
    return if (text.isNotEmpty()) {
        this.filter {
            it.countryName.contains(text, true) ||
                it.currencyCode.contains(text, true)
        }
    } else {
        this
    }
}
