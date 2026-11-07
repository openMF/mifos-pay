/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper

import kpt.core.model.fork.user.NewUser
import kpt.core.model.fork.user.RoleInfo
import kpt.core.model.fork.user.UserInfo
import kpt.core.model.network.entity.Role
import kpt.core.model.network.entity.user.NewUserEntity
import kpt.core.model.network.entity.user.User
private const val OFFICE_ID = 1
private const val MOBILE_WALLET_ROLE_ID = 2
private const val SUPER_USER_ROLE_ID = 1

/** Role ids assigned to newly created users: the mobile-wallet role and the super-user role. */
val NEW_USER_ROLE_IDS: ArrayList<Int> = arrayListOf(MOBILE_WALLET_ROLE_ID, SUPER_USER_ROLE_ID)

/** Maps a domain [NewUser] to the create-user network entity. */
fun NewUser.toEntity(): NewUserEntity {
    return NewUserEntity(
        username = username,
        firstname = firstname,
        lastname = lastname,
        email = email,
        password = password,
        officeId = OFFICE_ID,
        roles = NEW_USER_ROLE_IDS,
        sendPasswordToEmail = false,
        isSelfServiceUser = true,
        repeatPassword = password,
    )
}

/** Maps a [User] to a [UserInfo]. */
fun User.toUserInfo() = UserInfo(
    username = username,
    userId = userId,
    base64EncodedAuthenticationKey = base64EncodedAuthenticationKey,
    authenticated = authenticated,
    officeId = officeId,
    officeName = officeName,
    roles = roles.map { it.toRoleInfo() },
    permissions = permissions,
    clients = clients,
    shouldRenewPassword = shouldRenewPassword,
    isTwoFactorAuthenticationRequired = isTwoFactorAuthenticationRequired,
)

/** Maps a [Role] to a [RoleInfo]. */
fun Role.toRoleInfo() = RoleInfo(
    id = id ?: "",
    name = name ?: "",
    description = description ?: "",
    disabled = disabled,
)
