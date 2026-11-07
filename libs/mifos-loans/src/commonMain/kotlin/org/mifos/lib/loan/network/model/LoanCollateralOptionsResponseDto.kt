/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.model

import kotlinx.serialization.Serializable

/**
 * Network response DTO for the loan collateral options, as returned by the Fineract loans API.
 *
 * @property id Unique identifier.
 * @property name Name.
 * @property position Position in the ordering.
 * @property description Description.
 * @property active Whether this entry is active.
 */
@Serializable
data class LoanCollateralOptionsResponseDto(

    val id: Int? = null,

    val name: String? = null,

    val position: Int? = null,

    val description: String? = null,

    val active: Boolean? = null,
)
