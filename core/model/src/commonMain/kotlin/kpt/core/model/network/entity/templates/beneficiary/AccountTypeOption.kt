/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.templates.beneficiary

import kotlinx.serialization.Serializable

/**
 * An account type selectable when adding a beneficiary.
 *
 * @property id Unique id.
 * @property code Machine-readable code.
 * @property value Human-readable display value.
 */
@Serializable
data class AccountTypeOption(
    val id: Int? = null,
    val code: String? = null,
    val value: String? = null,
)
