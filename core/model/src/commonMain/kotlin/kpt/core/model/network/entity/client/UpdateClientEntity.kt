/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.client

import kotlinx.serialization.Serializable

/**
 * Request body for updating a client's personal and contact details.
 *
 * @property firstname First name.
 * @property lastname Last name.
 * @property externalId External identifier.
 * @property mobileNo Mobile phone number.
 * @property emailAddress Email address.
 */
@Serializable
data class UpdateClientEntity(
    val firstname: String,
    val lastname: String,
    val externalId: String,
    val mobileNo: String,
    val emailAddress: String,
)
