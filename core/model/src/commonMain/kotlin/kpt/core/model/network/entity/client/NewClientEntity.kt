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
import kpt.core.common.DateHelper
/**
 * Request body for registering a new client; the secondary constructor fills default office, legal form and dates.
 *
 * @property firstname First name.
 * @property lastname Last name.
 * @property externalId External identifier.
 * @property mobileNo Mobile phone number.
 * @property address Postal address.
 * @property savingsProductId Id of the savings product.
 * @property officeId Id of the office.
 * @property legalFormId Id of the legal form.
 * @property active Whether it is active.
 * @property dateFormat Date format pattern used for the date fields.
 * @property activationDate Activation date.
 * @property submittedOnDate Date submitted, as [year, month, day].
 * @property locale Locale used to parse numbers and dates.
 */
@Serializable
data class NewClientEntity(
    val firstname: String,
    val lastname: String,
    val externalId: String,
    val mobileNo: String,
    val address: Address,
    val savingsProductId: Int,
    val officeId: Int,
    val legalFormId: Int,
    val active: Boolean,
    val dateFormat: String,
    val activationDate: String,
    val submittedOnDate: String,
    val locale: String,
) {
    constructor(
        firstname: String,
        lastname: String,
        externalId: String,
        mobileNo: String,
        address: Address,
        savingsProductId: Int,
    ) : this(
        firstname = firstname,
        lastname = lastname,
        externalId = externalId,
        mobileNo = mobileNo,
        address = address,
        savingsProductId = savingsProductId,
        officeId = 1,
        legalFormId = 1,
        active = true,
        dateFormat = DateHelper.SHORT_MONTH,
        activationDate = DateHelper.formattedShortDate,
        submittedOnDate = DateHelper.formattedShortDate,
        locale = "en_US",
    )
}

/**
 * Postal address submitted when registering a client.
 *
 * @property addressLine1 First address line.
 * @property addressLine2 Second address line.
 * @property postalCode Postal code.
 * @property stateProvinceId Id of the state or province.
 * @property countryId Id of the country.
 * @property addressTypeId Id of the address type (default 805).
 */
@Serializable
data class Address(
    val addressLine1: String,
    val addressLine2: String,
    val postalCode: String,
    val stateProvinceId: String,
    val countryId: String,
    val addressTypeId: Int = 805,
)
