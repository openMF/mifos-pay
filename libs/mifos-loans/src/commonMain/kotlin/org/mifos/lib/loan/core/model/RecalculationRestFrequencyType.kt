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

import kotlinx.serialization.Serializable

/**
 * Domain model of the recalculation rest frequency type used in the loan application flow.
 *
 * @property id Unique identifier.
 * @property code Short code identifying the value.
 * @property value Human-readable value.
 */
@Serializable
data class RecalculationRestFrequencyType(
    val id: Int? = null,

    val code: String? = null,

    val value: String? = null,
)
