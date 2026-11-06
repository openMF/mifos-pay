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
