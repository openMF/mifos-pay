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

/**
 * A Fineract client (wallet customer) with identity, contact and office details.
 *
 * @property id Unique id.
 * @property accountNo Account number.
 * @property externalId External identifier.
 * @property status Client status.
 * @property active Whether the client is active.
 * @property activationDate Activation date.
 * @property firstname First name.
 * @property lastname Last name.
 * @property displayName Full display name.
 * @property mobileNo Mobile phone number.
 * @property emailAddress Email address.
 * @property dateOfBirth Date of birth.
 * @property isStaff Whether the client is a staff member.
 * @property officeId Id of the office.
 * @property officeName Name of the office.
 * @property timeline Lifecycle timeline.
 * @property savingsProductName Name of the savings product.
 * @property legalForm Legal form of the client.
 */
@Serializable
data class ClientEntity(
    val id: Long? = null,
    val accountNo: String? = null,
    val externalId: String? = null,
    val status: Status? = null,
    val active: Boolean = false,
    val activationDate: List<Long> = emptyList(),
    val firstname: String? = null,
    val lastname: String? = null,
    val displayName: String? = null,
    val mobileNo: String? = null,
    val emailAddress: String? = null,
    val dateOfBirth: List<Long> = emptyList(),
    val isStaff: Boolean? = null,
    val officeId: Long? = null,
    val officeName: String? = null,
    val timeline: ClientTimelineEntity? = null,
    val savingsProductName: String? = null,
    val legalForm: Status? = null,
)

/**
 * Timeline of key client lifecycle events (submission and activation).
 *
 * @property submittedOnDate Date submitted, as [year, month, day].
 * @property activatedOnDate Date activated, as [year, month, day].
 * @property activatedByUsername Username of the activating user.
 * @property activatedByFirstname First name of the activating user.
 * @property activatedByLastname Last name of the activating user.
 */
@Serializable
data class ClientTimelineEntity(
    val submittedOnDate: List<Long> = emptyList(),
    val activatedOnDate: List<Long> = emptyList(),
    val activatedByUsername: String? = null,
    val activatedByFirstname: String? = null,
    val activatedByLastname: String? = null,
)
