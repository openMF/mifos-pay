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

import kotlinx.serialization.Serializable

/**
 * A bank client (customer) profile as returned by Fineract.
 *
 * @property id Unique id of the client.
 * @property accountNo Client account number.
 * @property externalId External reference id.
 * @property active Whether the client is active.
 * @property activationDate Activation date as date components.
 * @property firstname First name.
 * @property lastname Last name.
 * @property displayName Full name for display.
 * @property mobileNo Mobile phone number.
 * @property emailAddress Email address.
 * @property dateOfBirth Date of birth as date components.
 * @property isStaff Whether the client is a staff member.
 * @property officeId Id of the client's office.
 * @property officeName Name of the client's office.
 * @property savingsProductName Name of the client's savings product.
 * @property timeline Key lifecycle dates of the client.
 * @property status Current status of the client.
 * @property legalForm Legal form of the client.
 */
@Serializable
data class Client(
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
    val timeline: ClientTimeline = ClientTimeline(),
    val status: ClientStatus = ClientStatus(),
    val legalForm: ClientStatus = ClientStatus(),
)
