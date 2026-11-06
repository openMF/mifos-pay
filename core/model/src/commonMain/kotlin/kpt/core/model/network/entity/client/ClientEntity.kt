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

@Serializable
data class ClientTimelineEntity(
    val submittedOnDate: List<Long> = emptyList(),
    val activatedOnDate: List<Long> = emptyList(),
    val activatedByUsername: String? = null,
    val activatedByFirstname: String? = null,
    val activatedByLastname: String? = null,
)
