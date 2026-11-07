/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.fork.user

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Authenticated user details returned at login.
 *
 * @property username Login username.
 * @property userId Identifier of the user.
 * @property base64EncodedAuthenticationKey Base64-encoded key used to authenticate API calls.
 * @property authenticated Whether the user is authenticated.
 * @property officeId Identifier of the office.
 * @property officeName Name of the office.
 * @property roles Roles assigned to the user.
 * @property permissions Permissions granted to the user.
 * @property clients Identifiers of the clients linked to the user.
 * @property shouldRenewPassword Whether the user must renew the password.
 * @property isTwoFactorAuthenticationRequired Whether two-factor authentication is required.
 */
@Parcelize
data class UserInfo(
    val username: String,
    val userId: Long,
    val base64EncodedAuthenticationKey: String,
    val authenticated: Boolean,
    val officeId: Int,
    val officeName: String,
    val roles: List<RoleInfo>,
    val permissions: List<String>,
    val clients: List<Long>,
    val shouldRenewPassword: Boolean,
    val isTwoFactorAuthenticationRequired: Boolean,
) : Parcelable
