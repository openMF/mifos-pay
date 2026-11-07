/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Domain model of the loan purpose options used in the loan application flow.
 *
 * @property id Unique identifier.
 * @property name Name.
 * @property position Position in the ordering.
 * @property description Description.
 * @property active Whether this entry is active.
 */
@Serializable
data class LoanPurposeOptions(

    val id: Int? = null,

    val name: String? = null,

    val position: Int? = null,

    val description: String? = null,

    @SerialName("isActive")
    val active: Boolean? = null,

)
