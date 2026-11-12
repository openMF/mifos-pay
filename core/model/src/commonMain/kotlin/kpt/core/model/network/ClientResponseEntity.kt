/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network

import kotlinx.serialization.Serializable

/**
 * Fineract response returned after creating a client with a savings account.
 *
 * @property officeId Id of the office.
 * @property clientId Id of the client.
 * @property resourceId Id of the created or affected resource.
 * @property savingsId Id of the savings account.
 */
@Serializable
data class ClientResponseEntity(
    val officeId: Int,
    val clientId: Int,
    val resourceId: Int,
    val savingsId: Int,
)
