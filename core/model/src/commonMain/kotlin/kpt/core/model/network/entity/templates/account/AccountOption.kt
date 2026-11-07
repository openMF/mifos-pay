/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.templates.account

import kotlinx.serialization.Serializable

/**
 * An account selectable as a source or destination in a transfer.
 *
 * @property accountId Id of the account.
 * @property accountNo Account number.
 * @property accountType Type of the account.
 * @property clientId Id of the client.
 * @property clientName Name of the client.
 * @property officeId Id of the office.
 * @property officeName Name of the office.
 */
@Serializable
data class AccountOption(
    val accountId: Int? = null,
    val accountNo: String? = null,
    val accountType: AccountType? = null,
    val clientId: Long? = null,
    val clientName: String? = null,
    val officeId: Int? = null,
    val officeName: String? = null,
)
