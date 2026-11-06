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

@Serializable
data class ClientPayload(
    val firstname: String? = null,
    val lastname: String? = null,
    val middlename: String? = null,
    val officeId: Int? = null,
    val staffId: Int? = null,
    val genderId: Int? = null,
    val active: Boolean? = null,
    val activationDate: String? = null,
    val submittedOnDate: String? = null,
    val dateOfBirth: String? = null,
    val mobileNo: String? = null,
    val externalId: String? = null,
    val clientTypeId: Int? = null,
    val clientClassificationId: Int? = null,
    val dateFormat: String? = "DD_MMMM_YYYY",
    val locale: String? = "en",
    val datatables: List<DataTablePayload> = ArrayList(),
)
