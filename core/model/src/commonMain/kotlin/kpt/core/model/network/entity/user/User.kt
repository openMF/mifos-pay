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
/**
 * An authenticated Fineract user with credentials, roles, permissions and linked clients.
 *
 * @property username Login username.
 * @property userId Id of the user.
 * @property base64EncodedAuthenticationKey Base64 encoded key used for basic authentication.
 * @property authenticated Whether the user is authenticated.
 * @property officeId Id of the office.
 * @property officeName Name of the office.
 * @property roles Roles assigned to the user.
 * @property permissions Permissions granted to the user.
 * @property clients Ids of the clients linked to the user.
 * @property shouldRenewPassword Whether the password must be renewed.
 * @property isTwoFactorAuthenticationRequired Whether two-factor authentication is required.
 */
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
