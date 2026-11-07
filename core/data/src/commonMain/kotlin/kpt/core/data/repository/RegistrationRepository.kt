/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

import kpt.core.model.network.entity.register.RegisterPayload
import kpt.core.model.network.entity.register.UserVerify
/** Repository for user registration and verification. */
interface RegistrationRepository {
    /** Registers a new user. */
    suspend fun registerUser(registerPayload: RegisterPayload)

    /** Verifies a newly registered user. */
    suspend fun verifyUser(userVerify: UserVerify)
}
