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
 * Network response DTO for the loan timeline, as returned by the Fineract loans API.
 *
 * @property expectedDisbursementDate Expected disbursement date as year, month and day components.
 */
@Serializable
data class LoanTimelineResponseDto(
    val expectedDisbursementDate: List<Int>,
)
