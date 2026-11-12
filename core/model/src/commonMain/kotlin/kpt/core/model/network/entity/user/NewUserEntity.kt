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

/**
 * Request body for creating a new Fineract user.
 *
 * @property username Login username.
 * @property firstname First name.
 * @property lastname Last name.
 * @property email Email address.
 * @property password Password.
 * @property officeId Id of the office the user belongs to.
 * @property roles Ids of the roles to assign.
 * @property sendPasswordToEmail Whether the password is emailed to the user.
 * @property isSelfServiceUser Whether the user is a self-service user.
 * @property repeatPassword Password repeated for confirmation.
 */
@Serializable
class NewUserEntity(
    val username: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String,
    val officeId: Int,
    val roles: ArrayList<Int>,
    val sendPasswordToEmail: Boolean,
    val isSelfServiceUser: Boolean,
    val repeatPassword: String,
)
