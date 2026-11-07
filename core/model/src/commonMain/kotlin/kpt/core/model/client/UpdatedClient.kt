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
 * Editable profile fields sent when updating a client.
 *
 * @property firstname First name.
 * @property lastname Last name.
 * @property externalId External reference id.
 * @property mobileNo Mobile phone number.
 * @property emailAddress Email address.
 */
@Parcelize
data class UpdatedClient(
    val firstname: String,
    val lastname: String,
    val externalId: String,
    val mobileNo: String,
    val emailAddress: String,
) : Parcelable
