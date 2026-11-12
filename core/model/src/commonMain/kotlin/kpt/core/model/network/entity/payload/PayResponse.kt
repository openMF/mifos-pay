/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.payload

import kotlinx.serialization.Serializable

/**
 * Fineract response returned after a payment transaction.
 *
 * @property officeId Id of the office.
 * @property clientId Id of the client.
 * @property savingsId Id of the savings account.
 * @property resourceId Id of the created or affected resource.
 * @property changes Fields that were changed.
 */
@Serializable
data class PayResponse(
    val officeId: Int,
    val clientId: Int,
    val savingsId: Int,
    val resourceId: Int,
    val changes: kpt.core.model.network.entity.payload.Change,
)

/**
 * Fields changed by a payment transaction.
 *
 * @property paymentTypeId Id of the payment type used.
 */
@Serializable
data class Change(
    val paymentTypeId: Int,
)
