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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.currency.RateHistory
import kpt.core.model.currency.RatePoint

@Serializable
data class RateHistoryDto(
    val amount: Double,
    val base: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    val rates: Map<String, Map<String, Double>>,
) {
    fun toDomain(targetCurrency: String): RateHistory = RateHistory(
        from = base,
        to = targetCurrency,
        startDate = startDate,
        endDate = endDate,
        rates = rates.entries
            .sortedBy { it.key }
            .mapNotNull { (date, currencyMap) ->
                currencyMap[targetCurrency]?.let { RatePoint(date, it) }
            },
    )
}
