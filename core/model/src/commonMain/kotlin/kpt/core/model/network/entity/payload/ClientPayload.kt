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
 * Request body for creating a client, optionally with data tables.
 *
 * @property firstname First name.
 * @property lastname Last name.
 * @property middlename Middle name.
 * @property officeId Id of the office.
 * @property staffId Id of the assigned staff member.
 * @property genderId Id of the gender.
 * @property active Whether it is active.
 * @property activationDate Activation date.
 * @property submittedOnDate Date submitted, as [year, month, day].
 * @property dateOfBirth Date of birth.
 * @property mobileNo Mobile phone number.
 * @property externalId External identifier.
 * @property clientTypeId Id of the client type.
 * @property clientClassificationId Id of the client classification.
 * @property dateFormat Date format pattern used for the date fields.
 * @property locale Locale used to parse numbers and dates.
 * @property datatables Data tables to submit with the client.
 */
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
