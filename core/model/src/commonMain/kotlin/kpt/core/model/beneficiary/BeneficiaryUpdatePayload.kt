/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.beneficiary

import kotlinx.serialization.Serializable

/**
 * Request body for updating a beneficiary's name and transfer limit.
 *
 * @property name New display name.
 * @property transferLimit New maximum amount allowed per transfer.
 */
@Serializable
data class BeneficiaryUpdatePayload(
    val name: String? = null,
    val transferLimit: Int = 0,
)
