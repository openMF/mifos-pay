/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.authentication

import kotlinx.serialization.Serializable

/**
 * Request body for authenticating a user against the Fineract self-service API.
 *
 * @property username Login username.
 * @property password Password.
 */
@Serializable
data class AuthenticationPayload(
    val username: String,
    val password: String,
)
