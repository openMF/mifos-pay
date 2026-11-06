/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.account

import kotlinx.serialization.Serializable
import kpt.core.model.savingsaccount.AccountType
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
@Serializable
data class Account(
    val image: String = "",
    val name: String,
    val number: String,
    val balance: Double = 0.0,
    val id: Long = 0L,
    val externalId: String? = null,
    val productName: String? = null,
    val productId: Long = 0L,
    val currency: Currency,
    val status: Status,
    val clientName: String = "",
    val accountType: AccountType? = null,
    val officeName: String? = null,
    val officeId: Int? = null,
)
