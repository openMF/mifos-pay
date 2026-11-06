/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.user

import kotlinx.serialization.Serializable
import kpt.core.model.network.entity.Role
@Serializable
data class User(
    val username: String,
    val userId: Long,
    val base64EncodedAuthenticationKey: String,
    val authenticated: Boolean,
    val officeId: Int,
    val officeName: String,
    val roles: List<Role> = emptyList(),
    val permissions: List<String> = emptyList(),
    val clients: List<Long> = emptyList(),
    val shouldRenewPassword: Boolean,
    val isTwoFactorAuthenticationRequired: Boolean,
)
