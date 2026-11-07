/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore

import kpt.core.datastore.model.ClientPreferences
import kpt.core.datastore.model.RolePreferences
import kpt.core.datastore.model.UserInfoPreferences
import kpt.core.model.client.Client
import kpt.core.model.fork.user.RoleInfo
import kpt.core.model.fork.user.UserInfo
/** Maps stored [ClientPreferences] to the domain [Client]. */
fun ClientPreferences.toClientInfo(): Client {
    return Client(
        id = id,
        accountNo = accountNo,
        externalId = externalId,
        active = active,
        activationDate = activationDate,
        firstname = firstname,
        lastname = lastname,
        displayName = displayName,
        mobileNo = mobileNo,
        emailAddress = emailAddress,
        dateOfBirth = dateOfBirth,
        isStaff = isStaff,
        officeId = officeId,
        officeName = officeName,
        savingsProductName = savingsProductName,
    )
}

/** Maps the domain [Client] to its stored [ClientPreferences] form. */
fun Client.toClientPreferences(): ClientPreferences {
    return ClientPreferences(
        id = id,
        accountNo = accountNo,
        externalId = externalId,
        active = active,
        activationDate = activationDate,
        firstname = firstname,
        lastname = lastname,
        displayName = displayName,
        mobileNo = mobileNo,
        emailAddress = emailAddress,
        dateOfBirth = dateOfBirth,
        isStaff = isStaff,
        officeId = officeId,
        officeName = officeName,
        savingsProductName = savingsProductName,
    )
}

/** Maps stored [RolePreferences] to the domain [RoleInfo]. */
fun RolePreferences.toRoleInfo(): RoleInfo {
    return RoleInfo(
        id = id,
        name = name,
        description = description,
        disabled = disabled,
    )
}

/** Maps the domain [RoleInfo] to its stored [RolePreferences] form. */
fun RoleInfo.toRolePreferences(): RolePreferences {
    return RolePreferences(
        id = id,
        name = name,
        description = description,
        disabled = disabled,
    )
}

/** Maps stored [UserInfoPreferences] to the domain [UserInfo]. */
fun UserInfoPreferences.toUserInfo(): UserInfo {
    return UserInfo(
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
}

/** Maps the domain [UserInfo] to its stored [UserInfoPreferences] form. */
fun UserInfo.toUserInfoPreferences(): UserInfoPreferences {
    return UserInfoPreferences(
        username = username,
        userId = userId,
        base64EncodedAuthenticationKey = base64EncodedAuthenticationKey,
        authenticated = authenticated,
        officeId = officeId,
        officeName = officeName,
        roles = roles.map { it.toRolePreferences() },
        permissions = permissions,
        clients = clients,
        shouldRenewPassword = shouldRenewPassword,
        isTwoFactorAuthenticationRequired = isTwoFactorAuthenticationRequired,
    )
}
