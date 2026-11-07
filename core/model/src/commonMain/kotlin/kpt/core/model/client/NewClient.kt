/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Data needed to register a new client.
 *
 * @property firstname First name.
 * @property lastname Last name.
 * @property externalId External reference id.
 * @property mobileNo Mobile phone number.
 * @property address Postal address.
 * @property savingsProductId Id of the savings product to open.
 */
@Parcelize
data class NewClient(
    val firstname: String,
    val lastname: String,
    val externalId: String,
    val mobileNo: String,
    val address: ClientAddress,
    val savingsProductId: Int,
) : Parcelable
