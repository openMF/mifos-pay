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

@Serializable
data class SavingAccountEntity(
    val id: Long,
    val accountNo: String,
    val productId: Long,
    val productName: String,
    val shortProductName: String,
    val status: Status,
    val currency: Currency,
    val accountBalance: Double = 0.0,
    val accountType: AccountType,
    val timeline: Timeline,
    val subStatus: SubStatus,
    val lastActiveTransactionDate: List<Long> = emptyList(),
    val depositType: DepositType?,
    val externalId: String? = null,
)
