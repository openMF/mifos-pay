/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.frankfurter.dto

import kotlinx.serialization.Serializable
import kpt.core.model.currency.ExchangeRates

@Serializable
data class ExchangeRatesDto(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
) {
    fun toDomain(): ExchangeRates = ExchangeRates(
        base = base,
        date = date,
        rates = rates,
    )
}
