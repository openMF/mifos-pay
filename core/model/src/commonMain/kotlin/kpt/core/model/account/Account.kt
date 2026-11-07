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
/**
 * A wallet account (savings account) shown to the user, with its balance, currency, status and owning office.
 *
 * @property image Icon or image reference for the account.
 * @property name Display name of the account.
 * @property number Account number.
 * @property balance Current account balance.
 * @property id Unique account identifier.
 * @property externalId Optional external reference id of the account.
 * @property productName Name of the savings product the account was opened under.
 * @property productId Identifier of the savings product.
 * @property currency Currency the account is denominated in.
 * @property status Lifecycle status of the account.
 * @property clientName Name of the client that owns the account.
 * @property accountType Type of the account (savings, loan or share), if known.
 * @property officeName Name of the office the account belongs to.
 * @property officeId Identifier of the office the account belongs to.
 */
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
