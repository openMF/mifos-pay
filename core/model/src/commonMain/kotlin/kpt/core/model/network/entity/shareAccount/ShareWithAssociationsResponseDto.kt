/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.shareAccount

import kotlinx.serialization.Serializable
import kpt.core.model.network.entity.common.CurrencyResponseDto
@Serializable
data class ShareWithAssociationsResponseDto(
    val id: Long? = null,
    val accountNo: String? = null,
    val clientId: Long? = null,
    val clientName: String? = null,

    val productId: Long? = null,
    val productName: String? = null,

    val status: ShareStatusResponseDto? = null,
    val currency: CurrencyResponseDto? = null,
    val timeline: ShareTimelineResponseDto? = null,
    val summary: ShareSummaryResponseDto? = null,

    val currentMarketPrice: Double? = null,
    val savingsAccountId: Long? = null,
    val savingsAccountNumber: String? = null,
    val allowDividendCalculationForInactiveClients: Boolean? = null,

    val lockinPeriod: Int? = null,
    val minimumActivePeriod: Int? = null,

    val dividends: List<String> = emptyList(),
)
