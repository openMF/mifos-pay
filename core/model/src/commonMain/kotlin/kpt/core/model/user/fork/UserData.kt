/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user.fork

import kpt.core.model.client.Client
import kpt.core.model.user.UserInfo
/**
 * Locally stored data about the signed-in wallet user.
 *
 * @property token Authentication token of the session.
 * @property name Full name of the user.
 * @property username Login username.
 * @property email Email address of the user.
 * @property mobileNo Mobile phone number.
 * @property userId Identifier of the user.
 * @property clientId Identifier of the user client record.
 * @property clientVpa Virtual payment address of the client.
 * @property accountId Identifier of the user primary account.
 * @property firebaseRegId Firebase registration id used for push notifications.
 * @property client Client profile of the user.
 * @property user Authenticated user details.
 */
data class UserData(
    val token: String,
    val name: String,
    val username: String,
    val email: String,
    val mobileNo: String,
    val userId: Int,
    val clientId: Int,
    val clientVpa: String,
    val accountId: Int,
    val firebaseRegId: String,
    val client: Client,
    val user: UserInfo,
)
