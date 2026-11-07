/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.model

import kotlinx.serialization.Serializable

/**
 * Persisted snapshot of the logged-in client's profile.
 * @property id Client id.
 * @property accountNo Client account number.
 * @property externalId External id.
 * @property active Whether the client is active.
 * @property activationDate Activation date as Fineract date parts.
 * @property firstname First name.
 * @property lastname Last name.
 * @property displayName Display name.
 * @property mobileNo Mobile number.
 * @property emailAddress Email address.
 * @property dateOfBirth Date of birth as Fineract date parts.
 * @property isStaff Whether the client is a staff member.
 * @property officeId Owning office id.
 * @property officeName Owning office name.
 * @property savingsProductName Default savings product name.
 */
@Serializable
data class ClientPreferences(
    val id: Long,
    val accountNo: String,
    val externalId: String,
    val active: Boolean,
    val activationDate: List<Long>,
    val firstname: String,
    val lastname: String,
    val displayName: String,
    val mobileNo: String,
    val emailAddress: String,
    val dateOfBirth: List<Long>,
    val isStaff: Boolean,
    val officeId: Long,
    val officeName: String,
    val savingsProductName: String,
) {
    /** Holds the default (empty) value. */
    companion object {
        /** Empty client used before anyone has logged in. */
        val DEFAULT = ClientPreferences(
            id = 0L,
            accountNo = "",
            externalId = "",
            active = false,
            activationDate = emptyList(),
            firstname = "",
            lastname = "",
            displayName = "",
            mobileNo = "",
            emailAddress = "",
            dateOfBirth = emptyList(),
            isStaff = false,
            officeId = 0,
            officeName = "",
            savingsProductName = "",
        )
    }
}
