/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.register

import kotlinx.serialization.Serializable

/**
 * Request body for self-service user registration.
 *
 * @property username Login username.
 * @property firstName First name.
 * @property lastName Last name.
 * @property email Email address.
 * @property mobileNumber Mobile phone number.
 * @property accountNumber Account number.
 * @property password Password.
 * @property authenticationMode Authentication mode for verification.
 */
@Serializable
data class RegisterPayload(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val mobileNumber: String? = null,
    val accountNumber: String? = null,
    val password: String? = null,
    val authenticationMode: String? = null,
)
