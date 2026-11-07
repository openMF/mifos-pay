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
 * Request body for changing a user's password.
 *
 * @property password New password.
 * @property repeatPassword Password repeated for confirmation.
 */
@Serializable
data class UpdateUserEntityPassword(
    val password: String,
    val repeatPassword: String,
)

/**
 * Response returned after a password change.
 *
 * @property officeId Id of the office.
 * @property resourceId Id of the created or affected resource.
 * @property changes Fields that were changed.
 */
@Serializable
data class UpdateUserPasswordResponse(
    val officeId: Long,
    val resourceId: Long,
    val changes: Changes,
)

/**
 * Fields changed by a password update.
 *
 * @property password Whether the password was changed.
 */
@Serializable
data class Changes(
    val password: Boolean,
)
