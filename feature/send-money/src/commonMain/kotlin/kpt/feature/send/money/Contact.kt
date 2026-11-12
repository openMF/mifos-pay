/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import kotlinx.serialization.Serializable

/** Whether a [Contact] is an individual or a business. */
enum class ContactType {
    /** An individual person. */
    PERSON,

    /** A business or merchant. */
    BUSINESS,
}

/**
 * A device contact that can be picked as a payment recipient.
 * @property id Unique identifier of the contact.
 * @property name Display name of the contact.
 * @property phoneNumber Formatted phone number of the contact.
 * @property upiId UPI ID of the contact, if known.
 * @property type Whether the contact is a person or a business.
 */
@Serializable
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val upiId: String? = null,
    val type: ContactType = ContactType.PERSON,
)
