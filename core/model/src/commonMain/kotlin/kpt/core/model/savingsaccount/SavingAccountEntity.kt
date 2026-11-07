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

/**
 * Persisted savings account row with its status, currency and balance.
 *
 * @property id Unique identifier.
 * @property accountNo Account number.
 * @property productId Identifier of the savings product.
 * @property productName Name of the savings product.
 * @property shortProductName Short name of the savings product.
 * @property status Current status.
 * @property currency Currency of the amounts.
 * @property accountBalance Current account balance.
 * @property accountType Type of the account.
 * @property timeline Lifecycle dates of the account.
 * @property subStatus Sub-status of the account.
 * @property lastActiveTransactionDate Date of the last active transaction as year, month and day components.
 * @property depositType Deposit account type.
 * @property externalId External identifier.
 */
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
