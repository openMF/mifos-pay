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
/**
 * Balance summary of a savings account.
 *
 * @property currency Currency of the amounts.
 * @property totalDeposits Total amount deposited.
 * @property totalWithdrawals Total amount withdrawn.
 * @property totalInterestPosted Total interest posted.
 * @property accountBalance Current account balance.
 * @property totalOverdraftInterestDerived Total overdraft interest derived.
 * @property interestNotPosted Interest accrued but not yet posted.
 * @property availableBalance Balance available for withdrawal.
 */
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

/** Formats [amount] using this summary currency code. */
fun Summary.formatAmount(amount: Double): String {
    return CurrencyFormatter.format(
        balance = amount,
        currencyCode = currency.code,
        maximumFractionDigits = null,
    )
}
