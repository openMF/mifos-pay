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
 * Key lifecycle dates and the staff member who activated a client.
 *
 * @property submittedOnDate Date the client was submitted.
 * @property activatedOnDate Date the client was activated.
 * @property activatedByUsername Username of the activating user.
 * @property activatedByFirstname First name of the activating user.
 * @property activatedByLastname Last name of the activating user.
 */
@Serializable
data class ClientTimeline(
    val submittedOnDate: List<Long> = emptyList(),
    val activatedOnDate: List<Long> = emptyList(),
    val activatedByUsername: String? = null,
    val activatedByFirstname: String? = null,
    val activatedByLastname: String? = null,
)
