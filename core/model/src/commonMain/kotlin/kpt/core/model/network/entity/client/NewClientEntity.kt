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

@Serializable
data class Address(
    val addressLine1: String,
    val addressLine2: String,
    val postalCode: String,
    val stateProvinceId: String,
    val countryId: String,
    val addressTypeId: Int = 805,
)
