/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable
import kpt.core.common.CurrencyFormatter
@Serializable
data class Summary(
    val currency: Currency,
    val totalDeposits: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val totalInterestPosted: Double = 0.0,
    val accountBalance: Double = 0.0,
    val totalOverdraftInterestDerived: Double = 0.0,
    val interestNotPosted: Double = 0.0,
    val availableBalance: Double = 0.0,
)

fun Summary.formatAmount(amount: Double): String {
    return CurrencyFormatter.format(
        balance = amount,
        currencyCode = currency.code,
        maximumFractionDigits = null,
    )
}
