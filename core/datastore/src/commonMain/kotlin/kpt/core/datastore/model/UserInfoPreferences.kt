/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.model

import kotlinx.serialization.Serializable

/**
 * Persisted snapshot of the authenticated user's login result.
 * @property username Login username.
 * @property userId User id.
 * @property base64EncodedAuthenticationKey Base64 key used for the Basic auth header.
 * @property authenticated Whether authentication succeeded.
 * @property officeId Office id.
 * @property officeName Office name.
 * @property roles Roles assigned to the user.
 * @property permissions Permission codes granted to the user.
 * @property clients Ids of the clients linked to the user.
 * @property shouldRenewPassword Whether the server requires a password change.
 * @property isTwoFactorAuthenticationRequired Whether two-factor authentication is required.
 */
@Serializable
data class UserInfoPreferences(
    val username: String,
    val userId: Long,
    val base64EncodedAuthenticationKey: String,
    val authenticated: Boolean,
    val officeId: Int,
    val officeName: String,
    val roles: List<RolePreferences>,
    val permissions: List<String>,
    val clients: List<Long>,
    val shouldRenewPassword: Boolean,
    val isTwoFactorAuthenticationRequired: Boolean,
) {
    /** Holds the default (logged-out) value. */
    companion object {
        /** Unauthenticated user used before login. */
        val DEFAULT = UserInfoPreferences(
            username = "",
            userId = 0,
            base64EncodedAuthenticationKey = "",
            authenticated = false,
            officeId = 0,
            officeName = "",
            roles = emptyList(),
            permissions = emptyList(),
            clients = emptyList(),
            shouldRenewPassword = false,
            isTwoFactorAuthenticationRequired = false,
        )
    }
}
